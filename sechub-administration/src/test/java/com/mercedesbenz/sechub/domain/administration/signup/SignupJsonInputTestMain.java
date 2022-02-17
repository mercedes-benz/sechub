// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;

public class SignupJsonInputTestMain {

    public static void main(String[] args) throws JSONConverterException {
        SignupJsonInput input = new SignupJsonInput();
        input.setEmailAdress("albert.tregnaghi@example.org");
        input.setUserId("Albert Tregnaghi");
        input.setApiVersion("1.0");
        System.out.println(input.toJSON());
    }
}
