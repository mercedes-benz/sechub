// SPDX-License-Identifier: MIT

export enum ReportFormat {
    JSON = 'json',
    HTML = 'html',
    SPDX_JSON = 'spdx-json',
}

/**
 * Checks if the report format is valid ReportFormat and returns it.
 * @param reportFormat report format from the action input
 */
export function checkReportFormat(reportFormat: string | null): ReportFormat | null {
    if (reportFormat === null) {
        return null;
    }
    // check if input.action is a valid FalsePositivesActionType
    if (!Object.values<string>(ReportFormat).includes(reportFormat)) {
        throw new Error(`Invalid report format: ${reportFormat}. Valid values are: ${Object.values(ReportFormat)}`);
    }
    return reportFormat as ReportFormat;
}
