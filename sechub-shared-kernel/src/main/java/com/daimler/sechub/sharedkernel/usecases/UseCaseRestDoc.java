// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases;

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

	public static final String DEFAULT_VARIANT = "default";
	
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
	 * ordering is also the ordering in output files!
	 * 
	 * @return
	 */
	SpringRestDocOutput[] wanted() default {
		/* @formatter:off */
		SpringRestDocOutput.PATH_PARAMETERS,
		
		SpringRestDocOutput.REQUEST_FIELDS, 

		SpringRestDocOutput.RESPONSE_FIELDS, 
		
		SpringRestDocOutput.CURL_REQUEST
		}; 

	public enum SpringRestDocOutput{
		CURL_REQUEST("curl-request.adoc"),
		HTTP_REQUEST("http-request.adoc"),
		HTTP_RESPONSE("http-response.adoc"),
		HTTPIE_REQUEST("httpie-request.adoc"),
		REQUEST_BODY("request-body.adoc"),
		REQUEST_FIELDS("request-fields.adoc"),
		RESPONSE_BODY("request-body.adoc"),
		RESPONSE_FIELDS("response-fields.adoc"),
		PATH_PARAMETERS("path-parameters.adoc")
		;
		/* @formatter:on */

		private String wantedFileName;

		SpringRestDocOutput(String fileName) {
			this.wantedFileName = fileName;
		}

		public boolean isWanted(File file) {
			return file.getName().equals(wantedFileName);
		}
	}

}
