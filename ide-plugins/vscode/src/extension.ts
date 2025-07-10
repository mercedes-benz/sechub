// SPDX-License-Identifier: MIT
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as path from 'path';
import * as vscode from 'vscode';
import * as callHierarchyViewActions from './action/callHierarchyViewActions';
import * as importActions from './action/importActions';
import * as reportViewActions from './action/reportViewActions';
import { FileLocationExplorer } from './fileLocationExplorer';
import { FindingNodeLinkBuilder } from './model/findingNodeLinkBuilder';
import { HierarchyItem, SecHubCallHierarchyTreeDataProvider } from './provider/secHubCallHierarchyTreeDataProvider';
import { SecHubInfoTreeDataProvider } from './provider/secHubInfoTreeDataProvider';
import { ReportItem, SecHubReportTreeDataProvider } from './provider/secHubReportTreeDataProvider';

import { loadFromFile } from './utils/sechubUtils';
import { SecHubReport } from 'sechub-openapi-ts-client';
import { multiStepInput } from './sechubCredentialsMultistepInput';
import { SECHUB_CREDENTIAL_KEYS } from './utils/sechubConstants';
import { ServerItem, SecHubServerTreeProvider } from './provider/sechubServerTreeDataProvider';
import { DefaultClient } from './api/defaultClient';
import { changeServerUrl } from './commands/changeServerUrl';
import { changeCredentials } from './commands/changeCredentials';

export async function activate(context: vscode.ExtensionContext) {
	console.log('SecHub plugin activation requested.');
	
	let loadTestData = context.extensionMode === vscode.ExtensionMode.Development;
	let report: SecHubReport | undefined = undefined;
	if (loadTestData) {
		report = loadFromFile(resolveFileLocation("test_sechub_report-1.json"));
	}

	setUpApiClient(context);

	let secHubContext: SecHubContext = new SecHubContext(report, context);

	buildServerView(secHubContext);
	buildReportView(secHubContext);
	buildCallHierarchyView(secHubContext);
	buildInfoView(secHubContext);

	hookActions(secHubContext);

	registerCommands(secHubContext);

	console.log('SecHub plugin has been activated.');
}

function registerCommands(sechubContext: SecHubContext) {

    const changeServerUrlCommand = vscode.commands.registerCommand('sechub.changeServerUrl', () => changeServerUrl(sechubContext));
    const changeCredentialsCommand = vscode.commands.registerCommand('sechub.changeCredentials', () => changeCredentials(sechubContext));
	
	sechubContext.extensionContext.subscriptions.push(changeServerUrlCommand, 
		changeCredentialsCommand);
}

function setUpApiClient(context: vscode.ExtensionContext) {
	// Check if SecHub credentials are already set
	// If not, prompt the user to set them up
    const serverUrl = context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl);
    const username = context.secrets.get(SECHUB_CREDENTIAL_KEYS.username);
    const apiToken = context.secrets.get(SECHUB_CREDENTIAL_KEYS.apiToken);
    Promise.all([username, apiToken]).then(([username, apiToken]) => {
        if (!serverUrl || !username || !apiToken) {
            multiStepInput(context).then(() => {
                vscode.window.showInformationMessage('SecHub credentials have been set.');
            }).catch(err => {
                vscode.window.showErrorMessage(`Failed to set SecHub credentials: ${err}`);
            });
        } else {
            vscode.window.showInformationMessage('SecHub credentials are already set.');
        }
    });

	// Initialize the SecHub client
	DefaultClient.initialize(context).then(() => {
		vscode.window.showInformationMessage('SecHub client initialized successfully.');
	}).catch(err => {
		vscode.window.showErrorMessage(`Failed to initialize SecHub client:	${err}`);
	});
}

function buildServerView(context: SecHubContext) {
	var view = vscode.window.createTreeView('sechubServerView', {
		treeDataProvider: context.serverTreeProvider
	});
	context.serverView = view;
}

function buildReportView(context: SecHubContext) {
	var view =vscode.window.createTreeView('sechubReportView', {
		treeDataProvider: context.reportTreeProvider
	});
	context.reportView=view;
}

function buildCallHierarchyView(context: SecHubContext) {
	var view = vscode.window.createTreeView('sechubCallHierarchyView', {
		treeDataProvider: context.callHierarchyTreeDataProvider
	});
	context.callHierarchyView=view;
}

function buildInfoView(context: SecHubContext) {
	vscode.window.createTreeView('sechubInfoView', {
		treeDataProvider: context.infoTreeProvider
	});
}

function hookActions(context: SecHubContext) {
	importActions.hookImportAction(context);
	reportViewActions.hookReportItemActions(context);
	callHierarchyViewActions.hookHierarchyItemActions(context);
}


export class SecHubContext {
	callHierarchyView: vscode.TreeView<HierarchyItem|undefined> | undefined = undefined;
	reportView: vscode.TreeView<ReportItem> | undefined = undefined;
	serverView: vscode.TreeView<ServerItem> | undefined = undefined;

	findingNodeLinkBuilder: FindingNodeLinkBuilder;
	callHierarchyTreeDataProvider: SecHubCallHierarchyTreeDataProvider;
	reportTreeProvider: SecHubReportTreeDataProvider;
	infoTreeProvider: SecHubInfoTreeDataProvider;
	report: SecHubReport | undefined;
	extensionContext: vscode.ExtensionContext;
	fileLocationExplorer: FileLocationExplorer;
	serverTreeProvider: SecHubServerTreeProvider;

	constructor(report: SecHubReport| undefined, extensionContext: vscode.ExtensionContext,
	) {
		this.reportTreeProvider = new SecHubReportTreeDataProvider(report);
		this.callHierarchyTreeDataProvider = new SecHubCallHierarchyTreeDataProvider(undefined);
		this.infoTreeProvider = new SecHubInfoTreeDataProvider(undefined, undefined);
		this.extensionContext = extensionContext;
		this.fileLocationExplorer = new FileLocationExplorer();
		this.findingNodeLinkBuilder = new FindingNodeLinkBuilder();
		this.serverTreeProvider = new SecHubServerTreeProvider(extensionContext);

		/* setup search folders for explorer */
		let workspaceFolders = vscode.workspace.workspaceFolders; // get the open folder path
		workspaceFolders?.forEach((workspaceFolder) => {
			this.fileLocationExplorer.searchFolders.add(workspaceFolder.uri.fsPath);
		});

	}
}

export function deactivate() { }

function resolveFileLocation(testfile: string): string {
	let testReportLocation = path.dirname(__filename) + "/../src/test/suite/" + testfile;
	return testReportLocation;
}
