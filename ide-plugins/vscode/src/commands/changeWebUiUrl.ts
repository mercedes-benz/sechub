// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { SecHubContext } from "../extension";
import { SECHUB_CREDENTIAL_KEYS } from "../utils/sechubConstants";
import { DefaultClient } from "../api/defaultClient";

export async function changeWebUiUrl(sechubContext: SecHubContext): Promise<void> {
    const serverUrl = sechubContext.extensionContext.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl);
    const defaultWebUiUrl = serverUrl ? `${serverUrl}/login` : '';
    const currentWebUiUrl = sechubContext.extensionContext.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.webUiUrl);

    const newWebUiUrl = await vscode.window.showInputBox({
        prompt: 'Enter SecHub Web-Ui URL',
        value: currentWebUiUrl || defaultWebUiUrl,
        validateInput: (input) => {
            if (!input) {
                return null; // No error if input is empty, it will use the default
            }
            try {
                new URL(input);
            } catch (e) {
                return 'Invalid URL format';
            }
            return null; // No error
        }
    });

    if (newWebUiUrl !== undefined) {
        const finalWebUiUrl = newWebUiUrl || defaultWebUiUrl;
        const trimmedUrl = finalWebUiUrl.replace(/\/+$/, '');

        await sechubContext.extensionContext.globalState.update(SECHUB_CREDENTIAL_KEYS.webUiUrl, trimmedUrl);
        vscode.window.showInformationMessage(`SecHub Web-ui URL updated to: '${trimmedUrl}'`);
    }
}