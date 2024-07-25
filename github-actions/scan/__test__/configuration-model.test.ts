// SPDX-License-Identifier: MIT

import { SecHubConfigurationModelBuilderData } from '../src/configuration-builder';
import { ContentType, ScanType } from '../src/configuration-model';

jest.mock('@actions/core');


describe('configuration-model:ScanType', function() {

    test('ScanType.ensureAccepted - no params results in default', () => {

        /* prepare */

        /* execute */
        const result = ScanType.ensureAccepted([]);

        /* test */
        expect(result).toEqual([SecHubConfigurationModelBuilderData.DEFAULT_SCAN_TYPE]);

    });

    test('ScanType.ensureAccepted - wrong params results in default', () => {

        /* execute */
        const result = ScanType.ensureAccepted(['x','y']);

        /* test */
        expect(result).toEqual([SecHubConfigurationModelBuilderData.DEFAULT_SCAN_TYPE]);

    });

    test('ScanType.ensureAccepted - partyl wrong params results in correct one only', () => {

        /* execute */
        const result = ScanType.ensureAccepted(['licensescan','y']);

        /* test */
        expect(result).toEqual([ScanType.LICENSE_SCAN]);

    });

    test('ScanType.ensureAccepted - wellknown params accepted case insensitive but converted', () => {

        /* execute */
        const result = ScanType.ensureAccepted(['licensescan','SECRETscan','codeScan']);

        /* test */
        expect(result).toContain(ScanType.LICENSE_SCAN);
        expect(result).toContain(ScanType.CODE_SCAN);
        expect(result).toContain(ScanType.SECRET_SCAN);

        expect(result.length).toBe(3);

    });

});

describe('configuration-model:ContentType', function() {

    test('ContentType.ensureAccepted - source accepted case insensitive but converted', () => {
    
        /* execute */
        const result = ContentType.ensureAccepted('souRce');
    
        /* test */
        expect(result).toBe(ContentType.SOURCE);
    
    });
    test('ContentType.ensureAccepted - binaries accepted case insensitive but converted', () => {
    
        /* execute */
        const result = ContentType.ensureAccepted('BINaries');
    
        /* test */
        expect(result).toBe(ContentType.BINARIES);
    
    });
    test('ContentType.ensureAccepted - other not accepted but converted to source as default', () => {
    
        /* execute */
        const result = ContentType.ensureAccepted('other');
    
        /* test */
        expect(result).toBe(ContentType.SOURCE);
    
    });

});