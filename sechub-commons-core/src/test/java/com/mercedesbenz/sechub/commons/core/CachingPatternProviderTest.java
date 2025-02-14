// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.Test;

class CachingPatternProviderTest {

    @Test
    void cache_maximum_2_wrong_regular_expression_results_in_alternative_accepting_everything() {

        /* prepare */
        PatternCompiler compiler = mock();
        when(compiler.compile(any())).thenThrow(new PatternSyntaxException("desc", "regex", 0));
        CachingPatternProvider providerToTest = new CachingPatternProvider(2, compiler);

        /* execute */
        Pattern result = providerToTest.get("i-am-simulated-wrong");

        /* test */
        assertThat(result.toString()).isEqualTo(".*");
    }

    @Test
    void cache_maximum_2_correct_regular_expression_results_in_correct_compiled_pattern() {

        /* prepare */
        PatternCompiler compiler = mock();
        Pattern pattern = mock();
        when(compiler.compile(eq("i-am-correct"))).thenReturn(pattern);
        CachingPatternProvider providerToTest = new CachingPatternProvider(2, compiler);

        /* execute */
        Pattern result = providerToTest.get("i-am-correct");

        /* test */
        verify(compiler).compile("i-am-correct");
        assertThat(result).isSameAs(pattern);
    }

    @Test
    void cache_maximum_2_correct_regular_expression_fetched_3_times_created_only_one_time() {

        /* prepare */
        PatternCompiler compiler = mock();
        Pattern pattern = mock();
        when(compiler.compile(eq("i-am-correct"))).thenReturn(pattern);
        CachingPatternProvider providerToTest = new CachingPatternProvider(2, compiler);

        /* execute */
        Pattern result = providerToTest.get("i-am-correct");
        result = providerToTest.get("i-am-correct");
        result = providerToTest.get("i-am-correct");

        /* test */
        verify(compiler, times(1)).compile("i-am-correct");
        assertThat(result).isSameAs(pattern);
    }

    @Test
    void cache_maximum_2_handling_3_regular_expressions_does_remove_older_entries_automatically_when_cache_size_limit_reached() {

        /* prepare */
        PatternCompiler compiler = mock();
        Pattern pattern1 = mock();
        Pattern pattern2 = mock();
        Pattern pattern3 = mock();

        when(compiler.compile(eq("p1"))).thenReturn(pattern1);
        when(compiler.compile(eq("p2"))).thenReturn(pattern2);
        when(compiler.compile(eq("p3"))).thenReturn(pattern3);
        CachingPatternProvider providerToTest = new CachingPatternProvider(2, compiler);

        /* execute 1 - empty cache */
        Pattern result1 = providerToTest.get("p1"); // p1 added (one left)
        Pattern result2 = providerToTest.get("p2"); // p2 added (zero left)

        /* test 1 - empty cache, both patterns are compiled */
        var expectedCompileP1amount = 1;
        var expectedCompileP2amount = 1;

        verify(compiler, times(expectedCompileP1amount)).compile("p1");
        verify(compiler, times(expectedCompileP2amount)).compile("p2");

        /* execute 2 - use cache only */
        for (int i = 0; i < 10; i++) {
            /* execute 0 */
            result1 = providerToTest.get("p1"); // must be from cache
            result2 = providerToTest.get("p2"); // must be from cache
        }

        /* test 2 - test fetching existing ones does not compile again */
        verify(compiler, times(expectedCompileP1amount)).compile("p1");
        verify(compiler, times(expectedCompileP2amount)).compile("p2");

        /* execute 3 - recreate removed pattern */
        result1 = providerToTest.get("p1"); // p1 added (one left)
        result2 = providerToTest.get("p2"); // p2 added (zero left)
        Pattern result3 = providerToTest.get("p3"); // p3 added, must remove p1 fetch here because oldest
        Pattern result1x = providerToTest.get("p1"); // fetch p1 again... will recreated

        /* test 3 - data is still correct and re-creation works */
        expectedCompileP1amount++;// p1 must be recreated (p1,p2,p3->p1 removed, p1 fetch--> recreate)
        var expectedCompileP3amount = 1;

        verify(compiler, times(expectedCompileP2amount)).compile("p2");
        verify(compiler, times(expectedCompileP3amount)).compile("p3");
        verify(compiler, times(expectedCompileP1amount)).compile("p1");

        assertThat(result1).isSameAs(pattern1);
        assertThat(result1x).isSameAs(pattern1);
        assertThat(result2).isSameAs(pattern2);
        assertThat(result3).isSameAs(pattern3);

        /* execute 4 - use cached values */
        result1 = providerToTest.get("p1");
        result3 = providerToTest.get("p3");

        /* test 4 */
        verify(compiler, times(expectedCompileP1amount)).compile("p1"); // p1 must not be created again
        verify(compiler, times(expectedCompileP3amount)).compile("p3"); // p3 must not be created again

        assertThat(result1).isSameAs(pattern1);
        assertThat(result3).isSameAs(pattern3);

        /* execute 5 - use next entry, not in cache, and max reached */
        result2 = providerToTest.get("p2");

        /* test 5 */
        expectedCompileP2amount++; // p2 must be recreated
        verify(compiler, times(expectedCompileP2amount)).compile("p2");
        assertThat(result2).isSameAs(pattern2);

        /* execute 6 - next entry not */
        result2 = providerToTest.get("p2");

        /* test 6 */
        verify(compiler, times(expectedCompileP2amount)).compile("p2"); // same expectedCompileP2amount as before - means comes from cache
        assertThat(result2).isSameAs(pattern2);

    }

}
