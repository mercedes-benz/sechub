// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as path from 'path';
import { SecHubCodeCallStack, SecHubFinding } from 'sechub-openapi-ts-client';
import { SECHUB_COMMANDS } from '../utils/sechubConstants';

export class SecHubCallHierarchyTreeDataProvider implements vscode.TreeDataProvider<HierarchyItem> {

  public update(finding: SecHubFinding | undefined) {
    this.finding = finding;
    this.refresh();
  }

  /* refresh mechanism for tree:*/
  private _onDidChangeTreeData: vscode.EventEmitter<HierarchyItem | undefined | null | void> = new vscode.EventEmitter<HierarchyItem | undefined | null | void>();
  readonly onDidChangeTreeData: vscode.Event<HierarchyItem | undefined | null | void> = this._onDidChangeTreeData.event;

  constructor(private finding: SecHubFinding | undefined) { }

  private refresh(): void {
    this._onDidChangeTreeData.fire();
  }

  getTreeItem(item: HierarchyItem): vscode.TreeItem {
    return item;
  }

  getChildren(item?: HierarchyItem): Thenable<HierarchyItem[]> {
    if (!this.finding) {
      return Promise.resolve([]);
    }

    if (item) {
      return Promise.resolve(item.children);
    } else {
      // no element found, so create...
      return Promise.resolve(
        this.createtHierarchyItems()
      );
    }
  }

  getParent(item?: HierarchyItem): vscode.ProviderResult<HierarchyItem> {
    if (!this.finding) {
      return undefined;
    }

    if (!item) {
      return undefined;
    } else {
      return item.parent;
    }
  }

  private createtHierarchyItems(): HierarchyItem[] {

    let items: HierarchyItem[] = [];

    let codeCallStack: SecHubCodeCallStack | undefined = this.finding?.code;
    let state: vscode.TreeItemCollapsibleState = codeCallStack?.calls ? vscode.TreeItemCollapsibleState.Expanded : vscode.TreeItemCollapsibleState.None;

    if (!(codeCallStack)) {
      return items;
    }
    let parent: HierarchyItem | undefined;

    do {
      let item: HierarchyItem = new HierarchyItem(this.finding, codeCallStack, state);
      item.command = {
        command: SECHUB_COMMANDS.openFinding,
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
      codeCallStack = codeCallStack.calls;
      parent = item;

    } while (codeCallStack);

    return items;
  }

}

export class HierarchyItem extends vscode.TreeItem {

  readonly children: HierarchyItem[] = [];
  codeCallstack: SecHubCodeCallStack;
  finding: SecHubFinding | undefined;
  parent: HierarchyItem | undefined;

  constructor(finding: SecHubFinding | undefined, codeCallstack: SecHubCodeCallStack, state: vscode.TreeItemCollapsibleState
  ) {
    if(!codeCallstack.relevantPart){
      codeCallstack.relevantPart = "";
    }
    super(codeCallstack.relevantPart, state);

    this.description = codeCallstack.location;
    this.tooltip = `${this.label}-${this.description}`;
    this.codeCallstack = codeCallstack;
    this.finding = finding;
  }

  add(child: HierarchyItem) {
    this.children.push(child);
  }
}