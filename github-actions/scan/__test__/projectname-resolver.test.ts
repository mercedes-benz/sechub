// SPDX-License-Identifier: MIT

import * as githubInput from '../src/github-input';
import { resolveProjectName } from '../src/projectname-resolver';

jest.mock('./../src/configuration-builder');


describe('projectname-resolver', function () {
    test('project name is read from config file when github input is empty', function () {
        /* prepare */
        const inputData = Object.create(githubInput.INPUT_DATA_DEFAULTS);
        const location = '__test__/test-resources/test-config-with-project-name-inside.json';

        /* execute */
        const result = resolveProjectName(inputData, location);

        /* test */
        expect(result).toBe('the-project-name-from-configfile');
    });

    test('project name is read from config file when github input is undefined', function () {
        /* prepare */
        const inputData = Object.create(githubInput.INPUT_DATA_DEFAULTS);
        inputData.projectName=undefined;
        const location = '__test__/test-resources/test-config-with-project-name-inside.json';

        /* execute */
        const result = resolveProjectName(inputData, location);

        /* test */
        expect(result).toBe('the-project-name-from-configfile');
    });
});
