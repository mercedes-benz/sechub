// SPDX-License-Identifier: MIT

import * as configBuilder from '../src/configuration-builder';
jest.mock('@actions/core');

describe('conifguration-builder', function() {
    it('createSecHubConfigModel creates with null always a model with api version 1.0.0', function () {
        /* execute */
        const model= configBuilder.createSecHubConfigurationModel(null,null);

        /* test */
        expect(model.apiVersion).toEqual('1.0');
    });

});