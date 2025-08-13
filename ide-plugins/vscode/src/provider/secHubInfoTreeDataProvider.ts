// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as findingNodeLinkBuilder from './items/infoViewItemMitreCWELinkBuilder';
import { SecHubCodeCallStack, SecHubFinding } from 'sechub-openapi-ts-client';
import { InfoItem, MetaDataInfoItem, WebScanInfoItem } from './items/infoItems';

export class SecHubInfoTreeDataProvider implements vscode.TreeDataProvider<InfoItem> {
	findingNodeLinkBuilder: findingNodeLinkBuilder.InfoViewItemMitreCWELinkBuilder;

	constructor(
		private findingNode: SecHubFinding | undefined,
		private callStack: SecHubCodeCallStack | undefined,
	) {
		this.findingNodeLinkBuilder = new findingNodeLinkBuilder.InfoViewItemMitreCWELinkBuilder();
	}

	/* refresh mechanism for tree:*/
	private _onDidChangeTreeData: vscode.EventEmitter<InfoItem | undefined | null | void> = new vscode.EventEmitter<
		InfoItem | undefined | null | void
	>();
	readonly onDidChangeTreeData: vscode.Event<InfoItem | undefined | null | void> = this._onDidChangeTreeData.event;
	static cweIdKey = 'CWE-ID:';
	static webScanSUmmaryKey = 'Summary:';

	private refresh(): void {
		this._onDidChangeTreeData.fire();
	}

	getTreeItem(element: InfoItem): vscode.TreeItem {
		return element;
	}

	getChildren(element?: InfoItem): Thenable<InfoItem[]> {
		if (element) {
			if (element instanceof MetaDataInfoItem || element instanceof WebScanInfoItem) {
				return Promise.resolve(element.children);
			} else {
				return Promise.resolve([]); // no children at the moment
			}
		} else {
			return Promise.resolve(this.getReportItems());
		}
	}

	public update(findingNode: SecHubFinding | undefined, callStack: SecHubCodeCallStack | undefined) {
		this.findingNode = findingNode;
		this.callStack = callStack;
		this.refresh();
	}

	/**
	 * Given the path to package.json, read all its dependencies and devDependencies.
	 */
	private getReportItems(): InfoItem[] {
		const rootItems: InfoItem[] = [];

		// codeScan, iacScan or secretScan
		if (this.callStack) {
			rootItems.push(
				new MetaDataInfoItem('Name:', this.findingNode?.name, undefined, vscode.TreeItemCollapsibleState.None),
			);
			rootItems.push(
				new MetaDataInfoItem(
					'Description:',
					this.findingNode?.description,
					undefined,
					vscode.TreeItemCollapsibleState.None,
				),
			);
			rootItems.push(
				new MetaDataInfoItem(
					SecHubInfoTreeDataProvider.cweIdKey,
					'CWE ' + this.findingNode?.cweId,
					this.findingNodeLinkBuilder.buildCWEOpenInBrowserCommand(this.findingNode),
					vscode.TreeItemCollapsibleState.None,
				),
			);
			rootItems.push(
				new MetaDataInfoItem(
					'Source:',
					this.callStack?.source?.trim(),
					undefined,
					vscode.TreeItemCollapsibleState.None,
				),
			);
			rootItems.push(
				new MetaDataInfoItem(
					'Relevant part:',
					this.callStack?.relevantPart,
					undefined,
					vscode.TreeItemCollapsibleState.None,
				),
			);
			rootItems.push(
				new MetaDataInfoItem('Line:', this.callStack?.line, undefined, vscode.TreeItemCollapsibleState.None),
			);
			rootItems.push(
				new MetaDataInfoItem(
					'Column:',
					this.callStack?.column,
					undefined,
					vscode.TreeItemCollapsibleState.None,
				),
			);
			rootItems.push(
				new MetaDataInfoItem('Type:', this.findingNode?.type, undefined, vscode.TreeItemCollapsibleState.None),
			);

			// webScan
		} else if (this.findingNode?.web) {
			rootItems.push(
				new MetaDataInfoItem(
					'Summary:',
					this.findingNode?.name,
					undefined,
					vscode.TreeItemCollapsibleState.None,
				),
			);
			rootItems.push(
				new MetaDataInfoItem(
					SecHubInfoTreeDataProvider.cweIdKey,
					'CWE ' + this.findingNode?.cweId,
					this.findingNodeLinkBuilder.buildCWEOpenInBrowserCommand(this.findingNode),
					vscode.TreeItemCollapsibleState.None,
				),
			);

			rootItems.push(
				new MetaDataInfoItem(
					'Description:',
					this.findingNode?.description,
					undefined,
					vscode.TreeItemCollapsibleState.None,
				),
			);
			rootItems.push(
				new MetaDataInfoItem(
					'Solution:',
					this.findingNode?.solution,
					undefined,
					vscode.TreeItemCollapsibleState.None,
				),
			);
			rootItems.push(
				new WebScanInfoItem('More Details', this.findingNode.web, vscode.TreeItemCollapsibleState.Collapsed),
			);
		}

		return rootItems;
	}
}
