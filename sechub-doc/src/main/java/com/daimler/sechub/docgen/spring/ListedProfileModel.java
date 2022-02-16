// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.spring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.daimler.sechub.docgen.spring.SpringProfilesPlantumlGenerator.SpringProfileGenoConfig;

public class ListedProfileModel {
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
        for (ListedProfile profile : profiles) {
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

    private void handleProfile(ListedProfile profile) {
        for (File file : profile.configFiles) {
            if (!ListedProfile.isYaml(file)) {
                continue;
            }
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                for (Iterator<String> it = lines.iterator(); it.hasNext();) {
//						spring.profiles.include:
                    // -... name
                    String line = it.next();
                    if (!line.trim().contentEquals("spring.profiles.include:")) {
                        continue;
                    }
                    boolean doneEvenWhenIteratorHasNext = false;
                    while (it.hasNext()) {
                        String l = it.next().trim();
                        if (!l.startsWith("-")) {
                            doneEvenWhenIteratorHasNext = true;
                            break;
                        }
                        String profileName = l.substring(1).trim();
                        ListedProfile profileToInclude = null;
                        for (ListedProfile incProfile : this.profiles) {
                            if (incProfile.getName().contentEquals(profileName)) {
                                profileToInclude = incProfile;
                                break;
                            }
                        }
                        if (profileToInclude != null) {
                            profile.includedProfiles.add(profileToInclude);
                        } else {
                            String message = "Include profile not found:" + profileName;
                            SpringProfilesPlantumlGenerator.LOG.error(message);
                            addError(message);
                        }

                    }
                    if (doneEvenWhenIteratorHasNext) {
                        break;
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read lines of " + file);
            }
        }
    }

    private void addError(String message) {
        errorMessages.add(message);
    }
}