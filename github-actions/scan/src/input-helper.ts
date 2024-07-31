// SPDX-License-Identifier: MIT

const COMMA = ',';

/**
 * Splits an input string by comma and sanitizes the result by removing leading and trailing whitespaces.
 * Result will contain non-empty strings only.
 *
 * @returns array of comma separated strings
 */
export function split(input: string): string[] {
    if (!input) return [];

    return input
        .split(COMMA)
        .map(item => item.trim())
        .filter(item => item.length > 0);
}
