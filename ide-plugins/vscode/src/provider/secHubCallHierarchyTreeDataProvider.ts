// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as path from 'path';
import { SecHubCodeCallStack, SecHubFinding } from 'sechub-openapi-ts-client';

export class SecHubCallHierarchyTreeDataProvider implements vscode.TreeDataProvider<HierarchyItem> {

  public update(findingNode: SecHubFinding) {
    this.finding = findingNode;
    this.refresh();
  }

  /* refresh mechanism for tree:*/
  private _onDidChangeTreeData: vscode.EventEmitter<HierarchyItem | undefined | null | void> = new vscode.EventEmitter<HierarchyItem | undefined | null | void>();
  readonly onDidChangeTreeData: vscode.Event<HierarchyItem | undefined | null | void> = this._onDidChangeTreeData.event;

  constructor(private finding: SecHubFinding | undefined) { }

  private refresh(): void {
    this._onDidChangeTreeData.fire();
  }

  getTreeItem(element: HierarchyItem): vscode.TreeItem {
    return element;
  }

  getChildren(element?: HierarchyItem): Thenable<HierarchyItem[]> {
    if (!this.finding) {
      vscode.window.showInformationMessage('No finding available');
      return Promise.resolve([]);
    }

    if (element) {
      return Promise.resolve(element.children);
    } else {
      // no element found, so create...
      return Promise.resolve(
        this.createtHierarchyItems()
      );
    }
  }

  getParent(element?: HierarchyItem): vscode.ProviderResult<HierarchyItem> {
    if (!this.finding) {
      return undefined;
    }

    if (!element) {
      return undefined;
    } else {
      return element.parent;
    }
  }

  private createtHierarchyItems(): HierarchyItem[] {

    let items: HierarchyItem[] = [];

    let codeCallStackElement: SecHubCodeCallStack | undefined = this.finding?.code;
    let state: vscode.TreeItemCollapsibleState = codeCallStackElement?.calls ? vscode.TreeItemCollapsibleState.Expanded : vscode.TreeItemCollapsibleState.None;

    if (!(codeCallStackElement)) {
      return items;
    }
    let parent: HierarchyItem | undefined;

    do {
      let item: HierarchyItem = new HierarchyItem(this.finding, codeCallStackElement, state);
      item.command = {
        command: "sechubCallHierarchyView.selectNode",
        title: "Select Node",
        arguments: [item]
      };
      if (items.length === 0) {
        items.push(item);
      }
      item.contextValue = "callHierarchyitem";
      if (parent) {
        parent.add(item);
        item.parent = parent;
      }
      /* go deeper ...*/
      codeCallStackElement = codeCallStackElement.calls;
      parent = item;

    } while (codeCallStackElement);

    return items;

  }

}

export class HierarchyItem extends vscode.TreeItem {

  readonly children: HierarchyItem[] = [];
  callstackElement: SecHubCodeCallStack;
  findingNode: SecHubFinding | undefined;
  parent: HierarchyItem | undefined;

  constructor(findingNode: SecHubFinding | undefined, callstackElement: SecHubCodeCallStack, state: vscode.TreeItemCollapsibleState
  ) {
    if(!callstackElement.relevantPart){
      callstackElement.relevantPart = "";
    }
    super(callstackElement.relevantPart, state);

    this.description = callstackElement.location;
    this.tooltip = `${this.label}-${this.description}`;
    this.callstackElement = callstackElement;
    this.findingNode = findingNode;
  }

  iconPath = {
    light: path.join(__filename, '..', '..', 'resources', 'light', 'HierarchyItem.svg'),
    dark: path.join(__filename, '..', '..', 'resources', 'dark', 'HierarchyItem.svg')
  };

  add(child: HierarchyItem) {
    this.children.push(child);
  }
}