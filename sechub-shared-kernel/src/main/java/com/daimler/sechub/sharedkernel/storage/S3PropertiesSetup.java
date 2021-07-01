// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.storage.core.S3Setup;

@Component
public class S3PropertiesSetup implements S3Setup{

	private static final String UNDEFINED ="undefined";
	@MustBeDocumented(value="Defines the access key for used s3 bucket",scope="storage",secret=true)
	@Value("${sechub.storage.s3.accesskey:"+UNDEFINED+"}") // we use undefined here. Will be used in isValid
    private String accessKey;

	@MustBeDocumented(value="Defines the secret key for used s3 bucket",scope="storage",secret=true)
	@Value("${sechub.storage.s3.secretkey:"+UNDEFINED+"}") // we use undefined here. Will be used in isValid
    private String secretKey;

	@MustBeDocumented(value="Defines the s3 bucket name",scope="storage")
	@Value("${sechub.storage.s3.bucketname:"+UNDEFINED+"}") // we use undefined here. Will be used in isValid
    private String bucketName;

	@MustBeDocumented(value="Defines the s3 endpoint - e.g. https://play.min.io",scope="storage")
	@Value("${sechub.storage.s3.endpoint:"+UNDEFINED+"}") // we use undefined here. Will be used in isValid
    private String endpoint;

	@Override
	public String getAccessKey() {
		return accessKey;
	}

	@Override
	public String getSecretkey() {
		return secretKey;
	}

	@Override
	public String getEndPoint() {
		return endpoint;
	}

	@Override
	public String getBucketName() {
		return bucketName;
	}

	@Override
	public boolean isAvailable() {
		boolean inValid =false;

		inValid = inValid || UNDEFINED.equals(accessKey);
		inValid = inValid || UNDEFINED.equals(secretKey);
		inValid = inValid || UNDEFINED.equals(endpoint);
		inValid = inValid || UNDEFINED.equals(bucketName);

		return ! inValid;
	}


}
