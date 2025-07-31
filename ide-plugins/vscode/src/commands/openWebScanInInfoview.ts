import { SecHubContext } from "../extension";
import { SecHubFinding } from "sechub-openapi-ts-client";

export function openWebScanInInfoview(context: SecHubContext, finding: SecHubFinding): void {

    if (!finding || !finding.web) {
        console.debug("No web scan information available in the finding.");
        return;
    }

    showInInfoView(context, finding);
}

function showInInfoView(context: SecHubContext, findingNode: SecHubFinding) {
    context.infoTreeProvider.update(findingNode, undefined);
}