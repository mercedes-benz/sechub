// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { Command } from 'vscode';
import * as findingNodeLinkBuilder from '../utils/findingNodeLinkBuilder';
import { SecHubCodeCallStack, SecHubFinding, SecHubReportWeb } from 'sechub-openapi-ts-client';

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
      if (element instanceof MetaDataInfoItem || element instanceof WebScanInfoItem) {
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
      rootItems.push(new MetaDataInfoItem("Name:", this.findingNode?.name, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new MetaDataInfoItem("Description:", this.findingNode?.description, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new MetaDataInfoItem(SecHubInfoTreeDataProvider.cweIdKey, "CWE " + this.findingNode?.cweId, this.findingNodeLinkBuilder.buildCWEOpenInBrowserCommand(this.findingNode), vscode.TreeItemCollapsibleState.None));
      rootItems.push(new MetaDataInfoItem("Source:", this.callStack?.source?.trim(), undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new MetaDataInfoItem("Relevant part:", this.callStack?.relevantPart, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new MetaDataInfoItem("Line:", this.callStack?.line, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new MetaDataInfoItem("Column:", this.callStack?.column, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new MetaDataInfoItem("Type:", this.findingNode?.type, undefined, vscode.TreeItemCollapsibleState.None));

      // webScan
    } else if (this.findingNode?.web){
      rootItems.push(new MetaDataInfoItem("Summary:", this.findingNode?.name, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new MetaDataInfoItem(SecHubInfoTreeDataProvider.cweIdKey, "CWE " + this.findingNode?.cweId, this.findingNodeLinkBuilder.buildCWEOpenInBrowserCommand(this.findingNode), vscode.TreeItemCollapsibleState.None));
      rootItems.push(new MetaDataInfoItem("Description:", this.findingNode?.description, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new MetaDataInfoItem("Solution:", this.findingNode?.solution, undefined, vscode.TreeItemCollapsibleState.None));
      rootItems.push(new WebScanInfoItem("Details", this.findingNode.web, vscode.TreeItemCollapsibleState.Collapsed));

    }

      return rootItems;
  }

}

export class InfoItem extends vscode.TreeItem {
}

export class MetaDataInfoItem extends InfoItem {
  children: InfoItem[] = [];

  constructor(key: string,
    value: string | number | undefined,
    command: Command | undefined,
    state: vscode.TreeItemCollapsibleState) {
    super(key, state);

    let description = "";

    if (value) {
      description = "" + value;
    }

    this.description = description;

    this.command = command;
    if (SecHubInfoTreeDataProvider.cweIdKey === key) {
      this.tooltip = "Click to open CWE description in browser";
    } if ("WebSCan" === key){
      this.tooltip = "Login to SecHub Web UI to see more details";
    } else {
      this.tooltip = key + "\n" + value;
    }
  }
}

export class WebScanInfoItem extends InfoItem {
  children: InfoItem[] = [];

    constructor(
        public readonly name: string,
        public readonly web: SecHubReportWeb,
        public readonly collapsibleState: vscode.TreeItemCollapsibleState
    ) {
        super(name, collapsibleState);

        this.children.push(...this.createWebScanDetails());
    }

    contextValue = 'finding';

    private createWebScanDetails(): InfoItem[] {
      const requestItem = new MetaDataInfoItem("Request", "", undefined, vscode.TreeItemCollapsibleState.Collapsed);
      const responseItem = new MetaDataInfoItem("Response", "", undefined, vscode.TreeItemCollapsibleState.Collapsed);

      if (this.web.request) {
          requestItem.children.push(new MetaDataInfoItem("Method:", this.web.request.protocol + ' ' + this.web.request.version + ' ' + this.web.request.method, undefined, vscode.TreeItemCollapsibleState.None));
          requestItem.children.push(new MetaDataInfoItem("Target:", this.web.request.target, undefined, vscode.TreeItemCollapsibleState.None));
          requestItem.children.push(new MetaDataInfoItem("Attack Vector:", this.web.attack?.vector || 'N/A', undefined, vscode.TreeItemCollapsibleState.None));

          const headers = new MetaDataInfoItem("Headers:", "", undefined, vscode.TreeItemCollapsibleState.Collapsed);
          if (this.web.request.headers) {
              for (const [key, value] of Object.entries(this.web.request.headers)) {
                  headers.children.push(new MetaDataInfoItem(key, value, undefined, vscode.TreeItemCollapsibleState.None));
              }
          }
          requestItem.children.push(headers);

          requestItem.children.push(new MetaDataInfoItem("Body:", this.web.request.body?.text || '{}', undefined, vscode.TreeItemCollapsibleState.None));
      }

      if (this.web.response) {
        responseItem.children.push(new MetaDataInfoItem("Status Code:", this.web.response.protocol + ' ' + this.web.response.version + ' ' +  this.web.response.statusCode, undefined, vscode.TreeItemCollapsibleState.None));
        responseItem.children.push(new MetaDataInfoItem("Evidence:", this.web.attack?.evidence?.snippet || 'N/A', undefined, vscode.TreeItemCollapsibleState.None));

        const headers = new MetaDataInfoItem("Headers:", "", undefined, vscode.TreeItemCollapsibleState.Collapsed);
        if (this.web.response.headers) {
            for (const [key, value] of Object.entries(this.web.response.headers)) {
                headers.children.push(new MetaDataInfoItem(key, value, undefined, vscode.TreeItemCollapsibleState.None));
            }
        }
        responseItem.children.push(headers);

        responseItem.children.push(new MetaDataInfoItem("Body:", this.web.response.body?.text || '{}', undefined, vscode.TreeItemCollapsibleState.None));
      }

      const items: InfoItem[] = [];
      items.push(requestItem);
      items.push(responseItem);
      return items;
    }
}