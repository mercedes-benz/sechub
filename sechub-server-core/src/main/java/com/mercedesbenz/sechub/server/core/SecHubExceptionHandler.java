// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonLocation;

@ControllerAdvice
public class SecHubExceptionHandler {

    @Value("multipart.max-file-size")
    private String maxFileSize;

    @ExceptionHandler(SizeLimitExceededException.class)
    public ResponseEntity<String> handleSizeLimitExceededException(SizeLimitExceededException ex) {
        return createFileUploadSizeExceedResponse(ex.getPermittedSize());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();

        if (mostSpecificCause instanceof SizeLimitExceededException sizeLimitExceededException) {
            /*
             * get the size limit from the root exception since it should always speak the
             * truth
             */
            return handleSizeLimitExceededException(sizeLimitExceededException);
        }

        /* fall back to configured max file size from application properties */
        DataSize size = DataSize.parse(maxFileSize);
        long maxFileSizeBytes = size.toBytes();

        return createFileUploadSizeExceedResponse(maxFileSizeBytes);
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

    private ResponseEntity<String> createFileUploadSizeExceedResponse(long maxSize) {
        String displayMaxSize = FileUtils.byteCountToDisplaySize(maxSize);
        String errMsg = "The file upload size must not exceed %s".formatted(displayMaxSize);

        /* @formatter:off */
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(errMsg);
        /* @formatter:on */
    }
}
