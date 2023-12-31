// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;

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

    private String emailAddress;

    private String userId;

    private String hashedApiToken;

    private Set<String> roles;

    private String linkWithOneTimeToken;

    private List<String> projectIds;

    private String subject;

    private String formerEmailAddress;

    @Override
    public Class<UserMessage> getJSONTargetClass() {
        return UserMessage.class;
    }

    public String getUserId() {
        return userId;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<String> getProjectIds() {
        return projectIds;
    }

    public String getEmailAddress() {
        return emailAddress;
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

    /**
     * Returns the former email address of an user. This information is only
     * available on events about email changes. All other events will have not this
     * information.
     *
     * @return former email address or <code>null</code>
     */
    public String getFormerEmailAddress() {
        return formerEmailAddress;
    }

    /**
     * Set the former email address of an user. Should only be called for user
     * events when an email address has changed. The {@link #getEmailAddress()}
     * shall contain the new email address in this case.
     *
     * @param formerEmailAddress
     */
    public void setFormerEmailAddress(String formerEmailAddress) {
        this.formerEmailAddress = formerEmailAddress;
    }

}
