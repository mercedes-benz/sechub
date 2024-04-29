// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.spring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mercedesbenz.sechub.docgen.spring.SpringProfilesPlantumlGenerator.SpringProfileGenoConfig;

public class ListedProfileModel {

    private static final Logger LOG = LoggerFactory.getLogger(ListedProfileModel.class);

    List<ListedProfile> profiles = new ArrayList<>();
    List<String> errorMessages = new ArrayList<>();

    public void add(File configFile) {
        String profileName = ListedProfile.calculateProfileName(configFile);
        ListedProfile found = null;

        for (ListedProfile profile : this.profiles) {
            if (profile.getName().equals(profileName)) {
                found = profile;
                break;
            }
        }
        if (found == null) {
            found = new ListedProfile(profileName);
            LOG.info("added listed profile: {}", profileName);
            this.profiles.add(found);
        }
        found.configFiles.add(configFile);

    }

    public void calculate(SpringProfileGenoConfig config) {
        /* add includes */
        buildModelRelations(config);
        filter(config);
    }

    private void filter(SpringProfileGenoConfig config) {
        if (config.isOverviewGeneration()) {
            /* we do not filter anything */
            return;
        }
        /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
        /* + ................Find filter profile............. + */
        /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
        ListedProfile filterProfile = null;
        for (ListedProfile profile : new ArrayList<>(profiles)) {
            if (isFilteredProfile(config, profile)) {
                filterProfile = profile;
                break;
            }
        }
        if (filterProfile == null) {
            throw new IllegalStateException("Cannot filter for non existing profile:" + config.getFilteredProfile());
        }
        /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
        /* + ................Remaining....................... + */
        /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
        Set<ListedProfile> remaining = new LinkedHashSet<>();
        addRecursive(remaining, filterProfile);
        // add satelites
        Set<String> sateliteProfiles = config.getSateliteProfiles();
        for (ListedProfile profile : profiles) {
            if (sateliteProfiles.contains(profile.getName())) {
                remaining.add(profile);
            }
        }

        /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
        /* + ................Remove others................... + */
        /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
        for (ListedProfile profile : new ArrayList<>(profiles)) {
            if (!remaining.contains(profile) && !profile.isBaseProfile()) {
                profiles.remove(profile);
            }
        }

    }

    private void addRecursive(Set<ListedProfile> remaining, ListedProfile profile) {
        remaining.add(profile);
        for (ListedProfile child : profile.includedProfiles) {
            addRecursive(remaining, child);
        }

    }

    private void buildModelRelations(SpringProfileGenoConfig config) {
        ListedProfile baseProfile = ensureProfile("");
        ensureProfile("test");
        List<ListedProfile> iterateList = new ArrayList<>(profiles);
        for (ListedProfile profile : iterateList) {
            handleProfile(profile);
        }
        for (ListedProfile profile : profiles) {
            if (profile == baseProfile) {
                continue;
            }
            if (config.isLinkingAlwaysToDefaultProfile() || isFilteredProfile(config, profile)) {
                profile.includedProfiles.add(baseProfile);
            }
        }
    }

    private boolean isFilteredProfile(SpringProfileGenoConfig config, ListedProfile profile) {
        if (config.getFilteredProfile() == null) {
            return false;
        }
        if (config.getFilteredProfile().equals(profile.getName())) {
            return true;
        }
        return false;
    }

    private ListedProfile ensureProfile(String profileName) {
        /* add include of "" to all other */
        ListedProfile baseProfile = null;
        for (ListedProfile profile : profiles) {
            if (profile.getName().contentEquals(profileName)) {
                baseProfile = profile;
            }
        }
        if (baseProfile == null) {
            baseProfile = new ListedProfile(profileName);
            profiles.add(baseProfile);
        }
        return baseProfile;
    }

    @SuppressWarnings("unchecked")
    private void handleProfile(ListedProfile profile) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        LOG.info("Handle profile: label:{}, name:{}", profile.getLabel(), profile.getName());
//        spring:
//            profiles:
//              group:
//                pds_integrationtest: "pds_localserver,pds_integrationtest"
//                pds_debug: "pds_debug"
//                pds_dev: "pds_dev,pds_localserver"
//                pds_prod: "pds_postgres,pds_server"

        for (File file : profile.configFiles) {

            if (!ListedProfile.isYaml(file)) {
                continue;
            }
            LOG.debug("Read from file: {}", file);
            Map<String, Object> result = null;
            try {
                result = objectMapper.readValue(file, Map.class);

            } catch (JsonMappingException e) {
                if (e.getMessage().contains("No content")) {
                    // just ignore empty files
                    continue;
                }
                addError(e.getMessage());
                continue;
            } catch (IOException e) {
                addError(e.getMessage());
                continue;
            }
            Object spring = result.get("spring");
            if (!(spring instanceof Map)) {
                continue;
            }
            Map<String, Object> springMap = (Map<String, Object>) spring;
            Object profiles = springMap.get("profiles");
            if (!(profiles instanceof Map)) {
                continue;
            }
            Map<String, Object> profilesMap = (Map<String, Object>) profiles;
            Object group = profilesMap.get("group");
            if (!(group instanceof Map)) {
                continue;
            }
            Map<String, Object> groupMap = (Map<String, Object>) group;

            Set<String> groupNames = groupMap.keySet();
            for (String groupName : groupNames) {
                String content = (String) groupMap.get(groupName);
                String[] splitted = content.split(",");
                LOG.info("group '{}' : {}", groupName, splitted);
                ListedProfile currentProfile = ensureProfile(groupName);
                for (String split : splitted) {
                    currentProfile.includedProfiles.add(ensureProfile(split.trim()));
                }
            }
        }
    }

    private void addError(String message) {
        errorMessages.add(message);
    }
}