// SPDX-License-Identifier: MIT
import * as path from 'path';

import { runTests } from '@vscode/test-electron';

async function main() {
	try {
		// The folder containing the Extension Manifest package.json
		// Passed to `--extensionDevelopmentPath`
		const extensionDevelopmentPath = path.resolve(__dirname, '../../');

		// The path to test runner
		// Passed to --extensionTestsPath
		const extensionTestsPath = path.resolve(__dirname, './suite/index');

		// Download VS Code, unzip it and run the integration test
		// Test with latest version of VS Code
		await runTests({ extensionDevelopmentPath, extensionTestsPath });

		// Test with a specific version of VS Code
		// The common denominator of VS Code and Eclipse Theia
		//await runTests({ version: '1.74.0', extensionDevelopmentPath, extensionTestsPath });
	} catch (err) {
		console.error('Failed to run tests');
		process.exit(1);
	}
}

main();
