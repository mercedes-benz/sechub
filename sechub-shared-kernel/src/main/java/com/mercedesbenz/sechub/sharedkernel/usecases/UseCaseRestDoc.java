// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A RestDoc defines a rest call association to an usecase! Define this
 * annotation to your test case method inside a junit test which is annotated
 * with spring AutoConfigureRestDocs annotation and it will automatically appear
 * inside documentation - with linking to and from dedicated use case.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseCaseRestDoc {

    public static final String DEFAULT_VARIANT = "";

    /**
     * Must be an annotation class which itself is annoted with
     * {@link UseCaseDefinition}. There is raw type {@link Annotation} used because
     * of java 8 lack of expression.
     *
     * @return usecase annotation class this rest call is associated
     */
    Class<? extends Annotation> useCase();

    /**
     * A variant defines a special case of rest doc (e.g. when a report is generated
     * and it has a JSON adn HTML variant you can use the variant flag to identify
     * it and force splitting inside documentation) When not Defined
     * {@link RestDocPathFactory#DEFAULTVARIANT} is used.
     *
     * @return
     */
    String variant() default DEFAULT_VARIANT;

    /**
     * Defines the output files from spring rest doc wanted for generation. The
     * ordering is also used inside output generation.
     *
     * @return array
     */
    SpringRestDocOutput[] wanted() default {
        /* @formatter:off */
		SpringRestDocOutput.PATH_PARAMETERS,

		SpringRestDocOutput.QUERY_PARAMETERS,

		SpringRestDocOutput.REQUEST_PARAMETERS,

		SpringRestDocOutput.REQUEST_HEADERS,

		SpringRestDocOutput.REQUEST_FIELDS,

		SpringRestDocOutput.CURL_REQUEST,

		SpringRestDocOutput.RESPONSE_BODY,

		SpringRestDocOutput.RESPONSE_FIELDS,
		};

    public enum SpringRestDocType{
        DEFINITION,

        EXAMPLE,

        RESOURCE_DATA,
    }

	public enum SpringRestDocOutput{
		CURL_REQUEST("curl-request.adoc", SpringRestDocType.EXAMPLE),

		HTTP_REQUEST("http-request.adoc",SpringRestDocType.EXAMPLE),

		HTTP_RESPONSE("http-response.adoc",SpringRestDocType.EXAMPLE),

		HTTPIE_REQUEST("httpie-request.adoc",SpringRestDocType.EXAMPLE),

		REQUEST_BODY("request-body.adoc",SpringRestDocType.EXAMPLE),

		REQUEST_FIELDS("request-fields.adoc",SpringRestDocType.DEFINITION),

		RESPONSE_BODY("response-body.adoc",SpringRestDocType.EXAMPLE),

		RESPONSE_FIELDS("response-fields.adoc",SpringRestDocType.DEFINITION),

		PATH_PARAMETERS("path-parameters.adoc",SpringRestDocType.DEFINITION),

		QUERY_PARAMETERS("query-parameters.adoc",SpringRestDocType.DEFINITION),

		REQUEST_PARAMETERS("request-parameters.adoc",SpringRestDocType.DEFINITION),

		REQUEST_HEADERS("request-headers.adoc",SpringRestDocType.DEFINITION),

		RESOURCE("resource.json", SpringRestDocType.RESOURCE_DATA),

		;
		/* @formatter:on */

        private String fileName;
        private SpringRestDocType type;

        SpringRestDocOutput(String fileName, SpringRestDocType type) {
            this.fileName = fileName;
            this.type = type;
        }

        public boolean isRepresentedBy(File file) {
            return file.getName().equals(fileName);
        }

        public boolean isExample() {
            return SpringRestDocType.EXAMPLE == type;
        }

        public boolean isDefinition() {
            return SpringRestDocType.DEFINITION == type;
        }

    }

}
