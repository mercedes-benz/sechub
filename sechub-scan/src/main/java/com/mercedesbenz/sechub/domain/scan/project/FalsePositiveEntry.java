// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

public class FalsePositiveEntry {

    public static final String PROPERTY_JOBDATA = "jobData";
    public static final String PROPERTY_AUTHOR = "author";
    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_METADATA = "metaData";
    public static final String PROPERTY_PROJECTDATA = "projectData";

    private FalsePositiveJobData jobData;

    private String author;

    private FalsePositiveMetaData metaData;

    private FalsePositiveProjectData projectData;

    private Date created = new Date(); // we use initial now

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getCreated() {
        return created;
    }

    public void setJobData(FalsePositiveJobData jobData) {
        this.jobData = jobData;
    }

    public FalsePositiveJobData getJobData() {
        return jobData;
    }

    public void setProjectData(FalsePositiveProjectData projectData) {
        this.projectData = projectData;
    }

    public FalsePositiveProjectData getProjectData() {
        return projectData;
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
