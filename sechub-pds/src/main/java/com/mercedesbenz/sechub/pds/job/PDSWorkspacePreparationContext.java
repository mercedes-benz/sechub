// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

class PDSWorkspacePreparationContext {

    private boolean noneAccepted;
    private boolean binaryAccepted;
    private boolean sourceAccepted;
    private boolean extractedSourceAvailable;
    private boolean extractedBinaryAvailable;

    public void setNoneAccepted(boolean noneAccepted) {
        this.noneAccepted = noneAccepted;
    }

    public void setBinaryAccepted(boolean binaryAccepted) {
        this.binaryAccepted = binaryAccepted;
    }

    public void setSourceAccepted(boolean sourceAccepted) {
        this.sourceAccepted = sourceAccepted;
    }

    public void setExtractedBinaryAvailable(boolean extractedBinaryAvailable) {
        this.extractedBinaryAvailable = extractedBinaryAvailable;
    }

    public void setExtractedSourceAvailable(boolean extractedSourceAvailable) {
        this.extractedSourceAvailable = extractedSourceAvailable;
    }

    public boolean isSourceAccepted() {
        return sourceAccepted;
    }

    public boolean isBinaryAccepted() {
        return binaryAccepted;
    }

    public boolean isNoneAccepted() {
        return noneAccepted;
    }

    public boolean isExtractedBinaryAvailable() {
        return extractedBinaryAvailable;
    }

    public boolean isExtractedSourceAvailable() {
        return extractedSourceAvailable;
    }

    @Override
    public String toString() {
        return "PDSWorkspacePreparationContext [noneAccepted=" + noneAccepted + ", binaryAccepted=" + binaryAccepted + ", sourceAccepted=" + sourceAccepted
                + ", extractedSourceAvailable=" + extractedSourceAvailable + ", extractedBinaryAvailable=" + extractedBinaryAvailable + "]";
    }

}