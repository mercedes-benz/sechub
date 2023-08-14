// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchiveType;
import com.mercedesbenz.sechub.commons.core.CommonConstants;

class ArchiveCreationContext {

    class CreationPathContext {
        private Set<String> paths = new TreeSet<>();
        private Set<String> excludes = new LinkedHashSet<>();
        private Set<String> includes = new LinkedHashSet<>();

        private List<PathMatcher> excludePathMatchers;
        private List<PathMatcher> includePathMatchers;

        private CreationPathContext() {

        }

        public Set<String> getPaths() {
            return Collections.unmodifiableSet(paths);
        }

        public boolean isExcluded(File file) {
            List<PathMatcher> matchers = getExcludePathMatchers();
            return isMatching(file, matchers);
        }

        public boolean isIncluded(File file) {
            List<PathMatcher> matchers = getIncudePathMatchers();
            return isMatching(file, matchers);
        }

        private List<PathMatcher> getExcludePathMatchers() {
            if (excludePathMatchers == null) {
                excludePathMatchers = createPathMatchers(excludes);
            }
            return excludePathMatchers;
        }

        private List<PathMatcher> getIncudePathMatchers() {
            if (includePathMatchers == null) {
                includePathMatchers = createPathMatchers(includes);
            }
            return includePathMatchers;
        }

        private boolean isMatching(File file, List<PathMatcher> matchers) {
            for (PathMatcher matcher : matchers) {
                if (matcher.matches(file.toPath())) {
                    return true;
                }
            }
            return false;
        }

        private List<PathMatcher> createPathMatchers(Collection<String> expressions) {
            List<PathMatcher> matchers = new ArrayList<>();
            for (String expression : expressions) {
                try {
                    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + expression);
                    matchers.add(matcher);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Include/exclude pattern was wrong:" + expression);
                }
            }
            return matchers;
        }
    }

    public static final String LEGACY_IDENTIFIER_UNIQUE_NAME = "";

    private Map<String, CreationPathContext> mapUniqueNamesToPaths = new TreeMap<>();
    private ArchiveType archiveType;
    private File archiveFile;

    private Path targetFolder;

    public ArchiveCreationContext(ArchiveType archiveType, Path targetFolder) {
        if (archiveType == null) {
            throw new IllegalArgumentException("Archive type may not be null!");
        }
        if (targetFolder == null) {
            throw new IllegalArgumentException("Target folder may not be null!");
        }
        this.archiveType = archiveType;
        this.targetFolder = targetFolder;

    }

    public File getArchiveFile() {
        if (archiveFile == null) {
            String fileName;
            switch (archiveType) {
            case TAR:
                fileName = CommonConstants.FILENAME_BINARIES_TAR;
                break;
            case ZIP:
                fileName = CommonConstants.FILENAME_SOURCECODE_ZIP;
                break;
            default:
                fileName = "unknown";
                break;

            }
            archiveFile = new File(targetFolder.toFile(), fileName);
            archiveFile.getParentFile().mkdirs();
        }
        return archiveFile;
    }

    public ArchiveType getArchiveType() {
        return archiveType;
    }

    public void addBaseFolderOrFilePaths(String uniqueName, Collection<String> fileOrFolders, Collection<String> includes, Collection<String> excludes) {
        CreationPathContext data = mapUniqueNamesToPaths.computeIfAbsent(uniqueName, (n) -> new CreationPathContext());
        data.includePathMatchers = null;
        data.excludePathMatchers = null;

        data.paths.addAll(fileOrFolders);
        data.includes.addAll(includes);
        data.excludes.addAll(excludes);

    }

    public Map<String, CreationPathContext> getUniqueToPathsMap() {
        return Collections.unmodifiableMap(mapUniqueNamesToPaths);
    }

    public boolean isEmpty() {
        return mapUniqueNamesToPaths.isEmpty();
    }
}