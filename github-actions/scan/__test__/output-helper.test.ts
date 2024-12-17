// SPDX-License-Identifier: MIT
import * as outputHelper from '../src/output-helper';
import * as core from '@actions/core';

jest.mock('@actions/core');

describe('storeOutput', () => {
    const mockedCore = core as jest.Mocked<typeof core>;

    it('test-key shall set SECHUB_OUTPUT_TEST_KEY', () => {
        /* execute */
        outputHelper.storeOutput('test-key', 'test value1');
        
        /* test */
        expect(mockedCore.exportVariable).toBeCalledWith('SECHUB_OUTPUT_TEST_KEY', 'test value1');
    });

});