// SPDX-License-Identifier: MIT
import * as assert from 'assert';
import * as path from 'path';
import * as fs from 'fs';
import { Severity, ScanType, FalsePositiveProjectConfiguration } from 'sechub-openapi-ts-client';
import { getFalsePositivesByIDForJobReport, loadFromFile } from '../../utils/sechubUtils';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';

suite('Extension Test Suite', () => {
	vscode.window.showInformationMessage('Start all tests.');

	test('SecHub test report file can be loaded and contains job uuid', () => {
		/* execute */
		const model = loadFromFile(resolveFileLocation('../resources/test_sechub_report-1.json'));

		/* test */
		assert.strictEqual('061234c8-40aa-4dcf-81f8-7bb8f723b780', model.jobUUID);
	});

	test('SecHub test report file can be loaded and contains 277 findings', () => {
		/* execute */
		const model = loadFromFile(resolveFileLocation('../resources/test_sechub_report-1.json'));

		/* test */
		assert(model.result);
		const findings = model.result.findings;

		assert(findings);
		assert.strictEqual(277, findings.length);
	});

	test('SecHub test report file can be loaded and contains medium finding with id3', () => {
		/* execute */
		const model = loadFromFile(resolveFileLocation('../resources/test_sechub_report-1.json'));

		/* test */
		assert(model.result);
		const findings = model.result.findings;
		assert(findings);
		const firstFinding = findings[0];
		assert.strictEqual(3, firstFinding.id);
		assert.strictEqual(Severity.Medium, firstFinding.severity);

		const codeCallstack1 = firstFinding.code;
		assert.strictEqual(82, codeCallstack1?.line);
		assert.strictEqual(65, codeCallstack1?.column);
		assert.strictEqual('input', codeCallstack1?.relevantPart);

		const codeCallstack2 = codeCallstack1?.calls;
		assert.strictEqual('whiteList', codeCallstack2?.relevantPart);
	});

	test('SecHub load test report originating from GoSec scan', () => {
		/* execute */
		const model = loadFromFile(resolveFileLocation('../resources/test_sechub_report_gosec.json'));

		/* test */
		assert(model.result);
		const findings = model.result.findings;
		assert(findings);
		const secondFinding = findings[1];
		assert.strictEqual(2, secondFinding.id);
		assert.strictEqual(Severity.High, secondFinding.severity);
		assert.strictEqual(ScanType.CodeScan, secondFinding.type);

		const codeCallstack1 = secondFinding.code;
		assert.strictEqual('vulnerable-go/source/app/app.go', codeCallstack1?.location);
		assert.strictEqual(90, codeCallstack1?.line);
		assert.strictEqual(13, codeCallstack1?.column);
		assert.strictEqual('hash := md5.Sum([]byte(password)) // CWE-327', codeCallstack1?.source);

		assert.strictEqual(undefined, codeCallstack1?.relevantPart);

		const codeCallstack2 = codeCallstack1?.calls;
		assert.strictEqual(undefined, codeCallstack2);
	});

	test('SecHub load test report originating from Gitleaks (Secret Scan) scan', () => {
		/* execute */
		const model = loadFromFile(resolveFileLocation('../resources/test_sechub_report_gitleaks.json'));

		/* test */
		assert(model.result);
		const findings = model.result.findings;
		assert(findings);
		const firstFinding = findings[0];
		assert.strictEqual(1, firstFinding.id);
		assert.strictEqual(Severity.Medium, firstFinding.severity);
		assert.strictEqual(ScanType.SecretScan, firstFinding.type);
		assert.strictEqual(
			'generic-api-key has detected secret for file vulnerable-go/source/app/app.go.',
			firstFinding.description,
		);

		const codeCallstack1 = firstFinding.code;
		assert.strictEqual('vulnerable-go/source/app/app.go', codeCallstack1?.location);
		assert.strictEqual(76, codeCallstack1?.line);
		assert.strictEqual(11, codeCallstack1?.column);
		assert.strictEqual('21232f297a57a5a743894a0e4a801fc3', codeCallstack1?.source);
	});

	test('SecHub load test report of scan error', () => {
		/* execute */
		const model = loadFromFile(resolveFileLocation('../resources/test_sechub_report_error.json'));

		/* test */
		assert(model.result);
		assert(model.result.findings);
		assert.strictEqual(0, model.result.findings.length);
	});

	test('SecHub load test report callstack, but no column, source or relevant part', () => {
		/* execute */
		const model = loadFromFile(resolveFileLocation('../resources/test_sechub_report-3.json'));

		/* test */
		assert(model.result);
		const findings = model.result.findings;

		assert(findings);
		assert.strictEqual(2, findings.length);
		const firstFinding = findings[0];
		assert.strictEqual(1, firstFinding.id);
		assert.strictEqual(Severity.Critical, firstFinding.severity);
		assert.strictEqual(ScanType.CodeScan, firstFinding.type);

		const f1CodeCallstack1 = firstFinding.code;
		assert.strictEqual(82, f1CodeCallstack1?.line);
		assert.strictEqual(0, f1CodeCallstack1?.column);
		assert.strictEqual(undefined, f1CodeCallstack1?.source);
		assert.strictEqual(undefined, f1CodeCallstack1?.relevantPart);

		const f1CodeCallstack2 = f1CodeCallstack1?.calls;
		assert.strictEqual(36, f1CodeCallstack2?.line);
		assert.strictEqual(0, f1CodeCallstack2?.column);
		assert.strictEqual(undefined, f1CodeCallstack2?.source);
		assert.strictEqual(undefined, f1CodeCallstack2?.relevantPart);

		const f1CodeCallstack3 = f1CodeCallstack2?.calls;
		assert.strictEqual(undefined, f1CodeCallstack3);

		const secondFinding = findings[1];
		assert.strictEqual(2, secondFinding.id);
		assert.strictEqual(Severity.Low, secondFinding.severity);
		assert.strictEqual(ScanType.CodeScan, secondFinding.type);

		const f2CodeCallstack1 = secondFinding.code;
		assert.strictEqual(12, f2CodeCallstack1?.line);
		assert.strictEqual(0, f2CodeCallstack1?.column);
		assert.strictEqual(undefined, f2CodeCallstack1?.source);
		assert.strictEqual(undefined, f2CodeCallstack1?.relevantPart);

		const f2CodeCallstack2 = f2CodeCallstack1?.calls;
		assert.strictEqual(undefined, f2CodeCallstack2);
	});

	test('Get false positives for job Returns list of False positives', () => {
		/* prepare */
		// read fp config from file
		const location = resolveFileLocation('../resources/test_sechub_fp_configuration.json');
		const fpConfig = readFPConfigFromFile(location);
		const jobUUID = '24cfa2a4-4a30-4947-92a2-1ea62ce7d88c';

		/* execute */
		const ids: number[] = getFalsePositivesByIDForJobReport(fpConfig, jobUUID);

		/* test */
		assert.strictEqual(3, ids.length);
		assert.strictEqual(1, ids[0]);
		assert.strictEqual(2, ids[1]);
		assert.strictEqual(47, ids[2]);
	});

	test('Get false positives for job Returns empty list if no false positives found', () => {
		/* prepare */
		// read fp config from file
		const location = resolveFileLocation('../resources/test_sechub_fp_configuration.json');
		const fpConfig = readFPConfigFromFile(location);
		const jobUUID = 'non-existing-job-uuid';

		/* execute */
		const ids: number[] = getFalsePositivesByIDForJobReport(fpConfig, jobUUID);

		/* test */
		assert.strictEqual(0, ids.length);
	});
});

function resolveFileLocation(testfile: string): string {
	const testReportLocation = path.dirname(__filename) + '/../../../src/test/suite/' + testfile;
	return testReportLocation;
}

function readFPConfigFromFile(location: string): FalsePositiveProjectConfiguration {
	const rawConfig = fs.readFileSync(location, 'utf8');
	return JSON.parse(rawConfig) as FalsePositiveProjectConfiguration;
}
