// SPDX-License-Identifier: MIT

import {initReportFormats, initSecHubJson} from '../src/init-scan';

jest.mock('./../src/configuration-builder');
import {createSecHubConfigJsonFile} from '../src/configuration-builder';


describe('initSecHubJson', function () {
    it('throws error if configPath is set, but file does not exist', function () {
        /* prepare */
        const configPath = 'not-existing-json.json';

        /* execute + test */
        expect(() => initSecHubJson('runtime/sechub.json', configPath, [], [])).toThrow(Error);
    });

    it('returns parameter if configPath is set and file exists', function () {
        /* prepare */
        const configPath = '__test__/test-resources/test-config.json';

        /* execute */
        const parameter = initSecHubJson('runtime/sechub.json', configPath, [], []);

        /* test */
        expect(parameter).toContain(configPath);
    });

    it('creates sechub.json if configPath is not set', function () {
        /* execute */
        const parameter = initSecHubJson('runtime/sechub.json','', [], []);

        /* test */
        expect(parameter).toEqual('runtime/sechub.json');
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

    it('json rp missing json report at the beginning', function () {
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
