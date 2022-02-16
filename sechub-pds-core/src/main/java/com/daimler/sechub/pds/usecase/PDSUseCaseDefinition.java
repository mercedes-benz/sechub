// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.usecase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface PDSUseCaseDefinition {

    /**
     * An unique identifier. This identifier will also be used for title + also for
     * sorting!
     *
     * @return
     */
    PDSUseCaseIdentifier id();

    /**
     * A description what the usecase is for. Should be done in asciidoctor syntax.
     * If the descriptions ends with ".adoc" its assumed that this is a file name
     * instead! <br>
     * <br>
     * For example: <br>
     * <code>description="user/createUser.adoc"</code> <br>
     * will be tried by asciidoctor generator as an include of this file instead of
     * just inserting it! The path will be relative to a dedicated usecase base
     * folder where all usecase asciidoc files are defined.
     *
     * @return description text or filename
     */
    String description();

    /**
     * A short use case title
     *
     * @return
     */
    String title();

    /**
     * A group of this use case is related to
     *
     * @return
     */
    PDSUseCaseGroup[] group();

}