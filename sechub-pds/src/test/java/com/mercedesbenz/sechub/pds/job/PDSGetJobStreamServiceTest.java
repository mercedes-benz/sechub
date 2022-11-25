package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSGetJobStreamService.TRUNCATED_STREAM_SIZE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PDSGetJobStreamServiceTest {

    private PDSGetJobStreamService serviceToTest;
    private UUID jobUUID;
    private PDSJobRepository repository;
    private PDSJob job;

    @BeforeEach
    public void before() {
        repository = mock(PDSJobRepository.class);

        jobUUID = UUID.randomUUID();
        job = new PDSJob();
        job.uUID = jobUUID;
        job.created = LocalDateTime.of(2020, 06, 23, 16, 35, 01);
        job.owner = "theOwner";

        when(repository.findById(jobUUID)).thenReturn(Optional.of(job));

        serviceToTest = new PDSGetJobStreamService();
        serviceToTest.repository = repository;
    }

    @Test
    public void truncate_empty_stream() {
        /* prepare */
        String emptyStream = "";

        /* execute */
        String truncateResult = serviceToTest.truncateStream(emptyStream);

        /* test */
        assertEquals(0, truncateResult.length());
        assertEquals("", truncateResult);
    }

    @Test
    public void truncate_stream_smaller_then_truncated_stream_size() {
        /* prepare */
        int smallStreamSize = TRUNCATED_STREAM_SIZE - 10;
        String smallStream = "x".repeat(smallStreamSize);

        /* execute */
        String truncateResult = serviceToTest.truncateStream(smallStream);

        /* test */
        assertEquals(smallStreamSize, truncateResult.length());
        assertEquals(smallStream, truncateResult);
    }

    @Test
    public void truncate_stream_larger_then_truncated_stream_size() {
        /* prepare */
        int largeStreamSize = TRUNCATED_STREAM_SIZE + 10;
        String largeStream = "x".repeat(largeStreamSize);

        /* execute */
        String truncateResult = serviceToTest.truncateStream(largeStream);

        /* test */
        assertEquals(TRUNCATED_STREAM_SIZE, truncateResult.length());
        assertEquals(largeStream.substring(largeStream.length() - TRUNCATED_STREAM_SIZE), truncateResult);
    }

    @Test
    public void truncate_stream_same_as_truncated_stream_size() {
        /* prepare */
        int sameStreamSize = TRUNCATED_STREAM_SIZE;
        String sameStream = "x".repeat(sameStreamSize);

        /* execute */
        String truncateResult = serviceToTest.truncateStream(sameStream);

        /* test */
        assertEquals(TRUNCATED_STREAM_SIZE, truncateResult.length());
        assertEquals(sameStream, truncateResult);
    }

    @Test
    public void get_truncated_error_stream() {
        /* prepare */
        job.errorStreamText = "This is an error stream";

        /* execute */
        String truncateResult = serviceToTest.getJobErrorStream(jobUUID);

        /* test */
        assertEquals("This is an error stream", truncateResult);
    }

    @Test
    public void get_truncated_output_stream() {
        /* prepare */
        job.errorStreamText = "This is an output stream";

        /* execute */
        String truncateResult = serviceToTest.getJobErrorStream(jobUUID);

        /* test */
        assertEquals("This is an output stream", truncateResult);
    }
}
