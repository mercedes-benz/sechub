// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.security;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PDSPasswordTransformerTest {

    private PDSPasswordTransformer passwordTransformer;

    @Before
    public void before() {
        passwordTransformer = new PDSPasswordTransformer();
    }

    @Test
    public void transform_password_without_noop() {
        String originPassword = "my-pwd";
        String transformedPassword = passwordTransformer.transformPassword(originPassword);
        assertEquals("{noop}my-pwd", transformedPassword);
    }

    @Test
    public void skip_password_with_noop() {
        String originPassword = "{noop}my-pwd";
        String transformedPassword = passwordTransformer.transformPassword(originPassword);
        assertEquals("{noop}my-pwd", transformedPassword);
    }

    @Test
    public void skip_password_with_bcrypt() {
        String originPassword = "{bcrypt}my-pwd";
        String transformedPassword = passwordTransformer.transformPassword(originPassword);
        assertEquals("{bcrypt}my-pwd", transformedPassword);
    }

}