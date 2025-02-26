// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as sechubExtension from './../extension';
import * as secHubModel from './../model/sechubModel';

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

				let findingModel = secHubModel.loadFromFile(filePath);

				let scanTypes = getScanTypesTypesOfFindingModel(findingModel);

				if (scanTypes.has(secHubModel.ScanType.codeScan) || (scanTypes.has(secHubModel.ScanType.secretScan))) {
					context.reportTreeProvider.update(findingModel);
					context.findingModel = findingModel;
				} else {
					var foundScanTypesStr = "No scan types found.";

					if (scanTypes.size > 0) {
						foundScanTypesStr = "Found scan types: " + Array.from(scanTypes).join(", ");
					}

					vscode.window.showErrorMessage("Unable to import report. Wrong scan types. Can only import scan types: `codeScan` or `secretScan`. " + foundScanTypesStr);
				}
			}
		});
	});

	context.extensionContext.subscriptions.push(importReportFileCommandDisposable);
}

function getScanTypesTypesOfFindingModel(findingModel: secHubModel.FindingModel): Set<secHubModel.ScanType> {
	const scanTypes = new Set<secHubModel.ScanType>();

	for (let finding of findingModel.result.findings) {
		scanTypes.add(finding.type);
	}

	return scanTypes;
}

