// SPDX-License-Identifier: MIT

import { ContentType } from '../src/content-type';

jest.mock('@actions/core');


describe('ContentType', function() {

    test('ContentType.ensureAccepted - source accepted case insensitive but converted', () => {
    
        /* execute */
        const result = ContentType.safeAcceptedContentType('souRce');
    
        /* test */
        expect(result).toBe(ContentType.SOURCE);
    
    });
    test('ContentType.ensureAccepted - binaries accepted case insensitive but converted', () => {
    
        /* execute */
        const result = ContentType.safeAcceptedContentType('BINaries');
    
        /* test */
        expect(result).toBe(ContentType.BINARIES);
    
    });
    test('ContentType.ensureAccepted - other not accepted but converted to source as default', () => {
    
        /* execute */
        const result = ContentType.safeAcceptedContentType('other');
    
        /* test */
        expect(result).toBe(ContentType.SOURCE);
    
    });

});