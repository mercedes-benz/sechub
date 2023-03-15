// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

/**
 * Content of an artifact, see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317422">SARIF
 * 2.1.0 specification entry (3.3)</a>
 *
 * @author Albert Tregnaghi
 *
 */
public class ArtifactContent extends SarifObject {

    private String text;
    private String binary;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * set binary content in MIME Base64 format
     *
     * @return binary content in MIME Base64 format
     */
    public String getBinary() {
        return binary;
    }

    /**
     * Set binary content
     *
     * @param mimeBase64encodeString the binary content encoded in base64 encoded
     */
    public void setBinary(String mimeBase64encodeString) {
        this.binary = mimeBase64encodeString;
    }
}
