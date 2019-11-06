package com.daimler.sechub.sharedkernel.storage.s3;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import com.daimler.sechub.sharedkernel.storage.JobStorage;

import io.minio.ErrorCode;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.ServerSideEncryption;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import io.minio.messages.Item;

public class MinioS3JobStorage implements JobStorage {

	private static final Logger LOG = LoggerFactory.getLogger(MinioS3JobStorage.class);

	private MinioClient client;
	private String bucketName;
	private String projectId;
	private UUID jobUUID;

	public MinioS3JobStorage(MinioClient client, String bucketName, String projectId, UUID jobUUID) {
		this.bucketName = bucketName;
		this.client = client;
		this.projectId = projectId;
		this.jobUUID = jobUUID;
	}

	@Override
	public void store(String name, InputStream inputStream) throws IOException {
		notNull(name, "name may not be null!");
		notNull(inputStream, "inputStream may not be null!");

		try (InputStream stream=inputStream){
			if (!client.bucketExists(bucketName)) {
				client.makeBucket(bucketName);
			}
			Map<String, String> headerMap = null;
			Long available = Long.valueOf(stream.available());
			/* TODO de-jcup, 2019-11-05: s3 server side encryption - implement/handle */
			ServerSideEncryption sse = null;
			String contentType = null;

			client.putObject(bucketName, getObjectName(name), stream, available, headerMap, sse, contentType);

		} catch (Exception e) {
			throw new IOException("Store of " + name + " to s3 failed", e);
		}

	}

	private String getObjectName(String name) {
		return getObjectPrefix() + name;
	}

	private String getObjectPrefix() {
		return "jobstorage/" + projectId + "/" + jobUUID + "/";
	}

	@Override
	public InputStream fetch(String name) throws IOException {
		try {
			return client.getObject(bucketName, getObjectName(name));
		} catch (Exception e) {
			throw new IOException("Was not able to fetch object from s3 bucket:" + name);
		}
	}

	@Override
	public void deleteAll() throws IOException {
		Iterable<Result<Item>> data = null;
		try {
			data = client.listObjects(bucketName, getObjectPrefix());
		} catch (XmlPullParserException e) {
			LOG.error("Delete on S3 failed - cannot list objects for {}", getObjectPrefix(), e);
			return;
		}

		Set<String> pathes = new LinkedHashSet<>();
		Iterator<Result<Item>> it = data.iterator();
		while (it.hasNext()) {
			Result<Item> next = it.next();
			Item item;
			try {
				item = next.get();
				pathes.add(item.objectName());
			} catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException
					| ErrorResponseException | InternalException | XmlPullParserException e) {
				LOG.error("One delete on S3 failed for item in {}", getObjectPrefix(), e);
			}
		}
		client.removeObjects(bucketName, pathes);
	}

	@Override
	public boolean isExisting(String name) throws IOException {
		try {
			client.statObject(bucketName, getObjectName(name));
			return true;
		} catch (ErrorResponseException e) {
			if (e.errorResponse().errorCode() != ErrorCode.NO_SUCH_OBJECT) {
				throw new IOException("Check for existing of " + name + " failed", e);
			}
			return false;
		} catch (Exception e) {
			throw new IOException("Check for existing of " + name + " failed", e);
		}
	}

}
