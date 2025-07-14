import { SECHUB_CREDENTIAL_KEYS, SECHUB_REPORT_KEYS } from '../utils/sechubConstants';
import { DefaultClient } from '../api/defaultClient';
import * as vscode from 'vscode';
import { ProjectData } from 'sechub-openapi-ts-client';

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
            return Promise.resolve(element.children);
        }
    }

    private async getRootItems(): Promise<ServerItem[]> {
        const serverUrl = this.context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl) || 'No server URL set';

        let state: string = 'Unknown';
        const client = await DefaultClient.getInstance(this.context);
        try {
            const response = await client.getApiClient().withUserSelfServiceApi().userFetchUserDetailInformation();
            state = 'Connected with ' + (response?.userId || 'Unknown User');
        } catch (error) {
            console.error('Error checking SecHub server connection:', error);
            vscode.window.showErrorMessage('Failed to connect to SecHub server. Please check your credentials and server URL.');
            state = 'Disconnected';
        }

        const project: ProjectData | undefined = this.context.globalState.get(SECHUB_REPORT_KEYS.selectedProject);
        const projectIdText = project ? 'for Project: ' + project.projectId : 'No Project Selected';
        const reportTableItem = new ServerItem('Jobs', projectIdText , vscode.TreeItemCollapsibleState.Collapsed, []);

        const jobs = await client.userListsJobsForProject(project?.projectId || '');
        if (jobs && jobs.content && jobs.content.length > 0) {
            const jobItems = jobs.content.map(job => new ServerItem(job.jobUUID || '', `Status: ${job.executionState}`, vscode.TreeItemCollapsibleState.None));
            reportTableItem.children.push(...jobItems);
        } else {
            reportTableItem.children.push(new ServerItem('No Jobs Found', '', vscode.TreeItemCollapsibleState.None));
        }
        //const projectSelectionItem = new ServerItem('Select Project', '', vscode.TreeItemCollapsibleState.Collapsed, [], 'selectProjectItem');
        
        return [
            new ServerItem('Server URL:', serverUrl, vscode.TreeItemCollapsibleState.None),
            new ServerItem('Connection State:', state, vscode.TreeItemCollapsibleState.None),
            reportTableItem
        ];
    }

    refresh(): void {
        this._onDidChangeTreeData.fire();
    }
}

export class ServerItem extends vscode.TreeItem {

    constructor(key: string, 
        value: string, 
        state: vscode.TreeItemCollapsibleState,
        public children: ServerItem[] = [],
    ) {
        super(key, state);
        this.description = value;
    }

    contextValue = 'serverItem';
}