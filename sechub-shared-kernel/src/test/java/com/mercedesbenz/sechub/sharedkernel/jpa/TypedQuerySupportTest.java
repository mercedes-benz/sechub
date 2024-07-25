// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.jpa;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

public class TypedQuerySupportTest {
    private TypedQuerySupport<String> supportToTest;

    @Before
    public void before() {
        supportToTest = new TypedQuerySupport<>(String.class);
    }

    @Test
    public void support_returns_an_optional_when_no_result_exception_is_thrown() {
        /* prepare */
        Query query = mock(Query.class);
        when(query.getSingleResult()).thenThrow(new NoResultException("does not exist"));

        /* execute */
        Optional<String> optional = supportToTest.getSingleResultAsOptional(query);
        /* test */
        assertNotNull(optional);
    }

}
