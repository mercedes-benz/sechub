// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.daimler.sechub.test.SechubTestComponent;

@SechubTestComponent
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
			assertThat(value.description, is(equalTo(description)));
			return this;
		}

		public AssertSecHubFinding hasMethod(String method) {
			assertThat(value.method, is(equalTo(method)));
			return this;
		}

		public AssertSecHubFinding hasName(String name) {
			assertThat(value.name, is(equalTo(name)));
			return this;
		}

		public AssertSecHubFinding hasParameters(String parameters) {
			assertThat(value.parameters, is(equalTo(parameters)));
			return this;
		}

		public AssertSecHubFinding hasPath(String path) {
			assertThat(value.path, is(equalTo(path)));
			return this;
		}

		public AssertSecHubFinding hasParameterName(String pname) {
			assertThat(value.parameterName, is(equalTo(pname)));
			return this;
		}

		public AssertSecHubFinding hasQuery(String query) {
			assertThat(value.query, is(equalTo(query)));
			return this;
		}

		public AssertSecHubFinding hasNoRefs() {
			assertNotNull(value.references);
			assertEquals(0, value.references.size());
			return this;
		}

		public AssertSecHubFinding hasRefs(String... refs) {
			assertNotNull(value.references);
			assertThat(value.references.toArray(new String[value.references.size()]), is(arrayContaining(refs)));
			return this;
		}

		public AssertSecHubFinding hasRequest(String request) {
			assertThat(value.request, is(equalTo(request)));
			return this;
		}

		public AssertSecHubFinding hasResolution(String resolution) {
			assertThat(value.resolution, is(equalTo(resolution)));
			return this;
		}

		public AssertSecHubFinding hasResponse(String response) {
			assertThat(value.response, is(equalTo(response)));
			return this;
		}

		public AssertSecHubFinding hasService(String service) {
			assertThat(value.service, is(equalTo(service)));
			return this;
		}

		public AssertSecHubFinding hasSeverity(Severity severity) {
			assertThat(value.severity, is(equalTo(severity)));
			return this;
		}

		public AssertSecHubFinding hasTarget(String target) {
			assertThat(value.target, is(equalTo(target)));
			return this;
		}

		public AssertSecHubFinding hasWebsite(String website) {
			assertThat(value.website, is(equalTo(website)));
			return this;
		}

		public AssertSecHubFinding hasCreator(String creator) {
			assertThat(value.createdBy, is(equalTo(creator)));
			return this;
		}

		public AssertSecHubFinding hasCreatorDate(Date creationTime) {
			assertThat(value.created, is(equalTo(creationTime)));
			return this;
		}

		public AssertSecHubFinding hasNoHostnames() {
			assertNotNull(value.hostnames);
			assertEquals(0, value.hostnames.size());
			return this;
		}

		public AssertSecHubFinding hasHostNames(String... hostnames) {
			assertNotNull("Hostnames are null!", value.hostnames);
			assertEquals("Amount of hostnames differs!", value.hostnames.size(), hostnames.length);

			List<String> hostnamesAsList = Arrays.asList(hostnames);
			if (!value.hostnames.containsAll(hostnamesAsList)) {
				fail("Hostname count same, but hostnames not as expected!\nExpected: " + hostnamesAsList + "\nResulted:"
						+ hostnames);
			}

			return this;
		}

	}

	public AssertSecHubFinding hasFindingWithId(int id) {
		List<SecHubFinding> vulnerabilities = result.getFindings();
		for (SecHubFinding v : vulnerabilities) {
			if (v.id == id) {
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
