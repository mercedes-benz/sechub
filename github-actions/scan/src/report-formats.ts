// SPDX-License-Identifier: MIT

import {split} from './input-helper';

export const availableFormats = ['json', 'html', 'spdx-json'];

/**
 * Convert input string to array and filter invalid formats.
 * @param inputFormats Formats from the action input
 */
export function getValidFormatsFromInput(inputFormats: string): string[] {
    const formats = split(inputFormats);
    if (formats.length === 0) { return []; }
    return formats.filter((item) => availableFormats.includes(item));
}
