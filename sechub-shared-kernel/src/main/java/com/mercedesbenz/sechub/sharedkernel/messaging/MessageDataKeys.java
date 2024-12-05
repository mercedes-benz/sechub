// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubDomainEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectTemplateData;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectToTemplate;

/**
 *
 * Constant class for {@link MessageDataKey} definitions used inside sechub
 * communication
 *
 * @author Albert Tregnaghi
 *
 */
public class MessageDataKeys {

    private static final SchedulerJobMessageDataProvider SCHEDULER_JOB_MESSAGE_DATA_PROVIDER = new SchedulerJobMessageDataProvider();
    private static final ClusterMemberMessageDataProvider CLUSTER_MEMBER_MESSAGE_DATA_PROVIDER = new ClusterMemberMessageDataProvider();
    private static final SecHubConfigurationMessageDataProvider SECHUB_CONFIGURATION_MESSAGE_DATA_PROVIDER = new SecHubConfigurationMessageDataProvider();
    private static final UUIDMessageDataProvider UID_MESSAGE_DATA_PROVIDER = new UUIDMessageDataProvider();
    private static final SecHubMessagesListDataProvider SECHUB_MESSAGES_LIST_MESSAGE_DATA_PROVIDER = new SecHubMessagesListDataProvider();
    private static final StringMessageDataProvider STRING_MESSAGE_DATA_PROVIDER = new StringMessageDataProvider();
    private static final StorageMessageDataProvider STORAGE_MESSAGE_DATA_PROVIDER = new StorageMessageDataProvider();
    private static final AnalyticMessageDataProvider ANALYTIC_MESSAGE_DATA_PROVIDER = new AnalyticMessageDataProvider();
    private static final MappingMessageDataProvider MAPPING_MESSAGE_DATA_PROVIDER = new MappingMessageDataProvider();
    private static final JobMessageDataProvider JOB_MESSAGE_DATA_PROVIDER = new JobMessageDataProvider();
    private static final ProjectMessageDataProvider PROJECT_MESSAGE_DATA_PROVIDER = new ProjectMessageDataProvider();
    private static final SchedulerMessageDataProvider SCHEDULER_MESSAGE_DATA_PROVIDER = new SchedulerMessageDataProvider();
    private static final AdministrationConfigMessageDataProvider ADMIN_CONFIG_MESSAGE_DATA_PROVIDER = new AdministrationConfigMessageDataProvider();
    private static final UserMessageDataProvider USER_MESSAGE_DATA_PROVIDER = new UserMessageDataProvider();
    private static final LocalDateTimeMessageDataProvider LOCAL_DATE_TIME_MESSAGE_DATA_PROVIDER = new LocalDateTimeMessageDataProvider();
    private static final SecHubEncryptionMessageDataProvider SECHUB_ENCRYPTION_MESSAGE_DATA_PROVIDER = new SecHubEncryptionMessageDataProvider();
    private static final SecHubEncryptionStatusMessageDataProvider SECHUB_DOMAIN_ENCRYPTION_STATUS_MESSAGE_DATA_PROVIDER = new SecHubEncryptionStatusMessageDataProvider();
    private static final SecHubProjectToTemplateMessageDataProvider SECHUB_PROJECT_TO_TEMPLATE_MESSAGE_DATA_PROVIDER = new SecHubProjectToTemplateMessageDataProvider();
    private static final SecHubProjectTemplatesMessageDataProvider SECHUB_PROJECT_TEMPLATES_MESSAGE_DATA_PROVIDER = new SecHubProjectTemplatesMessageDataProvider();

    /*
     * Only reason why this is not an emum is that we want to have generic type
     * information about what is contained in key...
     */
    private MessageDataKeys() {

    }

    /**
     * Contains a string with base url of sechub system
     */
    public static final MessageDataKey<String> ENVIRONMENT_BASE_URL = createKey("environment.base.url", STRING_MESSAGE_DATA_PROVIDER);
    public static final MessageDataKey<String> EXECUTED_BY = createKey("common.executedby", STRING_MESSAGE_DATA_PROVIDER);
    public static final MessageDataKey<String> REPORT_TRAFFIC_LIGHT = createKey("report.trafficlight", STRING_MESSAGE_DATA_PROVIDER);
    public static final MessageDataKey<SecHubMessagesList> REPORT_MESSAGES = createKey("report.messages", SECHUB_MESSAGES_LIST_MESSAGE_DATA_PROVIDER);

    public static final MessageDataKey<UUID> SECHUB_JOB_UUID = createKey("sechub.job.uuid", UID_MESSAGE_DATA_PROVIDER);
    public static final MessageDataKey<UUID> SECHUB_EXECUTION_UUID = createKey("sechub.execution.uuid", UID_MESSAGE_DATA_PROVIDER);

    public static final MessageDataKey<SecHubEncryptionData> SECHUB_ENCRYPT_ROTATION_DATA = createKey("sechub.encrypt.rotation.data",
            SECHUB_ENCRYPTION_MESSAGE_DATA_PROVIDER);

    public static final MessageDataKey<SecHubDomainEncryptionStatus> SECHUB_DOMAIN_ENCRYPTION_STATUS = createKey("sechub.domain.encryption.status",

            SECHUB_DOMAIN_ENCRYPTION_STATUS_MESSAGE_DATA_PROVIDER);

    public static final MessageDataKey<SecHubProjectToTemplate> PROJECT_TO_TEMPLATE = createKey("sechub.project2template.data",
            SECHUB_PROJECT_TO_TEMPLATE_MESSAGE_DATA_PROVIDER);

    public static final MessageDataKey<SecHubProjectTemplateData> PROJECT_TEMPLATES = createKey("sechub.project.templates.data",
            SECHUB_PROJECT_TEMPLATES_MESSAGE_DATA_PROVIDER);

    /**
     * Use this generic key when you just want to define timestamp without using a
     * dedicated model where it is already contained.
     */
    public static final MessageDataKey<LocalDateTime> LOCAL_DATE_TIME_SINCE = createKey("localdatetime.since", LOCAL_DATE_TIME_MESSAGE_DATA_PROVIDER);

    public static final MessageDataKey<SecHubConfiguration> SECHUB_UNENCRYPTED_CONFIG = createKey("sechub.unencryptedconfig",
            SECHUB_CONFIGURATION_MESSAGE_DATA_PROVIDER);

    public static final MessageDataKey<ClusterMemberMessage> ENVIRONMENT_CLUSTER_MEMBER_STATUS = createKey("environment.cluster.member.status",
            CLUSTER_MEMBER_MESSAGE_DATA_PROVIDER);

    public static final MessageDataKey<SchedulerJobMessage> SCHEDULER_JOB_STATUS = createKey("sechub.scheduler.job.status",
            SCHEDULER_JOB_MESSAGE_DATA_PROVIDER);
    /**
     * Must contain userid, email address
     */
    public static final MessageDataKey<UserMessage> USER_CONTACT_DATA = createUserMessageKey("user.signup.data");
    /**
     * Must contain userid, email address
     */
    public static final MessageDataKey<UserMessage> USER_SIGNUP_DATA = createUserMessageKey("user.signup.data");

    /**
     * Must contain userid, email address and initial roles
     */
    public static final MessageDataKey<UserMessage> USER_CREATION_DATA = createUserMessageKey("user.creation.data");

    /**
     * Must contain userid, hashed api token and email address
     */
    public static final MessageDataKey<UserMessage> USER_API_TOKEN_DATA = createUserMessageKey("user.apitoken.data");

    /**
     * Must contain userid and email address
     */
    public static final MessageDataKey<UserMessage> USER_DELETE_DATA = createUserMessageKey("user.delete.data");

    /**
     * Contains userid, email and a link containing a onetimetoken
     */
    public static final MessageDataKey<UserMessage> USER_ONE_TIME_TOKEN_INFO = createUserMessageKey("user.onetimetoken.info");

    /**
     * Contains userid + project ids
     */
    public static final MessageDataKey<UserMessage> PROJECT_TO_USER_DATA = createUserMessageKey("project2user.data");

    /**
     * Contains userid only
     */
    public static final MessageDataKey<UserMessage> USER_ID_DATA = createUserMessageKey("user.name");

    /**
     * Contains userid + roles
     */
    public static final MessageDataKey<UserMessage> USER_ROLES_DATA = createUserMessageKey("user.roles.data");

    /**
     * Must contain project id and whitelist entries
     */
    public static final MessageDataKey<ProjectMessage> PROJECT_CREATION_DATA = createProjectMessageKey("project.creation.data");

    /**
     * Must contain project id
     */
    public static final MessageDataKey<ProjectMessage> PROJECT_DELETE_DATA = createProjectMessageKey("project.delete.data");

    /**
     * Must contain project id and whitelist entries
     */
    public static final MessageDataKey<ProjectMessage> PROJECT_WHITELIST_UPDATE_DATA = createProjectMessageKey("project.whitelist.update.data");

    /**
     * Must contain project id, job uuid, and since (as JobMessage)
     */
    public static final MessageDataKey<JobMessage> JOB_CREATED_DATA = createJobMessageKey("job.created.data");

    /**
     * Must contain project id, job uuid, json configuration, owner, since
     */
    public static final MessageDataKey<JobMessage> JOB_STARTED_DATA = createJobMessageKey("job.started.data");

    /**
     * Must contain job uuid,since
     */
    public static final MessageDataKey<JobMessage> JOB_DONE_DATA = createJobMessageKey("job.done.data");

    /**
     * Must contain job uuid,since
     */
    public static final MessageDataKey<JobMessage> JOB_FAILED_DATA = createJobMessageKey("job.failed.data");
    /**
     * Must contain job uuid,since
     */
    public static final MessageDataKey<JobMessage> JOB_SUSPENDED_DATA = createJobMessageKey("job.suspended.data");

    /**
     * Must contain job uuid,job owner (but can be null)
     */
    public static final MessageDataKey<JobMessage> JOB_CANCEL_DATA = createJobMessageKey("job.cancel.data");
    public static final MessageDataKey<JobMessage> JOB_RESTART_DATA = createJobMessageKey("job.restart.data");

    public static final MessageDataKey<SchedulerMessage> SCHEDULER_STATUS_DATA = createSchedulerStatusMessageKey("scheduler.status");

    public static final MessageDataKey<MappingMessage> CONFIG_MAPPING_DATA = createMappingMessageKey("config.mapping.data");

    public static final MessageDataKey<ProjectMessage> PROJECT_OWNER_CHANGE_DATA = createProjectMessageKey("project.owner.change.data");

    /**
     * Does contain former access level and new one
     */
    public static final MessageDataKey<ProjectMessage> PROJECT_ACCESS_LEVEL_CHANGE_DATA = createProjectMessageKey("project.accesslevel.change.data");

    /**
     * Does contain userid, former email address, new email address
     */
    public static final MessageDataKey<UserMessage> USER_EMAIL_ADDRESS_CHANGE_DATA = createUserMessageKey("user.emailaddress.change.data");

    /**
     * Does contain amount of days after before auto cleanup must be executed
     */
    public static final MessageDataKey<AdministrationConfigMessage> AUTO_CLEANUP_CONFIG_CHANGE_DATA = createAdministrationConfigMessageKey(
            "autocleanup.config.change.data");

    public static final MessageDataKey<AnalyticMessageData> ANALYTIC_SCAN_RESULT_DATA = createAnalyticsData("analytic.scan.result.data");

    public static final MessageDataKey<StorageMessageData> UPLOAD_STORAGE_DATA = createStorageData("upload.storage.data");

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Helpers ..................................+ */
    /* +-----------------------------------------------------------------------+ */
    private static MessageDataKey<UserMessage> createUserMessageKey(String id) {
        return createKey(id, USER_MESSAGE_DATA_PROVIDER);
    }

    private static MessageDataKey<AdministrationConfigMessage> createAdministrationConfigMessageKey(String id) {
        return createKey(id, ADMIN_CONFIG_MESSAGE_DATA_PROVIDER);
    }

    private static MessageDataKey<SchedulerMessage> createSchedulerStatusMessageKey(String id) {
        return createKey(id, SCHEDULER_MESSAGE_DATA_PROVIDER);
    }

    private static MessageDataKey<ProjectMessage> createProjectMessageKey(String id) {
        return createKey(id, PROJECT_MESSAGE_DATA_PROVIDER);
    }

    private static MessageDataKey<JobMessage> createJobMessageKey(String id) {
        return createKey(id, JOB_MESSAGE_DATA_PROVIDER);
    }

    private static MessageDataKey<MappingMessage> createMappingMessageKey(String id) {
        return createKey(id, MAPPING_MESSAGE_DATA_PROVIDER);
    }

    private static MessageDataKey<AnalyticMessageData> createAnalyticsData(String id) {
        return createKey(id, ANALYTIC_MESSAGE_DATA_PROVIDER);
    }

    private static MessageDataKey<StorageMessageData> createStorageData(String id) {
        return createKey(id, STORAGE_MESSAGE_DATA_PROVIDER);
    }

    private static <T> MessageDataKey<T> createKey(String id, MessageDataProvider<T> provider) {
        return new MessageDataKey<>(id, provider);
    }

}
