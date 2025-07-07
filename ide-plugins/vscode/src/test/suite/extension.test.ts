// SPDX-License-Identifier: MIT
import * as assert from 'assert';
import * as path from 'path';
import { SecHubReport, Severity, ScanType } from 'sechub-openapi-typescript';
import { loadFromFile } from '../../utils/sechubUtils';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';
import * as myExtension from '../../extension';

suite('Extension Test Suite', () => {
	vscode.window.showInformationMessage('Start all tests.');

	test('Smoke test', () => {
		assert.strictEqual("works", "works");
	});

	test('SecHub test report file can be loaded and contains job uuid', () => {
		/* execute */
		let model = loadFromFile(resolveFileLocation("test_sechub_report-1.json"));

		/* test */
		assert.strictEqual("061234c8-40aa-4dcf-81f8-7bb8f723b780", model.jobUUID);

	});

	test('SecHub test report file can be loaded and contains 277 findings', () => {
		/* execute */
		let model = loadFromFile(resolveFileLocation("test_sechub_report-1.json"));

		/* test */
		assert(model.result);
		let findings = model.result.findings;

		assert(findings);
		assert.strictEqual(277, findings.length);

	});

	test('SecHub test report file can be loaded and contains medium finding with id3', () => {
		/* execute */
		let model = loadFromFile(resolveFileLocation("test_sechub_report-1.json"));

		/* test */
		assert(model.result);
		let findings = model.result.findings;
		assert(findings);
		let firstFinding = findings[0];
		assert.strictEqual(3, firstFinding.id);
		assert.strictEqual(Severity.Medium, firstFinding.severity);

		let codeCallstack1 = firstFinding.code;
		assert.strictEqual(82, codeCallstack1?.line);
		assert.strictEqual(65, codeCallstack1?.column);
		assert.strictEqual("input", codeCallstack1?.relevantPart);

		let codeCallstack2 = codeCallstack1?.calls;
		assert.strictEqual("whiteList", codeCallstack2?.relevantPart);

	});

	test('SecHub load test report originating from GoSec scan', () => {
		/* execute */
		let model = loadFromFile(resolveFileLocation("test_sechub_report_gosec.json"));

		/* test */
		assert(model.result);
		let findings = model.result.findings;
		assert(findings);
		let secondFinding = findings[1];
		assert.strictEqual(2, secondFinding.id);
		assert.strictEqual(Severity.High, secondFinding.severity);
		assert.strictEqual(ScanType.CodeScan, secondFinding.type);

		let codeCallstack1 = secondFinding.code;
		assert.strictEqual("vulnerable-go/source/app/app.go", codeCallstack1?.location);
		assert.strictEqual(90, codeCallstack1?.line);
		assert.strictEqual(13, codeCallstack1?.column);
		assert.strictEqual("hash := md5.Sum([]byte(password)) // CWE-327", codeCallstack1?.source);
		
		assert.strictEqual(undefined, codeCallstack1?.relevantPart);

		let codeCallstack2 = codeCallstack1?.calls;
		assert.strictEqual(undefined, codeCallstack2);
	});

	test('SecHub load test report originating from Gitleaks (Secret Scan) scan', () => {
		/* execute */
		let model = loadFromFile(resolveFileLocation("test_sechub_report_gitleaks.json"));

		/* test */
		assert(model.result);
		let findings = model.result.findings;
		assert(findings);
		let firstFinding = findings[0];
		assert.strictEqual(1, firstFinding.id);
		assert.strictEqual(Severity.Medium, firstFinding.severity);
		assert.strictEqual(ScanType.SecretScan, firstFinding.type);
		assert.strictEqual("generic-api-key has detected secret for file vulnerable-go/source/app/app.go.", firstFinding.description);

		let codeCallstack1 = firstFinding.code;
		assert.strictEqual("vulnerable-go/source/app/app.go", codeCallstack1?.location);
		assert.strictEqual(76, codeCallstack1?.line);
		assert.strictEqual(11, codeCallstack1?.column);
		assert.strictEqual("21232f297a57a5a743894a0e4a801fc3", codeCallstack1?.source);
	});

	test('SecHub load test report of scan error', () => {
		/* execute */
		let model = loadFromFile(resolveFileLocation("test_sechub_report_error.json"));

		/* test */
		assert(model.result);
		assert(model.result.findings);
		assert.strictEqual(0, model.result.findings.length);
	});

	test('SecHub load test report callstack, but no column, source or relevant part', () => {
		/* execute */
		let model = loadFromFile(resolveFileLocation("test_sechub_report-3.json"));

		/* test */
		assert(model.result);
		let findings = model.result.findings;

		assert(findings);
		assert.strictEqual(2, findings.length);
		let firstFinding = findings[0];
		assert.strictEqual(1, firstFinding.id);
		assert.strictEqual(Severity.Critical, firstFinding.severity);
		assert.strictEqual(ScanType.CodeScan, firstFinding.type);

		let f1CodeCallstack1 = firstFinding.code;
		assert.strictEqual(82, f1CodeCallstack1?.line);
		assert.strictEqual(0, f1CodeCallstack1?.column);
		assert.strictEqual(undefined, f1CodeCallstack1?.source);
		assert.strictEqual(undefined, f1CodeCallstack1?.relevantPart);

		let f1CodeCallstack2 = f1CodeCallstack1?.calls;
		assert.strictEqual(36, f1CodeCallstack2?.line);
		assert.strictEqual(0, f1CodeCallstack2?.column);
		assert.strictEqual(undefined, f1CodeCallstack2?.source);
		assert.strictEqual(undefined, f1CodeCallstack2?.relevantPart);

		let f1CodeCallstack3 = f1CodeCallstack2?.calls;
		assert.strictEqual(undefined, f1CodeCallstack3);

		let secondFinding = findings[1];
		assert.strictEqual(2, secondFinding.id);
		assert.strictEqual(Severity.Low, secondFinding.severity);
		assert.strictEqual(ScanType.CodeScan, secondFinding.type);

		let f2CodeCallstack1 = secondFinding.code;
		assert.strictEqual(12, f2CodeCallstack1?.line);
		assert.strictEqual(0, f2CodeCallstack1?.column);
		assert.strictEqual(undefined, f2CodeCallstack1?.source);
		assert.strictEqual(undefined, f2CodeCallstack1?.relevantPart);

		let f2CodeCallstack2 = f2CodeCallstack1?.calls;
		assert.strictEqual(undefined, f2CodeCallstack2);
	});
});

function resolveFileLocation(testfile: string): string {
	let testReportLocation = path.dirname(__filename) + "/../../../src/test/suite/" + testfile;
	return testReportLocation;
}
