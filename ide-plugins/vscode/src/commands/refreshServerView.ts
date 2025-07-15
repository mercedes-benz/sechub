import * as vscode from 'vscode';
import { SecHubContext } from '../extension';

export async function refreshServerView(sechubContext: SecHubContext): Promise<void> {
    try {
        await sechubContext.serverWebViewProvider.refresh();
    } catch (error) {
        console.error('Error refreshing SecHub server view:', error);
        vscode.window.showErrorMessage('Failed to refresh SecHub server view. Please check the logs for more details.');
    }
}