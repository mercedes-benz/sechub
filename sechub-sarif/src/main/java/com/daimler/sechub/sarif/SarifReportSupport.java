// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.sarif.model.Driver;
import com.daimler.sechub.sarif.model.Level;
import com.daimler.sechub.sarif.model.Report;
import com.daimler.sechub.sarif.model.ReportingConfiguration;
import com.daimler.sechub.sarif.model.Result;
import com.daimler.sechub.sarif.model.Rule;
import com.daimler.sechub.sarif.model.Run;
import com.daimler.sechub.sarif.model.Tool;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SarifReportSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SarifReportSupport.class);

    private ObjectMapper mapper;

    public SarifReportSupport() {
        JsonFactoryBuilder builder = new JsonFactoryBuilder();
        JsonFactory factory = new JsonFactory(builder);
        mapper = new ObjectMapper(factory).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    public Report loadReport(Path path) throws IOException {
        Objects.requireNonNull(path, "path may not be null!");
        return loadReport(path.toFile());
    }

    public Report loadReport(File file) throws IOException {
        Objects.requireNonNull(file, "file may not be null!");
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist:" + file.getAbsolutePath());
        }

        try (FileInputStream inputStream = new FileInputStream(file)) {
            Report report = mapper.readValue(inputStream, Report.class);
            return report;
        }

    }

    public String toJSON(Report report) throws IOException {
        if (report == null) {
            return null;
        }

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
    }

    public boolean isValidSarif(String json) {
        try {
            loadReport(json);
            return true;
        } catch (Exception e) {
            /* ignore error - except for tracing */
            LOG.trace("Not accepted as JSON - " + e.getMessage());
        }
        return false;
    }

    /**
     * Loads SARIF data from given JSON - must be UTF-8 see
     * https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317419
     * 
     * @param json
     * @return
     * @throws IOException
     */
    public Report loadReport(String json) throws IOException {
        Objects.requireNonNull(json, "json may not be null!");

        Report report = mapper.readValue(json.getBytes("UTF-8"), Report.class);
        if (LOG.isTraceEnabled()) {
            LOG.trace("origin:\n{}", json);
            LOG.trace("imported:\n{}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report));
        }
        return report;

    }

    public Rule fetchRuleForResult(Result result, Run run) {
        // Each run has ONE tool, multiple results and taxonomies
        Tool tool = run.getTool();
        Driver driver = tool.getDriver();

        List<Rule> rules = driver.getRules();

        String ruleId = result.getRuleId();

        /* @formatter:off */
        Rule ruleFound = 
                        rules.
                        stream().
                        filter(rule ->rule.getId().equals(ruleId)).
                        findFirst().orElse(null);
        /* @formatter:on */
        return ruleFound;
    }

    /**
     * Tries first the result level. If not set, the level will be obtained by
     * default configuration if available. If not found {@link Level#NONE} is
     * returned
     * 
     * @param result
     * @param run
     * @return level, never null
     */
    public Level resolveLevel(Result result, Run run) {
        Level level = result.getLevel();
        if (level != null) {
            return level;
        }
        Rule rule = fetchRuleForResult(result, run);
        if (rule != null) {
            /* @formatter:off 
               
               first fetch default from rule
               see https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317850
               
               @formatter:on */
            ReportingConfiguration defaultConfiguration = rule.getDefaultConfiguration();
            if (defaultConfiguration != null) {
                level = defaultConfiguration.getLevel();
            }
        }
        if (level == null) {
            level = Level.NONE;
        }
        return level;
    }

}
