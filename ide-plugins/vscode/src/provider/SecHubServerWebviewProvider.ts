import { SecHubContext } from '../extension';
import { SECHUB_COMMANDS, SECHUB_VIEW_IDS } from '../utils/sechubConstants';
import { JobListTable } from '../webview/jobTable';
import { ServerStateContainer } from '../webview/serverStateContainer';
import { DefaultClient } from '../api/defaultClient';
import * as vscode from 'vscode';

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
						this.syncReportFromServer(data.jobUUID, data.projectId);
					}
					break;
			}
		});
	}

	public async refresh() {
		if (this._view) {
			this._view.show?.(true);
			this._view.webview.html = await this._getHtmlForWebview(this._view.webview);
		}
	}

	private async syncReportFromServer(jobUUID: string, projectId: string) {
		const client = await DefaultClient.getInstance(this._sechubContext.extensionContext);
		const report = await client.fetchReport(projectId, jobUUID);
		if (report) {
			this._sechubContext.reportTreeProvider.update(report);
			this._sechubContext.report = report;
		} else {
			vscode.window.showErrorMessage('Failed to fetch report from the server.');
		}
	}

	private async _getHtmlForWebview(webview: vscode.Webview) {
		// Use a nonce to only allow a specific script to be run.
		const nonce = getNonce();

		const styleMainUri = webview.asWebviewUri(vscode.Uri.joinPath(this._extensionUri, 'media', 'css', 'main.css'));

		const javascriptUri = webview.asWebviewUri(vscode.Uri.joinPath(this._extensionUri, 'media', 'js', 'main.js'));

		const serverState = await this.serverStateContainer.createServerStateContainer(this._sechubContext.extensionContext);
		const serverStateHtml = serverState.html;
		this.isConnected = serverState.isConnected;
		let dataTableHtml = '<div> Can not load Data without SecHub connection. </div>';
		// render the job list table only if connected
		if (this.isConnected) {
			dataTableHtml = await this.jobListDataTable.createJobTable(this._sechubContext.extensionContext);
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
				<meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src ${webview.cspSource}; script-src 'nonce-${nonce}';">

				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				
				<link href="${styleMainUri}" rel="stylesheet">

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

function getNonce() {
	let text = '';
	const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	for (let i = 0; i < 32; i++) {
		text += possible.charAt(Math.floor(Math.random() * possible.length));
	}
	return text;
}