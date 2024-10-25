// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * <p>
 * {@code WithMockJwtUser} is a custom annotation used to create a mock security
 * context with a JWT token for testing purposes. This annotation can be applied
 * to test methods (or classes) to simulate an authenticated user with a
 * specified JWT token.
 * </p>
 *
 * <p>
 * The {@code WithMockJwtUser} annotation is processed by the
 * {@link WithMockJwtSecurityContextFactory} to set up the security context with
 * the provided JWT token.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <p>
 * <code>
 *  &#064;Test <br>
 *  &#064;WithMockJwtUser <br>
 *  void test() { <br>
 *      ...<br>
 *  }
 * </code>
 * </p>
 *
 * <p>
 * This will setup a default authenticated user with a JWT token for the test.
 * </p>
 *
 * <p>
 * To specify a custom JWT token, use the {@code jwt} attribute:
 * </p>
 *
 * <p>
 * <code>
 *  &#064;Test <br>
 *  &#064;WithMockJwtUser(jwt = "jwt") <br>
 *  void test() { <br>
 *      ...<br>
 *  }
 * </code>
 * </p>
 *
 * <p>
 * This is useful in scenarios where you want to test the behavior of a JWT
 * mismatch (Unauthorized).
 * </p>
 *
 * @see WithMockJwtSecurityContextFactory
 *
 * @author hamidonos
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory.class)
public @interface WithMockJwtUser {
    String jwt() default SecurityTestConfiguration.JWT;
}