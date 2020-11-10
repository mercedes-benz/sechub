package com.daimler.sechub.domain.administration.project;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Column;

@Entity
@IdClass(ProjectMetaData.class)
@Table(name = ProjectMetaDataEntry.TABLE_NAME)
public class ProjectMetaDataEntry {

	/* +-----------------------------------------------------------------------+ */
	/* +............................ SQL ......................................+ */
	/* +-----------------------------------------------------------------------+ */
	public static final String TABLE_NAME = "ADM_PROJECT_METADATA";
	
	public static final String COLUMN_PROJECT_ID = "PROJECT_ID";
	public static final String COLUMN_METADATA_KEY = "METADATA_KEY";
	public static final String COLUMN_METADATA_VALUE = "METADATA_VALUE";
	
	/* +-----------------------------------------------------------------------+ */
	/* +............................ JPQL .....................................+ */
	/* +-----------------------------------------------------------------------+ */
	public static final String CLASS_NAME = ProjectMetaDataEntry.class.getSimpleName();
	
	public ProjectMetaDataEntry() {
		// jpa only
	}
	
	ProjectMetaDataEntry(String projectId, String key, String value) {
		this.projectId = projectId;
		this.key = key;
		this.value = value;
	}
	
	@Id
	@Column(name = COLUMN_PROJECT_ID)
	String projectId;
	
	@Id
	@Column(name = COLUMN_METADATA_KEY)
	String key;
	
	@Column(name = COLUMN_METADATA_VALUE)
	String value;
	
}
