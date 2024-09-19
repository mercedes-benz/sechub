import * as core from '@actions/core';

/**
 * Reads the given field from JSON.
 * @param {string} field - The field relative to root, where the value should be found. The field can be a nested field, e.g. result.count.
 * @param jsonData - The json data to read the field from.
 * @returns {*} - The value found for the given field or undefined if not found.
 */
export function getFieldFromJson(field: string, jsonData: any): any | undefined {
    // Split the given field into individual keys
    const keys = field.split('.');

    // Traverse the JSON object to find the requested field
    let currentKey = jsonData;
    for (const key of keys) {
        // eslint-disable-next-line no-prototype-builtins
        if (currentKey && currentKey.hasOwnProperty && typeof currentKey.hasOwnProperty === 'function' && currentKey.hasOwnProperty(key)) {
            currentKey = currentKey[key];
        } else {
            core.warning(`Field "${key}" not found in the JSON report.`);
            return undefined;
        }
    }

    return currentKey;
}
