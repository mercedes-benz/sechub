// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

public class StacktraceUtil {

    /**
     * Find root cause for given throwable. When given throwable is
     * <code>null</code> returned value will aslo be <code>null</code>.
     *
     * @param throwable
     * @return root cause or <code>null</code>
     */
    public static Throwable findRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    /**
     * Creates a description for given throwable. When throwable is null "null" as
     * string will be returned. Otherwise full name of throwable and the message
     * will be returned.
     *
     * @param throwable
     * @return string, never <code>null</code>
     */
    public static String createDescription(Throwable throwable) {
        if (throwable == null) {
            return "null";
        }
        return throwable.getClass().getName() + ":" + throwable.getMessage();
    }

    /**
     * Creates a full stacktrace description
     *
     * @param throwable
     * @return
     */
    public static String createFullTraceDescription(Throwable throwable) {
        if (throwable == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(createDescription(throwable));
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            sb.append("\n  ");
            sb.append(stackTraceElement.toString());
        }
        sb.append("\n");
        return sb.toString();

    }
}
