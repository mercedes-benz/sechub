// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;

/**
 * This message data object contains all possible information about a project
 * which can be interesting for messaging. BUT: It dependes on the
 * {@link MessageID} which parts are set.
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between (api) schedule domain and administration - and maybe others")
public class ProjectMessage implements JSONable<ProjectMessage> {

    private Set<URI> whitelist = new LinkedHashSet<>();

    private String projectId;

    private String projectOwnerEmailAddress;

    // applicable if owner was changed
    private String previousOwnerEmailAddress;

    private Set<String> userEmailAddresses = new LinkedHashSet<>(2);

    private String projectActionTriggeredBy;

    private ProjectAccessLevel formerAccessLevel;

    private ProjectAccessLevel newAccessLevel;

    private String projectOwnerUserId;

    @Override
    public Class<ProjectMessage> getJSONTargetClass() {
        return ProjectMessage.class;
    }

    /**
     * Add user email address suitable
     *
     * @param emailAddress
     */
    public void addUserEmailAddress(String emailAddress) {
        userEmailAddresses.add(emailAddress);
    }

    /**
     * Mail addresses for people being involved inside this message. Will be only
     * filled where necessary - e.g. when a project was deleted and some persons
     * (additional to super admins) have to be informed
     *
     * @return email addresses for this project message. never <code>null</code>
     */
    public Set<String> getUserEmailAddresses() {
        return userEmailAddresses;
    }

    public void setProjectOwnerEmailAddress(String emailAddress) {
        this.projectOwnerEmailAddress = emailAddress;
    }

    public String getProjectOwnerEmailAddress() {
        return projectOwnerEmailAddress;
    }

    public void setPreviousProjectOwnerEmailAddress(String emailAddress) {
        this.previousOwnerEmailAddress = emailAddress;
    }

    public String getPreviousProjectOwnerEmailAddress() {
        return previousOwnerEmailAddress;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setWhitelist(Set<URI> whitelist) {
        this.whitelist = whitelist;
    }

    public void setProjectOwnerUserId(String projectOwnerUserId) {
        this.projectOwnerUserId = projectOwnerUserId;
    }

    public Set<URI> getWhitelist() {
        return whitelist;
    }

    public void setProjectActionTriggeredBy(String projectActionTriggeredBy) {
        this.projectActionTriggeredBy = projectActionTriggeredBy;
    }

    public String getProjectActionTriggeredBy() {
        return projectActionTriggeredBy;
    }

    public void setFormerAccessLevel(ProjectAccessLevel formerAccessLevel) {
        this.formerAccessLevel = formerAccessLevel;
    }

    public ProjectAccessLevel getFormerAccessLevel() {
        return formerAccessLevel;
    }

    public void setNewAccessLevel(ProjectAccessLevel newAccessLevel) {
        this.newAccessLevel = newAccessLevel;
    }

    public ProjectAccessLevel getNewAccessLevel() {
        return newAccessLevel;
    }

    public String getProjectOwnerUserId() {
        return projectOwnerUserId;
    }

}
