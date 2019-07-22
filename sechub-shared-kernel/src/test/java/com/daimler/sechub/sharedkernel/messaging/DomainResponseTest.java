// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DomainResponseTest {
	
	
	private MessageDataProvider<String> mockedProvider;
	private MessageDataKey<String> mockedDataKey;

	@SuppressWarnings("unchecked")
	@Before
	public void before() throws Exception {
		mockedProvider = Mockito.mock(MessageDataProvider.class);
		mockedDataKey=Mockito.mock(MessageDataKey.class);
		when(mockedDataKey.getId()).thenReturn("id");
		when(mockedDataKey.getProvider()).thenReturn(mockedProvider);
	}

	@Test
	public void response_with_an_exection_is_marked_as_failed() {
		assertTrue(new DomainMessageSynchronousResult(null, new RuntimeException()).hasFailed());
	}

	@Test
	public void response_without_an_exection_is_NOT_marked_as_failed() {
		assertFalse(new DomainMessageSynchronousResult(null, null).hasFailed());
	}

	@Test
	public void response_without_a_parameter_map_returns_value_from_provider() {
		when(mockedProvider.get(any())).thenReturn("fromProviderAsString");
		assertEquals("fromProviderAsString", new DomainMessageSynchronousResult(null, null).get(mockedDataKey));
	}
	
	@Test
	public void response_without_a_parameter_map_returns_value_from_provider_even_null() {
		when(mockedProvider.get(any())).thenReturn(null);
		assertEquals(null, new DomainMessageSynchronousResult(null, null).get(mockedDataKey));
	}

	@Test
	public void response_contains_data_transfered_from_provider_to_string_and_back_again() {
		when(mockedProvider.getString("v1")).thenReturn("xv1");
		when(mockedProvider.get("xv1")).thenReturn("version-from-provider1");
		when(mockedProvider.getString("v2")).thenReturn("xv2");
		when(mockedProvider.get("xv2")).thenReturn("version-from-provider2");
		/* prepare */

		/* execute + test */
		DomainMessageSynchronousResult domainResponse = new DomainMessageSynchronousResult(null);
		domainResponse.set(mockedDataKey, "v1");
		assertEquals("version-from-provider1", domainResponse.get(mockedDataKey));

		domainResponse.set(mockedDataKey, "v2");
		assertEquals("version-from-provider2", domainResponse.get(mockedDataKey));

		domainResponse.set(mockedDataKey, null);
		assertEquals(null, domainResponse.get(mockedDataKey));
	}

	@Test
	public void a_failed_response_with_added_keys_contains_still_data_from_provider() {
		/* prepare */
		when(mockedProvider.get("xv1")).thenReturn("v1.0");
		when(mockedProvider.getString("v1")).thenReturn("xv1");

		DomainMessageSynchronousResult response = new DomainMessageSynchronousResult(null, new RuntimeException());
		response.set(mockedDataKey, "v1");

		/* execute + test */
		assertEquals("v1.0", response.get(mockedDataKey));
		assertTrue(response.hasFailed());
		;
	}

}
