// SPDX-License-Identifier: MIT

import {extractJobUUID, getReport, scan} from '../src/sechub-cli';
import {execFileSync} from 'child_process';
import {sanitize} from "../src/shell-arg-sanitizer";

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

jest.mock('child_process', () => ({
    execFileSync: jest.fn(() => output)
}));

jest.mock('../src/shell-arg-sanitizer');

beforeEach(() => {
    jest.clearAllMocks();
});

describe('scan', function() {

    it('sanitizes shell arguments', () => {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            configFileLocation: '/path/to/config.json',
            workspaceFolder: '/path/to/workspace',
            inputData: {
                addScmHistory: 'false'
            }
        };
        (sanitize as jest.Mock).mockImplementation((arg) => {
            return arg;
        });

        /* execute */
        scan(context);

        /* test */
        expect(sanitize).toBeCalledTimes(4);
        expect(sanitize).toBeCalledWith('/path/to/sechub-cli');
        expect(sanitize).toBeCalledWith('/path/to/config.json');
        expect(sanitize).toBeCalledWith('/path/to/workspace');
        expect(sanitize).toBeCalledWith('');
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
        expect(execFileSync).toBeCalledTimes(1);
        expect(execFileSync)
            .toBeCalledWith(
                '/path/to/sechub-cli', ['-configfile', '/path/to/config.json', '-output', '/path/to/workspace', '-addScmHistory', 'scan'],
                {
                    env: {
                        SECHUB_SERVER: process.env.SECHUB_SERVER,
                        SECHUB_USERID: process.env.SECHUB_USERID,
                        SECHUB_APITOKEN: process.env.SECHUB_APITOKEN,
                        SECHUB_PROJECT: process.env.SECHUB_PROJECT,
                        SECHUB_DEBUG: process.env.SECHUB_DEBUG,
                        SECHUB_TRUSTALL: process.env.SECHUB_TRUSTALL,
                    },
                    encoding: 'utf-8'
                }
            );
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
        expect(execFileSync).toBeCalledTimes(1);
        expect(execFileSync)
            .toBeCalledWith(
                '/path/to/sechub-cli', ['-configfile', '/path/to/config.json', '-output', '/path/to/workspace', '', 'scan'],
                {
                    env: {
                        SECHUB_SERVER: process.env.SECHUB_SERVER,
                        SECHUB_USERID: process.env.SECHUB_USERID,
                        SECHUB_APITOKEN: process.env.SECHUB_APITOKEN,
                        SECHUB_PROJECT: process.env.SECHUB_PROJECT,
                        SECHUB_DEBUG: process.env.SECHUB_DEBUG,
                        SECHUB_TRUSTALL: process.env.SECHUB_TRUSTALL,
                    },
                    encoding: 'utf-8'
                }
            );
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
        const jobUUID= extractJobUUID(output);

        /* test */
        expect(jobUUID).toEqual('6880e518-88db-406a-bc67-851933e7e5b7');
    });

    it('returns job uuid from string with "job: xxxx"', function () {

        const output = `
        The uuid for job:1234
        can be extracted
        `;

        /* execute */
        const jobUUID= extractJobUUID(output);

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
        const jobUUID= extractJobUUID(output);

        /* test */
        expect(jobUUID).toEqual('');
    });
});

describe('getReport', function () {

    it('sanitizes shell arguments', () => {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            projectName: 'project-name',
        };
        (sanitize as jest.Mock).mockImplementation((arg) => {
            return arg;
        });

        /* execute */
        getReport('job-uuid', 'json', context);

        /* test */
        expect(sanitize).toBeCalledTimes(4);
        expect(sanitize).toBeCalledWith('/path/to/sechub-cli');
        expect(sanitize).toBeCalledWith('job-uuid');
        expect(sanitize).toBeCalledWith('project-name');
        expect(sanitize).toBeCalledWith('json');
    });

});