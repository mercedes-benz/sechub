// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * "A run object describes a single run of an analysis tool and contains the
 * output of that run." see
 *
 * <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317497">SARIF
 * 2.1.0 specification entry</a>
 *
 */
@JsonPropertyOrder({ "repositoryUri", "revisionId", "branch", "revisionTag", "asOfTimeUtc", "mappedTo" })
public class VersionControlDetails extends SarifObject {

    private String repositoryUri;
    private String revisionId;
    private String branch;
    private String revisionTag;
    private String asOfTimeUtc;

    private ArtifactLocation mappedTo;

    public String getRepositoryUri() {
        return repositoryUri;
    }

    public void setRepositoryUri(String repositoryUri) {
        this.repositoryUri = repositoryUri;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getRevisionTag() {
        return revisionTag;
    }

    public void setRevisionTag(String revisionTag) {
        this.revisionTag = revisionTag;
    }

    public String getAsOfTimeUtc() {
        return asOfTimeUtc;
    }

    public void setAsOfTimeUtc(String asOfTimeUtc) {
        this.asOfTimeUtc = asOfTimeUtc;
    }

    public ArtifactLocation getMappedTo() {
        return mappedTo;
    }

    public void setMappedTo(ArtifactLocation mappedTo) {
        this.mappedTo = mappedTo;
    }

    @Override
    public String toString() {
        return "VersionControlDetails [repositoryUri=" + repositoryUri + ", revisionId=" + revisionId + ", branch=" + branch + ", revisionTag=" + revisionTag
                + ", asOfTimeUtc=" + asOfTimeUtc + ", mappedTo=" + mappedTo + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(asOfTimeUtc, branch, mappedTo, repositoryUri, revisionId, revisionTag);
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
        VersionControlDetails other = (VersionControlDetails) obj;
        return Objects.equals(asOfTimeUtc, other.asOfTimeUtc) && Objects.equals(branch, other.branch) && Objects.equals(mappedTo, other.mappedTo)
                && Objects.equals(repositoryUri, other.repositoryUri) && Objects.equals(revisionId, other.revisionId)
                && Objects.equals(revisionTag, other.revisionTag);
    }

}
