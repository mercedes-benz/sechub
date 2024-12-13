// SPDX-License-Identifier: MIT
import * as outputHelper from '../src/output-helper';

import * as fs from 'fs';
import * as path from 'path';

describe('storeOutput', () => {
    const outputPath = path.join(__dirname, 'test_output.txt');

    beforeAll(() => {
        process.env.GITHUB_OUTPUT = outputPath;
    });

    afterEach(() => {
        if (fs.existsSync(outputPath)) {
            fs.unlinkSync(outputPath);
        }
    });

    it('should append a line with key=value to the file', () => {
        /* execute */
        outputHelper.storeOutput('TEST_KEY', 'TEST_VALUE');
        
        /* test */
        const content = fs.readFileSync(outputPath, 'utf8');
        expect(content).toBe('TEST_KEY=TEST_VALUE\n');
    });

    it('should append multiple lines correctly', () => {
        /* execute */
        outputHelper.storeOutput('KEY1', 'VALUE1');
        outputHelper.storeOutput('KEY2', 'VALUE2');

        /* test */
        const content = fs.readFileSync(outputPath, 'utf8');
        expect(content).toBe('KEY1=VALUE1\nKEY2=VALUE2\n');
    });

    it('should throw an error if GITHUB_OUTPUT is not set', () => {
        /* prepare */
        delete process.env.GITHUB_OUTPUT;
        
        /* execute + test */
        expect(() => outputHelper.storeOutput('KEY', 'VALUE')).toThrow('GITHUB_OUTPUT environment variable is not set');
    });
});