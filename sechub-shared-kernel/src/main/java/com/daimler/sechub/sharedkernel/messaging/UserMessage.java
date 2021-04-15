// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This message data object contains all possible information about an user
 * which can be interesting for messaging. BUT: It depends on the
 * {@link MessageID} which parts are set. E.g. an API token change will only
 * contain information about userid and new api token... but about roles etc.
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between (api) authorization domain and administration - and maybe others")
public class UserMessage implements JSONable<UserMessage> {

    private String emailAdress;

    private String userId;

    private String hashedApiToken;

    private Set<String> roles;

    private String linkWithOneTimeToken;

    private List<String> projectIds;

    private String subject;

    @Override
    public Class<UserMessage> getJSONTargetClass() {
        return UserMessage.class;
    }

    public String getUserId() {
        return userId;
    }

    public void setEmailAdress(String emailAdress) {
        this.emailAdress = emailAdress;
    }

    public List<String> getProjectIds() {
        return projectIds;
    }

    public String getEmailAdress() {
        return emailAdress;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return first project id from {@link #getProjectIds()} or <code>null</code>
     */
    public String getProjectId() {
        if (projectIds == null) {
            return null;
        }
        if (projectIds.isEmpty()) {
            return null;
        }
        return projectIds.iterator().next();
    }

    public void setProjectId(String projectId) {
        this.projectIds = Arrays.asList(projectId);
    }

    public void setProjectIds(List<String> projectIds) {
        this.projectIds = projectIds;
    }

    public String getHashedApiToken() {
        return hashedApiToken;
    }

    public void setLinkWithOneTimeToken(String linkWithOneTimeToken) {
        this.linkWithOneTimeToken = linkWithOneTimeToken;
    }

    public String getLinkWithOneTimeToken() {
        return linkWithOneTimeToken;
    }

    public void setHashedApiToken(String hashedApiToken) {
        this.hashedApiToken = hashedApiToken;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /**
     * Set subject for the message. Will be used for example on emails as email
     * subject.
     * 
     * @param reason
     */
    public void setSubject(String reason) {
        this.subject = reason;
    }

    public String getSubject() {
        return subject;
    }

}
