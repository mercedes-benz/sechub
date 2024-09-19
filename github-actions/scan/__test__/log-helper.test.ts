// SPDX-License-Identifier: MIT

import { logExitCode } from '../src/exitcode';
jest.mock('@actions/core');
import * as core from '@actions/core';

describe('logExitCode', function () {
    it('uses core.info', function () {
        /* execute */
        logExitCode(0);

        /* test */
        expect(core.info).toHaveBeenCalledTimes(1);
    });

    it('uses core.error', function () {
        /* execute */
        logExitCode(1);

        /* test */
        expect(core.error).toHaveBeenCalledTimes(1);
    });
});
