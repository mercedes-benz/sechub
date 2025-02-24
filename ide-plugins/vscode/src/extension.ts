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
import * as secHubModel from './model/sechubModel';
import { HierarchyItem, SecHubCallHierarchyTreeDataProvider } from './provider/secHubCallHierarchyTreeDataProvider';
import { SecHubInfoTreeDataProvider } from './provider/secHubInfoTreeDataProvider';
import { ReportItem, SecHubReportTreeDataProvider } from './provider/secHubReportTreeDataProvider';

export function activate(context: vscode.ExtensionContext) {
	console.log('SecHub plugin activation requested.');
	
	let loadTestData = context.extensionMode === vscode.ExtensionMode.Development;
	let initialFindingModel = undefined;
	if (loadTestData) {
		initialFindingModel = secHubModel.loadFromFile(resolveFileLocation("test_sechub_report-1.json"));
	}

	let secHubContext: SecHubContext = new SecHubContext(initialFindingModel, context);

	buildReportView(secHubContext);
	buildCallHierarchyView(secHubContext);
	buildInfoView(secHubContext);

	hookActions(secHubContext);

	console.log('SecHub plugin has been activated.');
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

	findingNodeLinkBuilder: FindingNodeLinkBuilder;
	callHierarchyTreeDataProvider: SecHubCallHierarchyTreeDataProvider;
	reportTreeProvider: SecHubReportTreeDataProvider;
	infoTreeProvider: SecHubInfoTreeDataProvider;
	findingModel: secHubModel.FindingModel | undefined;
	extensionContext: vscode.ExtensionContext;
	fileLocationExplorer: FileLocationExplorer;

	constructor(findingModel: secHubModel.FindingModel | undefined, extensionContext: vscode.ExtensionContext,
	) {
		this.reportTreeProvider = new SecHubReportTreeDataProvider(findingModel);
		this.callHierarchyTreeDataProvider = new SecHubCallHierarchyTreeDataProvider(undefined);
		this.infoTreeProvider = new SecHubInfoTreeDataProvider(undefined, undefined);
		this.extensionContext = extensionContext;
		this.fileLocationExplorer = new FileLocationExplorer();
		this.findingNodeLinkBuilder = new FindingNodeLinkBuilder();

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
