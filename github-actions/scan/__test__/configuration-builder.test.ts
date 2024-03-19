// SPDX-License-Identifier: MIT

import * as configBuilder from '../src/configuration-builder';
import { SecHubConfigurationModelBuilderData } from '../src/configuration-builder';
jest.mock('@actions/core');

describe('configuration-builder', function() {
    test('null parameters - a model is created with api version 1.0.0', function () {
        /* execute */
        const data = new SecHubConfigurationModelBuilderData();
        const model= configBuilder.createSecHubConfigurationModel(data);

        /* test */
        expect(model.apiVersion).toEqual('1.0');
    });

    test('codescan green', () => {

        /* prepare */
        const data = new SecHubConfigurationModelBuilderData();
        data.includeFolders= ['folder1'];

        const model= configBuilder.createSecHubConfigurationModel(data);

        /* execute */

        /* test */
        const json = JSON.stringify(model);

        console.log('json='+json);
        expect(model.apiVersion).toEqual('1.0');
        expect(model.codeScan);


    });

});