// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { SecHubReport, SecHubMessage, SecHubStatus, SecHubMessageType } from 'sechub-openapi-ts-client';
import { SECHUB_COMMANDS } from '../utils/sechubConstants';
import { ReportItem, ReportMetadataItem, ReportFindingItem } from './items/reportItems';

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
      if (element instanceof ReportMetadataItem) {
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


  public update(report: SecHubReport | undefined) {
    this.report = report;
    this.refresh();
  }

  /**
   * Given the path to package.json, read all its dependencies and devDependencies.
   */
  private getReportItems(): ReportItem[] {
    let rootItems: ReportItem[] = [];

    if(!this.report) {
      return rootItems;
    }

    if(!this.report?.result){
      return rootItems;
    }

    if (!this.report?.result.findings){
      vscode.window.showInformationMessage('No findings in your SecHub report to show!');
      return rootItems;
    }

    rootItems.push(new ReportMetadataItem("Report UUID:", this.report?.jobUUID, vscode.TreeItemCollapsibleState.None));
    rootItems.push(new ReportMetadataItem("Traffic light:", this.report?.trafficLight, vscode.TreeItemCollapsibleState.None));

    this.createMetadataInformationTreeItem(rootItems);

    let findingItems: ReportMetadataItem = new ReportMetadataItem("Findings:", "" + this.report?.result?.findings.length, vscode.TreeItemCollapsibleState.Expanded);
    rootItems.push(findingItems);

    this.report?.result?.findings.forEach((finding) => {
      let item: ReportItem = new ReportFindingItem(finding);
      item.contextValue = "reportItem";
      if(finding.web){
        item.command = {
          command: SECHUB_COMMANDS.openWebScanInInfoview,
          title: "Select Node",
          arguments: [item]
        };
      } else {
        item.command = {
          command: SECHUB_COMMANDS.openFindingCallStack,
          title: "Select Node",
          arguments: [item]
        };
      }
      findingItems.children.push(item);
    });
    return rootItems;

  }

  private createMetadataInformationTreeItem(rootItems: ReportItem[]){
    /* adding metainformation if available */
    const metadataInfoItem = new ReportMetadataItem("Job Information", "", vscode.TreeItemCollapsibleState.Collapsed);

    if(this.report){

      const reportState: SecHubStatus = this.report.status ? this.report.status : "FAILED";
      metadataInfoItem.children.push(new ReportMetadataItem("Status:", reportState, vscode.TreeItemCollapsibleState.None));

      const executedScans: string[] = this.report.metaData?.executed || [];
      if(executedScans.length > 0) {
        metadataInfoItem.children.push(new ReportMetadataItem("Executed Scans:", executedScans.join(", "), vscode.TreeItemCollapsibleState.None));
      }

      const labelsDataItem = new ReportMetadataItem("Labels:", "", vscode.TreeItemCollapsibleState.Collapsed);
      const labels: { [key: string]: any; } = this.report.metaData?.labels || {};
      for (const [key, value] of Object.entries(labels)) {
        labelsDataItem.children.push(new ReportMetadataItem(key, value, vscode.TreeItemCollapsibleState.None));
      }
      if (labelsDataItem.children.length > 0) {
        metadataInfoItem.children.push(labelsDataItem);
      }

      const messageMetadataItem = new ReportMetadataItem("Messages:", "" + (this.report?.messages?.length || 0), vscode.TreeItemCollapsibleState.Collapsed);
      const reportMessages: SecHubMessage[] = this.report.messages || [];
      reportMessages.forEach(message => {
        messageMetadataItem.children.push(new ReportMetadataItem(message.type ? message.type : SecHubMessageType.Info, message.text, vscode.TreeItemCollapsibleState.None));
      });
      
      metadataInfoItem.children.push(messageMetadataItem);

    } else {
      metadataInfoItem.children.push(new ReportMetadataItem("No Report loaded", "", vscode.TreeItemCollapsibleState.None));
    }
  
    rootItems.push(metadataInfoItem);
  }
}


