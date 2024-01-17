// SPDX-License-Identifier: MIT

import {downloadReports} from "../src/post-scan";

jest.mock('@actions/core');
import * as core from '@actions/core';
import { getReport } from '../src/sechub-cli';
jest.mock('../src/sechub-cli');

describe('downloadReports', function () {
   it('writes to log if formats is empty', function () {
       /* execute */
       downloadReports([]);

       /* test */
       expect(core.info).toHaveBeenCalledTimes(1);
       expect(getReport).toHaveBeenCalledTimes(0);
   });
});
