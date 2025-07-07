// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as sechubExtension from './../extension';
import { ScanType } from 'sechub-openapi-ts-client';
import { loadFromFile } from './../utils/sechubUtils';

export function hookImportAction(context: sechubExtension.SecHubContext) {
	let importReportFileCommandDisposable = vscode.commands.registerCommand('sechubReportView.importReportFile', () => {

		const options: vscode.OpenDialogOptions = {

			title: "Import SecHub report file",
			canSelectMany: false,
			openLabel: 'Open',
			filters: {
				// eslint-disable-next-line @typescript-eslint/naming-convention
				'SecHub report files': ['json'],
				// eslint-disable-next-line @typescript-eslint/naming-convention
				'All files': ['*']
			}
		};

		vscode.window.showOpenDialog(options).then(fileUri => {
			if (fileUri && fileUri[0]) {
				let filePath = fileUri[0].fsPath;

				vscode.window.showInformationMessage('Started SecHub report import...');

				let report = loadFromFile(filePath);
				let scanTypes: Array<ScanType> = report.metaData?.executed || [];

				if(scanTypes.length === 0){
					vscode.window.showErrorMessage("No scan was executed in this report.");
					return;
				}

				
				if (scanTypes.includes(ScanType.CodeScan) || (scanTypes.includes(ScanType.IacScan) || (scanTypes.includes(ScanType.SecretScan)))) {
					context.reportTreeProvider.update(report);
					context.report = report;
				} else {

					const scanTypesString = scanTypes.join(', ');
					vscode.window.showErrorMessage(`SecHub Plugin only support codeScan, iacScan and secretScan in IDE, but your scan was: ${scanTypesString}`);
				}
			}
		});
	});

	context.extensionContext.subscriptions.push(importReportFileCommandDisposable);
}


