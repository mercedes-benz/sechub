// SPDX-License-Identifier: MIT
import { FindingNodeReportItem, ReportItem } from './../provider/secHubReportTreeDataProvider';
import { SecHubContext } from "../extension";

export async function openCWEinBrowser(sechubContext: SecHubContext, reportItem: ReportItem): Promise<void> {
    if (reportItem instanceof FindingNodeReportItem) {
        sechubContext.findingNodeLinkBuilder.buildCWELinkAndOpenInBrowser(reportItem.sechubFinding);
    }
}