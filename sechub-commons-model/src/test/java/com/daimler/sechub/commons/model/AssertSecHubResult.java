// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AssertSecHubResult {

	public static AssertSecHubResult assertSecHubResult(SecHubResult result) {
		if (result == null) {
			fail("Given sechub result is null!");
		}
		return new AssertSecHubResult(result);
	}

	private SecHubResult result;

	private AssertSecHubResult(SecHubResult result) {
		this.result = result;
	}

	public class AssertSecHubFinding {

		private SecHubFinding value;

		public AssertSecHubFinding(SecHubFinding v) {
			this.value = v;
		}

		public AssertSecHubResult andResult() {
			return AssertSecHubResult.this;
		}

		public AssertSecHubFinding hasDescription(String description) {
			assertThat(value.getDescription(), is(equalTo(description)));
			return this;
		}

		public AssertSecHubFinding hasMethod(String method) {
			assertThat(value.getMethod(), is(equalTo(method)));
			return this;
		}

		public AssertSecHubFinding hasName(String name) {
			assertThat(value.getName(), is(equalTo(name)));
			return this;
		}

		public AssertSecHubFinding hasParameters(String parameters) {
			assertThat(value.getParameterName(), is(equalTo(parameters)));
			return this;
		}

		public AssertSecHubFinding hasPath(String path) {
			assertThat(value.getPath(), is(equalTo(path)));
			return this;
		}

		public AssertSecHubFinding hasParameterName(String pname) {
			assertThat(value.getParameterName(), is(equalTo(pname)));
			return this;
		}

		public AssertSecHubFinding hasQuery(String query) {
			assertThat(value.getQuery(), is(equalTo(query)));
			return this;
		}

		public AssertSecHubFinding hasNoRefs() {
			assertNotNull(value.getReferences());
			assertEquals(0, value.getReferences().size());
			return this;
		}

		public AssertSecHubFinding hasRefs(String... refs) {
			assertNotNull(value.getReferences());
			assertThat(value.getReferences().toArray(new String[value.getReferences().size()]), is(arrayContaining(refs)));
			return this;
		}

		public AssertSecHubFinding hasRequest(String request) {
			assertThat(value.getRequest(), is(equalTo(request)));
			return this;
		}

		public AssertSecHubFinding hasResolution(String resolution) {
			assertThat(value.getResolution(), is(equalTo(resolution)));
			return this;
		}

		public AssertSecHubFinding hasResponse(String response) {
			assertThat(value.getResponse(), is(equalTo(response)));
			return this;
		}

		public AssertSecHubFinding hasService(String service) {
			assertThat(value.getService(), is(equalTo(service)));
			return this;
		}

		public AssertSecHubFinding hasSeverity(Severity severity) {
			assertThat(value.getSeverity(), is(equalTo(severity)));
			return this;
		}

		public AssertSecHubFinding hasTarget(String target) {
			assertThat(value.getTarget(), is(equalTo(target)));
			return this;
		}

		public AssertSecHubFinding hasWebsite(String website) {
			assertThat(value.getWebsite(), is(equalTo(website)));
			return this;
		}

		public AssertSecHubFinding hasCreator(String creator) {
			assertThat(value.getCreatedBy(), is(equalTo(creator)));
			return this;
		}

		public AssertSecHubFinding hasCreatorDate(Date creationTime) {
			assertThat(value.getCreated(), is(equalTo(creationTime)));
			return this;
		}

		public AssertSecHubFinding hasNoHostnames() {
			assertNotNull(value.getHostnames());
			assertEquals(0, value.getHostnames().size());
			return this;
		}

		public AssertSecHubFinding hasHostNames(String... hostnames) {
			assertNotNull("Hostnames are null!", value.getHostnames());
			assertEquals("Amount of hostnames differs!", value.getHostnames().size(), hostnames.length);

			List<String> hostnamesAsList = Arrays.asList(hostnames);
			if (!value.getHostnames().containsAll(hostnamesAsList)) {
				fail("Hostname count same, but hostnames not as expected!\nExpected: " + hostnamesAsList + "\nResulted:"
						+ value.getHostnames());
			}

			return this;
		}

	}

	public AssertSecHubFinding hasFindingWithId(int id) {
		List<SecHubFinding> vulnerabilities = result.getFindings();
		for (SecHubFinding v : vulnerabilities) {
			if (v.getId() == id) {
				return new AssertSecHubFinding(v);
			}
		}
		fail("No Finding with ID:" + id + " found!");
		return null;
	}

	public AssertSecHubResult hasFindings(int count) {
		List<SecHubFinding> vulnerabilities = result.getFindings();
		if (vulnerabilities == null) {
			if (count == 0) {
				return this;
			}
			fail("No findings at all found!");
		}

		assertEquals("Not expected amount of findings found:", count, vulnerabilities.size());

		return this;
	}

}
