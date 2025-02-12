package com.mercedesbenz.sechub.commons.core;

import static java.util.Objects.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provider gives a cached way to provide patterns. Because {@link Pattern}
 * is very expensive to create.
 *
 */
public class CachingPatternProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CachingPatternProvider.class);

    private Map<String, PatternCacheEntry> map = new TreeMap<>();
    private SortedSet<PatternCacheEntry> sortedEntries = new TreeSet<>();

    private int maximumSize;
    private PatternCompiler compiler;

    private static final Pattern ACCEPT_EVERYTHING_PATTERN = Pattern.compile(".*");

    public CachingPatternProvider(int maximumCacheSize, PatternCompiler compiler) {
        if (maximumCacheSize < 2) {
            throw new IllegalArgumentException("Maximum cache size must be greater than 1!");
        }
        this.maximumSize = maximumCacheSize;
        this.compiler = requireNonNull(compiler);
    }

    /**
     * Resolve compiled pattern for given pattern string.
     *
     * @param regularExpression regular expression as string
     * @return compiled pattern or <code>null</code> if it was not possible to
     *         create a compiled pattern (because of syntax problems)
     */
    public Pattern get(String regularExpression) {
        requireNonNull(regularExpression, "regular expression text may not be null!");

        PatternCacheEntry entry = map.get(regularExpression);
        if (entry == null) {
            entry = createNewEntryAndShrinkCacheIfNecesary(regularExpression);

        }
        return entry.compiledPattern;
    }

    private PatternCacheEntry createNewEntryAndShrinkCacheIfNecesary(String regularExpression) {
        PatternCacheEntry entry = new PatternCacheEntry();
        entry.pattern = regularExpression;
        try {
            Pattern compiledPattern = compiler.compile(regularExpression);
            entry.compiledPattern = compiledPattern;

        } catch (PatternSyntaxException e) {
            /*
             * this is a configuration problem at administrator side! We log the problem as
             * an error and use an alternative which accepts everything.
             */
            entry.compiledPattern = ACCEPT_EVERYTHING_PATTERN;
            LOG.error("The pattern is not valid: '{}'. Use instead compiled pattern: '{}'", regularExpression, entry.compiledPattern, e);
        }

        map.put(regularExpression, entry);
        sortedEntries.add(entry); // we add it here for cleanup

        /* check max size */
        while (sortedEntries.size() > maximumSize) {
            PatternCacheEntry removeMe = sortedEntries.iterator().next();

            /* remove from both collections: */
            sortedEntries.remove(removeMe);
            map.remove(removeMe.pattern);
        }
        return entry;
    }

    private class PatternCacheEntry implements Comparable<PatternCacheEntry> {
        private Pattern compiledPattern;
        private String pattern;
        private LocalDateTime created;

        private PatternCacheEntry() {
            this.created = LocalDateTime.now();
        }

        @Override
        public int compareTo(PatternCacheEntry o) {
            if (o == null) {
                return 1;
            }
            return created.compareTo(o.created);
        }

        @Override
        public String toString() {
            return "PatternCacheEntry [pattern=" + pattern + ",created=" + created + ", compiledPattern=" + compiledPattern + "]";
        }

    }
}
