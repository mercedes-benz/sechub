// SPDX-License-Identifier: MIT
import * as path from 'path';
import * as vscode from 'vscode';
import { Command } from 'vscode';
import * as findingNodeLinkBuilder from './../model/findingNodeLinkBuilder';
import { SecHubCodeCallStack, SecHubFinding } from 'sechub-openapi-ts-client';

export class SecHubInfoTreeDataProvider implements vscode.TreeDataProvider<InfoItem> {
  findingNodeLinkBuilder: findingNodeLinkBuilder.FindingNodeLinkBuilder;

  constructor(private findingNode: SecHubFinding | undefined, private callStack: SecHubCodeCallStack | undefined) {
    this.findingNodeLinkBuilder = new findingNodeLinkBuilder.FindingNodeLinkBuilder();
  }

  /* refresh mechanism for tree:*/
  private _onDidChangeTreeData: vscode.EventEmitter<InfoItem | undefined | null | void> = new vscode.EventEmitter<InfoItem | undefined | null | void>();
  readonly onDidChangeTreeData: vscode.Event<InfoItem | undefined | null | void> = this._onDidChangeTreeData.event;
  static cweIdKey: string = "CWE-ID:";

  private refresh(): void {
    this._onDidChangeTreeData.fire();
  }

  getTreeItem(element: InfoItem): vscode.TreeItem {
    return element;
  }

  getChildren(element?: InfoItem): Thenable<InfoItem[]> {
    if (!this.callStack) {
      vscode.window.showInformationMessage('No call stack data available');
      return Promise.resolve([]);
    }

    if (element) {
      if (element instanceof MetaDataInfoItem) {
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


  public update(findingNode: SecHubFinding | undefined, callStack: SecHubCodeCallStack) {
    this.findingNode = findingNode;
    this.callStack = callStack;
    this.refresh();
  }


  /**
   * Given the path to package.json, read all its dependencies and devDependencies.
   */
  private getReportItems(): InfoItem[] {
    let rootItems: InfoItem[] = [];

    rootItems.push(new MetaDataInfoItem("Name:", this.findingNode?.name, undefined, vscode.TreeItemCollapsibleState.None));
    rootItems.push(new MetaDataInfoItem("Description:", this.findingNode?.description, undefined, vscode.TreeItemCollapsibleState.None));
    rootItems.push(new MetaDataInfoItem(SecHubInfoTreeDataProvider.cweIdKey, "CWE " + this.findingNode?.cweId, this.findingNodeLinkBuilder.buildCWEOpenInBrowserCommand(this.findingNode), vscode.TreeItemCollapsibleState.None));
    rootItems.push(new MetaDataInfoItem("Source:", this.callStack?.source?.trim(), undefined, vscode.TreeItemCollapsibleState.None));
    rootItems.push(new MetaDataInfoItem("Relevant part:", this.callStack?.relevantPart, undefined, vscode.TreeItemCollapsibleState.None));
    rootItems.push(new MetaDataInfoItem("Line:", this.callStack?.line, undefined, vscode.TreeItemCollapsibleState.None));
    rootItems.push(new MetaDataInfoItem("Column:", this.callStack?.column, undefined, vscode.TreeItemCollapsibleState.None));
    rootItems.push(new MetaDataInfoItem("Type:", this.findingNode?.type, undefined, vscode.TreeItemCollapsibleState.None));
    
    return rootItems;
  }

}

export class InfoItem extends vscode.TreeItem {
}

export class MetaDataInfoItem extends InfoItem {
  children: InfoItem[] = [];

  constructor(key: string, value: string | number | undefined, command: Command | undefined, state: vscode.TreeItemCollapsibleState) {
    super(key, state);

    var description = "";

    if (value) {
      description = "" + value;
    }

    this.description = "" + description;

    this.command = command;
    if (SecHubInfoTreeDataProvider.cweIdKey === key) {
      this.tooltip = "Click to open CWE description in browser";
    } else {
      this.tooltip = key + "\n" + value;
    }
  }
}

export class FindingMetaInfoItem extends InfoItem {
  readonly findingNode: SecHubFinding;

  constructor(findingNode: SecHubFinding
  ) {
    super(findingNode.id + " - " + findingNode.severity, vscode.TreeItemCollapsibleState.None);

    this.description = findingNode.name;
    this.tooltip = `${this.label}-${this.description}`;
    this.findingNode = findingNode;
  }


  iconPath = {
    light: path.join(__filename, '..', '..', 'resources', 'light', 'ReportItem.svg'),
    dark: path.join(__filename, '..', '..', 'resources', 'dark', 'ReportItem.svg')
  };
}
