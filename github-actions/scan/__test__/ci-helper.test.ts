// SPDX-License-Identifier: MIT

import * as cli from '../src/configuration-builder';
jest.mock('@actions/core');

describe('cli-helper', function() {
    it('createSecHubConfigModel creates with null always a model with api version 1.0.0', function () {
        /* execute */
        const model= cli.createSecHubConfigurationModel(null,null);

        /* test */
        expect(model.apiVersion).toEqual('1.0');
    });

});