// SPDX-License-Identifier: MIT
import { SecHubContext } from '../extension';
import { SECHUB_COMMANDS, SECHUB_VIEW_IDS, SECHUB_API_CLIENT_CONFIG_KEYS, SECHUB_CONTEXT_STORAGE_KEYS } from '../utils/sechubConstants';
import { JobListTable } from '../webview/jobTable';
import { ServerStateContainer } from '../webview/serverStateContainer';
import { DefaultClient } from '../api/defaultClient';
import * as vscode from 'vscode';
import { preSelectedProjectValid, getNonce } from '../utils/sechubUtils';


export class SecHubServerWebviewProvider implements vscode.WebviewViewProvider {

	public static readonly viewType = SECHUB_VIEW_IDS.serverView;

	private _view?: vscode.WebviewView;
	private _sechubContext: SecHubContext;
	private jobListDataTable = new JobListTable();
	private serverStateContainer = new ServerStateContainer();
	private isConnected: boolean = false;

	constructor(
		private readonly _extensionUri: vscode.Uri,
		_sechubContext: SecHubContext,
	) { 
		this._sechubContext = _sechubContext;
	}

	public async resolveWebviewView(
		webviewView: vscode.WebviewView,
		_context: vscode.WebviewViewResolveContext,
		_token: vscode.CancellationToken,
	) {
		this._view = webviewView;

		webviewView.webview.options = {
			enableScripts: true,

			localResourceRoots: [
				this._extensionUri
			]
		};

		webviewView.webview.html = await this._getHtmlForWebview(webviewView.webview);

		webviewView.webview.onDidReceiveMessage(data => {
			switch (data.type) {
				case 'changeProject':
					{
						vscode.commands.executeCommand(SECHUB_COMMANDS.selectProject);
					}
					break;
				case 'changeServerUrl':
					{
						vscode.commands.executeCommand(SECHUB_COMMANDS.changeServerUrl);
					}
					break;
				case 'changeCredentials':
					{
						vscode.commands.executeCommand(SECHUB_COMMANDS.changeCredentials);
					}
					break;
				case 'fetchReport':
					{
						this.syncReportFromServer(data.jobUUID, data.projectId, data.result);
					}
					break;
				case 'changePage':
					{
						this.jobListDataTable.changePage(data.direction);
						this.refresh();
					}
					break;
				case 'openWebUi':
					{
						const leftClick = data.data.leftClick;
						this.openWebUi(leftClick);
					}
					break;
			}
		});
	}

	public async refresh() {
		if (this._view) {
			this._view.show?.(true);
			await preSelectedProjectValid(this._sechubContext.extensionContext);			
			this._view.webview.html = await this._getHtmlForWebview(this._view.webview);
		}
	}

	private openWebUi(leftClick: any) {
		if (leftClick) {
			let webUiUrl = this._sechubContext.extensionContext.globalState.get<string>(SECHUB_CONTEXT_STORAGE_KEYS.webUiUrl);
			if (webUiUrl) {

				if (!webUiUrl.endsWith('/login')) {
					const projectId = this._sechubContext.extensionContext.globalState.get<string>(SECHUB_CONTEXT_STORAGE_KEYS.selectedProject);
					if (projectId) {
						webUiUrl += `/projects/${projectId}`;
					}
				}
				vscode.env.openExternal(vscode.Uri.parse(webUiUrl));
			} else {
				vscode.window.showErrorMessage('No SecHub Web-UI URL configured. Please set it in with command "SecHub: Change Web-UI URL".');
			}
		} else {
			vscode.commands.executeCommand(SECHUB_COMMANDS.changeWebUiUrl);
		}
	}

	private async syncReportFromServer(jobUUID: string, projectId: string, result: string) {
		if (!jobUUID || !projectId || !result) {
			vscode.window.showErrorMessage('Invalid parameters to fetch report. Please ensure job UUID, project ID, and result are provided.');
			return;
		}

		if (result !== 'OK') {
			vscode.window.showErrorMessage(`Job has no report yet. Please wait for the job to finish.`);
			return;
		}

		const client = await DefaultClient.getInstance(this._sechubContext.extensionContext);
		const report = await client.fetchReport(projectId, jobUUID);
		if (report) {
			this._sechubContext.setReport(report);
		} else {
			vscode.window.showErrorMessage('Failed to fetch report from the server.');
		}
	}

	private async _getHtmlForWebview(webview: vscode.Webview) {
		// Use a nonce to only allow a specific script to be run.
		const nonce = getNonce();

		const styleMainUri = webview.asWebviewUri(vscode.Uri.joinPath(this._extensionUri, 'media', 'css', 'main.css'));
		const javascriptUri = webview.asWebviewUri(vscode.Uri.joinPath(this._extensionUri, 'media', 'js', 'server.js'));

		const codiconsUri = webview.asWebviewUri(vscode.Uri.joinPath(vscode.Uri.joinPath(this._extensionUri, 'node_modules', '@vscode/codicons', 'dist', 'codicon.css')));


		const serverState = await this.serverStateContainer.renderServerStateContainer(this._sechubContext.extensionContext);
		const serverStateHtml = serverState.html;
		this.isConnected = serverState.isConnected;
		let dataTableHtml = '<div> Can not load Data without SecHub connection. </div>';
		// render the job list table only if connected
		if (this.isConnected) {
			dataTableHtml = await this.jobListDataTable.renderJobTable(this._sechubContext.extensionContext);
		}
		
		const htmlSource = `
		<!DOCTYPE html>
			<html lang="en">
			<head>
				<meta charset="UTF-8">
				<!--
					Use a content security policy to only allow loading styles from our extension directory,
					and only allow scripts that have a specific nonce.
					(See the 'webview-sample' extension sample for img-src content security policy examples)
				-->
				<meta http-equiv="Content-Security-Policy" content="default-src 'none';  font-src ${webview.cspSource}; style-src ${webview.cspSource}; script-src 'nonce-${nonce}';">

				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				
				<link href="${styleMainUri}" rel="stylesheet">
				<link href="${codiconsUri}" rel="stylesheet" />

				<title>Server</title>
			</head>
			<body class="vscode-light">
				${serverStateHtml}
				${dataTableHtml}
			<script nonce="${nonce}" src="${javascriptUri}" script-src 'nonce-${nonce}'></script>
			</body>
			</html>
		`;

		return htmlSource;
	}
}
