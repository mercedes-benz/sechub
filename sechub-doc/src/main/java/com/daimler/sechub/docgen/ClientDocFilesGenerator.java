// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientDocFilesGenerator implements Generator {

    private static final Logger LOG = LoggerFactory.getLogger(ClientDocFilesGenerator.class);

    public String generateDefaultZipAllowedFilePatternsTable() {
        Path path = Paths.get("./../sechub-cli/script/supported-source-extensions.txt");
        try {
            List<String> lines = Files.readAllLines(path);
            return createTAble(lines);
        } catch (IOException e) {
            throw new IllegalStateException("Was not ale to read source extensions data, so generation must fail", e);
        }
    }

    private String createTAble(List<String> lines) {
        List<SupportedFileExtensionsEntry> entries = createTableModel(lines);

        StringBuilder sb = new StringBuilder();
        add(sb, "[options=\"header\",cols=\"1,1\"]");
        add(sb, "|===");
        add(sb, "|Language(s) |File extensions");
        for (SupportedFileExtensionsEntry entry : entries) {
            add(sb, "|" + entry.description + "   |" + createFileEndingsText(entry.fileEndings));
        }
        add(sb, "|===");

        return sb.toString();
    }

    private String createFileEndingsText(List<String> fileEndings) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<String> it = fileEndings.iterator(); it.hasNext();) {
            String fileEnding = it.next();
            sb.append(fileEnding);
            if (it.hasNext()) {
                sb.append(" +\n");
            }
        }
        return sb.toString();
    }

    private void add(StringBuilder sb, String text) {
        sb.append(text).append("\n");
    }

    private List<SupportedFileExtensionsEntry> createTableModel(List<String> lines) {
        List<SupportedFileExtensionsEntry> entries = new ArrayList<>();
        for (String line : lines) {
            String[] split1 = line.split(":");

            SupportedFileExtensionsEntry entry = new SupportedFileExtensionsEntry();
            entry.description = split1[0];

            String endings = split1[1];
            String[] fileEndings = endings.split(" ");
            for (String fileEnding : fileEndings) {
                if (fileEnding.isEmpty()) {
                    continue;
                }
                // when rendering "c++ +" or "h++ +" the asciidoc output inside a table is
                // strange
                // so we avoid this by using the reserved attribute "plus" instead
                String asciidocPlusProblemSolved = fileEnding.replaceAll("\\+", "{plus}");
                entry.fileEndings.add(asciidocPlusProblemSolved);
            }

            entries.add(entry);
        }
        return entries;
    }

    private class SupportedFileExtensionsEntry {
        private String description;
        private List<String> fileEndings = new ArrayList<>();
    }

    public static void main(String[] args) {
        String output = new ClientDocFilesGenerator().generateDefaultZipAllowedFilePatternsTable();
        LOG.info("output:\n{}", output);
    }
}
