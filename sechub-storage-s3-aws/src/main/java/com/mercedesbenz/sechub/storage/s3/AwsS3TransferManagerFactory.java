package com.mercedesbenz.sechub.storage.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

public class AwsS3TransferManagerFactory {
	public TransferManager create(AmazonS3 client) {
		long multipartUploadThreshold = 5 * 1024 * 1025; // threshold 5 MiB
		return TransferManagerBuilder.standard()
		  .withS3Client(client)
		  .withMultipartUploadThreshold(multipartUploadThreshold)
		  .build();
	}
}
