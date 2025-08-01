// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { SECHUB_COMMANDS } from '../utils/sechubConstants';
import { SecHubContext } from './../extension';
import { SecHubFinding } from 'sechub-openapi-ts-client';

export async function openFindingCallStack(sechubContext: SecHubContext, finding: SecHubFinding): Promise<void> {
    /* shows the hierachy of a finding and selects the first item in it */

    sechubContext.callHierarchyTreeDataProvider.update(finding);

        /* fetch first child and select first one in hierarchy view (this will show up info as well) */
    sechubContext.callHierarchyTreeDataProvider.getChildren().then((hierarchyItems) => {
        if (hierarchyItems.length === 0) {
            console.debug("No hierarchy items found, cannot open call stack.");
            return;
        }
        const  hierarchyItem = hierarchyItems[0];
        sechubContext.callHierarchyView?.reveal(hierarchyItem, { select: true });

        // unfortunately the former call does just select the item in UI but
        // does not handle the other parts.
        // so we must trigger here manually:
        vscode.commands.executeCommand(SECHUB_COMMANDS.openFinding, hierarchyItem.finding, hierarchyItem.codeCallstack);

    });
}