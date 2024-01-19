// SPDX-License-Identifier: MIT

import * as launcher from '../src/launcher';
import * as shell from 'shelljs';
import { isDebug, debug, getInput } from '@actions/core';
import { info } from '@actions/core';
import { error } from '@actions/core';
import { warning } from '@actions/core';
import * as input from '../src/input';
import { LaunchContext } from '../src/launcher';
import { create } from '@actions/artifact';
jest.mock('@actions/core');
jest.mock('@actions/artifact');
/*
 * This is an integration test suite for github-action "scan".
 * As precondition you have to start a local sechub server in integration test mode and execute "setup-integrationtest.sh".
 * After this is done, you can execute these tests.
*/

const sechub_debug = shell.env['SECHUB_DEBUG'];
const debug_enabled = sechub_debug=='true';

beforeEach(() => {
    jest.resetAllMocks();

    (getInput as jest.Mock).mockImplementation((name) => {
        return mockedInputMap.get(name);
    });
    if (debug_enabled){
        (debug as jest.Mock).mockImplementation((message) => {
            console.debug('gh-debug: %s', message);
        });
        (isDebug as jest.Mock).mockImplementation(() => {
            return true;
        });
    }
    

    (info as jest.Mock).mockImplementation((message) => {
        console.log('gh-info: %s', message);
    });
    (warning as jest.Mock).mockImplementation((message) => {
        console.log('gh-warning: %s', message);
    });
    (error as jest.Mock).mockImplementation((message) => {
        console.log('gh-error: %s', message);
    });

    (create as jest.Mock).mockName('artifactClient');
    (create as jest.Mock).mockImplementation(() => {
        return {
            'uploadArtifact' : jest.fn(),
        };
    });
});


const mockedInputMap = new Map();

function initInputMap() {
    mockedInputMap.clear();

    mockedInputMap.set(input.PARAM_SECHUB_SERVER_URL, 'https://localhost:8443');
    mockedInputMap.set(input.PARAM_SECHUB_USER, 'int-test_superadmin');
    mockedInputMap.set(input.PARAM_API_TOKEN, 'int-test_superadmin-pwd');
    mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project');

    mockedInputMap.set(input.PARAM_CLIENT_VERSION, '1.2.0');

    mockedInputMap.set(input.PARAM_REPORT_FORMATS, 'json');
    mockedInputMap.set(input.PARAM_TRUST_ALL, 'true'); // self signed certificate in test...
}


describe('integrationtest', function () {
    test('integrationtest 1', function () {

        /* prepare */
        initInputMap();

        /* execute */
        const launchPromise = launcher.launch();

        /* test */
        assertExitCode(launchPromise, 0);

    });

});

async function assertExitCode(launchPromise: Promise<LaunchContext>, exitCode: number) {
    const context = await launchPromise;

    expect(context.lastClientExitCode).toEqual(exitCode);
}