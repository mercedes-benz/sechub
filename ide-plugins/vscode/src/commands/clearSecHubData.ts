import * as vscode from 'vscode';
import { SecHubContext } from "../extension";
import {SECHUB_CREDENTIAL_KEYS, SECHUB_REPORT_KEYS } from "../utils/sechubConstants";

export async function clearSecHubData(sechubContext: SecHubContext): Promise<void> {

    const confirmClear = await vscode.window.showWarningMessage(
        'Are you sure you want to clear all SecHub data? This will remove all stored credentials',
        { modal: true },
        'Yes',
        'No');

    if (confirmClear !== 'Yes') {
        vscode.window.showInformationMessage('SecHub data clearing cancelled.');
        return;
    }

    // Clear global state for SecHub credentials and selected project
    try {
        await sechubContext.extensionContext.globalState.update(SECHUB_CREDENTIAL_KEYS.serverUrl, undefined);
        await sechubContext.extensionContext.globalState.update(SECHUB_REPORT_KEYS.selectedProject, undefined);
        await sechubContext.extensionContext.secrets.delete(SECHUB_CREDENTIAL_KEYS.username);
        await sechubContext.extensionContext.secrets.delete(SECHUB_CREDENTIAL_KEYS.apiToken);
    }catch (error) {
        console.error('Error clearing SecHub credentials:', error);
    }

    sechubContext.serverWebViewProvider.refresh();
    vscode.window.showInformationMessage('SecHub data cleared successfully.');
}