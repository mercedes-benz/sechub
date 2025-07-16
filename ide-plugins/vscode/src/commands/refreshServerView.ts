import * as vscode from 'vscode';
import { SecHubContext } from '../extension';

export async function refreshServerView(sechubContext: SecHubContext): Promise<void> {
    try {
        await sechubContext.serverWebViewProvider.refresh();
        vscode.window.showInformationMessage('SecHub server view refreshed successfully.');
    } catch (error) {
        console.error('Error refreshing SecHub server view:', error);
        vscode.window.showErrorMessage('Failed to refresh SecHub server view. Please check the logs for more details.');
    }
}