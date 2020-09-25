// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.metadata;

import static com.daimler.sechub.commons.core.util.SimpleStringUtils.*;

import java.util.Objects;

/**
 * Classification content. Empty strings will be automatically converted to <code>null</code>
 * values, so we have no unnecessary data inside JSON output and its easier to
 * check something is missing or not
 * 
 * @author Albert Tregnaghi
 *
 */
public class SerecoClassification {

    private String owasp; // ...clear
    private String wasc; // wasc - webapplication consortium?
    private String cwe; // common weakness enum - see https://cwe.mitre.org/
    private String capec; // common attack pattern.
    private String pci31; // pcidss payment card data security standard
    private String pci32;
    private String hipaa; // health care ... USA
    private String nist;// National Institute of Standards and Technology -
                        // https://en.wikipedia.org/wiki/National_Institute_of_Standards_and_Technology
    private String fisma;// Federal Information Security Modernization Act (homeland security)
    private String cve;
    /**
     * https://www.owasp.org/index.php/OWASP_Proactive_Controls
     */
    private String owaspProactiveControls; // security techniques that should be used in every project

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SerecoClassification other = (SerecoClassification) obj;
        return Objects.equals(capec, other.capec) && Objects.equals(cve, other.cve) && Objects.equals(cwe, other.cwe) && Objects.equals(fisma, other.fisma)
                && Objects.equals(hipaa, other.hipaa) && Objects.equals(nist, other.nist) && Objects.equals(owasp, other.owasp)
                && Objects.equals(owaspProactiveControls, other.owaspProactiveControls) && Objects.equals(pci31, other.pci31)
                && Objects.equals(pci32, other.pci32) && Objects.equals(wasc, other.wasc);
    }

    public String getCapec() {
        return capec;
    }

    public String getCve() {
        return cve;
    }

    public String getCwe() {
        return cwe;
    }

    public String getFisma() {
        return fisma;
    }

    public String getHipaa() {
        return hipaa;
    }

    public String getNist() {
        return nist;
    }

    public String getOwasp() {
        return owasp;
    }

    public String getOwaspProactiveControls() {
        return owaspProactiveControls;
    }

    public String getPci31() {
        return pci31;
    }

    public String getPci32() {
        return pci32;
    }

    public String getWasc() {
        return wasc;
    }

    @Override
    public int hashCode() {
        return Objects.hash(capec, cve, cwe, fisma, hipaa, nist, owasp, owaspProactiveControls, pci31, pci32, wasc);
    }

    public void setCapec(String capec) {
        this.capec = emptyToNull(capec);
    }

    public void setCve(String cve) {
        this.cve = emptyToNull(cve);
    }

    public void setCwe(String cwe) {
        this.cwe = emptyToNull(cwe);
    }

    public void setFisma(String fisma) {
        this.fisma = emptyToNull(fisma);
    }

    public void setHipaa(String hipaa) {
        this.hipaa = emptyToNull(hipaa);
    }

    public void setNist(String nist) {
        this.nist = emptyToNull(nist);
    }

    public void setOwasp(String owasp) {
        this.owasp = emptyToNull(owasp);
    }

    public void setOwaspProactiveControls(String owaspProactiveControls) {
        this.owaspProactiveControls = emptyToNull(owaspProactiveControls);
    }

    public void setPci31(String pci31) {
        this.pci31 = emptyToNull(pci31);
    }

    public void setPci32(String pci32) {
        this.pci32 = emptyToNull(pci32);
    }

    public void setWasc(String wasc) {
        this.wasc = emptyToNull(wasc);
    }

    @Override
    public String toString() {
        return "SerecoClassification [" + (owasp != null ? "owasp=" + owasp + ", " : "") + (wasc != null ? "wasc=" + wasc + ", " : "")
                + (cwe != null ? "cwe=" + cwe + ", " : "") + (capec != null ? "capec=" + capec + ", " : "") + (pci31 != null ? "pci31=" + pci31 + ", " : "")
                + (pci32 != null ? "pci32=" + pci32 + ", " : "") + (hipaa != null ? "hipaa=" + hipaa + ", " : "") + (nist != null ? "nist=" + nist + ", " : "")
                + (fisma != null ? "fisma=" + fisma + ", " : "") + (cve != null ? "cve=" + cve + ", " : "")
                + (owaspProactiveControls != null ? "owaspProactiveControls=" + owaspProactiveControls : "") + "]";
    }

}
