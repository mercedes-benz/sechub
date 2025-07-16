// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { DefaultClient } from '../api/defaultClient';
import { SECHUB_CREDENTIAL_KEYS } from '../utils/sechubConstants';

export interface ServerState {
    html: string;
    isConnected: boolean;
}

export class ServerStateContainer {

    async renderServerStateContainer(context: vscode.ExtensionContext): Promise<ServerState> {

        const serverUrl = context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl) || 'No server URL set';

        let serverStateContainer = '';
        let isServerConnected = false;
        let serverState = 'Disconnected';

        const client = await DefaultClient.getInstance(context);
        const data = await client.userFetchUserDetailInformation();
        if (data) {
            if(data.userId) {
                isServerConnected = true;
                serverState = `Connected as ${data.userId}`;
            }
        }

        serverStateContainer = `<div id="serverStateContainer">
            <div id="serverUrlContainer">
                <p id="serverUrl">Server URL: <span>${serverUrl}</span></p>
            </div>
            <div id="serverUserContainer">
                <p id="connectionState">${serverState}</span></p>
            </div>
        </div>`;
        // todo: should also return the server state, so that it can be used in the Server webview

        return {
            html: serverStateContainer,
            isConnected: isServerConnected
        };
    }
}