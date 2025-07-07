// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as path from 'path';
import { SecHubReport, SecHubFinding } from 'sechub-openapi-ts-client';

export class SecHubReportTreeDataProvider implements vscode.TreeDataProvider<ReportItem> {

  constructor(private report: SecHubReport | undefined) { }

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
    if (!this.report) {
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


  public update(report: SecHubReport) {
    this.report = report;
    this.refresh();
  }

  /**
   * Given the path to package.json, read all its dependencies and devDependencies.
   */
  private getReportItems(): ReportItem[] {
    let rootItems: ReportItem[] = [];

    if(!this.report?.result){
      vscode.window.showInformationMessage('No result in your SecHub report to show!');
      return [];
    }

    if (!this.report?.result.findings){
      vscode.window.showInformationMessage('No findings in your SecHub report to show!');
      return [];
    }

    rootItems.push(new FindingModelMetaDataReportItem("Report UUID:", this.report?.jobUUID, vscode.TreeItemCollapsibleState.None));
    rootItems.push(new FindingModelMetaDataReportItem("Traffic light:", this.report?.trafficLight, vscode.TreeItemCollapsibleState.None));


    let findingItems: FindingModelMetaDataReportItem = new FindingModelMetaDataReportItem("Findings:", "" + this.report?.result?.findings.length, vscode.TreeItemCollapsibleState.Expanded);
    rootItems.push(findingItems);

    this.report?.result?.findings.forEach((finding) => {
      let item: ReportItem = new FindingNodeReportItem(finding);
      item.contextValue = "reportItem";
      item.command = {
        command: "sechubReportView.selectNode",
        title: "Select Node",
        arguments: [item]
      };
      findingItems.children.push(item);
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
  readonly sechubFinding: SecHubFinding;

  constructor(sechubFinding: SecHubFinding
  ) {
    super(sechubFinding.id + " - " + sechubFinding.severity, vscode.TreeItemCollapsibleState.None);

    this.description = sechubFinding.name;
    this.tooltip = `${this.label}-${this.description}`;
    this.sechubFinding = sechubFinding;
  }


  iconPath = {
    light: path.join(__filename, '..', '..', 'resources', 'light', 'ReportItem.svg'),
    dark: path.join(__filename, '..', '..', 'resources', 'dark', 'ReportItem.svg')
  };
}
