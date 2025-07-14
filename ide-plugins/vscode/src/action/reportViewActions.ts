// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as sechubExtension from './../extension';
import { FindingNodeReportItem, ReportItem } from './../provider/secHubReportTreeDataProvider';

export function hookReportItemActions(context: sechubExtension.SecHubContext) {
	let showCallHierarchyCallBack = (reportItem: ReportItem) => {
		if (reportItem instanceof FindingNodeReportItem) {
			context.callHierarchyTreeDataProvider.update(reportItem.sechubFinding);

			/* fetch first child and select first one in hierarchy view (this will show up info as well*/
			context.callHierarchyTreeDataProvider.getChildren().then((hierarchyItems) => {
				if (hierarchyItems.length === 0) {
					return;
				}
				var hierarchyItem = hierarchyItems[0];
				context.callHierarchyView?.reveal(hierarchyItem, { select: true });

				// unfortunately the former call does just select the item in UI but
				// does not handle the other parts.
				// so we must trigger here manually:
				vscode.commands.executeCommand("sechubCallHierarchyView.selectNode", hierarchyItem);
			});
		}
	};

	let openCWEinBrowserCallback = (reportItem: ReportItem) => {
		if (reportItem instanceof FindingNodeReportItem) {
			context.findingNodeLinkBuilder.buildCWELinkAndOpenInBrowser(reportItem.sechubFinding);
		}
	};

	let openCWEinBrowserCommandDisposable = vscode.commands.registerCommand('sechubReportView.openCWEinBrowser', openCWEinBrowserCallback);
	context.extensionContext.subscriptions.push(openCWEinBrowserCommandDisposable);

	let selectCallHierarchyCommandDisposable = vscode.commands.registerCommand("sechubReportView.selectNode", showCallHierarchyCallBack);
	context.extensionContext.subscriptions.push(selectCallHierarchyCommandDisposable);

}
