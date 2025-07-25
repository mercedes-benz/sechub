// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { SecHubCodeCallStack, SecHubFinding } from 'sechub-openapi-ts-client';
import { SECHUB_COMMANDS } from '../utils/sechubConstants';
import { HierarchyItem } from './items/hierarchyItems';

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
      // console.debug("No finding available, returning empty hierarchy items.");
      // item = new EmptyHierarchyItem("Webscan:", "Code Call Hierarchy is not available for webscan findings.");
      // return Promise.resolve([item]);
      return Promise.resolve([]);
    }

    if (item) {
      return Promise.resolve(item.children);
    } else {
      // no element found, so create...
      return Promise.resolve(
        this.createHierarchyItems()
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

  private createHierarchyItems(): HierarchyItem[] {

    let items: HierarchyItem[] = [];

    let codeCallStack: SecHubCodeCallStack | undefined = this.finding?.code;
    let state: vscode.TreeItemCollapsibleState = codeCallStack?.calls ? vscode.TreeItemCollapsibleState.Expanded : vscode.TreeItemCollapsibleState.None;

    if (!(codeCallStack)) {
      console.debug("No code callstack found for this finding, returning empty hierarchy item.");
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
      item.contextValue = "callHierarchyItem";
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