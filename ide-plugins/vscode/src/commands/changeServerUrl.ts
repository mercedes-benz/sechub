// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { SecHubContext } from "../extension";
import { SECHUB_CREDENTIAL_KEYS } from "../utils/sechubConstants";
import { DefaultClient } from "../api/defaultClient";

export async function changeServerUrl(sechubContext: SecHubContext): Promise<void> {
    const currentServerUrl = sechubContext.extensionContext.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl) || '';
        const newServerUrl = await vscode.window.showInputBox({
            prompt: 'Enter SecHub Server URL',
            value: currentServerUrl,
            validateInput: (value: string) => {
                if (!value) {
                    return 'Server URL cannot be empty';
                }
                try { new URL(value); } catch { return 'Invalid URL format'; }
                return null;
            }
        });
        if (newServerUrl) {
            await sechubContext.extensionContext.globalState.update(SECHUB_CREDENTIAL_KEYS.serverUrl, newServerUrl);
            await DefaultClient.createClient(sechubContext.extensionContext);
            sechubContext.serverTreeProvider.refresh();
            vscode.window.showInformationMessage(`SecHub Server URL updated to: '${newServerUrl}'`);
        }
    }