// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.shutdown;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.core.shutdown.ShutdownListener;

class SpringApplicationShutdownHandlerTest {

    private static final ShutdownListener shutdownListener = mock();
    private static SpringApplicationShutdownHandler shutdownHandlerToTest;

    @BeforeEach
    void setUp() {
        shutdownHandlerToTest = new SpringApplicationShutdownHandler();
    }

    @Test
    void register_shutdown_listeners() {
        /* execute */
        shutdownHandlerToTest.register(shutdownListener);

        /* test */
        Collection<ShutdownListener> shutdownListeners = shutdownHandlerToTest.getShutdownListeners();
        assertThat(shutdownListeners).containsExactly(shutdownListener);
    }

    @Test
    void getShutdownListeners_returns_unmodifiable_collection() {
        /* prepare */
        shutdownHandlerToTest.register(shutdownListener);

        /* execute */
        Collection<ShutdownListener> shutdownListeners = shutdownHandlerToTest.getShutdownListeners();

        /* test */
        assertThatThrownBy(() -> shutdownListeners.add(mock(ShutdownListener.class))).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void onApplicationEvent_informs_shutdown_listeners() {
        /* prepare */
        Collection<ShutdownListener> shutdownListeners = List.of(mock(), mock(), mock());
        shutdownListeners.forEach(shutdownHandlerToTest::register);

        /* execute */
        shutdownHandlerToTest.onApplicationEvent(mock());

        /* test */
        Collection<ShutdownListener> actualShutdownListener = shutdownHandlerToTest.getShutdownListeners();
        /* @formatter:off */
        assertThat(actualShutdownListener)
                .hasSize(shutdownListeners.size())
                .allSatisfy(listener -> verify(listener).onShutdown());
        /* @formatter:on */

    }

    @Test
    void onApplicationEvent_informs_every_shutdown_listener_even_if_one_throws_exception() {
        /* prepare */
        ShutdownListener throwingListener = mock();
        doThrow(new RuntimeException("Shutdown failed")).when(throwingListener).onShutdown();
        ShutdownListener nonThrowingListener = mock();

        shutdownHandlerToTest.register(throwingListener);
        shutdownHandlerToTest.register(nonThrowingListener);

        /* execute */
        shutdownHandlerToTest.onApplicationEvent(mock());

        /* test */
        verify(throwingListener).onShutdown();
        verify(nonThrowingListener).onShutdown();
    }
}