// SPDX-License-Identifier: MIT

import { launch } from './launcher';
import { handleError } from './action-helper';

main().catch(handleError);

async function main(): Promise<void> {
    // Seperated launcher and main method.
    // Reason: launch mechanism would be loaded on imports
    //         before we can handle mocking in integration tests!
    await launch();
}

