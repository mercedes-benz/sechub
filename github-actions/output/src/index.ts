import * as core from '@actions/core';

async function run() {
  try {
    const nameToGreet = core.getInput('who-to-greet');
    const greeting = `Hello ${nameToGreet}`;
    core.setOutput('greeting', greeting);
  } catch (error) {
    if (error instanceof Error) {
      core.setFailed(error.message);
    }
  }
}

run();
