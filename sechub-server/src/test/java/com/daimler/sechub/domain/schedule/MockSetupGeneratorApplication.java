// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.adapter.checkmarx.MockedCheckmarxAdapter;
import com.daimler.sechub.adapter.mock.MockedAdapterSetup;
import com.daimler.sechub.adapter.mock.MockedAdapterSetupCombination;
import com.daimler.sechub.adapter.mock.MockedAdapterSetupEntry;
import com.daimler.sechub.adapter.mock.MockedAdapterSetupService;
import com.daimler.sechub.adapter.nessus.MockedNessusAdapter;
import com.daimler.sechub.adapter.netsparker.MockedNetsparkerAdapter;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;
import com.daimler.sechub.test.ExampleConstants;

public class MockSetupGeneratorApplication {

	private static final String LONGRUNNING_BUT_GREEN_DEMO_URI_HOST = "longrunning.but.green.demo."+ExampleConstants.URI_TARGET_SERVER;
	private static final String HTTPS_LONGRUNNING_BUT_GREEN_DEMO_URI_HOST = "https://"+LONGRUNNING_BUT_GREEN_DEMO_URI_HOST;

	private static final int LONG_RUNNING_TIME_MS = 10000;
	private static final Logger LOG = LoggerFactory.getLogger(MockSetupGeneratorApplication.class);

	public static void main(String[] args) throws Exception {

		MockedAdapterSetup setup = new MockedAdapterSetup();
		List<MockedAdapterSetupEntry> entries = setup.getEntries();
		entries.add(createNetsparkerEntry());
		entries.add(createCheckmarxEntry());
		entries.add(createNessusEntry());

		/* execute */
		String json = JSONAdapterSupport.FOR_UNKNOWN_ADAPTER.toJSON(setup);

		/* additionally write to real disk so having setup */
		File file = new File(MockedAdapterSetupService.DEFAULT_FILE_PATH);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(json);
		}
		LOG.info("wrote mock adapter setup \nto {}, \ncontent was\n{}", file, json);

	}

	private static MockedAdapterSetupEntry createCheckmarxEntry() {
		MockedAdapterSetupEntry entry = new MockedAdapterSetupEntry();
		entry.setAdapterId(new MockedCheckmarxAdapter().getAdapterId());
		addCombination(entry, LONGRUNNING_BUT_GREEN_DEMO_URI_HOST, "checkmarx-mockdata-empty.xml",LONG_RUNNING_TIME_MS);
		addCombination(entry, "../sechub-doc/src/main/java", "checkmarx-mockdata-multiple.xml");
		addCombination(entry, MockedAdapterSetupCombination.ANY_OTHER_TARGET, "checkmarx-mockdata-empty.xml");

		return entry;
	}

	private static MockedAdapterSetupEntry createNetsparkerEntry() {
		MockedAdapterSetupEntry entry = new MockedAdapterSetupEntry();
		entry.setAdapterId(new MockedNetsparkerAdapter().getAdapterId());
		addCombination(entry, HTTPS_LONGRUNNING_BUT_GREEN_DEMO_URI_HOST, "netsparker-mockdata-green.xml",LONG_RUNNING_TIME_MS);
		addCombination(entry, "https://vulnerable.demo.example.org",
				"netsparker-mockdata-one-important-vulnerability.xml");
		addCombination(entry, "https://safe.demo.example.org", "netsparker-mockdata-green.xml");
		addCombination(entry, MockedAdapterSetupCombination.ANY_OTHER_TARGET, "netsparker-mockdata-green.xml");

		return entry;
	}

	private static MockedAdapterSetupEntry createNessusEntry() {
		MockedAdapterSetupEntry entry = new MockedAdapterSetupEntry();
		entry.setAdapterId(new MockedNessusAdapter().getAdapterId());
		addCombination(entry, HTTPS_LONGRUNNING_BUT_GREEN_DEMO_URI_HOST, "nessus-mockdata-green.xml",LONG_RUNNING_TIME_MS);
		addCombination(entry, "https://vulnerable.demo.example.org", "nessus-mockdata-different-serverities.xml");
		addCombination(entry, "https://safe.demo.example.org", "nessus-mockdata-green.xml");
		addCombination(entry, MockedAdapterSetupCombination.ANY_OTHER_TARGET, "nessus-mockdata-green.xml");

		return entry;
	}

	private static void addCombination(MockedAdapterSetupEntry entry, String targetURL, String filePath) {
		addCombination(entry, targetURL, filePath, -1);
	}

	private static void addCombination(MockedAdapterSetupEntry entry, String targetURL, String filePath,
			long timeToElapseInMs) {
		MockedAdapterSetupCombination combi1 = new MockedAdapterSetupCombination();
		combi1.setFilePath(createEnsuredFile(filePath));
		combi1.setTarget(targetURL);
		combi1.setTimeToElapseInMilliseconds(timeToElapseInMs);

		entry.getCombinations().add(combi1);
	}

	private static String createEnsuredFile(String string) {
		String path = "./../sechub-other/mockdata/" + string;
		File file = new File(path);
		if (file.exists()) {
			return path;
		}
		throw new IllegalStateException("Mocked filepath:" + file + " does not exist!");
	}

}
