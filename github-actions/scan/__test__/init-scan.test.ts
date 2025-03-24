// SPDX-License-Identifier: MIT

import {initReportFormats, initSecHubJson} from '../src/init-scan';

jest.mock('./../src/configuration-builder');
import {SecHubConfigurationModelBuilderData, createSecHubConfigJsonFile} from '../src/configuration-builder';

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
    const generatedConfigPath = '__test__/test-resources/generated-test-config.json';
    afterEach(() => {
        if (fs.existsSync(generatedConfigPath)) {
            fs.unlinkSync(generatedConfigPath);
        }
    });

    it('throws error if configPath is set, but file does not exist', function () {
        /* prepare */
        const configPath = 'not-existing-json.json';
        const builderData = new SecHubConfigurationModelBuilderData();

        /* execute + test */
        expect(() => initSecHubJson('somewhere/runtime/sechub.json', configPath, builderData)).toThrow(Error);
    });

    it('returns parameter if configPath is set and file exists', function () {
        /* prepare */
        const configPath = '__test__/test-resources/test-config.json';
        
        const builderData = new SecHubConfigurationModelBuilderData();

        /* execute */
        const parameter = initSecHubJson(generatedConfigPath, configPath, builderData);

        /* test */
        expect(parameter).toContain(generatedConfigPath);
    });

    it('creates sechub.json if configPath is not set', function () {
        /* execute */
        const builderData = new SecHubConfigurationModelBuilderData();
        const parameter = initSecHubJson(generatedConfigPath,'', builderData);

        /* test */
        expect(parameter).toEqual(generatedConfigPath);
        expect(createSecHubConfigJsonFile).toHaveBeenCalledTimes(1);
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
