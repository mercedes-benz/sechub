// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

public class AdapterException extends Exception {

    private static final long serialVersionUID = -2254915005975876283L;

    public AdapterException(AdapterLogId id, String message, Throwable cause) {
        super(id.withMessage(message), cause);
    }

    public AdapterException(AdapterLogId id, String message) {
        super(id.withMessage(message));
    }

    public AdapterException(AdapterLogId id, Throwable cause) {
        super(id.withMessage(null), cause);
    }

    /**
     * Throws given exception as adapter exception. If the exception is already an
     * adapter exception the origin exception will be thrown (the given message and
     * adapter idwill be ignored then)
     *
     * @param message
     * @param t
     * @throws AdapterException
     */
    public static void throwAsAdapterException(AdapterLogId id, String message, Throwable t) throws AdapterException {
        throw asAdapterException(id, message, t);
    }

    /**
     * Returns an exception for given one. If the exception is already an adapter
     * exception the origin exception will be thrown (the given message and adapter
     * idwill be ignored then)
     *
     * @param id
     * @param message
     * @param t
     * @return
     * @throws AdapterException
     */
    public static AdapterException asAdapterException(AdapterLogId id, String message, Throwable t) {
        if (t instanceof AdapterException) {
            return (AdapterException) t;
        } else {
            return new AdapterException(id, message, t);
        }
    }

}
