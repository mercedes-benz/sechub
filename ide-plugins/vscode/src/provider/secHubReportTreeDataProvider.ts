// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as path from 'path';
import * as sechubModel from '../model/sechubModel';

export class SecHubReportTreeDataProvider implements vscode.TreeDataProvider<ReportItem> {

  constructor(private findingModel: sechubModel.FindingModel | undefined) { }

  /* refresh mechanism for tree:*/
  private _onDidChangeTreeData: vscode.EventEmitter<ReportItem | undefined | null | void> = new vscode.EventEmitter<ReportItem | undefined | null | void>();
  readonly onDidChangeTreeData: vscode.Event<ReportItem | undefined | null | void> = this._onDidChangeTreeData.event;

  private refresh(): void {
    this._onDidChangeTreeData.fire();
  }

  getTreeItem(element: ReportItem): vscode.TreeItem {
    return element;
  }

  getChildren(element?: ReportItem): Thenable<ReportItem[]> {
    if (!this.findingModel) {
      vscode.window.showInformationMessage('No finding model available');
      return Promise.resolve([]);
    }

    if (element) {
      if (element instanceof FindingModelMetaDataReportItem) {
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


  public update(findingModel: sechubModel.FindingModel) {
    this.findingModel = findingModel;
    this.refresh();
  }

  /**
   * Given the path to package.json, read all its dependencies and devDependencies.
   */
  private getReportItems(): ReportItem[] {
    let rootItems: ReportItem[] = [];
    rootItems.push(new FindingModelMetaDataReportItem("Report UUID:", this.findingModel?.jobUUID, vscode.TreeItemCollapsibleState.None));
    rootItems.push(new FindingModelMetaDataReportItem("Traffic light:", this.findingModel?.trafficLight, vscode.TreeItemCollapsibleState.None));
    let findings: FindingModelMetaDataReportItem = new FindingModelMetaDataReportItem("Findings:", "" + this.findingModel?.result.findings.length, vscode.TreeItemCollapsibleState.Expanded);
    rootItems.push(findings);

    this.findingModel?.result.findings.forEach((finding) => {
      let item: ReportItem = new FindingNodeReportItem(finding);
      item.contextValue = "reportItem";
      item.command = {
        command: "sechubReportView.selectNode",
        title: "Select Node",
        arguments: [item]
      };
      findings.children.push(item);
    });
    return rootItems;

  }

}

export class ReportItem extends vscode.TreeItem {
}

export class FindingModelMetaDataReportItem extends ReportItem {
  children: ReportItem[] = [];

  constructor(key: string, value: string | undefined, state: vscode.TreeItemCollapsibleState) {
    super(key, state);
    this.description = value;
  }

}

export class FindingNodeReportItem extends ReportItem {
  readonly findingNode: sechubModel.FindingNode;

  constructor(findingNode: sechubModel.FindingNode
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
