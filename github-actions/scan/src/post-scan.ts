// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as artifact from '@actions/artifact';
import * as shell from 'shelljs';
import { getReport } from '../../shared/src/sechub-cli';
import { getWorkspaceDir } from '../../shared/src/fs-helper';
import { logExitCode } from '../../shared/src/log-helper';
import * as input from './input';
import * as fs from 'fs';
import { ReportFormat } from '../../shared/src/report-formats';
import { ScanSettings } from './init-scan';

/**
 * Downloads the reports for the given formats.
 * @param scanSettings The settings for the scan
 * @param jobUUID The job UUID for which the report should be downloaded
 */
export function downloadJsonReport(scanSettings: ScanSettings, jobUUID: string): object | undefined {
    core.startGroup('Download JSON Report');
    if (scanSettings.reportFormat !== null && scanSettings.reportFormat !== ReportFormat.JSON) {
        core.info(`Get Report as ${ReportFormat.JSON}`);
        const exitCode = getReport(scanSettings.configPath, jobUUID, input.projectName);
        logExitCode(exitCode ? exitCode.code : 0);
    }
    const json = loadJsonReport();
    core.endGroup();
    return json;
}

/**
 * Load and parse the SecHub JSON report.
 * @returns {object | undefined} - The parsed JSON report or undefined if not found or there was an error.
 */
function loadJsonReport(): object | undefined {
    const fileName = getJsonReportFileName();
    const filePath = `${getWorkspaceDir()}/${fileName}`;

    try {
        return JSON.parse(fs.readFileSync(filePath, 'utf8'));
    } catch (error) {
        core.warning(`Error reading or parsing JSON file: ${error}`);
        return undefined;
    }
}

/**
 * Uploads all given files as artifact
 * @param name Name for the zip file.
 * @param paths All file paths to include into the artifact.
 */
export async function uploadArtifact(name: string, paths: string[]) {
    core.startGroup('Upload artifacts');
    try {
        const artifactClient = artifact.create();
        const artifactName = name;
        const options = { continueOnError: true };

        const workspace = getWorkspaceDir();
        shell.exec(`ls ${workspace}`);
        core.debug('rootDirectory: ' + workspace);
        core.debug('files: ' + paths);

        await artifactClient.uploadArtifact(artifactName, paths, workspace, options);
    } catch (e: unknown) {
        const message = e instanceof Error ? e.message : 'Unknown error';
        core.error(`ERROR while uploading artifacts: ${message}`);
    }
    core.endGroup();
}

/**
 * Reads the given field from the SecHub JSON report.
 * @param {string} field - The field relative to root, where the value should be found. The field can be a nested field, e.g. result.count.
 * @param jsonData - The json data to read the field from.
 * @returns {*} - The value found for the given field or undefined if not found.
 */
function getFieldFromJsonReport(field: string, jsonData: any): any {
    // Split the given field into individual keys
    const keys = field.split('.');

    // Traverse the JSON object to find the requested field
    let currentKey = jsonData;
    for (const key of keys) {
        if (currentKey && currentKey.hasOwnProperty && typeof currentKey.hasOwnProperty === 'function' && currentKey.hasOwnProperty(key)) {
            currentKey = currentKey[key];
        } else {
            core.warning(`Field "${key}" not found in the JSON report.`);
            return undefined;
        }
    }

    return currentKey;
}

/**
 * Get the JSON report file name from the workspace directory.
 * @returns {string} - The JSON report file name or an empty string if not found.
 */
function getJsonReportFileName(): string {
    const workspaceDir = getWorkspaceDir();
    const filesInWorkspace = shell.ls(workspaceDir);

    for (const fileName of filesInWorkspace) {
        if (/sechub_report.*\.json$/.test(fileName)) {
            return fileName;
        }
    }

    core.warning('JSON report file not found in the workspace directory.');
    return '';
}

/**
 * Reports specific outputs to GitHub Actions based on the SecHub result.
 */
export function reportOutputs(jsonData: any): void {
    core.startGroup('Reporting outputs to GitHub');
    const findings = analyzeFindings(jsonData);
    const trafficLight = getFieldFromJsonReport('trafficLight', jsonData);
    const totalFindings = getFieldFromJsonReport('result.count', jsonData);
    const humanReadableSummary = buildSummary(trafficLight, totalFindings, findings);
    setOutput('scan-trafficlight', trafficLight, 'string');
    setOutput('scan-findings-count', totalFindings, 'number');
    setOutput('scan-findings-high', findings.highCount, 'number');
    setOutput('scan-findings-medium', findings.mediumCount, 'number');
    setOutput('scan-findings-low', findings.lowCount, 'number');
    setOutput('scan-readable-summary', humanReadableSummary, 'string');
    core.endGroup();
}


/**
 * Analyzes the SecHub JSON report and returns the number of findings for each severity, if any found.
 * If no findings were reported, it returns 0 for each severity.
 * @returns {{mediumCount: number, highCount: number, lowCount: number}}
 */
function analyzeFindings(jsonData: any): { mediumCount: number; highCount: number; lowCount: number } {
    const findings = getFieldFromJsonReport('result.findings', jsonData);

    // if no findings were reported.
    if (findings === undefined) {
        core.debug('No findings reported to be categorized.');
        return {
            mediumCount: 0,
            highCount: 0,
            lowCount: 0,
        };
    }

    let mediumCount = 0;
    let highCount = 0;
    let lowCount = 0;
    let unmapped = 0;

    findings.forEach((finding: { severity: string; }) => {
        switch (finding.severity) {
        case 'MEDIUM':
            mediumCount++;
            break;
        case 'HIGH':
            highCount++;
            break;
        case 'LOW':
            lowCount++;
            break;
        default:
            unmapped++;
            break;
        }
    });

    if (unmapped > 0) {
        core.debug('Unmapped findings: ${unmapped}');
    }

    return {
        mediumCount,
        highCount,
        lowCount,
    };
}

/**
 * Builds a human-readable summary of the SecHub scan result using the given traffic light, total findings and findings per severity.
 * @param trafficLight
 * @param totalFindings
 * @param findings
 * @returns String with human-readable description of the scan's outcome, which can be directly useed for a notification mechanism.
 */
function buildSummary(trafficLight: string, totalFindings: number, findings: { mediumCount: number; highCount: number; lowCount: number }): string {
    if (trafficLight === undefined) {
        return 'SecHub scan could not be executed.';
    }

    totalFindings = totalFindings ?? 0;

    const reportResult: string[] = [`SecHub reported traffic light color ${trafficLight}`]

    if (totalFindings === 0) {
        reportResult.push('without findings');
    } else if (totalFindings === 1) {
        reportResult.push(`with ${totalFindings} finding, categorized as follows:`);
    } else {
        reportResult.push(`with ${totalFindings} findings, categorized as follows:`);
    }

    const findingsCategorized = [];
    if (findings.highCount > 0) {
        findingsCategorized.push(`HIGH (${findings.highCount})`);
    }

    if (findings.mediumCount > 0) {
        findingsCategorized.push(`MEDIUM (${findings.mediumCount})`);
    }

    if (findings.lowCount > 0) {
        findingsCategorized.push(`LOW (${findings.lowCount})`);
    }

    const reportResultString = reportResult.join(' ');
    const findingsCategorizedString = findingsCategorized.join(', ');

    return [reportResultString, findingsCategorizedString]
        .filter(x => !!x)
        .join(' ');
}

/**
 * Sets the value of an output variable for the GitHub Action.
 * If the provided value is undefined, it sets a default value based on the data format. 'FAILURE' for strings and '0' for numbers.
 * @param {string} field - The name of the output variable.
 * @param {*} value - The value to set for the output variable.
 * @param {string} dataFormat - The desired data format ('string' or 'number').
 */
function setOutput(field: string, value: any, dataFormat: string) {

    value = value ?? (dataFormat === 'number' ? 0 : 'FAILURE');

    core.debug(`Output ${field} set to ${value}`);
    core.setOutput(field, value.toString()); // Ensure value is converted to a string as GitHub Actions expects output variables to be strings.
}
