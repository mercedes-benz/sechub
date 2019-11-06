package com.daimler.sechub.integrationtest.storage;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import com.adobe.testing.s3mock.junit4.S3MockRule;
import com.amazonaws.services.s3.AmazonS3;
import com.daimler.sechub.sharedkernel.storage.s3.MinioS3JobStorage;

import io.minio.MinioClient;

public class MinioS3JobStorageTest {

	@ClassRule
	public static final S3MockRule S3_MOCK_RULE = S3MockRule.builder().silent().build();

	private static AmazonS3 amazonTestClient;

	private static MinioClient minioClient;

	@BeforeClass
	public static void beforeClass() throws Exception {
		amazonTestClient = S3_MOCK_RULE.createS3Client();
		minioClient = new MinioClient("http://localhost:" + S3_MOCK_RULE.getHttpPort());
	}

	@Test
	public void a_new_storage_does_not_create_a_new_bucket() throws Exception {

		/* execute */
		new MinioS3JobStorage(minioClient, "bucket1", "projectName", UUID.randomUUID());

		/* test */
		assertTrue(amazonTestClient.listBuckets().isEmpty()); // also no bucket2 from former test execution...

	}

	@Test
	public void after_store_the_inputstream_is_closed() throws Exception {

		/* prepare */
		MinioS3JobStorage storage = new MinioS3JobStorage(minioClient, "bucket2", "projectName", UUID.randomUUID());

		Path tmpFile = Files.createTempFile("storage_test", ".txt");

		/* execute */
		InputStream inputStream = new FileInputStream(tmpFile.toFile());
		InputStream inputStreamSpy = Mockito.spy(inputStream);
		storage.store("testB", inputStreamSpy);

		/* test */
		Mockito.verify(inputStreamSpy).close(); // stream must be closed

	}

	@Test
	public void store_stores_textfile_correct_and_can_be_fetched() throws Exception {

		/* prepare */
		UUID jobjUUID = UUID.randomUUID();
		String testContent="line1\nline2";
		MinioS3JobStorage storage = new MinioS3JobStorage(minioClient, "bucket2", "projectName", jobjUUID);

		Path tmpFile = Files.createTempFile("storage_test", ".txt");
		BufferedWriter bw = Files.newBufferedWriter(tmpFile);

		bw.write(testContent);
		bw.close();

		/* execute */
		storage.store("testA", new FileInputStream(tmpFile.toFile()));

		/* test */
		String objectName = "jobstorage/projectName/"+jobjUUID+"/testA";
		assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName)); // test location is as expected

		MinioS3JobStorage storage2 = new MinioS3JobStorage(minioClient, "bucket2", "projectName", jobjUUID);
		InputStream loadedStream = storage2.fetch("testA");
		StringWriter writer = new StringWriter();
		IOUtils.copy(loadedStream, writer,Charset.defaultCharset());

		String loaded = writer.toString();

		assertEquals(testContent,loaded); //  test content can be fetched

	}

	@Test
	public void stored_object_is_deleted_by_deleteall() throws Exception {
		/* prepare */
		UUID jobUUID = UUID.randomUUID();
		MinioS3JobStorage storage = new MinioS3JobStorage(minioClient, "bucket2", "projectName", jobUUID);

		Path tmpFile = Files.createTempFile("storage_test", ".txt");

		/* execute */
		InputStream inputStream = new FileInputStream(tmpFile.toFile());
		InputStream inputStreamSpy = Mockito.spy(inputStream);
		storage.store("testC", inputStreamSpy);

		String objectName = "jobstorage/projectName/"+jobUUID+"/testC";
		assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName));

		/* execute */
		storage.deleteAll();

		/* test */
		assertFalse(amazonTestClient.doesObjectExist("bucket2", objectName));

	}

}
