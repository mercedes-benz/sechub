// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import { downloadReports, reportOutputs } from '../src/post-scan';
import { getReport } from '../../shared/src/sechub-cli';

jest.mock('@actions/core');
const mockedCore = core as jest.Mocked<typeof core>;

jest.mock('../../shared/src/sechub-cli');
const mockedGetReport = getReport as jest.MockedFunction<typeof getReport>;
describe('downloadReports', function () {
    afterEach(() => {
        jest.clearAllMocks();
    });
    it('writes to log if formats is empty', function () {
        downloadReports([]);

        expect(mockedCore.info).toHaveBeenCalledTimes(1);
        expect(mockedGetReport).toHaveBeenCalledTimes(0);
    });

    it('calls getReport with correct parameters for non-empty formats', function () {
        const fsMock = require('fs');
        fsMock.readFileSync = jest.fn(() => '{"test": "test"}'); // Mock an empty JSON report
        const formats = ['json', 'html'];
        const sampleJson = {'test': 'test'};
        const actualJson = downloadReports(formats);

        expect(mockedCore.info).toHaveBeenCalledTimes(2); // Assumes 3 formats, adjust based on the number of formats in the array
        expect(mockedGetReport).toHaveBeenCalledTimes(2);
        expect(actualJson).toEqual(sampleJson);
    });
});

describe('reportOutputs', function () {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('calls set github output with correct values when JSON report is correct', function () {
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

        reportOutputs(sampleJson);

        expect(mockedCore.debug).toHaveBeenCalledTimes(6);
        expect(mockedCore.setOutput).toHaveBeenCalledTimes(6);
        expect(mockedCore.setOutput).toBeCalledWith('scan-trafficlight', 'RED');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-count', '2');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-high', '1');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-medium', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-low', '1');
        expect(mockedCore.setOutput).toBeCalledWith('scan-readable-summary', 'SecHub reported traffic light color RED with 2 findings, categorized as follows: HIGH (1), LOW (1)');
    });

    it('calls set github output with correct values when JSON report did not exist', function () {
        reportOutputs(undefined);

        expect(mockedCore.debug).toHaveBeenCalledTimes(7);
        expect(mockedCore.debug).toBeCalledWith('No findings reported to be categorized.');
        expect(mockedCore.setOutput).toHaveBeenCalledTimes(6);
        expect(mockedCore.setOutput).toBeCalledWith('scan-trafficlight', 'FAILURE');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-count', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-high', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-medium', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-low', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-readable-summary', 'SecHub scan could not be executed.');
    });

    it('calls set github output with correct values when traffic light is green without findings.', function () {
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

        reportOutputs(sampleJson);

        expect(mockedCore.debug).toHaveBeenCalledTimes(6);
        expect(mockedCore.setOutput).toHaveBeenCalledTimes(6);
        expect(mockedCore.setOutput).toBeCalledWith('scan-trafficlight', 'GREEN');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-count', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-high', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-medium', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-findings-low', '0');
        expect(mockedCore.setOutput).toBeCalledWith('scan-readable-summary', 'SecHub reported traffic light color GREEN without findings');
    });
});

