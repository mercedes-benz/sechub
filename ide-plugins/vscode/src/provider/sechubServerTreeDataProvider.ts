import { SECHUB_CREDENTIAL_KEYS } from '../utils/sechubConstants';
import { DefaultClient, getDefaultClient } from '../api/defaultClient';
import * as vscode from 'vscode';

export class SecHubServerTreeProvider implements vscode.TreeDataProvider<ServerItem> {

    private _onDidChangeTreeData: vscode.EventEmitter<ServerItem | undefined | void> = new vscode.EventEmitter<ServerItem | undefined | void>();
    readonly onDidChangeTreeData: vscode.Event<ServerItem | undefined | void> = this._onDidChangeTreeData.event;

    constructor(private context: vscode.ExtensionContext) {}

    getTreeItem(element: ServerItem): vscode.TreeItem {
        return element;
    }

    getChildren(element?: ServerItem): Thenable<ServerItem[]> {
        if (!element) {
            // Return root items
            return this.getRootItems();
        } else {
            // Return children of the given element (if any)
            return Promise.resolve([]);
        }
    }

    private async getRootItems(): Promise<ServerItem[]> {
        const serverUrl = this.context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl) || 'No server URL set';

        let state: string = 'Unknown';
        try {
            const client: DefaultClient = await getDefaultClient(this.context);
            await client.withSystemApi.anonymousCheckAliveGet();
            state = 'Connected';
        } catch (error) {
            console.error('Error checking SecHub server connection:', error);
            vscode.window.showErrorMessage('Failed to connect to SecHub server. Please check your credentials and server URL.');
            state = 'Disconnected';
        }
        
        return [
            new ServerItem('Server URL:', serverUrl, vscode.TreeItemCollapsibleState.None),
            new ServerItem('Connection State:', state, vscode.TreeItemCollapsibleState.None),
        ];
    }

    refresh(): void {
        this._onDidChangeTreeData.fire();
    }
}

export class ServerItem extends vscode.TreeItem {

    constructor(key: string, value: string, state: vscode.TreeItemCollapsibleState) {
        super(key, state);
        this.description = value;
    }

    iconPath = {
        light: vscode.Uri.file(__dirname + '/resources/light/server.svg'),
        dark: vscode.Uri.file(__dirname + '/resources/dark/server.svg')
    };

    contextValue = 'serverItem';
}