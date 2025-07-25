import * as vscode from 'vscode';
import { Command } from 'vscode';
import { SecHubInfoTreeDataProvider } from '../secHubInfoTreeDataProvider';
import { SecHubReportWeb } from 'sechub-openapi-ts-client';

export class InfoItem extends vscode.TreeItem {
}

export class InfoMetaDataItem extends InfoItem {
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
    } if ("WebSCan" === key) {
      this.tooltip = "Login to SecHub Web UI to see more details";
    } else {
      this.tooltip = key + "\n" + value;
    }
  }
}

export class InfoWebScanItem extends InfoItem {
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

  /* creates tree view items for web scan details from SecHubReportWeb */
  private createWebScanDetails(): InfoItem[] {
    const requestItem = new InfoMetaDataItem("Request", "", undefined, vscode.TreeItemCollapsibleState.Collapsed);
    const responseItem = new InfoMetaDataItem("Response", "", undefined, vscode.TreeItemCollapsibleState.Collapsed);
    const attackItem = new InfoMetaDataItem("Attack", "", undefined, vscode.TreeItemCollapsibleState.Collapsed);

    if (this.web.request) {
      requestItem.children.push(new InfoMetaDataItem("Method:", this.web.request.protocol + ' ' + this.web.request.version + ' ' + this.web.request.method, undefined, vscode.TreeItemCollapsibleState.None));
      requestItem.children.push(new InfoMetaDataItem("Target:", this.web.request.target, undefined, vscode.TreeItemCollapsibleState.None));

      const headers = new InfoMetaDataItem("Headers:", "", undefined, vscode.TreeItemCollapsibleState.Collapsed);
      if (this.web.request.headers) {
        for (const [key, value] of Object.entries(this.web.request.headers)) {
          headers.children.push(new InfoMetaDataItem(key, value, undefined, vscode.TreeItemCollapsibleState.None));
        }
      }
      requestItem.children.push(headers);

      requestItem.children.push(new InfoMetaDataItem("Body:", this.web.request.body?.text || '{}', undefined, vscode.TreeItemCollapsibleState.None));
    }

    if (this.web.response) {
      responseItem.children.push(new InfoMetaDataItem("Status Code:", this.web.response.protocol + ' ' + this.web.response.version + ' ' + this.web.response.statusCode, undefined, vscode.TreeItemCollapsibleState.None));

      const headers = new InfoMetaDataItem("Headers:", "", undefined, vscode.TreeItemCollapsibleState.Collapsed);
      if (this.web.response.headers) {
        for (const [key, value] of Object.entries(this.web.response.headers)) {
          headers.children.push(new InfoMetaDataItem(key, value, undefined, vscode.TreeItemCollapsibleState.None));
        }
      }
      responseItem.children.push(headers);

      responseItem.children.push(new InfoMetaDataItem("Body:", this.web.response.body?.text || '{}', undefined, vscode.TreeItemCollapsibleState.None));
    }

    if (this.web.attack) {
      attackItem.children.push(new InfoMetaDataItem("Vector:", this.web.attack.vector || 'N/A', undefined, vscode.TreeItemCollapsibleState.None));
      attackItem.children.push(new InfoMetaDataItem("Evidence:", this.web.attack.evidence?.snippet || 'N/A', undefined, vscode.TreeItemCollapsibleState.None));
      attackItem.children.push(new InfoMetaDataItem("Body Location:", this.web.attack.evidence?.bodyLocation?.startLine || 'N/A', undefined, vscode.TreeItemCollapsibleState.None));
    }

    const items: InfoItem[] = [];
    items.push(requestItem);
    items.push(responseItem);
    items.push(attackItem);
    return items;
  }
}

