package com.daimler.sechub.domain.scan.config;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class NamePatternIdproviderTest {

	private NamePatternIdprovider providerToTest;

	@Before
	public void before() {
		providerToTest = new NamePatternIdprovider("provider.testid");
	}

	@Test
	public void providerId_is_returned_and_null_is_allowed_too() {
		assertEquals("testid",new NamePatternIdprovider("testid").getProviderId());
		assertEquals(null, new NamePatternIdprovider(null).getProviderId());
	}

	@Test
	public void when_entry_is_matching_id_is_returned() {
		/* prepare*/


		NamePatternToIdEntry entry1 = mock(NamePatternToIdEntry.class);
		when(entry1.isMatching("abc")).thenReturn(true);
		when(entry1.getId()).thenReturn("id1");

		providerToTest.add(entry1);

		/* execute + test*/
		assertEquals("id1", providerToTest.getIdForName("abc"));
	}

	@Test
	public void when_entry_is_not_matching_null_is_returned() {
		/* prepare*/


		NamePatternToIdEntry entry1 = mock(NamePatternToIdEntry.class);
		when(entry1.getId()).thenReturn("id1");

		providerToTest.add(entry1);

		/* execute + test*/
		assertEquals(null, providerToTest.getIdForName("abc"));
	}

	@Test
	public void no_entries_null_is_returned() {
		/* prepare*/


		/* execute + test*/
		assertEquals(null, providerToTest.getIdForName("abc"));
	}

	@Test
	public void null_entry_added_null_is_returned() {
		/* prepare*/

		providerToTest.add(null);

		/* execute + test*/
		assertEquals(null, providerToTest.getIdForName("abc"));
	}

	@Test
	public void when_two_entries_are_matching_the_first_one_is_returned() {
		/* prepare*/


		NamePatternToIdEntry entry1 = mock(NamePatternToIdEntry.class);
		when(entry1.isMatching("abc")).thenReturn(true);
		when(entry1.getId()).thenReturn("id1");

		NamePatternToIdEntry entry2 = mock(NamePatternToIdEntry.class);
		when(entry2.isMatching("abc")).thenReturn(true);
		when(entry2.getId()).thenReturn("id2");

		providerToTest.add(entry1);
		providerToTest.add(entry2);

		/* execute + test*/
		assertEquals("id1", providerToTest.getIdForName("abc"));
	}

	@Test
	public void when_two_entries_added_and_last_one_is_matching_the_last_id_is_returned() {
		/* prepare*/


		NamePatternToIdEntry entry1 = mock(NamePatternToIdEntry.class);
		when(entry1.isMatching("abc")).thenReturn(false);
		when(entry1.getId()).thenReturn("id1");

		NamePatternToIdEntry entry2 = mock(NamePatternToIdEntry.class);
		when(entry2.isMatching("abc")).thenReturn(true);
		when(entry2.getId()).thenReturn("id2");

		providerToTest.add(entry1);
		providerToTest.add(entry2);

		/* execute + test*/
		assertEquals("id2", providerToTest.getIdForName("abc"));
	}

}
