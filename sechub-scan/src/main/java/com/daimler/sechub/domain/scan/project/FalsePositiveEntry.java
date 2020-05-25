package com.daimler.sechub.domain.scan.project;

import java.util.Objects;

public class FalsePositiveEntry {

    private FalsePositiveJobData jobData;

    private String author;

    private FalsePositiveMetaData metaData;

    public void setJobData(FalsePositiveJobData jobData) {
        this.jobData = jobData;
    }

    public FalsePositiveJobData getJobData() {
        return jobData;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public FalsePositiveMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(FalsePositiveMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        return "FalsePositiveEntry [jobData=" + jobData + ", author=" + author + ", metaData=" + metaData + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, jobData, metaData);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FalsePositiveEntry other = (FalsePositiveEntry) obj;
        return Objects.equals(author, other.author) && Objects.equals(jobData, other.jobData) && Objects.equals(metaData, other.metaData);
    }

}
