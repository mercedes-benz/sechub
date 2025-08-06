// SPDX-License-Identifier: MIT
import { SecHubReport, FalsePositiveProjectConfiguration } from 'sechub-openapi-ts-client';
import { SECHUB_CONTEXT_STORAGE_KEYS } from '../utils/sechubConstants';
import * as vscode from 'vscode';
import { getFalsePositivesByIDForJobReport } from '../utils/sechubUtils';
import { FalsePositiveCache } from '../cache/falsePositiveCache';

export class ReportListTable {
	private falsePositivesForReport: number[] = [];

	async renderReportTable(context: vscode.ExtensionContext, report: SecHubReport): Promise<string> {
		const htmlLabels: string[] = this.generateHtmlLabels(report);
		const htmlMessages: string[] = this.generateHtmlMessages(report);

		const unsyncedFalsePositives = FalsePositiveCache.getEntryByJobUUID(context, report.jobUUID || '');
		const unsynchedFindingIds: number[] = unsyncedFalsePositives ? unsyncedFalsePositives.findingIDs : [];

		const jobInfo = `
        <div class="vscode-sidebar-colors sidebar-header"> <span id="reportViewtrafficLight" class="traffic-light ${report.trafficLight?.toLowerCase()}"></span><b>Job UUID:</b> ${report.jobUUID}</div>
        <div class="expandable-header vscode-sidebar-colors" data-target="metadataInfo">
            <i class="codicon codicon-chevron-right"></i>
            <b>Job Information:</b>
        </div>
        <div id="metadataInfo" class="expandable-content vscode-sidebar-colors">
            <div><b>Status:</b> ${report.status}</div>
            <div class="expandable-header" data-target="labelInfo">
                <i class="codicon codicon-chevron-right"></i>
                <b>Labels:</b>
            </div>
            <div id="labelInfo" class="expandable-content vscode-sidebar-colors">
                ${htmlLabels.length > 0 ? htmlLabels.join('') : '<div>No labels available.</div>'}
            </div>
            <div><b>Executed Scans:</b> ${report.metaData?.executed?.join(', ') || 'None'}</div>
            <div class="expandable-header" data-target="messageInfo">
                <i class="codicon codicon-chevron-right"></i>
                <b>Messages:</b>
        </div>
        <div id="messageInfo" class="expandable-content vscode-sidebar-colors">
            ${htmlMessages.length > 0 ? htmlMessages.join('') : '<div>No messages available.</div>'}
        </div>
        </div>
        <div class="vscode-sidebar-colors margin-bottom"><b>Findings:</b> ${report.result?.findings?.length || 0}</div>`;

		const findings = report.result?.findings || [];
		if (findings.length === 0) {
			return `${jobInfo}`;
		}

		const markFalsePositiveButton = `<button id="markAsFalsePositiveButton" class="tooltip sechubSecondaryButton" disabled style="display: none;">
        <i class="codicon codicon-sync"></i>
        <span class="tooltiptext">Mark selected findings als False Positives.</span>
        </button>`;

		const header = `
        <thead>
            <tr>
                <th>
                    <input type="checkbox" id="selectAllFalsePositives">
                    ${markFalsePositiveButton}
                </th>
                <th>ID</th>
                <th>Severity</th>
                <th>CWE</th>
                <th>Name</th>
            </tr>
        <thead>`;

		const falsePositiveConfig = context.globalState.get<FalsePositiveProjectConfiguration>(
			SECHUB_CONTEXT_STORAGE_KEYS.falsePositiveConfiguration,
		);
		if (falsePositiveConfig && report.jobUUID) {
			this.falsePositivesForReport = getFalsePositivesByIDForJobReport(falsePositiveConfig, report.jobUUID);
		}

		const checkBoxUnchecked = `<input type="checkbox"  class="item-checkbox">`;
		const checkBoxChecked = `<input type="checkbox" checked class="item-checkbox">`;
		const webScanFidning = `<i class="codicon codicon-globe sechubIcon"></i><span class="tooltiptext">IDE does not support marking webscan findings. <br/> Please use SecHub Web-ui.</span>`;
		const markedFalesPositive = `<i class="codicon codicon-pass sechubIcon"></i><span class="tooltiptext">Finding is already marked as false positive.</span>`;

		let tableContent = '';
		if (report.result && report.result.findings) {
			report.result?.findings.forEach(finding => {
				const isWebFinding = finding.web ? true : false;
				const isFalsePositive = this.isFalsePositive(finding.id);
				const isUnsyncedFalsePositive = unsynchedFindingIds.includes(finding.id || 0);

				const type = isWebFinding
					? webScanFidning
					: isFalsePositive
						? markedFalesPositive
						: isUnsyncedFalsePositive
							? checkBoxChecked
							: checkBoxUnchecked;

				tableContent += `
                            <tr class="sechub-finding-row" data-finding-id="${finding.id}">
                                <td class="tooltip">${type}</td>
                                <td>${finding.id}</td>
                                <td>${finding.severity}</td>
                                <td><button id="openCWEinBrowserButton" class="sechubSecondaryButton">${finding.cweId}</button></td>
                                <td>${finding.name}</td>
                            </tr>`;
			});
		}
		const table = `${jobInfo}<table id="sechubReportTable" class="vscode-sidebar-colors sechubTable">${header}<tbody>${tableContent}</body></table>`;

		return table;
	}

	private generateHtmlLabels(report: SecHubReport) {
		const labels: { [key: string]: unknown } = report.metaData?.labels || {};
		const htmlLabels: string[] = [];
		for (const [key, value] of Object.entries(labels)) {
			htmlLabels.push(`<div><b>${key}:</b> ${value}</div>`);
		}
		return htmlLabels;
	}

	private generateHtmlMessages(report: SecHubReport) {
		const messages: string[] = [];
		report.messages?.forEach(message => {
			const type = message.type ? message.type : 'INFO';
			messages.push(`<div><b>${type} </b>${message.text}</div>`);
		});
		return messages;
	}

	private isFalsePositive(findingId: number | undefined): boolean {
		if (findingId === undefined) {
			return false;
		}
		return this.falsePositivesForReport.includes(findingId);
	}
}
