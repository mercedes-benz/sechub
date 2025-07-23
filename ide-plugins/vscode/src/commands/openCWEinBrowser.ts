// SPDX-License-Identifier: MIT
import { ReportFindingItem, ReportItem } from './../provider/secHubReportTreeDataProvider';
import { SecHubContext } from "../extension";

export async function openCWEinBrowser(sechubContext: SecHubContext, reportItem: ReportItem): Promise<void> {
    if (reportItem instanceof ReportFindingItem) {
        sechubContext.findingNodeLinkBuilder.buildCWELinkAndOpenInBrowser(reportItem.sechubFinding);
    }
}