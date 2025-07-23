import { ReportItem, ReportFindingItem } from "../provider/secHubReportTreeDataProvider";
import { SecHubContext } from "../extension";
import { SecHubFinding } from "sechub-openapi-ts-client";

export function openWebScanInInfoview(context: SecHubContext, reportItem: ReportItem): void {

    if (!(reportItem instanceof ReportFindingItem)) {
        console.debug("Invalid report item provided to openWebScan.");
        return;
    }

    const finding = reportItem.sechubFinding;
    if (!finding || !finding.web) {
        console.debug("No web scan information available in the finding.");
        return;
    }

    showInInfoView(context, finding);
}

function showInInfoView(context: SecHubContext, findingNode: SecHubFinding) {
    context.infoTreeProvider.update(findingNode, undefined);
}