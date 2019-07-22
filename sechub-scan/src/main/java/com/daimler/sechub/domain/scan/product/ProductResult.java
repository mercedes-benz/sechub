// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import static javax.persistence.EnumType.*;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

/**
 * Represents a product result for a SecHub job UUID
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = ProductResult.TABLE_NAME)
public class ProductResult {

	/* +-----------------------------------------------------------------------+ */
	/* +............................ SQL ......................................+ */
	/* +-----------------------------------------------------------------------+ */
	public static final String TABLE_NAME = "SCAN_PRODUCT_RESULT";

	public static final String COLUMN_UUID = "UUID";
	public static final String COLUMN_SECHUB_JOB_UUID = "SECHUB_JOB_UUID";
	public static final String COLUMN_PRODUCT_IDENTIFIER = "PRODUCT_ID";

	public static final String COLUMN_RESULT = "RESULT";

	public static final String COLUMN_STARTED = "STARTED";
	public static final String COLUMN_ENDED = "ENDED";
	/* +-----------------------------------------------------------------------+ */
	/* +............................ JPQL .....................................+ */
	/* +-----------------------------------------------------------------------+ */
	public static final String CLASS_NAME = ProductResult.class.getSimpleName();

	public static final String PROPERTY_SECHUB_JOB_UUID = "secHubJobUUID";
	public static final String PROPERTY_PRODUCT_IDENTIFIER = "productIdentifier";

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = COLUMN_UUID, updatable = false, nullable = false)
	UUID uUID;

	public UUID getUUID() {
		return uUID;
	}

	/**
	 * The type of the product. Just to identify result content
	 */
	@Enumerated(STRING)
	@Column(name = COLUMN_PRODUCT_IDENTIFIER, nullable = false)
	private ProductIdentifier productIdentifier;

	@Type(type="text") // why not using @Lob, because hibernate/postgres issues. see https://stackoverflow.com/questions/25094410/hibernate-error-while-persisting-text-datatype?noredirect=1#comment39048566_25094410
	@Column(name = COLUMN_RESULT)
	private String result;

	@Column(name = COLUMN_SECHUB_JOB_UUID, updatable = false, nullable = false)
	UUID secHubJobUUID;

	@Version
	@Column(name = "VERSION")
	Integer version;

	@Column(name = COLUMN_STARTED) // remark: we setup hibernate to use UTC settings - see application.properties
	LocalDateTime started;

	@Column(name = COLUMN_ENDED) // remark: we setup hibernate to use UTC settings - see application.properties
	LocalDateTime ended;

	ProductResult() {
		// jpa only
	}

	/**
	 * Create the product result
	 *
	 * @param report
	 * @param productIdentifier
	 * @param result
	 *            as string
	 */
	public ProductResult(UUID secHubJobUUID, ProductIdentifier productIdentifier, String result) {
		if (secHubJobUUID == null) {
			throw new IllegalArgumentException("SecHub JOB UUID may not be null!");
		}
		if (productIdentifier == null) {
			throw new IllegalArgumentException("Product identifier not be null!");
		}
		this.secHubJobUUID = secHubJobUUID;
		this.productIdentifier = productIdentifier;
		this.result = result;

	}

	public ProductIdentifier getProductIdentifier() {
		return productIdentifier;
	}

	public UUID getSecHubJobUUID() {
		return secHubJobUUID;
	}

	public String getResult() {
		return result;
	}

	public void setStarted(LocalDateTime started) {
		this.started = started;
	}

	public LocalDateTime getStarted() {
		return started;
	}

	public void setEnded(LocalDateTime ended) {
		this.ended = ended;
	}

	public LocalDateTime getEnded() {
		return ended;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uUID == null) ? 0 : uUID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductResult other = (ProductResult) obj;
		if (uUID == null) {
			if (other.uUID != null)
				return false;
		} else if (!uUID.equals(other.uUID))
			return false;
		return true;
	}

}
