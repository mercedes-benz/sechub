// SPDX-License-Identifier: MIT
import { SecHubFinding } from 'sechub-openapi-ts-client';
import * as vscode from 'vscode';

export class FindingNodeLinkBuilder {

    public buildCWELinkAndOpenInBrowser(findingNode: SecHubFinding | undefined) {
        const uri = this.buildCWELink(findingNode);
        if (!uri) {
            return;
        }
        vscode.commands.executeCommand("vscode.open", uri);
    }

    public buildCWELink(findingNode: SecHubFinding | undefined): vscode.Uri | undefined {
        if (!findingNode) {
            return undefined;
        }
        if (!findingNode.cweId) {
            return undefined;
        }
        return vscode.Uri.parse("https://cwe.mitre.org/data/definitions/" + findingNode.cweId + ".html");
    }

    public buildCWEOpenInBrowserCommand(findingNode: SecHubFinding | undefined): vscode.Command | undefined {
        if (!findingNode) {
            return undefined;
        }
        const uri = this.buildCWELink(findingNode);
        if (!uri) {
            return undefined;
        }
        return {
            title: "Open CWE " + findingNode.cweId + " in browser",
            command: "vscode.open",
            arguments: [uri]
        };
    }
}



