package com.mercedesbenz.sechub.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveEntry;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveJobData;

public class FalsePositiveEntriesToJobFindingMapTransformerTest {

	private static final String TEST_AUTHOR = "tester";
	private static final String TEST_CREATED = "2025-07-31 09:06:00";
	private FalsePositiveEntriesToJobFindingMapTransformer transformerToTest;

	@Before
	public void before() {
		transformerToTest = new FalsePositiveEntriesToJobFindingMapTransformer();
	}

	@Test
	public void a_finding_for_a_job_is_returned_in_map_with_finding_id_as_key_and_has_job_data_author_and_created() {
		/* prepare */
		UUID jobUUID = UUID.randomUUID();
		List<FalsePositiveEntry> list = new ArrayList<FalsePositiveEntry>();
		list.add(createEntry(jobUUID, 1));

		/* execute */
		Map<Integer, FindingNodeFalsePositiveInfo> result = transformerToTest.transform(list, jobUUID);

		/* test */
		FindingNodeFalsePositiveInfo found = result.get(1);
		assertNotNull(found);
		assertEquals(Integer.valueOf(1), found.getFindingId());
		assertEquals(jobUUID, found.getJobUUID());
		assertEquals(TEST_AUTHOR,found.getAuthor());
		assertEquals(TEST_CREATED,found.getCreated());

	}

	@Test
	public void multiple_findings_for_a_job_are_returned_in_map_with_finding_id_as_key_and_has_job_data() {
		/* prepare */
		UUID jobUUID = UUID.randomUUID();
		UUID jobUUID2 = UUID.randomUUID();
		List<FalsePositiveEntry> list = new ArrayList<FalsePositiveEntry>();
		list.add(createEntry(jobUUID, 1));
		list.add(createEntry(jobUUID, 2));
		list.add(createEntry(jobUUID2, 3));

		/* execute */
		Map<Integer, FindingNodeFalsePositiveInfo> result = transformerToTest.transform(list, jobUUID);

		/* test */
		FindingNodeFalsePositiveInfo found1 = result.get(1);
		assertNotNull(found1);
		assertEquals(Integer.valueOf(1), found1.getFindingId());
		assertEquals(jobUUID, found1.getJobUUID());

		FindingNodeFalsePositiveInfo found2 = result.get(2);
		assertNotNull(found2);
		assertEquals(Integer.valueOf(2), found2.getFindingId());
		assertEquals(jobUUID, found2.getJobUUID());
		
		FindingNodeFalsePositiveInfo found3 = result.get(3);
		assertNull(found3);

	}


	@Test
	public void a_finding_for_a_job_is_NOT_returned_in_map_for_other_job() {
		/* prepare */
		UUID jobUUID = UUID.randomUUID();
		List<FalsePositiveEntry> list = new ArrayList<FalsePositiveEntry>();
		list.add(createEntry(jobUUID, 1));

		UUID otherJobUUID = UUID.randomUUID();

		/* execute */
		Map<Integer, FindingNodeFalsePositiveInfo> result = transformerToTest.transform(list, otherJobUUID);

		/* test */
		FindingNodeFalsePositiveInfo found = result.get(1);
		assertNull(found);

	}
	
	private FalsePositiveEntry createEntry(UUID jobUUID, int findingId) {
		FalsePositiveEntry entry1 = new FalsePositiveEntry();
		FalsePositiveJobData jobData1 = new FalsePositiveJobData();
		jobData1.setFindingId(findingId);
		jobData1.setJobUUID(jobUUID);
		entry1.setJobData(jobData1);
		entry1.setAuthor(TEST_AUTHOR);
		entry1.setCreated(TEST_CREATED);
		return entry1;
	}

}
