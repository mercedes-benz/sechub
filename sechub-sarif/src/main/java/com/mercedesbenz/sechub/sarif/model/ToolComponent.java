// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Tool component object, see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317533">SARIF
 * 2.1.0 specification entry</a>
 *
 *
 * @author Albert Tregnaghi
 *
 */
@JsonPropertyOrder({ "guid", "name", "fullName", "product", "productSuite", "semanticVersion", "version", "releaseDateUtc", "downloadUri", "informationUri",
        "organization", "shortDescription", "fullDescription", "language", "rules", "taxa", "isComprehensive", "minimumRequiredLocalizedDataSemanticVersion" })
public class ToolComponent extends SarifObject {
    private String guid;
    private String name;
    private String fullName;
    private String product;
    private String productSuite;
    private String semanticVersion;
    private String version;
    private String releaseDateUtc;
    private String downloadUri;
    private String informationUri;
    private String organization;
    private Message shortDescription;
    private Message fullDescription;
    private String language;
    private List<Rule> rules;
    private List<Taxon> taxa;
    private boolean isComprehensive;
    private String minimumRequiredLocalizedDataSemanticVersion;

    public ToolComponent() {
        taxa = new LinkedList<>();
        rules = new LinkedList<>();
    }

    /**
     * @return guid or <code>null</code> when not defined
     */
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * @return name or <code>null</code> when not defined
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return fullName or <code>null</code> when not defined
     */
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return product or <code>null</code> when not defined
     */
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * @return productSuite or <code>null</code> when not defined
     */
    public String getProductSuite() {
        return productSuite;
    }

    public void setProductSuite(String productSuite) {
        this.productSuite = productSuite;
    }

    /**
     * @return semanticVersion or <code>null</code> when not defined
     */
    public String getSemanticVersion() {
        return semanticVersion;
    }

    public void setSemanticVersion(String semanticVersion) {
        this.semanticVersion = semanticVersion;
    }

    /**
     * @return version or <code>null</code> when not defined
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return releaseDateUtc or <code>null</code> when not defined
     */
    public String getReleaseDateUtc() {
        return releaseDateUtc;
    }

    public void setReleaseDateUtc(String releaseDateUtc) {
        this.releaseDateUtc = releaseDateUtc;
    }

    /**
     * @return downloadUri or <code>null</code> when not defined
     */
    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    /**
     * @return informationUri or <code>null</code> when not defined
     */
    public String getInformationUri() {
        return informationUri;
    }

    public void setInformationUri(String informationUri) {
        this.informationUri = informationUri;
    }

    /**
     * @return organization or <code>null</code> when not defined
     */
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * @return Message object with short description or <code>null</code> when not
     *         defined
     */
    public Message getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(Message shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * @return Message object with full description or <code>null</code> when not
     *         defined
     */
    public Message getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(Message fullDescription) {
        this.fullDescription = fullDescription;
    }

    /**
     * @return language or <code>null</code> when not defined
     */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return list of Rule objects or <code>null</code> when not defined
     */
    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * @return list of Taxon objects or <code>null</code> when not defined
     */
    public List<Taxon> getTaxa() {
        return taxa;
    }

    public void setTaxa(List<Taxon> taxa) {
        this.taxa = taxa;
    }

    public boolean isComprehensive() {
        return isComprehensive;
    }

    public void setComprehensive(boolean isComprehensive) {
        this.isComprehensive = isComprehensive;
    }

    /**
     * @return minimumRequiredLocalizedDataSemanticVersion or <code>null</code> when
     *         not defined
     */
    public String getMinimumRequiredLocalizedDataSemanticVersion() {
        return minimumRequiredLocalizedDataSemanticVersion;
    }

    public void setMinimumRequiredLocalizedDataSemanticVersion(String minimumRequiredLocalizedDataSemanticVersion) {
        this.minimumRequiredLocalizedDataSemanticVersion = minimumRequiredLocalizedDataSemanticVersion;
    }

    @Override
    public String toString() {
        return "ToolComponent [guid=" + guid + ", name=" + name + ", fullName=" + fullName + ", product=" + product + ", productSuite=" + productSuite
                + ", semanticVersion=" + semanticVersion + ", version=" + version + ", releaseDateUtc=" + releaseDateUtc + ", downloadUri=" + downloadUri
                + ", informationUri=" + informationUri + ", organization=" + organization + ", shortDescription=" + shortDescription + ", fullDescription="
                + fullDescription + ", language=" + language + ", rules=" + rules + ", taxa=" + taxa + ", isComprehensive=" + isComprehensive
                + ", minimumRequiredLocalizedDataSemanticVersion=" + minimumRequiredLocalizedDataSemanticVersion + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(downloadUri, fullDescription, fullName, guid, informationUri, isComprehensive, language,
                minimumRequiredLocalizedDataSemanticVersion, name, organization, product, productSuite, releaseDateUtc, rules, semanticVersion,
                shortDescription, taxa, version);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ToolComponent other = (ToolComponent) obj;
        return Objects.equals(downloadUri, other.downloadUri) && Objects.equals(fullDescription, other.fullDescription)
                && Objects.equals(fullName, other.fullName) && Objects.equals(guid, other.guid) && Objects.equals(informationUri, other.informationUri)
                && isComprehensive == other.isComprehensive && Objects.equals(language, other.language)
                && Objects.equals(minimumRequiredLocalizedDataSemanticVersion, other.minimumRequiredLocalizedDataSemanticVersion)
                && Objects.equals(name, other.name) && Objects.equals(organization, other.organization) && Objects.equals(product, other.product)
                && Objects.equals(productSuite, other.productSuite) && Objects.equals(releaseDateUtc, other.releaseDateUtc)
                && Objects.equals(rules, other.rules) && Objects.equals(semanticVersion, other.semanticVersion)
                && Objects.equals(shortDescription, other.shortDescription) && Objects.equals(taxa, other.taxa) && Objects.equals(version, other.version);
    }

}