// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import org.springframework.stereotype.Component;

/**
 * A component for authorization value obfuscation - e.g. to avoid logging user
 * credentials.
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
public class AuthorizeValueObfuscator {

    private static final int AMOUNT_OF_REAL_PASSWORD_CHARACTERS_SHOWN_AS_HINT = 2;
    private static final String BASIC_WITH_SPACE_LOWERCASED = "basic ";

    /**
     * Obfuscates given authentication value (password, api token etc.). If given
     * value has a character length greater or equal given minimum value size for
     * hint, the leading two password characters will appear inside obfuscated
     * string. If a "BASIC " is at the beginning, it will also be shown in plain
     * text, but it is not used for password length determination, but only the real
     * password! Then three asterisks follow with a hint about length.
     * <h3>Example</h3>
     * 
     * <pre>
     *  obfuscate("Basic aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHdk",52); 
     *  will return
     *  "Basic aW***length:58",
     * 
     *  obfuscate("aW350LXalc3RexampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHdk",52);
     *  will return
     *  "aW***length:52"
     *  
     *  obfuscate("aW350LXalc3RxampleXWzZXI6aW50LXRlc3Rfb23seXVzZXtHdk",52);
     *  will return
     *  "***length:51"
     * </pre>
     * 
     * @param value                  the value to obfuscate. <code>null</code> wil
     *                               always be obfuscated to "null" string
     * @param valueMinSizeToShowHint this value defines at which password/api token
     *                               length two characters will become visible
     *                               inside obfuscated part. <b>Be aware</b>: This
     *                               decreases security level of password and you
     *                               should not set the value too low. E.g. it is
     *                               not a good idea to set this for passwords with
     *                               length of 10. In this case, if anybody would be
     *                               able to read the log files, the password would
     *                               be so secure like an 8 characters password -
     *                               which is easy to brute force.
     * @return obfuscated string, never <code>null</code>.
     */
    public String obfuscate(String value, int valueMinSizeToShowHint) {
        if (value == null) {
            return "null";
        }
        int passwordHintSize = calculateCharsToShowForHint(value);
        int passwordLengthWithoutBasic = value.length() - passwordHintSize + AMOUNT_OF_REAL_PASSWORD_CHARACTERS_SHOWN_AS_HINT;

        boolean showHint = passwordLengthWithoutBasic >= valueMinSizeToShowHint;

        StringBuilder sb = new StringBuilder();
        if (showHint) {
            if (passwordHintSize < value.length()) {
                sb.append(value.substring(0, passwordHintSize));
            }
        }
        sb.append("***length:" + value.length());
        return sb.toString();

    }

    private int calculateCharsToShowForHint(String value) {
        int showRealChars = AMOUNT_OF_REAL_PASSWORD_CHARACTERS_SHOWN_AS_HINT;
        if (value.toLowerCase().startsWith(BASIC_WITH_SPACE_LOWERCASED)) {
            showRealChars += BASIC_WITH_SPACE_LOWERCASED.length();
        }
        return showRealChars;
    }
}
