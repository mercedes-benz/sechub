package com.daimler.sechub.storage.s3;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.daimler.sechub.storage.s3.aws.AwsS3JobStorage;
import com.daimler.sechub.test.TestPortProvider;

public class AwsS3JobStorageTest {

	@ClassRule
	public static final S3MockRule S3_MOCK_RULE = S3MockRule.builder().
			withHttpPort(TestPortProvider.DEFAULT_INSTANCE.getS3MockServerHttpPort()).
			withHttpsPort(TestPortProvider.DEFAULT_INSTANCE.getS3MockServerHttpsPort()).
//			silent().
			build();

	private static AmazonS3 amazonTestClient;

	@BeforeClass
	public static void beforeClass() throws Exception {
		amazonTestClient = S3_MOCK_RULE.createS3Client();
	}

	@Test
	public void a_new_storage_does_not_create_a_new_bucket() throws Exception {

		/* execute */
		new AwsS3JobStorage(amazonTestClient, "bucket1", "projectName", UUID.randomUUID());

		/* test */
		assertTrue(amazonTestClient.listBuckets().isEmpty()); // also no bucket2 from former test execution...

	}

	@Test
	public void after_store_the_inputstream_is_closed() throws Exception {

		/* prepare */
		AwsS3JobStorage storage = new AwsS3JobStorage(amazonTestClient, "bucket2", "projectName", UUID.randomUUID());

		Path tmpFile = Files.createTempFile("storage_test", ".txt");

		/* execute */
		InputStream inputStream = new FileInputStream(tmpFile.toFile());
		InputStream inputStreamSpy = Mockito.spy(inputStream);
		storage.store("testB", inputStreamSpy);

		/* test */
		Mockito.verify(inputStreamSpy,Mockito.atLeast(1)).close(); // stream must be closed

	}

	@Test
	public void store_stores_textfile_correct_and_can_be_fetched() throws Exception {

		/* prepare */
		UUID jobjUUID = UUID.randomUUID();
		String testContent="line1\nline2";
		AwsS3JobStorage storage = new AwsS3JobStorage(amazonTestClient, "bucket2", "projectName", jobjUUID);

		Path tmpFile = Files.createTempFile("storage_test", ".txt");
		BufferedWriter bw = Files.newBufferedWriter(tmpFile);

		bw.write(testContent);
		bw.close();

		/* execute */
		storage.store("testA", new FileInputStream(tmpFile.toFile()));

		/* test */
		String objectName = "jobstorage/projectName/"+jobjUUID+"/testA";
		assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName)); // test location is as expected

		AwsS3JobStorage storage2 = new AwsS3JobStorage(amazonTestClient, "bucket2", "projectName", jobjUUID);
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
		AwsS3JobStorage storage = storeTestData(jobUUID);

		String objectName = "jobstorage/projectName/"+jobUUID+"/testC";
		assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName));

		/* execute */
		storage.deleteAll();

		/* test */
		assertFalse(amazonTestClient.doesObjectExist("bucket2", objectName));

	}

	@Test
	public void two_jobstorages_with_one_stored_object_one_storage_is_deleted_by_deleteall_other_still_exists() throws Exception {
		/* prepare */
		UUID jobUUID = UUID.randomUUID();
		UUID jobUUID2 = UUID.randomUUID();

		AwsS3JobStorage storage = storeTestData(jobUUID);
		storeTestData(jobUUID2);

		String objectName = "jobstorage/projectName/"+jobUUID+"/testC";
		String objectName2 = "jobstorage/projectName/"+jobUUID2+"/testC";

		assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName));
		assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName2));

		/* execute */
		storage.deleteAll();

		/* test */
		assertFalse(amazonTestClient.doesObjectExist("bucket2", objectName));
		assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName2)); // still exists

	}

	private AwsS3JobStorage storeTestData(UUID jobUUID) throws IOException, FileNotFoundException {
		AwsS3JobStorage storage = new AwsS3JobStorage(amazonTestClient, "bucket2", "projectName", jobUUID);

		Path tmpFile = Files.createTempFile("storage_test", ".txt");

		/* execute */
		InputStream inputStream = new FileInputStream(tmpFile.toFile());
		InputStream inputStreamSpy = Mockito.spy(inputStream);
		storage.store("testC", inputStreamSpy);
		return storage;
	}

}
