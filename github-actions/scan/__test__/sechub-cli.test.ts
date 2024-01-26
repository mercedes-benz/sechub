// SPDX-License-Identifier: MIT

import * as cli from '../src/sechub-cli';
jest.mock('@actions/core');

describe('sechub-cli', function() {
    it('extractJobUUID returns job uuid from sechub client output snippet', function () {
        
        const output = `
        WARNING: Configured to trust all - means unknown service certificate is accepted. Don't use this in production!
        2024-03-08 13:58:18 (+01:00) Zipping folder: __test__/integrationtest/test-sources (/home/xyzgithub-actions/scan/__test__/integrationtest/test-sources)
        2024-03-08 13:58:18 (+01:00) Creating new SecHub job: 6880e518-88db-406a-bc67-851933e7e5b7
        2024-03-08 13:58:18 (+01:00) Uploading source zip file
        2024-03-08 13:58:18 (+01:00) Approve sechub job
        2024-03-08 13:58:18 (+01:00) Waiting for job 6880e518-88db-406a-bc67-851933e7e5b7 to be done
                                     .
        2024-03-08 13:58:20 (+01:00) Fetching result (format=json) for job 6880e518-88db-406a-bc67-851933e7e5b7
        other
        `;

        /* execute */
        const jobUUID= cli.extractJobUUID(output);

        /* test */
        expect(jobUUID).toEqual('6880e518-88db-406a-bc67-851933e7e5b7');
    });
    it('extractJobUUID returns job uuid from string with "job: xxxx"', function () {
        
        const output = `
        The uuid for job:1234
        can be extracted
        `;

        /* execute */
        const jobUUID= cli.extractJobUUID(output);

        /* test */
        expect(jobUUID).toEqual('1234');
    });

    it('extractJobUUID returns empty string when no job id is available', function () {
        
        const output = `
        WARNING: Configured to trust all - means unknown service certificate is accepted. Don't use this in production!
        2024-03-08 13:58:18 (+01:00) Zipping folder: __test__/integrationtest/test-sources (/home/xyzgithub-actions/scan/__test__/integrationtest/test-sources)
        2024-03-08 13:58:18 (+01:00) Uploading source zip file
        2024-03-08 13:58:18 (+01:00) Approve sechub job
        
        `;

        /* execute */
        const jobUUID= cli.extractJobUUID(output);

        /* test */
        expect(jobUUID).toEqual('');
    });

});