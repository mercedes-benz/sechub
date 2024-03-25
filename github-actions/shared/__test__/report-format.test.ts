// SPDX-License-Identifier: MIT

import { checkReportFormat, ReportFormat } from '../src/report-formats';

describe('checkReportFormat', function() {
    it.each(
        [
            ['json', ReportFormat.JSON],
            ['html', ReportFormat.HTML],
            ['spdx-json', ReportFormat.SPDX_JSON],
        ]
    )(
        'correctly return report formats for %s',
        function(inputFormat, expectedFormats) {
            /* execute */
            const validFormat = checkReportFormat(inputFormat);

            /* test */
            expect(validFormat).toEqual(expectedFormats);
        }
    );

    it('throws exception on invalid report format', function () {
        /* prepare */
        const inputFormat = 'xml';

        /* execute and test */
        expect(() => checkReportFormat(inputFormat)).toThrow(Error);
    });
});
