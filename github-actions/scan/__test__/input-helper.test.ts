// SPDX-License-Identifier: MIT

import {split} from '../src/input-helper';

jest.mock('@actions/core');

describe('split', function () {
    it('input undefined - returns empty array', function () {
        /* prepare */
        const input: any = undefined;

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual([]);
    });

    it('input null - returns empty array', function () {
        /* prepare */
        const input: any = null;

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual([]);
    });

    it('input empty - returns empty array', function () {
        /* prepare */
        const input: any = '';

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual([]);
    });

    it('input single whitespace - returns empty array', function () {
        /* prepare */
        const input: any = ' ';

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual([]);
    });

    it('input single comma - returns empty array', function () {
        /* prepare */
        const input: any = ',';

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual([]);
    });

    it('input single value - returns array with single value', function () {
        /* prepare */
        const input: any = 'a';

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual(['a']);
    });

    it('input multiple comma separated values - returns array with multiple values', function () {
        /* prepare */
        const input: any = 'a, b, c, d, e, f, g';

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual(['a', 'b', 'c', 'd', 'e', 'f', 'g']);
    });
});
