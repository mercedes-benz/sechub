// SPDX-License-Identifier: MIT
import { SecHubReport } from 'sechub-openapi-ts-client';
import * as fs from 'fs';
import * as vscode from 'vscode';

export function loadFromFile(location: string): SecHubReport {

    const rawReport = fs.readFileSync(location, 'utf8');
    return JSON.parse(rawReport) as SecHubReport;
}

export function openCWEIDInBrowser(cweId: string | undefined): void {
    if (!cweId || cweId === 'undefined') {
        return;
    }
    const uri = vscode.Uri.parse(`https://cwe.mitre.org/data/definitions/${cweId}.html`);
    vscode.commands.executeCommand("vscode.open", uri);
}