// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mercedesbenz.sechub.sharedkernel.Step;

import jakarta.annotation.security.RolesAllowed;

/**
 * Is used by Asciidoc generator to automatically generate documentation about
 * use cases.<br>
 * <br>
 * <h1>How to use ?</h1> Create another annotation e.g.
 * <code>UseCaseCreateUser</code> and annotate <code>UseCaseCreateUser</code>
 * annotation with <code>UseCaseDefinition</code>. Also define in
 * <code>UseCaseCreateUser</code> a {@link Step} as <code>value()</code>
 * <u>without</u> any default.
 *
 * <br>
 * <br>
 * When a {@link RolesAllowed} annotation is also on target methods (e.g. method
 * has <code>UseCaseCreateUser</code> and also
 * <code>RolesAllowed("MyAdmin")</code> the role information will be used in
 * generation as well!
 *
 * <br>
 * <br>
 * <h1>Important about use cases</h1>Usecase annotatinos annotated with this
 * annotation, should always have itself only @Target with
 * {@link ElementType#METHOD}!
 *
 * @author Albert Tregnaghi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface UseCaseDefinition {

    /**
     * An unique identifier. This identifier will also be used for title + also for
     * sorting!
     *
     * @return
     */
    UseCaseIdentifier id();

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
     * @return title
     */
    String title();

    /**
     * API Name (is used for open API file generation)
     *
     * @return name when an API call is possible or <code>null</code> if not
     *         possible.
     */
    String apiName();

    /**
     * A group of this use case is related to
     *
     * @return
     */
    UseCaseGroup[] group();

}