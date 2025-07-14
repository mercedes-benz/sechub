import { SecHubContext } from '../extension';
import { SECHUB_VIEW_IDS } from '../utils/sechubConstants';
import { JobListTable } from '../webview/jobListTable';
import * as vscode from 'vscode';

export class SecHubServerWebviewProvider implements vscode.WebviewViewProvider {

	public static readonly viewType = SECHUB_VIEW_IDS.serverView;

	private _view?: vscode.WebviewView;
	private _sechubContext: SecHubContext;
	private jobListDataTable = new JobListTable();

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
				case 'colorSelected':
					{
						vscode.window.activeTextEditor?.insertSnippet(new vscode.SnippetString(`#${data.value}`));
						break;
					}
			}
		});
	}

	public async refresh() {
		if (this._view) {
			this._view.show?.(true);
			this._view.webview.postMessage({ type: 'fetchJobs' });
			this._view.webview.html = await this._getHtmlForWebview(this._view.webview);
		}
	}

	private async _getHtmlForWebview(webview: vscode.Webview) {
		// Use a nonce to only allow a specific script to be run.
		const nonce = getNonce();
		
		const dataTable = await this.jobListDataTable.createJobListTable(this._sechubContext.extensionContext);
		
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

				<title>Server</title>
			</head>
			<body class="vscode-light">
				${dataTable}
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