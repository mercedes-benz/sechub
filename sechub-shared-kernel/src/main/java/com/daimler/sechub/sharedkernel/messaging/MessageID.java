// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum MessageID {
	START_SCAN( /* @formatter:off */
	            MessageDataKeys.SECHUB_UUID,
	            MessageDataKeys.BATCH_JOB_ID,
	            MessageDataKeys.EXECUTED_BY,
	            MessageDataKeys.SECHUB_CONFIG),
	/* @formatter:on */

	SCAN_DONE,

	SCAN_FAILED,
	
	/**
	 * Will happen because of scan has been restarted
	 */
	SCAN_ABANDONDED,

	/**
	 * This message will contain full data of an created user.
	 * Secure data will be only contained hashed.
	 *
	 */
	USER_CREATED(MessageDataKeys.USER_CREATION_DATA),

	/**
	 * This message will contain data for changes on user authorization.
	 *
	 */
	USER_API_TOKEN_CHANGED(MessageDataKeys.USER_API_TOKEN_DATA),

	/**
	 * Contains a link with one time token so user can create a new api token
	 */
	USER_NEW_API_TOKEN_REQUESTED(MessageDataKeys.USER_ONE_TIME_TOKEN_INFO),

	UNSUPPORTED_OPERATION,

	USER_ADDED_TO_PROJECT(MessageDataKeys.PROJECT_TO_USER_DATA),

	USER_REMOVED_FROM_PROJECT(MessageDataKeys.PROJECT_TO_USER_DATA),

	USER_ROLES_CHANGED(MessageDataKeys.USER_ROLES_DATA),

	USER_DELETED(MessageDataKeys.USER_DELETE_DATA),

	PROJECT_CREATED(MessageDataKeys.PROJECT_CREATION_DATA),

	PROJECT_DELETED(MessageDataKeys.PROJECT_DELETE_DATA),

	PROJECT_WHITELIST_UPDATED(MessageDataKeys.PROJECT_WHITELIST_UPDATE_DATA),

	/**
	 * Used when a new batch job has been started
	 */
	JOB_STARTED(MessageDataKeys.JOB_STARTED_DATA),

	/**
	 * Used when job was executed correctly. Independent if the sechub job fails or not. The (batch) execution was successful, means no internal error occurred.
	 */
	JOB_DONE(MessageDataKeys.JOB_DONE_DATA),

	USER_SIGNUP_REQUESTED(MessageDataKeys.USER_SIGNUP_DATA),

	/**
	 * Used when a batch job execution itself fails (job batch itself) means an internal error occurred.
	 */
	JOB_FAILED(MessageDataKeys.JOB_FAILED_DATA),

	/**
	 * Used when an action can change user role situation. The administration layer will
	 * recalculate roles and - if needed -trigger a {@link #USER_ROLES_CHANGED} event
	 */
	REQUEST_USER_ROLE_RECALCULATION(MessageDataKeys.USER_ID_DATA),

	/**
	 * Used to inform about new user becomes super administrator
	 */
	USER_BECOMES_SUPERADMIN(MessageDataKeys.USER_CONTACT_DATA, MessageDataKeys.ENVIRONMENT_BASE_URL),

	/**
	 * Used to inform about user is no longer super administrator
	 */
	USER_NO_LONGER_SUPERADMIN(MessageDataKeys.USER_CONTACT_DATA),


	REQUEST_SCHEDULER_DISABLE_JOB_PROCESSING,

	SCHEDULER_JOB_PROCESSING_ENABLED,

	SCHEDULER_JOB_PROCESSING_DISABLED,

	REQUEST_SCHEDULER_ENABLE_JOB_PROCESSING,

	/**
	 * Request status recalculation and send information events
	 */
	REQUEST_SCHEDULER_STATUS_UPDATE,

	/* Scheduler status update message, contains information about status*/
	SCHEDULER_STATUS_UPDATE,

	/* Request job to be canceled, contains JobUUID */
	REQUEST_JOB_CANCELATION(MessageDataKeys.JOB_CANCEL_DATA),

	/* Informs about Job being canceled, contains job uuid and owner*/
	JOB_CANCELED(MessageDataKeys.JOB_CANCEL_DATA),
	
	
    MAPPING_CONFIGURATION_CHANGED(MessageDataKeys.CONFIG_MAPPING_DATA),
	
    /* Request job to be restarted (soft)*/
    REQUEST_JOB_RESTART(MessageDataKeys.JOB_RESTART_DATA),

    /* Request job to be restarted (hard)*/
    REQUEST_JOB_RESTART_HARD(MessageDataKeys.JOB_RESTART_DATA), 
    
    JOB_RESTART_TRIGGERED(MessageDataKeys.JOB_RESTART_DATA,MessageDataKeys.ENVIRONMENT_BASE_URL), 
    
    JOB_RESTART_CANCELED(MessageDataKeys.JOB_RESTART_DATA, MessageDataKeys.ENVIRONMENT_BASE_URL),

    JOB_RESULTS_PURGED(MessageDataKeys.SECHUB_UUID, MessageDataKeys.ENVIRONMENT_BASE_URL),
    
    REQUEST_PURGE_JOB_RESULTS(MessageDataKeys.SECHUB_UUID, MessageDataKeys.ENVIRONMENT_BASE_URL),
    
    JOB_RESULT_PURGE_DONE(MessageDataKeys.SECHUB_UUID),
    
    JOB_RESULT_PURGE_FAILED(MessageDataKeys.SECHUB_UUID), 
    
    REQUEST_BATCH_JOB_STATUS(MessageDataKeys.BATCH_JOB_STATUS),
    
    BATCH_JOB_STATUS(MessageDataKeys.BATCH_JOB_STATUS), 
    
    /**
     * Informs that a scheduler has been started
     */
    SCHEDULER_STARTED(MessageDataKeys.ENVIRONMENT_BASE_URL, MessageDataKeys.ENVIRONMENT_CLUSTER_MEMBER_STATUS),
    
    PROJECT_OWNER_CHANGED(MessageDataKeys.PROJECT_OWNER_CHANGE_DATA, MessageDataKeys.ENVIRONMENT_BASE_URL),
    ;

	private Set<MessageDataKey<?>> unmodifiableKeys;

	/**
	 * The keys defined here MUST be available inside the message. But there can be additional ones as well!
	 * @param keys
	 */
	private MessageID(MessageDataKey<?> ... keys) {
		Set<MessageDataKey<?>> modifiableSet = new HashSet<>();
		if (keys!=null) {
			for (MessageDataKey<?> key: keys) {
				if (key==null) {
					continue;
				}
				modifiableSet.add(key);
			}
		}
		this.unmodifiableKeys=Collections.unmodifiableSet(modifiableSet);
	}

	public Set<MessageDataKey<?>> getContainedKeys() {
		return unmodifiableKeys;
	}

	public String getId() {
		return name();
	}

	static boolean isMessage(MessageID message, String id) {
		if (message==null) {
			return false;
		}
		if (id==null) {
			return false;
		}
		return id.equals(message.getId());
	}

	public boolean isSupportedKey(MessageDataKey<?> key) {
		if (key==null) {
			return false;
		}
		return unmodifiableKeys.contains(key);
	}


}
