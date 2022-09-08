package com.mercedesbenz.sechub.pds.tools;

import com.mercedesbenz.sechub.pds.tools.handler.ExitHandler;

class TestExitHandler implements ExitHandler {

    @Override
    public void exit(int exitCode) {
        throw new TestExitException(exitCode);
    }

}