// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.util.UUID;

import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;

/**
 *
 * Constant class for {@link MessageDataKey} definitions used inside sechub communication
 * @author Albert Tregnaghi
 *
 */
public class MessageDataKeys {

	/*
	 * Only reason why this is not an emum is that we want to have generic type
	 * information about what is contained in key...
	 */

	private MessageDataKeys() {

	}
	/**
	 * Contains a string with base url of sechub system
	 */
	public static final MessageDataKey<String> ENVIRONMENT_BASE_URL = createKey("environment.base.url",
			new StringMessageDataProvider());
	public static final MessageDataKey<String> EXECUTED_BY = createKey("common.executedby",
			new StringMessageDataProvider());
	public static final MessageDataKey<String> REPORT_TRAFFIC_LIGHT = createKey("report.trafficlight",
			new StringMessageDataProvider());
	public static final MessageDataKey<UUID> SECHUB_UUID = createKey("sechub.uuid", new UUIDMessageDataProvider());
	public static final MessageDataKey<SecHubConfiguration> SECHUB_CONFIG = createKey("sechub.config",
			new SecHubConfigurationMessageDataProvider());
	public static final MessageDataKey<BatchJobMessage> BATCH_JOB_ID= createKey("sechub.batchjob.id", new BatchJobMessageDataProvider());
	public static final MessageDataKey<BatchJobMessage> BATCH_JOB_STATUS= createKey("sechub.batchjob.status", new BatchJobMessageDataProvider());
	public static final MessageDataKey<ClusterMemberMessage> ENVIRONMENT_CLUSTER_MEMBER_STATUS = createKey("environment.cluster.member.status",
            new ClusterMemberMessageDataProvider());
	/**
	 * Must contain userid, mail adress
	 */
	public static final MessageDataKey<UserMessage> USER_CONTACT_DATA = createUserMessageKey("user.signup.data");
	/**
	 * Must contain userid, mail adress
	 */
	public static final MessageDataKey<UserMessage> USER_SIGNUP_DATA = createUserMessageKey("user.signup.data");

	/**
	 * Must contain userid, mail adress and initial roles
	 */
	public static final MessageDataKey<UserMessage> USER_CREATION_DATA = createUserMessageKey("user.creation.data");

	/**
	 * Must contain userid, hashed api token and email adress
	 */
	public static final MessageDataKey<UserMessage> USER_API_TOKEN_DATA = createUserMessageKey("user.apitoken.data");

	/**
	 * Must contain userid and email adress
	 */
	public static final MessageDataKey<UserMessage> USER_DELETE_DATA = createUserMessageKey("user.delete.data");

	/**
	 * Contains userid, email and a link containing a onetimetoken
	 */
	public static final MessageDataKey<UserMessage> USER_ONE_TIME_TOKEN_INFO = createUserMessageKey(
			"user.onetimetoken.info");

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
	public static final MessageDataKey<ProjectMessage> PROJECT_CREATION_DATA = createProjectMessageKey(
			"project.creation.data");

	/**
	 * Must contain project id
	 */
	public static final MessageDataKey<ProjectMessage> PROJECT_DELETE_DATA = createProjectMessageKey("project.delete.data");

	/**
	 * Must contain project id and whitelist entries
	 */
	public static final MessageDataKey<ProjectMessage> PROJECT_WHITELIST_UPDATE_DATA = createProjectMessageKey(
			"project.whitelist.update.data");

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
	 * Must contain job uuid,job owner (but can be null)
	 */
	public static final MessageDataKey<JobMessage> JOB_CANCEL_DATA = createJobMessageKey("job.cancel.data");
	public static final MessageDataKey<JobMessage> JOB_RESTART_DATA = createJobMessageKey("job.restart.data");

	public static final MessageDataKey<SchedulerMessage> SCHEDULER_STATUS_DATA = createSchedulerStatusMessageKey("scheduler.status");

	public static final MessageDataKey<MappingMessage> CONFIG_MAPPING_DATA = createMappingMessageKey("config.mapping.data");

	public static final MessageDataKey<ProjectMessage> PROJECT_OWNER_CHANGE_DATA = createProjectMessageKey("project.owner.change.data");
	
	/* +-----------------------------------------------------------------------+ */
	/* +............................ Helpers ..................................+ */
	/* +-----------------------------------------------------------------------+ */
	private static MessageDataKey<UserMessage> createUserMessageKey(String id) {
		return createKey(id, new UserMessageDataProvider());
	}

	private static MessageDataKey<SchedulerMessage> createSchedulerStatusMessageKey(String id) {
		return createKey(id, new SchedulerMessageDataProvider());
	}

	private static MessageDataKey<ProjectMessage> createProjectMessageKey(String id) {
		return createKey(id, new ProjectMessageDataProvider());
	}

	private static MessageDataKey<JobMessage> createJobMessageKey(String id) {
		return createKey(id, new JobMessageDataProvider());
	}
	
	private static MessageDataKey<MappingMessage> createMappingMessageKey(String id) {
        return createKey(id, new MappingMessageDataProvider());
    }
	

	private static <T> MessageDataKey<T> createKey(String id, MessageDataProvider<T> provider) {
		return new MessageDataKey<>(id, provider);
	}

}
