// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = ScheduleConfig.TABLE_NAME)
public class ScheduleConfig {

	/* +-----------------------------------------------------------------------+ */
	/* +............................ SQL ......................................+ */
	/* +-----------------------------------------------------------------------+ */
	public static final String TABLE_NAME = "SCHEDULE_CONFIG";

	public static final String COLUMN_KEY_ID = "CONFIG_ID";

	public static final String COLUMN_ONE_ROW_ONLY = "CONFIG_ONE_ROW_ONLY";

	public static final String COLUMN_JOB_PROCESSING_ENABLED = "CONFIG_JOB_PROCESSING_ENABLED";

	/* +-----------------------------------------------------------------------+ */
	/* +............................ JPQL .....................................+ */
	/* +-----------------------------------------------------------------------+ */
	public static final String CLASS_NAME = ScheduleConfig.class.getSimpleName();

	@Id
	@Column(name = COLUMN_KEY_ID, unique = true, nullable = false)
	Integer id;

	@Column(name = COLUMN_ONE_ROW_ONLY, unique = true, nullable = false)
	String value="enforce-only-one-row"; // because unique and not changeable we enforce only one row inside database...

	@Column(name = COLUMN_JOB_PROCESSING_ENABLED, nullable = false)
	boolean jobProcessingEnabled = true;

    @Version
	@Column(name = "VERSION")
	Integer version;

    public void setJobProcessingEnabled(boolean jobProcessingEnabled) {
		this.jobProcessingEnabled = jobProcessingEnabled;
	}

    public boolean isJobProcessingEnabled() {
		return jobProcessingEnabled;
	}

	public Integer getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ScheduleConfig other = (ScheduleConfig) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}



}