// SPDX-License-Identifier: MIT

import { create } from '@actions/artifact';
import { debug, error, getInput, info, isDebug, setFailed, warning } from '@actions/core';
import * as fs from 'fs';
import * as shell from 'shelljs';
import { getWorkspaceDir } from '../src/fs-helper';
import * as input from '../src/github-input';
import { getFieldFromJson } from '../src/json-helper';
import * as launcher from '../src/launcher';
import { LaunchContext } from '../src/launcher';
import { IntegrationTestContext } from './integrationtest/testframework';
jest.mock('@actions/core');
jest.mock('@actions/artifact');

/*
* This is an integration test suite for github-action "scan".
* As precondition you have to call "01-start.sh" and "03-init_sechub_data.sh"- or call 'sdc -pigh' to do this automatically.
* (Another option is to call `sdc -bgh' to do a full build for github action which does automatically execute the scripts and all integration tests)
*
* After start and prepare scripts have finished you can execute the integration tests via "npm run integration-test"
*
* At the end the servers can be stopped with  "05-stop.sh" (please look into script for an example and more details)
* (This is an explanation to start the tests locally - the github action workflow "github-action-scan.yml" does it in exact same way for CI/CD)
*
*/
const sechub_debug = shell.env['SECHUB_DEBUG'];
const debug_enabled = sechub_debug == 'true';
const client_version = resolveFromEnv('SECHUB_CLIENT_VERSION', 'build');

const integrationTestContext = new IntegrationTestContext();

integrationTestContext.workspaceDir = getWorkspaceDir();

integrationTestContext.serverPort = 8443; // TODO make this configurable - in our start script it is already configurable
integrationTestContext.serverUserId = 'int-test_superadmin'; // TODO make this configurable - in our start script it is already configurable
integrationTestContext.serverApiToken = 'int-test_superadmin-pwd'; // TODO make this configurable - in our start script it is already configurable

integrationTestContext.finish();

const mockedInputMap = new Map();

let mockedUploadFunction: jest.Mock;

beforeEach(() => {
    
    shell.echo('----------------------------------------------------------------------------------------------------------------------------------');
    shell.echo('Start integration test: ' + expect.getState().currentTestName);
    shell.echo('- client= ' + client_version);
    shell.echo('----------------------------------------------------------------------------------------------------------------------------------');

    jest.resetAllMocks();

    (getInput as jest.Mock).mockImplementation((name) => {
        return mockedInputMap.get(name);
    });
    if (debug_enabled) {
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

    mockedUploadFunction = jest.fn();
    (create as jest.Mock).mockImplementation(() => {
        return {
            'uploadArtifact': mockedUploadFunction,
        };
    });
});

function resolveFromEnv(name: string, defaultValue: string): string {
    return shell.env[name] || defaultValue;
}

function initInputMap() {
    mockedInputMap.clear();
    mockedInputMap.set(input.PARAM_SECHUB_SERVER_URL, `https://localhost:${integrationTestContext.serverPort}`);
    mockedInputMap.set(input.PARAM_SECHUB_USER, `${integrationTestContext.serverUserId}`);
    mockedInputMap.set(input.PARAM_API_TOKEN, `${integrationTestContext.serverApiToken}`);
    mockedInputMap.set(input.PARAM_CLIENT_VERSION, client_version); // integration tests can simulate the parameter with env variable - otherwise default
    mockedInputMap.set(input.PARAM_CLIENT_BUILD_FOLDER, resolveFromEnv('SECHUB_CLIENT_BUILD_FOLDER', '../../sechub-cli/build/go')); // integration tests can simulate the parameter with env variable - otherwise default
    mockedInputMap.set(input.PARAM_ADD_SCM_HISTORY, 'false');
    mockedInputMap.set(input.PARAM_REPORT_FORMATS, 'json');
    mockedInputMap.set(input.PARAM_TRUST_ALL, 'true'); // self signed certificate in test...
    mockedInputMap.set(input.PARAM_FAIL_JOB_ON_FINDING, 'true');
}

describe('integrationtest codescan generated config', () => {
    test('codescan green', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_INCLUDED_FOLDERS, '__test__/integrationtest/test-sources');
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-1');

        /* execute */
        const result = await launcher.launch();

        /* test */
        assertLastClientExitCode(result, 0);
        assertTrafficLight(result, 'GREEN');
        assertActionIsNotMarkedAsFailed();
        assertJsonReportContains(result, 'result-green');
        assertUploadDone();

    });
    test('codescan yellow', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_INCLUDED_FOLDERS, '__test__/integrationtest/test-sources');
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-2');

        /* execute */
        const result = await launcher.launch();

        /* test */
        assertLastClientExitCode(result, 0);
        assertActionIsNotMarkedAsFailed();
        assertTrafficLight(result, 'YELLOW');
        assertJsonReportContains(result, 'result-yellow');
        assertUploadDone();
    });

    test('codescan red', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_INCLUDED_FOLDERS, '__test__/integrationtest/test-sources');
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-3');

        /* execute */
        const result = await launcher.launch();

        /* test */
        assertLastClientExitCode(result, 1); // exit code 1, because RED
        assertTrafficLight(result, 'RED');
        assertActionIsMarkedAsFailed();
        assertJsonReportContains(result, 'result-red');
        assertUploadDone();
    });
    test('codescan red - fail-job-with-findings=false', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_INCLUDED_FOLDERS, '__test__/integrationtest/test-sources');
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-3');
        mockedInputMap.set(input.PARAM_FAIL_JOB_ON_FINDING, 'false');

        /* execute */
        const result = await launcher.launch();

        /* test */
        assertLastClientExitCode(result, 1);
        assertTrafficLight(result, 'RED');
        assertActionIsNotMarkedAsFailed(); // important: exit code 1 but action is NOT marked as failed because fail-job-with-findings=false
        assertJsonReportContains(result, 'result-red');
        assertUploadDone();
    });

});

describe('integrationtest secretscan generated config', () => {
    test('secretscan yellow, json only', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_INCLUDED_FOLDERS, '__test__/integrationtest/test-sources');
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-5');
        mockedInputMap.set(input.PARAM_SCAN_TYPES, 'secretscan');

        /* execute */
        const result = await launcher.launch();

        /* test */
        assertTrafficLight(result, 'YELLOW');
        assertLastClientExitCode(result, 0);
        assertActionIsNotMarkedAsFailed();
        assertJsonReportContains(result, 'generic-api-key has detected secret for file UnSAFE_Bank/Backend/docker-compose.yml');
        assertUploadDone();

    });
    test('secretscan yellow, html only', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_INCLUDED_FOLDERS, '__test__/integrationtest/test-sources');
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-5');
        mockedInputMap.set(input.PARAM_SCAN_TYPES, 'secretScan');
        mockedInputMap.set(input.PARAM_REPORT_FORMATS, 'html');

        /* execute */
        const result = await launcher.launch();

        /* test */
        assertTrafficLight(result, 'YELLOW');
        assertLastClientExitCode(result, 0);
        assertActionIsNotMarkedAsFailed();
        assertJsonReportContains(result, 'generic-api-key has detected secret for file UnSAFE_Bank/Backend/docker-compose.yml');
        assertUploadDone();

        loadHTMLReportAndAssertItContains(result, 'generic-api-key has detected secret for file UnSAFE_Bank/Backend/docker-compose.yml');

    });
    test('secretscan yellow, json,html', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_INCLUDED_FOLDERS, '__test__/integrationtest/test-sources');
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-5');
        mockedInputMap.set(input.PARAM_SCAN_TYPES, 'secretScan');
        mockedInputMap.set(input.PARAM_REPORT_FORMATS, 'json,html');

        /* execute */
        const result = await launcher.launch();

        /* test */
        assertTrafficLight(result, 'YELLOW');
        assertLastClientExitCode(result, 0);
        assertActionIsNotMarkedAsFailed();
        assertJsonReportContains(result, 'generic-api-key has detected secret for file UnSAFE_Bank/Backend/docker-compose.yml');
        assertUploadDone();

        loadHTMLReportAndAssertItContains(result, 'generic-api-key has detected secret for file UnSAFE_Bank/Backend/docker-compose.yml');

    });

});

describe('integrationtest licensescan generated config', () => {
    test('licensescan green, spdx-json', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_INCLUDED_FOLDERS, '__test__/integrationtest/test-sources');
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-6');
        mockedInputMap.set(input.PARAM_SCAN_TYPES, 'licensescan');
        mockedInputMap.set(input.PARAM_REPORT_FORMATS, 'spdx-json');

        /* execute */
        const result = await launcher.launch();

        /* test */
        assertTrafficLight(result, 'GREEN');
        assertLastClientExitCode(result, 0);
        assertActionIsNotMarkedAsFailed();
        assertJsonReportContains(result, 'findings'); // findings in json available - but green, because only licensescan
        assertUploadDone();

        loadSpdxJsonReportAndAssertItContains(result, 'LGPL');
    });

});

describe('integrationtest non-generated config', () => {
    test('config-path defined, but file not found', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-3');
        mockedInputMap.set(input.PARAM_CONFIG_PATH, 'unknown/not-existing-config.json');

        /* execute + test */
        await launcher.launch().catch(error => console.log(`Error handled : ${error}`));

    });

    test('config-path defined, file available, web scan with red trafficlight', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-4'); // we override here the config definition (which is explicit wrong in config file)

        const pwd = shell.pwd();
        const configDir = `${pwd}/__test__/integrationtest/test-config`;

        mockedInputMap.set(input.PARAM_CONFIG_PATH, `${configDir}/sechub-config-webscan-project-4.json`);

        /* execute */
        const result = await launcher.launch();

        /* test */
        assertLastClientExitCode(result, 1);
        assertActionIsMarkedAsFailed();
        assertTrafficLight(result, 'RED');
        assertJsonReportContains(result, 'XSS attackable parameter output: </p><script>alert(1)');
        assertUploadDone();

    });
    test('config-path defined, project name only in config file, file available, web scan with red trafficlight', async () => {

        /* prepare */
        initInputMap();

        const pwd = shell.pwd();
        const configDir = `${pwd}/__test__/integrationtest/test-config`;

        mockedInputMap.set(input.PARAM_CONFIG_PATH, `${configDir}/sechub-config-webscan-project-4-with-correct-project-name-inside.json`);

        /* execute */
        const result = await launcher.launch();

        /* test */
        assertLastClientExitCode(result, 1);
        assertActionIsMarkedAsFailed();
        assertTrafficLight(result, 'RED');
        assertJsonReportContains(result, 'XSS attackable parameter output: </p><script>alert(1)');
        assertUploadDone();

    });

});

describe('integrationtest define-false-positives generated config', () => {

    test('codescan first red then result is green after define-false-positives is executed', async () => {

        /* prepare */
        initInputMap();
        mockedInputMap.set(input.PARAM_INCLUDED_FOLDERS, '__test__/integrationtest/test-sources');
        mockedInputMap.set(input.PARAM_PROJECT_NAME, 'test-project-7');

        /* execute */
        const result1 = await launcher.launch();

        /* test */
        assertTrafficLight(result1, 'RED');
        assertLastClientExitCode(result1, 1);
        assertUploadDone();

        /* prepare 2 */
        const defineFalsePositivesFile = createDefineFalsePositivesFile(result1);
        mockedInputMap.set(input.PARAM_DEFINE_FALSE_POSITIVES, defineFalsePositivesFile);

        /* execute 2 */
        const result2 = await launcher.launch();

        /* test 2 */
        assertLastClientExitCode(result2, 0);
        assertTrafficLight(result2, 'GREEN');
        assertUploadDone();

        /* clean up */
        deleteFile(defineFalsePositivesFile);
    });

});

function assertActionIsMarkedAsFailed() {
    expect(setFailed).toHaveBeenCalledTimes(1);
}

function assertActionIsNotMarkedAsFailed() {
    expect(setFailed).toHaveBeenCalledTimes(0);
}

function assertLastClientExitCode(context: LaunchContext, exitCode: number) {
    expect(context.lastClientExitCode).toEqual(exitCode);
}

function assertTrafficLight(context: LaunchContext, trafficLight: string) {
    const found = getFieldFromJson('trafficLight', context.secHubReportJsonObject);
    expect(found).toEqual(trafficLight);

}

function assertJsonReportContains(context: LaunchContext, textPart: string) {
    const text = JSON.stringify(context.secHubReportJsonObject);
    expect(text).toContain(textPart);
}

function assertUploadDone() {
    expect(mockedUploadFunction).toHaveBeenCalled();
}

function loadHTMLReportAndAssertItContains(context: LaunchContext, textPart: string) {

    const fileName = context.secHubReportJsonFileName.replace('.json', '.html');
    const htmlPath = `./${fileName}`;
    if (context.debug) {
        const pwd = shell.pwd();
        shell.echo('current dir: ' + pwd);
        shell.echo('htmlPath: ' + htmlPath);
    }
    const html = fs.readFileSync(htmlPath, 'utf8');

    expect(html).toContain(textPart);
}

function loadSpdxJsonReportAndAssertItContains(context: LaunchContext, textPart: string) {

    const fileName = context.secHubReportJsonFileName.replace('.json', '.spdx.json');
    const spdxJsonPath = `./${fileName}`;
    if (context.debug) {
        const pwd = shell.pwd();
        shell.echo('current dir: ' + pwd);
        shell.echo('spdxJsonPath: ' + spdxJsonPath);
    }
    const spdxJson = fs.readFileSync(spdxJsonPath, 'utf8');

    expect(spdxJson).toContain(textPart);
}

function createDefineFalsePositivesFile(context: LaunchContext): string {
    const defineFalsePositivesJson = `{"apiVersion":"1.0","type":"falsePositiveDataList","jobData":[{"jobUUID":"${context.jobUUID}","findingId":1}]}`
    const fileName = "defineFalsePositivesFile.json";
    const filePath = `./${fileName}`;
    fs.writeFileSync(filePath, defineFalsePositivesJson);
    return filePath;
}

function deleteFile(file: string) {
    if (fs.existsSync(file)) {
        fs.unlinkSync(file);
    }
}