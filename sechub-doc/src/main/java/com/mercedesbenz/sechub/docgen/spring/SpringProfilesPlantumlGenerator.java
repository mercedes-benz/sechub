// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.spring;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.docgen.Generator;

public class SpringProfilesPlantumlGenerator implements Generator {

    static final Logger LOG = LoggerFactory.getLogger(SpringProfilesPlantumlGenerator.class);

    public static void main(String[] args) {
        System.out.println(new SpringProfilesPlantumlGenerator().generate(config().filterToProfile("prod").build()));
    }

    public static SpringProfileGenoConfigBuilder config() {
        return new SpringProfileGenoConfigBuilder();
    }

    public static class SpringProfileGenoConfigBuilder {
        private SpringProfileGenoConfig config;

        private SpringProfileGenoConfigBuilder() {
            config = new SpringProfileGenoConfig();
        }

        public SpringProfileGenoConfigBuilder withFiles() {
            config.withFiles = true;
            return this;
        }

        public SpringProfileGenoConfigBuilder showLinksToDefaultProfile() {
            config.linkingAlwaysToDefaultProfile = true;
            return this;
        }

        public SpringProfileGenoConfigBuilder filterToProfile(String filterProfile) {
            config.filteredProfile = filterProfile;
            return this;
        }

        public SpringProfileGenoConfigBuilder satelites(String... satelites) {
            for (String satelite : satelites) {
                config.sateliteProfiles.add(satelite);
            }
            return this;
        }

        public SpringProfileGenoConfig build() {
            return config;
        }
    }

    public static class SpringProfileGenoConfig {

        private boolean withFiles;
        private String filteredProfile;
        private boolean linkingAlwaysToDefaultProfile;
        private Set<String> sateliteProfiles = new LinkedHashSet<>();

        private SpringProfileGenoConfig() {

        }

        public boolean isWithFiles() {
            return withFiles;
        }

        public boolean isLinkingAlwaysToDefaultProfile() {
            return linkingAlwaysToDefaultProfile;
        }

        /**
         * @return profile which shall be generated only - if null its an overview
         */
        public String getFilteredProfile() {
            return filteredProfile;
        }

        public Set<String> getSateliteProfiles() {
            return Collections.unmodifiableSet(sateliteProfiles);
        }

        public boolean isOverviewGeneration() {
            return filteredProfile == null;
        }
    }

    public String generate(SpringProfileGenoConfig config) {
        LOG.info("Start spring profile plantuml generation. filtered profile={}, satelite profiles={}", config.getFilteredProfile(),
                config.getSateliteProfiles());

        File serverFolder = new File("./sechub-server/");
        if (!serverFolder.exists()) {
            /* not gradle but inside IDE: */
            serverFolder = new File("./../sechub-server");
        }
        if (!serverFolder.exists()) {
            throw new IllegalStateException("not found:" + serverFolder);
        }
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (!name.startsWith("application")) {
                    return false;
                }
                if (name.endsWith(".properties")) {
                    return true;
                }
                if (ListedProfile.isYaml(name)) {
                    return true;
                }
                return false;
            }
        };
        File resourceFolder = new File(serverFolder, "src/main/resources");
        File[] configFiles = resourceFolder.listFiles(filter);

        ListedProfileModel model = new ListedProfileModel();
        for (File configFile : configFiles) {
            model.add(configFile);
        }
        model.calculate(config);

        StringBuilder sb = new StringBuilder();
        createPlantUML(model, sb, config);

        return sb.toString();
    }

    private void createPlantUML(ListedProfileModel model, StringBuilder sb, SpringProfileGenoConfig config) {
        appendErrorsAsComment(model, sb);
        appendProfilesAsPackages(model, sb, config);
        appendLinksBetweenProfiles(model, sb);
    }

    private void appendProfilesAsPackages(ListedProfileModel model, StringBuilder sb, SpringProfileGenoConfig config) {
        for (ListedProfile profile : model.profiles) {
            if (profile.isBaseProfile()) {
                appendProfileAsComponent(sb, profile);
                sb.append("\n");
            } else {
                if (profile.getName().equals(config.getFilteredProfile())) {
                    sb.append("package <" + profile.getLabel() + ">{\n");
                    sb.append("   ");
                    appendProfileAsComponent(sb, profile);
                    sb.append("\n");
                    sb.append("}\n");
                } else {
                    appendProfileAsComponent(sb, profile);
                    sb.append("\n");
                }
                if (config.withFiles) {
                    for (File file : profile.configFiles) {
                        sb.append("   file \"" + file.getName()).append("\"\n");
                    }
                }
            }
        }
    }

    private void appendLinksBetweenProfiles(ListedProfileModel model, StringBuilder sb) {
        for (ListedProfile profile : model.profiles) {
            for (ListedProfile inc : profile.includedProfiles) {
                appendProfileAsComponent2(sb, profile);
                sb.append(" -> ");
                appendProfileAsComponent2(sb, inc);
                sb.append("\n");
            }
        }
    }

    private void appendErrorsAsComment(ListedProfileModel model, StringBuilder sb) {
        for (String errorMessage : model.errorMessages) {
            sb.append("'ERROR at generation time: ").append(errorMessage).append("\n");
        }
    }

    private void appendProfileAsComponent(StringBuilder sb, ListedProfile profile) {
        sb.append("node ").append(profile.getLabel()).append("");
    }

    private void appendProfileAsComponent2(StringBuilder sb, ListedProfile profile) {
        sb.append("").append(profile.getLabel()).append("");
    }

}
