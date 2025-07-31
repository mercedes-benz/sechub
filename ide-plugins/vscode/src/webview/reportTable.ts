// SPDX-License-Identifier: MIT
import { SecHubReport,
    FalsePositiveProjectConfiguration,
 } from 'sechub-openapi-ts-client';
import { SECHUB_CONTEXT_STORAGE_KEYS } from '../utils/sechubConstants';
import * as vscode from 'vscode';
import { getFalsePositivesByIDForJobReport } from '../utils/isFindingFalsePositive';

export class ReportListTable {

    private falsePositivesForReport : number[] = [];

    async renderReportTable(context: vscode.ExtensionContext, report: SecHubReport) : Promise<string> {

        const htmlLabels: string[] = this.generateHtmlLabels(report);
        const htmlMessages: string[] = this.generateHtmlMessages(report);
        
        const jobInfo = `
        <div class="vscode-sidebar-colors sidebar-header"><b>Job UUID:</b> ${report.jobUUID}</div>
        <div class="vscode-sidebar-colors"><span class="traffic-light ${report.trafficLight?.toLowerCase()}"></span></div>
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
                ${htmlLabels.length > 0 ? htmlLabels: '<div>No labels available.</div>'}
            </div>
            <div><b>Executed Scans:</b> ${report.metaData?.executed?.join(', ') || 'None'}</div>
            <div class="expandable-header" data-target="messageInfo">
                <i class="codicon codicon-chevron-right"></i>
                <b>Messages:</b>
        </div>
        <div id="messageInfo" class="expandable-content vscode-sidebar-colors">
            ${htmlMessages.length > 0 ? htmlMessages: '<div>No messages available.</div>'}
        </div>
        </div>
        <div class="vscode-sidebar-colors"><b>Findings:</b> ${report.result?.findings?.length || 0}</div>
        <button id="markAsFalsePositiveButton" class="sechubButton" disabled>Mark as False Positive</button>`;
        

        const header  = `
        <thead>
            <tr>
                <th><input type="checkbox" id="selectAllFalsePositives"></th>
                <th>ID</th>
                <th>Severity</th>
                <th>Name</th>
            </tr>
            <thead>`;

        const falsePositiveConfig = context.globalState.get<FalsePositiveProjectConfiguration>(SECHUB_CONTEXT_STORAGE_KEYS.falsePositiveConfiguration);
        if (falsePositiveConfig && report.jobUUID) {
            this.falsePositivesForReport = getFalsePositivesByIDForJobReport(falsePositiveConfig, report.jobUUID);
        }

        let tableContent = '';
        if (report.result && report.result.findings) {
            report.result?.findings.forEach(finding => {
                const isWebFinding = finding.web ? true : false;
                const isFalsePositive = this.isFalsePositive(finding.id);

                tableContent += `
                            <tr class="sechub-finding-row" data-finding-id="${finding.id}">
                                <td><input type="checkbox"  class="item-checkbox" ${isWebFinding || isFalsePositive ? 'disabled' : ''}></td>
                                <td>${finding.id}</td>
                                <td>${finding.severity}</td>
                                <td>${finding.name}</td>
                            </tr>`;
            });
        }
        const table: string = `${jobInfo}<table id="sechubReportTable" class="vscode-sidebar-colors">${header}<tbody>${tableContent}</body></table>`;

        return table;

    }

    private generateHtmlLabels(report: SecHubReport) {
        const labels: { [key: string]: any; } = report.metaData?.labels || {};
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
            messages.push(`<div><b>${type}</b>${message.text}</div>`);
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