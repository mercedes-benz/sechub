// SPDX-License-Identifier: MIT
import * as assert from 'assert';

import { Severity, ScanType } from 'sechub-openapi-ts-client';
import { InfoViewItemMitreCWELinkBuilder } from '../../provider/items/infoViewItemMitreCWELinkBuilder';

suite('FindingNodeLinkBuilder Test Suite', () => {
	let builderToTest: InfoViewItemMitreCWELinkBuilder;

	test('cwe 4711 results in link to https://cwe.mitre.org/data/definitions/4711.html', () => {
		/* prepare */
		builderToTest = new InfoViewItemMitreCWELinkBuilder();

		/* execute */
		const uri = builderToTest.buildCWELink({
			id: 1,
			name: 'findingX',
			description: 'description for findingX',
			severity: Severity.High,
			cweId: 4711,
			type: ScanType.CodeScan,
		});

		/* test */
		if (!uri) {
			assert.fail('no uri defined!');
		}
		assert.strictEqual('https://cwe.mitre.org/data/definitions/4711.html', uri.toString());
	});

	test('cwe and description not defined results in undefined', () => {
		/* prepare */
		builderToTest = new InfoViewItemMitreCWELinkBuilder();

		/* execute */
		const uri = builderToTest.buildCWELink({
			id: 1,
			name: 'findingX',
			severity: Severity.High,
			type: ScanType.CodeScan,
		});

		/* test */
		if (uri) {
			assert.fail('uri defined!');
		}
	});

	test('cwe not defined results in undefined', () => {
		/* prepare */
		builderToTest = new InfoViewItemMitreCWELinkBuilder();

		/* execute */
		const uri = builderToTest.buildCWELink({
			id: 1,
			name: 'findingX',
			description: 'description for findingX',
			severity: Severity.High,
			type: ScanType.CodeScan,
		});

		/* test */
		if (uri) {
			assert.fail('uri defined!');
		}
	});

	test('findingNode not defined results in undefined', () => {
		/* prepare */
		builderToTest = new InfoViewItemMitreCWELinkBuilder();

		/* execute */
		const uri = builderToTest.buildCWELink(undefined);

		/* test */
		if (uri) {
			assert.fail('uri defined!');
		}
	});
});
