package com.mercedesbenz.sechub.domain.scan.asset;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;

class ProductExecutorConfigAssetFileNameServiceTest {

    @Mock
    private ProductExecutorConfigAssetFileNameResolver resolver1;

    @Mock
    private ProductExecutorConfigAssetFileNameResolver resolver2;

    @Mock
    private ProductExecutorConfig config;

    private ProductExecutorConfigAssetFileNameService serviceToTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        serviceToTest = new ProductExecutorConfigAssetFileNameService(List.of(resolver1, resolver2));
    }

    @Test
    void resolveName_firstResolverReturnsFileName() {
        /* prepare */
        when(resolver1.resolveAssetFilename(config)).thenReturn("file1.txt");
        when(resolver2.resolveAssetFilename(config)).thenReturn(null);

        /* execute */
        String result = serviceToTest.resolveAssetFileName(config);

        /* test */
        assertEquals("file1.txt", result);
        verify(resolver1).resolveAssetFilename(config);
        verify(resolver2, never()).resolveAssetFilename(config);
    }

    @Test
    void resolveName_secondResolverReturnsFileName() {
        /* prepare */
        when(resolver1.resolveAssetFilename(config)).thenReturn(null);
        when(resolver2.resolveAssetFilename(config)).thenReturn("file2.txt");

        /* execute */
        String result = serviceToTest.resolveAssetFileName(config);

        /* test */
        assertEquals("file2.txt", result);
        verify(resolver1).resolveAssetFilename(config);
        verify(resolver2).resolveAssetFilename(config);
    }

    @Test
    void resolveName_noResolverReturnsFileName() {
        /* prepare */
        when(resolver1.resolveAssetFilename(config)).thenReturn(null);
        when(resolver2.resolveAssetFilename(config)).thenReturn(null);

        /* execute */
        String result = serviceToTest.resolveAssetFileName(config);

        /* test */
        assertNull(result);
        verify(resolver1).resolveAssetFilename(config);
        verify(resolver2).resolveAssetFilename(config);
    }

    @Test
    void testConstructor_nullResolverList() {
        assertThatThrownBy(() -> {
            new ProductExecutorConfigAssetFileNameService(null);
        }).isInstanceOf(NullPointerException.class).hasMessage("resolver list may not be null!");
    }

    @Test
    void testConstructor_emptyList_throws_illegal_state_exception() {
        assertThatThrownBy(() -> {
            new ProductExecutorConfigAssetFileNameService(Collections.emptyList());
        }).isInstanceOf(IllegalStateException.class).hasMessage("An empty list of file name resolvers was injected. At least one must be available!");
    }
}