// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonLocation;

@ControllerAdvice
public class SecHubExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SecHubExceptionHandler.class);

    @ExceptionHandler(SizeLimitExceededException.class)
    public ResponseEntity<String> handleSizeLimitExceededException(SizeLimitExceededException ex) {
        logger.error("Request size limit exceeded:", ex);
        String displayPermittedSize = FileUtils.byteCountToDisplaySize(ex.getPermittedSize());
        String errMsg = "The request size must not exceed %s".formatted(displayPermittedSize);
        return createNotAcceptableResponseEntity(errMsg);
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<String> handleFileSizeLimitExceededException(FileSizeLimitExceededException ex) {
        logger.error("File size limit exceeded:", ex);
        String displayPermittedSize = FileUtils.byteCountToDisplaySize(ex.getPermittedSize());
        String errMsg = "The file upload size must not exceed %s".formatted(displayPermittedSize);
        return createNotAcceptableResponseEntity(errMsg);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();

        if (mostSpecificCause instanceof SizeLimitExceededException sizeLimitExceededException) {
            return handleSizeLimitExceededException(sizeLimitExceededException);
        }

        if (mostSpecificCause instanceof FileSizeLimitExceededException fileSizeLimitExceededException) {
            return handleFileSizeLimitExceededException(fileSizeLimitExceededException);
        }

        logger.error("Max upload size exceeded:", ex);
        String displayMaxUploadSize = FileUtils.byteCountToDisplaySize(ex.getMaxUploadSize());
        String errMsg = "Upload size must not exceed %s".formatted(displayMaxUploadSize);
        return createNotAcceptableResponseEntity(errMsg);
    }

    @ExceptionHandler(JacksonException.class)
    public ResponseEntity<String> handleJacksonException(JacksonException je) {
        /*
         * In this case the Jackson exception comes from Spring Boot framework - e.g.
         * when a user defines a wrong SecHubCongfigurationModel as JSON and the
         * framework is not able to create the object for RestController methods because
         * Jackson mapping fails. Our internal JSonConverter creates its own type of
         * exception so it is ensured that this is no internal problem.
         *
         * The Jackson exception would be already handled as a bad request by Spring,
         * but the problem is here, that the Jackson exception message does contain too
         * much internal information, stacktrace data etc. Those data shall not be
         * returned to user side.
         *
         * So we handle this special.
         *
         */
        int status = HttpStatus.BAD_REQUEST.value();

        String jsonProblem = "JSON data failure";

        JsonLocation location = je.getLocation();
        if (location != null) {
            jsonProblem += " at line: " + location.getLineNr() + ", column: " + location.getColumnNr();
        }
        jsonProblem += ". ";
        jsonProblem += je.getOriginalMessage();

        /* @formatter:off */
        return ResponseEntity
                .status(status)
                .body(jsonProblem);
        /* @formatter:on */
    }

    @Bean
    RequestRejectedHandler requestRejectedHandler() {
        return new SecHubHttpStatusRequestRejectedHandler();
    }

    private static ResponseEntity<String> createNotAcceptableResponseEntity(String errMsg) {
        /* @formatter:off */
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(errMsg);
        /* @formatter:on */
    }
}
