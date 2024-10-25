// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import { collectReportData, reportOutputs } from '../src/post-scan';
import { getReport } from '../src/sechub-cli';
import { LAUNCHER_CONTEXT_DEFAULTS } from '../src/launcher';
import { readFileSync, appendFileSync } from 'fs-extra';
import * as os from "node:os";

jest.mock('@actions/core');
const mockedCore = core as jest.Mocked<typeof core>;

jest.mock('../src/sechub-cli');
const mockedGetReport = getReport as jest.MockedFunction<typeof getReport>;

jest.mock('fs-extra', () => ({
    writeFile: jest.fn(),
    readFileSync: jest.fn(),
    appendFileSync: jest.fn()
}));

const mockedReadFileSync = readFileSync as jest.MockedFunction<typeof readFileSync>;
const mockedAppendFileSync = appendFileSync as jest.MockedFunction<typeof appendFileSync>;
process.env['GITHUB_OUTPUT'] = 'dummy-file';

describe('collectReportData', function () {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('format empty - logs called, getReport not called', function () {

        /* prepare */
        const testContext = Object.create(LAUNCHER_CONTEXT_DEFAULTS);
        testContext.reportFormats= [];

        /* execute */
        collectReportData(testContext);

        /* test */
        expect(mockedCore.info).toHaveBeenCalledTimes(1);
        expect(mockedGetReport).toHaveBeenCalledTimes(0);
    });

    it('format "json" - logs called 1 time , getReport never called', function () {
        /* prepare */
        const testContext = Object.create(LAUNCHER_CONTEXT_DEFAULTS);
        testContext.reportFormats= ['json'];

        /* execute */
        collectReportData(testContext);

        /* test */
        expect(mockedCore.info).toHaveBeenCalledTimes(1);
        expect(mockedGetReport).toHaveBeenCalledTimes(0);
    });

    it('format "html" - logs called 2 times, getReport called 1 time', function () {

        /* prepare */
        const testContext = Object.create(LAUNCHER_CONTEXT_DEFAULTS);
        testContext.reportFormats= ['json','html'];
        testContext.jobUUID=1234; // necessary for download

        collectReportData(testContext);

        /* test */
        expect(mockedCore.info).toHaveBeenCalledTimes(2);
        expect(mockedGetReport).toHaveBeenCalledTimes(1);
    });

    it('format "json,html" - logs called 2 times , getReport called 1 time', function () {

        /* prepare */
        const testContext = Object.create(LAUNCHER_CONTEXT_DEFAULTS);
        testContext.reportFormats= ['json','html'];
        testContext.jobUUID=1234; // necessary for download

        /* execute */
        collectReportData(testContext);

        /* test */
        expect(mockedCore.info).toHaveBeenCalledTimes(2);
        expect(mockedGetReport).toHaveBeenCalledTimes(1);
    });

    it('calls getReport with parameters (except json) and report json object is as expected', function () {
        // eslint-disable-next-line @typescript-eslint/no-var-requires
        /* prepare */
        const testContext = Object.create(LAUNCHER_CONTEXT_DEFAULTS);
        testContext.reportFormats= ['json','html','xyz','bla'];
        testContext.jobUUID=1234; // necessary for download
        
        mockedReadFileSync.mockReturnValue('{"test": "test"}')
        const sampleJson = {'test': 'test'};

        /* execute */
        collectReportData(testContext);

        /* test */
        expect(mockedCore.info).toHaveBeenCalledTimes(4); // "json, html, xyz, bla" - 4 times logged (valid format check is not done here)
        expect(mockedGetReport).toHaveBeenCalledTimes(3); // we fetch not json via getReport again (already done before), so only "html, xyz, bla" used
        
        expect(testContext.secHubReportJsonObject).toEqual(sampleJson); // json object is available

    });
});

describe('reportOutputs', function () {
    /* prepare */
    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('calls set github output with correct values when JSON report is correct', function () {
        /* prepare */
        const sampleJson = {
            'result': {
                'count': 2,
                'findings': [
                    {
                        'id': 1,
                        'name': 'Client Dynamic File Inclusion',
                        'severity': 'HIGH',
                        'type': 'codeScan',
                        'cweId': 829
                    },
                    {
                        'id': 2,
                        'name': 'Potential Clickjacking on Legacy Browsers',
                        'severity': 'LOW',
                        'type': 'codeScan',
                        'cweId': 693
                    }
                ]
            },
            'status': 'SUCCESS',
            'trafficLight': 'RED',
            'jobUUID': '6952a899-3201-4b90-8716-e45d477cd750',
            'reportVersion': '1.0',
            'messages': []
        };

        /* execute */
        reportOutputs(sampleJson);

        /* test */
        expect(mockedCore.debug).toHaveBeenCalledTimes(6);
        expect(mockedAppendFileSync).toHaveBeenCalledTimes(6);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-trafficlight=RED${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-count=2${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-high=1${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-medium=0${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-low=1${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-readable-summary=SecHub reported traffic light color RED with 2 findings, categorized as follows: HIGH (1), LOW (1)${os.EOL}`);

        /* Disabled until GitHub Action setOutput is fixed

        expect(mockedCore.setOutput).toHaveBeenCalledTimes(6);
        expect(mockedCore.setOutput).toBeCalledWith('scan-trafficlight', 'RED');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-count', '2');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-high', '1');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-medium', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-low', '1');
        expect(mockedCore.setOutput).toBeCalledWith('scan-readable-summary', 'SecHub reported traffic light color RED with 2 findings, categorized as follows: HIGH (1), LOW (1)');

         */
    });

    it('calls set github output with correct values when JSON report did not exist', function () {
        /* execute */
        reportOutputs(undefined);

        /* test */

        expect(mockedCore.debug).toHaveBeenCalledTimes(7);
        expect(mockedAppendFileSync).toHaveBeenCalledTimes(6);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-trafficlight=FAILURE${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-count=0${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-high=0${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-medium=0${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-low=0${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-readable-summary=SecHub scan could not be executed.${os.EOL}`);


        /* Disabled until GitHub Action setOutput is fixed

        expect(mockedCore.debug).toBeCalledWith('No findings reported to be categorized.');
        expect(mockedCore.setOutput).toHaveBeenCalledTimes(6);
        expect(mockedCore.setOutput).toBeCalledWith('scan-trafficlight', 'FAILURE');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-count', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-high', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-medium', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-low', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-readable-summary', 'SecHub scan could not be executed.');

         */
    });

    it('calls set github output with correct values when traffic light is green without findings.', function () {
        /* prepare */
        const sampleJson = {
            'result': {
                'count': 0,
                'findings': []
            },
            'status': 'SUCCESS',
            'trafficLight': 'GREEN',
            'jobUUID': '6952a899-3201-4b90-8716-e45d477cd750',
            'reportVersion': '1.0',
            'messages': []
        };

        /* execute */
        reportOutputs(sampleJson);

        /* test */
        expect(mockedCore.debug).toHaveBeenCalledTimes(6);
        expect(mockedAppendFileSync).toHaveBeenCalledTimes(6);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-trafficlight=GREEN${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-count=0${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-high=0${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-medium=0${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-findings-low=0${os.EOL}`);
        expect(mockedAppendFileSync).toBeCalledWith(expect.any(String), `scan-readable-summary=SecHub reported traffic light color GREEN without findings${os.EOL}`);


        /* Disabled until GitHub Action setOutput is fixed

        expect(mockedCore.setOutput).toHaveBeenCalledTimes(6);
        expect(mockedCore.setOutput).toBeCalledWith('scan-trafficlight', 'GREEN');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-count', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-high', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-medium', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-low', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-readable-summary', 'SecHub reported traffic light color GREEN without findings');

         */
    });
});

