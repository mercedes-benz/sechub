// SPDX-License-Identifier: MIT

import * as artifact from '@actions/artifact';
import * as core from '@actions/core';
import * as fs from 'fs';
import { getWorkspaceDir } from './fs-helper';
import { LaunchContext } from './launcher';
import { logExitCode } from './exitcode';
import { getReport } from './sechub-cli';
import { getFieldFromJson } from './json-helper';
import { execFileSync } from 'child_process';
import { sanitize } from './shell-arg-sanitizer';
import { storeOutput } from './output-helper';
import { readFileSync } from './fs-wrapper';

const NEW_LINE_SEPARATOR = '\n';

/**
 * Collect all necessary report data, downloads additional report formats (e.g. 'html') if necessary
 */
export function collectReportData(context: LaunchContext) {
    core.startGroup('Collect report data');

    collectJsonReportData(context);
    downloadOtherReportsThanJson(context);

    core.endGroup();
}

function collectJsonReportData(context: LaunchContext) {

    /* json - already downloaded by client on scan, here we just ensure it exists and fetch the data from the model */
    const fileName = resolveReportNameForScanJob(context);
    const filePath = `${getWorkspaceDir()}/${fileName}`;
    let text = '';
    try {
        core.info('Get Report as json');
        text = readFileSync(filePath, 'utf8');
    } catch (error) {
        core.warning(`Error reading JSON file: ${error}`);
        return undefined;
    }

    const jsonObject = asJsonObject(text);

    /* setup data in context */
    context.secHubReportJsonObject = jsonObject;
    context.secHubReportJsonFileName = fileName;

}


function downloadOtherReportsThanJson(context: LaunchContext) {
    if (context.jobUUID) {
        const jobUUID = context.jobUUID;
        core.debug('JobUUID: ' + jobUUID);

        context.reportFormats.forEach((format) => {
            if (format != 'json') { // json is skipped, because already downloaded
                core.info(`Get Report as ${format}`);
                getReport(jobUUID, format, context);
                logExitCode(context.lastClientExitCode);
            }
        });
    } else {
        core.warning('No job uuid available, cannot download other reports!');
    }
}

/**
 * Parse the SecHub JSON report.
 * @returns {object | undefined} - The parsed JSON report or undefined if not found or there was an error.
 */
function asJsonObject(text: string): object | undefined {
    try {
        const jsonData = JSON.parse(text);
        return jsonData;
    } catch (error) {
        core.warning(`Error parsing JSON file: ${error}`);
        return undefined;
    }
}

/**
 * Uploads all given files as artifact
 * @param name Name for the zip file.
 * @param files All files to include into the artifact.
 */
export async function uploadArtifact(context: LaunchContext, name: string, files: string[]) {
    core.startGroup('Upload artifacts');
    try {
        const artifactClient = artifact.create();
        const artifactName = name;
        const options = { continueOnError: true };

        const rootDirectory = sanitize(context.workspaceFolder);
        core.debug('rootDirectory: ' + rootDirectory);
        if (core.isDebug()) {
            const filesInWorkspace = execFileSync('ls',
                [rootDirectory],
                {
                    encoding: 'utf-8'
                }
            ).split(NEW_LINE_SEPARATOR);

            for (const fileName of filesInWorkspace) {
                core.debug(fileName);
            }
        }
        core.debug('files: ' + files);

        await artifactClient.uploadArtifact(artifactName, files, rootDirectory, options);
        core.debug('artifact upload done');

    } catch (e: unknown) {
        const message = e instanceof Error ? e.message : 'Unknown error';
        core.error(`ERROR while uploading artifacts: ${message}`);
    }
    core.endGroup();
}

/**
 * Get the JSON report file name for the scan job from the workspace directory.
 * @returns {string} - The JSON report file name or an empty string if not found.
 */
function resolveReportNameForScanJob(context: LaunchContext): string {
    const workspaceDir = sanitize(getWorkspaceDir());
    const filesInWorkspace = execFileSync('ls',
        [workspaceDir],
        {
            encoding: 'utf-8'
        }
    ).split(NEW_LINE_SEPARATOR);

    if (!context.jobUUID) {
        core.error('Illegal state: No job uuid resolved - not allowed at this point');
        return '';
    }
    const jobUUID = context.jobUUID;
    const regexString = `sechub_report_.*${jobUUID}.*\\.json$`;

    core.debug(`resolveReportNameForScanJob: regexString='${regexString}'`);
    const regex = new RegExp(regexString);

    for (const fileName of filesInWorkspace) {
        if (regex.test(fileName)) {
            core.debug(`resolveReportNameForScanJob: regexString matched for file: '${fileName}'`);
            return fileName;
        }
    }

    core.warning('JSON report file not found in the workspace directory.');
    return '';
}

/**
 * Reports specific outputs to GitHub Actions based on the SecHub result
 * @returns traffic light
 */
export function reportOutputs(jsonData: any): string {
    core.startGroup('Reporting outputs to GitHub');

    const findings = analyzeFindings(jsonData);
    const trafficLight = getFieldFromJson('trafficLight', jsonData);
    const totalFindings = getFieldFromJson('result.count', jsonData);
    const humanReadableSummary = buildSummary(trafficLight, totalFindings, findings);

    setOutput('scan-trafficlight', trafficLight, 'string');
    setOutput('scan-findings-count', totalFindings, 'number');
    setOutput('scan-findings-high', findings.highCount, 'number');
    setOutput('scan-findings-medium', findings.mediumCount, 'number');
    setOutput('scan-findings-low', findings.lowCount, 'number');
    setOutput('scan-readable-summary', humanReadableSummary, 'string');

    core.endGroup();

    return trafficLight;
}


/**
 * Analyzes the SecHub JSON report and returns the number of findings for each severity, if any found.
 * If no findings were reported, it returns 0 for each severity.
 * @returns {{mediumCount: number, highCount: number, lowCount: number}}
 */
function analyzeFindings(jsonData: any): { mediumCount: number; highCount: number; lowCount: number } {
    const findings = getFieldFromJson('result.findings', jsonData);

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

    let output = `SecHub reported traffic light color ${trafficLight} with`;

    if (totalFindings === 0) {
        output += 'out findings';
    } else if (totalFindings === 1) {
        output += ` ${totalFindings} finding, categorized as follows:`;
    } else {
        output += ` ${totalFindings} findings, categorized as follows:`;
    }

    if (findings.highCount > 0) {
        output += ` HIGH (${findings.highCount}),`;
    }

    if (findings.mediumCount > 0) {
        output += ` MEDIUM (${findings.mediumCount}),`;
    }

    if (findings.lowCount > 0) {
        output += ` LOW (${findings.lowCount}),`;
    }

    output = output.replace(/,$/, '');

    return output;
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
    storeOutput(field, value.toString()); // Ensure value is converted to a string as GitHub Actions expects output variables to be strings.
}
