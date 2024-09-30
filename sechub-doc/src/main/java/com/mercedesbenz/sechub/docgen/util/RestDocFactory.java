// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseDefinition;

/**
 * Factory to create parts belonging to rest doc.
 *
 * @author Albert Tregnaghi
 *
 */
public class RestDocFactory {
    public static final String UC_RESTDOC = "uc_restdoc";

    private static Set<String> alreadyCreatedPathes = new HashSet<>();
    private static Pattern P_VARIANT_NAME_TO_ID = Pattern.compile(" ");
    private static Pattern TAG_PATTERN = Pattern.compile("api/[\\w-_]*/");

    public static String createVariantId(String variantName) {
        return P_VARIANT_NAME_TO_ID.matcher(variantName).replaceAll("-").toLowerCase();
    }

    private RestDocFactory() {

    }

    /**
     * Creates the name for the link to the rest documentation of the usecase
     *
     * @param useCase
     * @return name
     */
    public static String createPath(Class<? extends Annotation> useCase) {
        return createPath(useCase, null);
    }

    /**
     * Creates the name for the link to the rest documentation of the usecase
     *
     * @param useCase
     * @param variant a variant or <code>null</code>. A variant is used when same
     *                usecases got different variants -e.g. on reporting to differ
     *                between "HTML" and "JSON" output variants...
     * @return name
     */
    public static String createPath(Class<? extends Annotation> useCase, String variant) {
        StringBuilder sb = new StringBuilder();

        sb.append(createIdentifier(useCase));
        if (variant != null && !variant.isEmpty()) {
            sb.append("_");
            sb.append(createVariantId(variant));
        }
        String path = sb.toString();
        if (alreadyCreatedPathes.contains(path)) {
            throw new IllegalStateException("The path: " + path
                    + "\nis already created.\n\nThis means that a restdoc test did use this path already - and this is odd!\n\nPlease check if you have accidently copied a testcase and reused path creation for old usecase class!");
        }
        alreadyCreatedPathes.add(path);
        return path;
    }

    public static String createIdentifier(Class<? extends Annotation> useCase) {
        UseCaseDefinition usecaseAnnotation = useCase.getAnnotation(UseCaseDefinition.class);
        if (usecaseAnnotation == null) {
            throw new IllegalArgumentException("given use case must have annotation of use case definition inside but has not: " + useCase);
        }
        String apiNameIdentifier = usecaseAnnotation.apiName();
        if (apiNameIdentifier == null || apiNameIdentifier.isBlank()) {
            throw new IllegalArgumentException("use case annotation of class does not contain id:" + useCase);
        }
        return apiNameIdentifier;
    }

    public static String createSummary(Class<? extends Annotation> useCase) {
        UseCaseDefinition usecaseAnnotation = useCase.getAnnotation(UseCaseDefinition.class);

        StringBuilder sb = new StringBuilder();
        if (usecaseAnnotation == null) {
            throw new IllegalArgumentException("given use case must have annotation of use case definition inside but has not: " + useCase);
        } else {
            sb.append(usecaseAnnotation.title());
        }

        return sb.toString();
    }

    public static String createDescription(Class<? extends Annotation> useCase) {
        UseCaseDefinition usecaseAnnotation = useCase.getAnnotation(UseCaseDefinition.class);

        StringBuilder sb = new StringBuilder();
        if (usecaseAnnotation == null) {
            throw new IllegalArgumentException("given use case must have annotation of use case defintiion inside but hasnot :" + useCase);
        } else {
            String description = usecaseAnnotation.description();
            if (description.contains(".adoc")) {
                sb.append(usecaseAnnotation.title());
            } else {
                sb.append(description);
            }
        }

        return sb.toString();
    }

    public static String extractTag(String apiEndpoint) {
        String tag = null;
        Matcher matcher = TAG_PATTERN.matcher(apiEndpoint);

        if (matcher.find()) {
            tag = matcher.group();
            tag = tag.substring("api/".length(), tag.length() - "/".length());
        }

        return tag;
    }

}