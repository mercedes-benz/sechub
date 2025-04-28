// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as fs_wrapper from '../src/fs-wrapper';

import * as outputHelper from '../src/output-helper';
import { collectReportData, reportOutputs } from '../src/post-scan';
import { getReport } from '../src/sechub-cli';
import { LAUNCHER_CONTEXT_DEFAULTS } from '../src/launcher';

jest.mock('@actions/core');
jest.mock('../src/output-helper');
const mockedCore = core as jest.Mocked<typeof core>;
const mockedOutputHelper = outputHelper as jest.Mocked<typeof outputHelper>;

jest.mock('../src/sechub-cli');
const mockedGetReport = getReport as jest.MockedFunction<typeof getReport>;

describe('collectReportData', function () {

    afterEach(() => {
        jest.resetAllMocks();
    });

    it('format empty - logs called, getReport not called', function () {

        /* prepare */
        const testContext = Object.create(LAUNCHER_CONTEXT_DEFAULTS);
        testContext.reportFormats = [];

        /* execute */
        collectReportData(testContext);

        /* test */
        expect(mockedCore.info).toHaveBeenCalledTimes(1);
        expect(mockedGetReport).toHaveBeenCalledTimes(0);
    });

    it('format "json" - logs called 1 time , getReport never called', function () {
        /* prepare */
        const testContext = Object.create(LAUNCHER_CONTEXT_DEFAULTS);
        testContext.reportFormats = ['json'];

        /* execute */
        collectReportData(testContext);

        /* test */
        expect(mockedCore.info).toHaveBeenCalledTimes(1);
        expect(mockedGetReport).toHaveBeenCalledTimes(0);
    });

    it('format "html" - logs called 2 times, getReport called 1 time', function () {

        /* prepare */
        const testContext = Object.create(LAUNCHER_CONTEXT_DEFAULTS);
        testContext.reportFormats = ['json', 'html'];
        testContext.jobUUID = 1234; // necessary for download

        collectReportData(testContext);

        /* test */
        expect(mockedCore.info).toHaveBeenCalledTimes(2);
        expect(mockedGetReport).toHaveBeenCalledTimes(1);
    });

    it('format "json,html" - logs called 2 times , getReport called 1 time', function () {

        /* prepare */
        const testContext = Object.create(LAUNCHER_CONTEXT_DEFAULTS);
        testContext.reportFormats = ['json', 'html'];
        testContext.jobUUID = 1234; // necessary for download

        /* execute */
        collectReportData(testContext);

        /* test */
        expect(mockedCore.info).toHaveBeenCalledTimes(2);
        expect(mockedGetReport).toHaveBeenCalledTimes(1);
    });

    it('calls getReport with parameters (except json) and report json object is as expected', function () {

        /* prepare */
        const mockReadFileSyncResponse = '{"test": "test"}'; // pseudo test report as json

        jest.spyOn(fs_wrapper, 'readFileSync').mockReturnValue(mockReadFileSyncResponse);

        const testContext = Object.create(LAUNCHER_CONTEXT_DEFAULTS);
        testContext.reportFormats = ['json', 'html', 'xyz', 'bla'];
        testContext.jobUUID = 1234; // necessary for download

        
        /* execute */
        collectReportData(testContext);
        
        /* test */
        expect(mockedCore.info).toHaveBeenCalledTimes(4); // "json, html, xyz, bla" - 4 times logged (valid format check is not done here)
        expect(mockedGetReport).toHaveBeenCalledTimes(3); // we fetch not json via getReport again (already done before), so only "html, xyz, bla" used
        
        expect(testContext.secHubReportJsonObject).toEqual({ 'test': 'test' }); // json object is available

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
        expect(mockedOutputHelper.storeOutput).toHaveBeenCalledTimes(6);
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-trafficlight', 'RED');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-count', '2');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-high', '1');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-medium', '0');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-low', '1');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-readable-summary', 'SecHub reported traffic light color RED with 2 findings, categorized as follows: HIGH (1), LOW (1)');
    });

    it('calls set github output with correct values when JSON report did not exist', function () {
        /* execute */
        reportOutputs(undefined);

        /* test */
        expect(mockedCore.debug).toHaveBeenCalledTimes(7);
        expect(mockedCore.debug).toBeCalledWith('No findings reported to be categorized.');
        expect(mockedOutputHelper.storeOutput).toHaveBeenCalledTimes(6);
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-trafficlight', 'FAILURE');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-count', '0');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-high', '0');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-medium', '0');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-low', '0');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-readable-summary', 'SecHub scan could not be executed.');
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
        expect(mockedOutputHelper.storeOutput).toHaveBeenCalledTimes(6);
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-trafficlight', 'GREEN');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-count', '0');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-high', '0');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-medium', '0');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-findings-low', '0');
        expect(mockedOutputHelper.storeOutput).toBeCalledWith('scan-readable-summary', 'SecHub reported traffic light color GREEN without findings');
    });
});

