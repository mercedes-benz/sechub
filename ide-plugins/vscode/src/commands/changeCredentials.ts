// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { SecHubContext } from "../extension";
import { SECHUB_CREDENTIAL_KEYS } from "../utils/sechubConstants";
import { DefaultClient } from "../api/defaultClient";

export async function changeCredentials(sechubContext: SecHubContext): Promise<void> {
        const currentUsername = await sechubContext.extensionContext.secrets.get(SECHUB_CREDENTIAL_KEYS.username) || '';
        const currentApiToken = await sechubContext.extensionContext.secrets.get(SECHUB_CREDENTIAL_KEYS.apiToken) || '';
        const newUsername = await vscode.window.showInputBox({
            prompt: 'Enter SecHub Username',
            value: currentUsername,
            validateInput: (value: string) => !value ? 'Username cannot be empty' : null
        });
        if (newUsername) {
            await sechubContext.extensionContext.secrets.store(SECHUB_CREDENTIAL_KEYS.username, newUsername);
        }
        const newApiToken = await vscode.window.showInputBox({
            prompt: 'Enter SecHub API Token',
            value: currentApiToken,
            password: true,
            validateInput: (value: string) => !value ? 'API Token cannot be empty' : null
        });
        if (newApiToken) {
            await sechubContext.extensionContext.secrets.store(SECHUB_CREDENTIAL_KEYS.apiToken, newApiToken);
        }
        if (newUsername && newApiToken) {
            await DefaultClient.createClient(sechubContext.extensionContext);
            sechubContext.serverTreeProvider.refresh();
            vscode.window.showInformationMessage('SecHub credentials updated.');
        } else {
            vscode.window.showErrorMessage('Failed to update SecHub credentials. Please try again.');
        }
    }