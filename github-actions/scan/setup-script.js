// SPDX-License-Identifier: MIT

/*
    Wrapper script to execute the setup-script.sh file.
    This script is required because GitHub Actions runners for Node.js applications
    do not support direct execution of bash scripts.

    Refer to action.yml for additional details.
*/

const { exec } = require('child_process');

exec('bash setup-script.sh', (error, stdout, stderr) => {
    if (error) {
        console.error(`Error: ${error.message}`);
        return;
    }
    if (stderr) {
        console.error(`Stderr: ${stderr}`);
        return;
    }
    console.log(`Stdout: ${stdout}`);
});