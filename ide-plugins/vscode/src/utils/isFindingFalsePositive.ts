import { FalsePositiveProjectConfiguration } from 'sechub-openapi-ts-client';

export function getFalsePositivesByIDForJobReport(falsePositiveConfig: FalsePositiveProjectConfiguration, jobUUID: string): number[] {
    const falsePositivesEntrys = falsePositiveConfig.falsePositives || [];

    const falsePositivesFindingIDs: number[] = [];
    falsePositivesEntrys.forEach(entry => {
        if (entry.jobData?.jobUUID === jobUUID) {
            if(entry.jobData.findingId){
                falsePositivesFindingIDs.push(entry.jobData.findingId);
            }
        }
    });

    return falsePositivesFindingIDs;
}