// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as sechubExtension from './../extension';
import { loadFromFile } from './../utils/sechubUtils';

export async function importReportFromFile(context: sechubExtension.SecHubContext) {

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
            context.setReport(report);
        }
    });
}
