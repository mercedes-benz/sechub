// SPDX-License-Identifier: MIT

import {initReportFormats, initSecHubJson} from '../src/init-scan';

jest.mock('./../src/configuration-builder');
import {SecHubConfigurationModelBuilderData, createSecHubConfigJsonString} from '../src/configuration-builder';

import * as core from '@actions/core';
import * as fs from 'fs';

jest.mock('@actions/core');

const mockInfo = core.info as jest.MockedFunction<typeof core.info>;

const debugEnabled = false;

beforeEach(() => {
    mockInfo.mockImplementation((message: string | Error) => {
        if (debugEnabled) {
            console.log(`Info: ${message}`);
        }
    });
    mockInfo.mockClear();
});

describe('initSecHubJson', function () {
    const testTargetConfigFilePath = '__test__/test-resources/generated-test-config.json';
    afterEach(() => {
        if (fs.existsSync(testTargetConfigFilePath)) {
            fs.unlinkSync(testTargetConfigFilePath);
        }
    });

    it('throws error if configPath is set, but file does not exist', function () {
        /* prepare */
        const customConfigPath = 'not-existing-json.json';
        const builderData = new SecHubConfigurationModelBuilderData();

        /* execute + test */
        expect(() => initSecHubJson('somewhere/runtime/sechub.json', customConfigPath, builderData)).toThrow(Error);
    });

    it('target config file is created - when custom config file exists', function () {
        /* prepare */
        const customConfigPath = '__test__/test-resources/test-config.json';
        
        const builderData = new SecHubConfigurationModelBuilderData();

        /* execute */
        initSecHubJson(testTargetConfigFilePath, customConfigPath, builderData);

        /* test */
        expect(fs.existsSync(testTargetConfigFilePath)).toBe(true);
    });

    it('target config file is created - when custom config path is empty', function () {
        /* prepare */
        const builderData = new SecHubConfigurationModelBuilderData();
        (createSecHubConfigJsonString as jest.Mock).mockReturnValue('{}');

        /* execute */
        initSecHubJson(testTargetConfigFilePath,'', builderData);

        /* test */
        expect(fs.existsSync(testTargetConfigFilePath)).toBe(true);
        expect(createSecHubConfigJsonString).toHaveBeenCalledTimes(1);
    });
});

describe('initReportFormats', function () {
    it('throws error if no valid report formats found', function () {
        /* prepare */
        const reportFormats = 'yaml,xml';

        /* execute + test */
        expect(() => initReportFormats(reportFormats)).toThrow(Error);
    });

    it('json always available, even when only html report wanted', function () {
        /* prepare */
        const reportFormats = 'html';

        /* execute */
        const formats = initReportFormats(reportFormats);

        /* test */
        expect(formats.length).toBe(2);
        expect(formats[0]).toBe('json');
        expect(formats[1]).toBe('html');
    });

    it('moves json report to the beginning', function () {
        /* prepare */
        const reportFormats = 'html,json';

        /* execute */
        const formats = initReportFormats(reportFormats);

        /* test */
        expect(formats.length).toBe(2);
        expect(formats[0]).toBe('json');
        expect(formats[1]).toBe('html');
    });
});
