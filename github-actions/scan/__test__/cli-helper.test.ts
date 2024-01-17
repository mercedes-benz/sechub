// SPDX-License-Identifier: MIT

import * as cli from "../src/cli-helper";
jest.mock('@actions/core');
import * as core from '@actions/core';

describe('cli-helper', function() {
   it('createSecHubConfigModel creates with null always a model with api version 1.0.0', function () {
       /* execute */
       let model= cli.createSecHubConfigModel(null,null);
       
       /* test */
       expect(model.apiVersion).toEqual("1.0")
   });

});
