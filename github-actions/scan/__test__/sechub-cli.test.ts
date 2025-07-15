// SPDX-License-Identifier: MIT

import { exec } from 'shelljs';
import { scan, extractJobUUID, defineFalsePositives, getReport, spawnAndWait } from '../src/sechub-cli';
import { sanitize } from '../src/shell-arg-sanitizer';
import { execFileSync, spawn } from 'child_process';
import { EventEmitter } from 'events';

jest.mock('@actions/core');

jest.mock('../src/shell-arg-sanitizer', () => ({
    sanitize: jest.fn((arg) => arg),
}));

jest.mock('../src/fs-wrapper', () => ({
    readFileSync: jest.fn(() => output),
    openSync: jest.fn(() => 4711),
    mkdtempSync: jest.fn(() => '/temp-mocked'),
    closeSync: jest.fn(),
}));

const output = `
        WARNING: Configured to trust all - means unknown service certificate is accepted. Don't use this in production!
        2024-03-08 13:58:18 (+01:00) Creating new SecHub job: 6880e518-88db-406a-bc67-851933e7e5b7
        other
        `;

jest.mock('child_process', () => ({
    spawn: jest.fn(),
    execFileSync: jest.fn(() => output),
}));

afterEach(() => {
    jest.clearAllMocks();
});

afterAll(() => {
    jest.resetAllMocks();
});

function mockSpawn(exitCode = 0, signal: NodeJS.Signals | null = null) {
    const events = new EventEmitter();

    const childMock = {
        kill: jest.fn(),
        on: events.on.bind(events),
        once: events.once.bind(events),
        emit: events.emit.bind(events),
    } as any;

    (spawn as jest.Mock).mockReturnValue(childMock);

    // Simulate the process exiting after short delay
    setTimeout(() => {
        events.emit('exit', exitCode, signal);
    }, 5);

    return childMock;
}


describe('scan', () => {

    it('sanitizes shell arguments', async () => {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            configFileLocation: '/path/to/config.json',
            workspaceFolder: '/path/to/workspace',
            inputData: { addScmHistory: 'false' }
        };

        mockSpawn();

        /* execute */
        await scan(context);

        /* test */
        expect(sanitize).toBeCalledTimes(4);
        expect(sanitize).toHaveBeenCalledWith('/path/to/sechub-cli');
        expect(sanitize).toHaveBeenCalledWith('/path/to/config.json');
        expect(sanitize).toHaveBeenCalledWith('/path/to/workspace');
        expect(sanitize).toHaveBeenCalledWith('');
    });

    it('returns correct job id', async () => {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            configFileLocation: '/path/to/config.json',
            workspaceFolder: '/path/to/workspace',
            inputData: { addScmHistory: 'false' }
        };

        mockSpawn();

        /* execute */
        await scan(context);

        /* test */
        expect(context.lastClientExitCode).toBe(0);
        expect(context.jobUUID).toBe('6880e518-88db-406a-bc67-851933e7e5b7');
    });

    it('with addScmHistory=true includes the flag in arguments', async () => {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            configFileLocation: '/path/to/config.json',
            workspaceFolder: '/path/to/workspace',
            inputData: { addScmHistory: 'true' }
        };

        const child = mockSpawn();

        /* execute */
        await scan(context);

        /* test */
        const spawnArgs = (spawn as jest.Mock).mock.calls[0];

        expect(spawnArgs[0]).toBe('/path/to/sechub-cli');
        expect(spawnArgs[1]).toEqual([
            '-configfile', '/path/to/config.json',
            '-output', '/path/to/workspace',
            '-addScmHistory',
            'scan',
        ]);
    });

    it('with addScmHistory=false does NOT include the flag', async () => {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            configFileLocation: '/path/to/config.json',
            workspaceFolder: '/path/to/workspace',
            inputData: { addScmHistory: 'false' }
        };

        mockSpawn();

        /* execute */
        await scan(context);

        /* test */
        const spawnArgs = (spawn as jest.Mock).mock.calls[0];

        expect(spawnArgs[1]).toEqual([
            '-configfile', '/path/to/config.json',
            '-output', '/path/to/workspace',
            'scan',
        ]);
    });

    it('handles scan failure (non-zero exit)', async () => {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            configFileLocation: '/path/to/config.json',
            workspaceFolder: '/path/to/workspace',
            inputData: { addScmHistory: 'false' }
        };

        const child = mockSpawn(1);

        /* execute */
        await scan(context);

        /* test */
        expect(context.lastClientExitCode).toBe(1);
    });

});


describe('extractJobUUID', function () {

    it('returns job uuid from sechub client output snippet', function () {
        /* prepare */
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
        /* prepare */
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
        /* prepare */
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

describe('defineFalsePositives', function () {

    it('sanitizes shell arguments', () => {
        /* prepare */
        const context: any = {
            clientExecutablePath: '/path/to/sechub-cli',
            projectName: 'project-name',
            defineFalsePositivesFile: '/path/to/define-false-positive-file'
        };
        (sanitize as jest.Mock).mockImplementation((arg) => {
            return arg;
        });

        /* execute */
        defineFalsePositives(context);

        /* test */
        expect(sanitize).toBeCalledTimes(3);
        expect(sanitize).toBeCalledWith('/path/to/sechub-cli');
        expect(sanitize).toBeCalledWith('project-name');
        expect(sanitize).toBeCalledWith('/path/to/define-false-positive-file');
    });


    it.each([null, undefined, ''])('context.lastClientExitCode is 0 when defineFalsePositivesFile is %s', (file) => {
        /* prepare */
        const context: any = {
            defineFalsePositivesFile: file
        };
        (sanitize as jest.Mock).mockImplementation((arg) => {
            return arg;
        });

        /* execute */
        defineFalsePositives(context);

        /* test */
        expect(sanitize).toBeCalledTimes(0);
        expect(context.lastClientExitCode).toBe(0);
        expect(context.defineFalsePositivesFile).toBe(file);
    });
});

describe('spawnAndWait signal handling', () => {    
    let mockChildProcess: any;

    beforeEach(() => {
        // Create a mock child process object
        mockChildProcess = {
            kill: jest.fn(),
            on: jest.fn(),
        };

        // Set the mock return value for spawn
        (spawn as jest.Mock).mockReturnValue(mockChildProcess);
    });

    afterEach(() => {
        jest.resetAllMocks();
    });

    it('should forward SIGINT signal to child process', async () => {
        /* prepare */
        const command = 'dummyCommand';
        const args = ['dummyArg'];
        const options = {};

        /* execute */
        const promise = spawnAndWait(command, args, options);

        // Emit SIGINT signal
        expect(process.emit('SIGINT', 'SIGINT'));

        /* test */
        // Verify that the signal was forwarded
        expect(mockChildProcess.kill).toHaveBeenCalledWith('SIGINT');

        // Simulate child process exit
        mockChildProcess.on.mock.calls[0][1](0, null);

        await expect(promise).resolves.toBe(0);
    });

    it('should forward SIGTERM signal to child process', async () => {
        /* prepare */
        const command = 'dummyCommand';
        const args = ['dummyArg'];
        const options = {};

        /* execute */
        const promise = spawnAndWait(command, args, options);

        // Emit SIGTERM signal
        expect(process.emit('SIGTERM', 'SIGTERM'));

        /* test */
        // Verify that the signal was forwarded
        expect(mockChildProcess.kill).toHaveBeenCalledWith('SIGTERM');

        // Simulate child process exit
        mockChildProcess.on.mock.calls[0][1](0, null);

        await expect(promise).resolves.toBe(0);
    });

    it('should reject promise if child process is terminated by signal', async () => {
        /* prepare */
        const command = 'dummyCommand';
        const args = ['dummyArg'];
        const options = {};

        /* execute */
        const promise = spawnAndWait(command, args, options);

        // Emit SIGTERM signal
        expect(process.emit('SIGTERM', 'SIGTERM'));

        /* test */
        // Verify that the signal was forwarded
        expect(mockChildProcess.kill).toHaveBeenCalledWith('SIGTERM');

        // Simulate child process exit with signal
        mockChildProcess.on.mock.calls[0][1](null, 'SIGTERM');

        await expect(promise).rejects.toThrow('Process terminated by signal: SIGTERM');
    });

    it('should reject promise if child process encounters an error', async () => {
        /* prepare */
        const command = 'dummyCommand';
        const args = ['dummyArg'];
        const options = {};

        /* execute + test */
        const promise = spawnAndWait(command, args, options);

        const error = new Error('Child process error');
        mockChildProcess.on.mock.calls[1][1](error);

        await expect(promise).rejects.toThrow('Child process error');
    });
});