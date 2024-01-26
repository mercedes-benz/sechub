// SPDX-License-Identifier: MIT

import {getValidFormatsFromInput} from '../src/report-formats';

describe('getValidFormatsFromInput', function() {
    it('correctly return report formats for json', function () {
        /* prepare */
        const inputFormats = 'json';
        const expectedFormats = ['json'];

        /* execute */
        const validFormats = getValidFormatsFromInput(inputFormats);

        /* test */
        expect(validFormats).toEqual(expectedFormats);
    });

    it('correctly returns report formats for json,html', function () {
        /* prepare */
        const inputFormats = 'json,html';
        const expectedFormats = ['json', 'html'];

        /* execute */
        const validFormats = getValidFormatsFromInput(inputFormats);

        /* test */
        expect(validFormats).toEqual(expectedFormats);
    });

    it('correctly filter invalid report formats', function () {
        /* prepare */
        const inputFormats = 'json,xml,yml';
        const expectedFormats = ['json'];

        /* execute */
        const validFormats = getValidFormatsFromInput(inputFormats);

        /* test */
        expect(validFormats).toEqual(expectedFormats);
    });
});
