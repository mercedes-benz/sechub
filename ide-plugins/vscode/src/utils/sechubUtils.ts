import { SecHubReport } from 'sechub-openapi-typescript';

export function loadFromFile(location: string): SecHubReport {

    var fs = require('fs');
    var obj = JSON.parse(fs.readFileSync(location, 'utf8'));

    let report: SecHubReport = obj;

    return report;
}