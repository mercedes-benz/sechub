package com.mercedesbenz.sechub.integrationtest.api;

public enum TextSearchMode {
    /**
     * Results only accepted when inspected text is exactly the same as search
     * string.
     */
    EXACT,

    /**
     * Results are accepted when the given search string is contained inside
     * inspected text.
     */
    CONTAINS,

    /**
     * Results are accepted when the given search regular expression matches with
     * inspected text
     */
    REGLAR_EXPRESSON,
}