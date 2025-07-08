import { SecHubReport } from 'sechub-openapi-ts-client';
import * as fs from 'fs';

export function loadFromFile(location: string): SecHubReport {

    const rawReport = fs.readFileSync(location, 'utf8');
    return JSON.parse(rawReport) as SecHubReport;
}