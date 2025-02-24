// SPDX-License-Identifier: MIT
import * as assert from 'assert';

import * as secHubModel from '../../model/sechubModel';
import { FindingNodeLinkBuilder } from '../../model/findingNodeLinkBuilder';

suite('FindingNodeLinkBuilder Test Suite', () => {

	var builderToTest: FindingNodeLinkBuilder;

	test('cwe 4711 results in link to https://cwe.mitre.org/data/definitions/4711.html', () => {
		/* prepare */
		builderToTest = new FindingNodeLinkBuilder();

		/* execute */
		var uri = builderToTest.buildCWELink({id: 1, name: "findingX", description: "description for findingX", severity: secHubModel.Severity.high,cweId: 4711, type: secHubModel.ScanType.codeScan});

		/* test */
		if (!uri){
			assert.fail("no uri defined!");
		}
		assert.strictEqual("https://cwe.mitre.org/data/definitions/4711.html", uri.toString());

	});

	test('cwe and description not defined results in undefined', () => {
		/* prepare */
		builderToTest = new FindingNodeLinkBuilder();

		/* execute */
		var uri = builderToTest.buildCWELink({id: 1, name: "findingX", severity: secHubModel.Severity.high, type: secHubModel.ScanType.codeScan});

		/* test */
		if (uri){
			assert.fail("uri defined!");
		}
	});

	test('cwe not defined results in undefined', () => {
		/* prepare */
		builderToTest = new FindingNodeLinkBuilder();

		/* execute */
		var uri = builderToTest.buildCWELink({id: 1, name: "findingX", description: "description for findingX", severity: secHubModel.Severity.high, type: secHubModel.ScanType.codeScan});

		/* test */
		if (uri){
			assert.fail("uri defined!");
		}
	});

	test('findingNode not defined results in undefined', () => {
		/* prepare */
		builderToTest = new FindingNodeLinkBuilder();

		/* execute */
		var uri = builderToTest.buildCWELink(undefined);

		/* test */
		if (uri){
			assert.fail("uri defined!");
		}
	});
});
