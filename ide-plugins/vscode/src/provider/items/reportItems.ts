import { SecHubFinding } from 'sechub-openapi-ts-client';
import * as vscode from 'vscode';


export class ReportItem extends vscode.TreeItem {
}

export class ReportMetadataItem extends ReportItem {
  children: ReportItem[] = [];

  constructor(key: string, value: string | undefined, state: vscode.TreeItemCollapsibleState) {
    super(key, state);
    this.description = value;
  }

}

export class ReportFindingItem extends ReportItem {
  readonly sechubFinding: SecHubFinding;

  constructor(sechubFinding: SecHubFinding
  ) {
    super(sechubFinding.id + " - " + sechubFinding.severity, vscode.TreeItemCollapsibleState.None);

    this.description = sechubFinding.name;
    this.tooltip = `${this.label}-${this.description}`;
    this.sechubFinding = sechubFinding;
  }
}
