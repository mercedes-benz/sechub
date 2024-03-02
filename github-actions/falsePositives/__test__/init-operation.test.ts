// SPDX-License-Identifier: MIT

import {initSecHubJson} from '../src/init-operation';

jest.mock('../../shared/src/cli-helper');
jest.mock('@actions/core');
import {createSecHubJsonFile} from '../../shared/src/cli-helper';

describe('initSecHubJson', function () {
    it('returns parameter if configPath is set', function () {
        /* prepare */
        const configPath = 'sechub.json';

        /* execute */
        const parameter = initSecHubJson(configPath);

        /* test */
        expect(parameter).toContain(configPath);
    });

    it('creates sechub.json if configPath is not set', function () {
        /* execute */
        const parameter = initSecHubJson('');

        /* test */
        expect(parameter).toBeNull();
        expect(createSecHubJsonFile).toHaveBeenCalledTimes(1);
    });
});
