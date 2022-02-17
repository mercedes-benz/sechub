// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

/**
 * Abstract base class for trace log id generation. The implementations just
 * have to provide a UUID usable for trace logging <br>
 * <b>Important:</b> You should use log.isXYZenabled() to reduce string creation
 * when not needed
 *
 * @author Albert Tregnaghi
 *
 */
public abstract class TraceLogID<T> {

    private String asString;
    private String plainId;

    public TraceLogID(T source) {
        this(source, null);
    }

    public TraceLogID(T source, String postId) {
        String postfix;
        if (postId == null) {
            postfix = "UID";
        } else {
            postfix = postId;
        }
        try {
            plainId = createContent(source);
        } catch (Exception e) {
            plainId = null;
        }
        asString = "__[SECHUB-" + postfix + ":" + plainId + "]__";
    }

    /**
     * Creates content for ID
     *
     * @param source
     * @return
     * @throws Exception
     */
    protected abstract String createContent(T source) throws Exception;

    @Override
    public final String toString() {
        return asString;
    }

    public String getPlainId() {
        return plainId;
    }

}
