package com.mercedesbenz.sechub.commons.core.prepare;

/**
 * Represents a result from prepare phase.
 *
 */
public class PrepareResult {

    private PrepareStatus status;

    private PrepareResult() {
    }

    public PrepareResult(PrepareStatus status) {
        this.status = status;
    }

    /**
     * Parses given text and extracts a result.
     *
     * Examples with correct format:
     *
     * <pre>
     * SECHUB_PREPARE_RESULT;status=OK
     * </pre>
     *
     * or
     *
     * <pre>
     * SECHUB_PREPARE_RESULT;status=FAILED
     * </pre>
     *
     * @param text
     * @return result
     */
    public static PrepareResult fromString(String text) {
        PrepareResult result = new PrepareResult();
        if (text == null || text.isBlank()) {
            return result;
        }
        if (!text.startsWith(PrepareConstants.SECHUB_PREPARE_RESULT)) {
            return result;
        }
        String[] splitted = text.split(";");
        if (splitted.length == 0) {
            return result;
        }
        for (String part : splitted) {
            String[] pair = part.split("=");
            if (pair.length != 2) {
                continue;
            }
            String key = pair[0].trim();
            String value = pair[1].trim();
            if (PrepareConstants.STATUS.equalsIgnoreCase(key)) {
                result.status = PrepareStatus.fromString(value);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return PrepareConstants.SECHUB_PREPARE_RESULT + ";" + PrepareConstants.STATUS + "=" + status;
    }

    public PrepareStatus getStatus() {
        return status;
    }

    public boolean isPreparationDone() {
        return PrepareStatus.OK.equals(status);
    }
}
