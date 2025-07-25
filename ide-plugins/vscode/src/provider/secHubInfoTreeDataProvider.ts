// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as findingNodeLinkBuilder from '../utils/findingNodeLinkBuilder';
import { SecHubCodeCallStack, SecHubFinding } from 'sechub-openapi-ts-client';
import { InfoItem , InfoMetaDataItem, InfoWebScanItem } from './items/infoItems';

export class SecHubInfoTreeDataProvider implements vscode.TreeDataProvider<InfoItem> {
  findingNodeLinkBuilder: findingNodeLinkBuilder.FindingNodeLinkBuilder;

  constructor(private findingNode: SecHubFinding | undefined, private callStack: SecHubCodeCallStack | undefined) {
    this.findingNodeLinkBuilder = new findingNodeLinkBuilder.FindingNodeLinkBuilder();
  }

  /* refresh mechanism for tree:*/
  private _onDidChangeTreeData: vscode.EventEmitter<InfoItem | undefined | null | void> = new vscode.EventEmitter<InfoItem | undefined | null | void>();
  readonly onDidChangeTreeData: vscode.Event<InfoItem | undefined | null | void> = this._onDidChangeTreeData.event;
  static cweIdKey: string = "CWE-ID:";
  static webScanSUmmaryKey: string = "Summary:";

  private refresh(): void {
    this._onDidChangeTreeData.fire();
  }

  getTreeItem(element: InfoItem): vscode.TreeItem {
    return element;
  }

  getChildren(element?: InfoItem): Thenable<InfoItem[]> {

    if (element) {
      if (element instanceof InfoMetaDataItem || element instanceof InfoWebScanItem) {
        return Promise.resolve(element.children);
      } else {
        return Promise.resolve([]); // no children at the moment
      }
    } else {
      return Promise.resolve(
        this.getReportItems()
      );
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
    let rootItems: InfoItem[] = [];

    // codeScan, iacScan or secretScan
    if(this.callStack){
      rootItems.push(new InfoMetaDataItem("Name:", this.findingNode?.name, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoMetaDataItem("Description:", this.findingNode?.description, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoMetaDataItem(SecHubInfoTreeDataProvider.cweIdKey, "CWE " + this.findingNode?.cweId, this.findingNodeLinkBuilder.buildCWEOpenInBrowserCommand(this.findingNode), vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoMetaDataItem("Source:", this.callStack?.source?.trim(), undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoMetaDataItem("Relevant part:", this.callStack?.relevantPart, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoMetaDataItem("Line:", this.callStack?.line, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoMetaDataItem("Column:", this.callStack?.column, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoMetaDataItem("Type:", this.findingNode?.type, undefined, vscode.TreeItemCollapsibleState.None));

      // webScan
    } else if (this.findingNode?.web){
      rootItems.push(new InfoMetaDataItem("Summary:", this.findingNode?.name, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoMetaDataItem(SecHubInfoTreeDataProvider.cweIdKey, "CWE " + this.findingNode?.cweId, this.findingNodeLinkBuilder.buildCWEOpenInBrowserCommand(this.findingNode), vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoMetaDataItem("Description:", this.findingNode?.description, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoMetaDataItem("Solution:", this.findingNode?.solution, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new InfoWebScanItem("More Details", this.findingNode.web, vscode.TreeItemCollapsibleState.Collapsed));

    }

      return rootItems;
  }

}
