// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { SecHubContext } from '../extension';
import { HierarchyItem } from '../provider/secHubCallHierarchyTreeDataProvider';
import { SecHubCodeCallStack, SecHubFinding } from 'sechub-openapi-ts-client';

export async function openFinding(sechubContext: SecHubContext, hierarchyItem: HierarchyItem ): Promise<void> {
    /* this command is used to open a finding in the editor and show its details in the info view */

    if (!(hierarchyItem instanceof HierarchyItem)) {
        console.error("Invalid hierarchy item provided to openFinding.");
        return;
    }

    const codeCallStack = hierarchyItem.codeCallstack;
    openInEditor(sechubContext, codeCallStack);
    showInInfoView(sechubContext, hierarchyItem.finding, codeCallStack);
}

function showInInfoView(context: SecHubContext, findingNode: SecHubFinding | undefined, callStackItem: SecHubCodeCallStack) {
    context.infoTreeProvider.update(findingNode, callStackItem);
}

function openInEditor(context: SecHubContext, codeCallStack: SecHubCodeCallStack) {
    if(!codeCallStack.location) {
        console.error("Element location is undefined, can not open in editor.");
        return;
    }

    const result = context.fileLocationExplorer.searchFor(codeCallStack.location);
    if (result.size === 0) {
        console.error("Can not calculate file location for " + codeCallStack.location + ", can not open in editor.");
        return;
    }
    const fileLocation: string = result.values().next().value || '';

    if (!codeCallStack.column) {
        console.error("Element column is undefined, can not open in editor.");
        return;
    }

    let column = codeCallStack.column > 0 ? codeCallStack.column : 1;
    
    /* necessary to check if undefined because relevant part can be 'false' */
    if (codeCallStack.relevantPart === undefined || !codeCallStack.line) {
        console.error("Element relevantPart or line is undefined.");
        return;
    }

    let endPosLength = 0;
    if ("relevantPart" in codeCallStack) {
        endPosLength = codeCallStack.relevantPart.length;
    } else if ("source" in codeCallStack) {
        let source: string = codeCallStack["source"] || '';
        endPosLength = source.length;
    }

    const startPos = new vscode.Position(codeCallStack.line - 1, column - 1);
    const endPos = new vscode.Position(codeCallStack.line - 1, column - 1 + endPosLength);

    const selectionRange = new vscode.Range(startPos, endPos);
    const openDocumentCallback = (doc: vscode.TextDocument) => {
        vscode.window.showTextDocument(doc, { selection: selectionRange });
    };

    const uri = vscode.Uri.file(fileLocation);
    vscode.workspace.openTextDocument(uri).then(openDocumentCallback);
}
