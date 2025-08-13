// SPDX-License-Identifier: MIT
import { SecHubCodeCallStack, SecHubFinding } from 'sechub-openapi-ts-client';
import * as vscode from 'vscode';

export class HierarchyItem extends vscode.TreeItem {
	readonly children: HierarchyItem[] = [];
	codeCallstack: SecHubCodeCallStack;
	finding: SecHubFinding | undefined;
	parent: HierarchyItem | undefined;

	constructor(
		finding: SecHubFinding | undefined,
		codeCallstack: SecHubCodeCallStack,
		state: vscode.TreeItemCollapsibleState,
	) {
		if (!codeCallstack.relevantPart) {
			codeCallstack.relevantPart = '';
		}
		super(codeCallstack.relevantPart, state);

		this.description = codeCallstack.location;
		this.tooltip = `${this.label}-${this.description}`;
		this.codeCallstack = codeCallstack;
		this.finding = finding;
	}

	add(child: HierarchyItem) {
		this.children.push(child);
	}
}
