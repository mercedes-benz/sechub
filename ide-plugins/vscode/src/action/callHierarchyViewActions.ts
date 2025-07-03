// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import * as sechubExtension from './../extension';
import { HierarchyItem } from './../provider/secHubCallHierarchyTreeDataProvider';
import { SecHubCodeCallStack, SecHubFinding } from 'sechub-openapi-typescript/src/generated-sources/openapi';

export function hookHierarchyItemActions(context: sechubExtension.SecHubContext) {
	let callBack = (hierarchyItem: HierarchyItem) => {
		if (hierarchyItem instanceof HierarchyItem) {
			var element = hierarchyItem.callstackElement;

			openInEditor(context, element);
			showInInfoView(context, hierarchyItem.findingNode, element);
		}
	};

	// register and make disposable
	let selectEditorCommandDisposable = vscode.commands.registerCommand('sechubCallHierarchyView.selectNode', callBack);
	context.extensionContext.subscriptions.push(selectEditorCommandDisposable);
}


function showInInfoView(context: sechubExtension.SecHubContext, findingNode: SecHubFinding |undefined, element: SecHubCodeCallStack) {
	context.infoTreeProvider.update(findingNode,element);
}


function openInEditor(context: sechubExtension.SecHubContext, element: SecHubCodeCallStack) {
	if(!element){
		return;
	}
	var result = context.fileLocationExplorer.searchFor(element.location || "");
	if (result.size === 0) {
		console.log("No result found for " + element.location);
		return;
	}
	var fileLocation = result.values().next().value;
	console.log("File location:" + fileLocation);

	// ensure the column is not negative or zero
	var column = 1;
	if(!element.column){
		return;
	}
    if (element.column > 0) {
        column = element.column;
    }

	// either use the relevantPart or the source length
	var endPosLength = 0;
	if(!element.relevantPart || !element.line){
		return;
	}
	if ("relevantPart" in element) {
		endPosLength = element.relevantPart.length;
	} else if ("source" in element) {
		let source : string = element["source"] || '';
		endPosLength = source.length;
	}

	var startPos = new vscode.Position(element.line-1, column-1);
	var endPos = new vscode.Position(element.line-1, column-1 + endPosLength);

	var selectionRange = new vscode.Range(startPos, endPos);
	var openDocumentCallback = (doc: vscode.TextDocument) => {
		vscode.window.showTextDocument(doc, { selection: selectionRange });
	};
	var uri = vscode.Uri.file(fileLocation);
	vscode.workspace.openTextDocument(uri).then(openDocumentCallback);
}
