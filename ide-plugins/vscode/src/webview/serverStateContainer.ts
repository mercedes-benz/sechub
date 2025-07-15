import * as vscode from 'vscode';
import { DefaultClient } from '../api/defaultClient';
import { SECHUB_CREDENTIAL_KEYS } from '../utils/sechubConstants';  

export class ServerStateContainer {

    public async createServerStateContainer(context: vscode.ExtensionContext): Promise<string> {

        const serverUrl = context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl) || 'No server URL set';

        let serverStateContainer = '';

        const client = await DefaultClient.getInstance(context);
        const data = await client.userFetchUserDetailInformation();
        if (data) {
            serverStateContainer = `<div id="serverStateContainer">
                <div id="serverUrlContainer">
                    <p id="serverUrl">Server URL: <span>${serverUrl}</span></p>
                </div>
                <div id="serverUserContainer">
                    <p id="connectionState">Connection State: <span>${data.userId ? 'Connected with ' + data.userId : 'Disconnected'}</span></p>
                </div>
            </div>`;
        } else {
            serverStateContainer = `<div id="serverStateContainer">
                <div id="serverUrlContainer">
                    <p id="serverUrl">Server URL: <span>${serverUrl}</span></p>
                </div>
                <div id="serverUserContainer">
                    <p id="connectionState">Connection State: Disconnected<span></span></p>
                </div>
            </div>`;
        }

        return serverStateContainer;
    }
}