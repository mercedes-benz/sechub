// SPDX-License-Identifier: MIT

import * as cli from '../src/sechub-cli';
import { scan } from '../src/sechub-cli';
import * as shell from 'shelljs';

import * as shellCmdSanitizer from "../src/shell-cmd-sanitizer";

jest.mock('@actions/core');

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

jest.mock('shelljs', () => ({
    exec: jest.fn(() => ({
        code: 0,
        stdout: output,
        stderr: ''
    }))
}));

beforeEach(() => {
    jest.clearAllMocks();
});

describe('scan', function() {

    it('sanitizes shell command', () => {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            configFileLocation: '/path/to/config.json',
            workspaceFolder: '/path/to/workspace',
            inputData: {
                addScmHistory: 'false'
            }
        };
        const spySanitizeShellCommand = jest.spyOn(shellCmdSanitizer, 'sanitize');

        /* execute */
        scan(context);

        /* test */
        expect(spySanitizeShellCommand).toBeCalledTimes(1);
    });

    it('return correct job id', function () {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            configFileLocation: '/path/to/config.json',
            workspaceFolder: '/path/to/workspace',
            inputData: {
                addScmHistory: 'false'
            }
        };

        /* execute */
        scan(context);

        /* test */
        expect(context.lastClientExitCode).toEqual(0);
        expect(context.jobUUID).toEqual('6880e518-88db-406a-bc67-851933e7e5b7');
    });

    it('with addScmHistory flag true - executes SecHub client with -addScmHistory', function () {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            configFileLocation: '/path/to/config.json',
            workspaceFolder: '/path/to/workspace',
            inputData: {
                addScmHistory: 'true'
            }
        };

        /* execute */
        scan(context);

        /* test */
        expect(shell.exec).toBeCalledTimes(1);
        expect(shell.exec).toBeCalledWith('/path/to/sechub-cli -configfile /path/to/config.json -output /path/to/workspace -addScmHistory scan');
    });

    it('with addScmHistory flag false - executes SecHub client without -addScmHistory', function () {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            configFileLocation: '/path/to/config.json',
            workspaceFolder: '/path/to/workspace',
            inputData: {
                addScmHistory: 'false'
            }
        };

        /* execute */
        scan(context);

        /* test */
        expect(shell.exec).toBeCalledTimes(1);
        expect(shell.exec).toBeCalledWith('/path/to/sechub-cli -configfile /path/to/config.json -output /path/to/workspace scan');
    });

});

describe('extractJobUUID', function () {

    it('returns job uuid from sechub client output snippet', function () {

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

    it('returns job uuid from string with "job: xxxx"', function () {

        const output = `
        The uuid for job:1234
        can be extracted
        `;

        /* execute */
        const jobUUID= cli.extractJobUUID(output);

        /* test */
        expect(jobUUID).toEqual('1234');
    });

    it('returns empty string when no job id is available', function () {

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

describe('getReport', function () {

    it('sanitizes shell command', () => {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            projectName: 'project-name',
        };
        const spySanitizeShellCommand = jest.spyOn(shellCmdSanitizer, 'sanitize');

        /* execute */
        cli.getReport('job-uuid', 'json', context);

        /* test */
        expect(spySanitizeShellCommand).toBeCalledTimes(1);
    });

});