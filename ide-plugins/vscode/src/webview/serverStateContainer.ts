// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { DefaultClient } from '../api/defaultClient';
import { SECHUB_API_CLIENT_CONFIG_KEYS } from '../utils/sechubConstants';

export interface ServerState {
	html: string;
	isConnected: boolean;
}

export class ServerStateContainer {
	async renderServerStateContainer(context: vscode.ExtensionContext): Promise<ServerState> {
		const serverUrl =
			context.globalState.get<string>(SECHUB_API_CLIENT_CONFIG_KEYS.serverUrl) || 'No server URL set';

		let serverStateContainer = '';
		let isServerConnected = false;
		let serverState = 'Disconnected';

		const client = await DefaultClient.getInstance(context);
		const data = await client.userFetchUserDetailInformation();
		if (data) {
			if (data.userId) {
				isServerConnected = true;
				serverState = `Connected as ${data.userId}`;
			}
		}

		serverStateContainer = `<div id="serverStateContainer">
            <div id="serverUrlContainer" class="tooltip">
              <span class="tooltiptext">Configure SecHub Server URL</span>
                <p id="serverUrl">Server URL: <span>${serverUrl}</span></p>
            </div>
            <div id="serverUserContainer" class="tooltip">
                <span class="tooltiptext">Configure SecHub Username and APIToken</span>
                <p id="connectionState">${serverState}</span></p>
            </div>
            <div id="webUiContainer">
                <button id="openWebUiBtn" class="sechubSecondaryButton">Open Web-UI</button>
            </div>
                
        </div>`;

		return {
			html: serverStateContainer,
			isConnected: isServerConnected,
		};
	}
}
