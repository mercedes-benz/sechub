// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.falsepositive;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.CachedSingletonsRegistry;
import com.intellij.openapi.progress.ProgressIndicator;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveJobData;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositives;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccess;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccessFactory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Provides a local cache for false positive entries.
 */
public class FalsePositivesCacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(FalsePositivesCacheManager.class);
    private static final FalsePositivesCacheManager instance = new FalsePositivesCacheManager();
    private static final Cache cache = new Cache();
    private static final String apiVersion = "1.0";
    private static final String type = "falsePositiveDataList";

    private FalsePositivesCacheManager() {
        /* private constructor to enforce singleton */
    }

    public static FalsePositivesCacheManager getInstance() {
        return instance;
    }

    public void markFalsePositive(UUID jobUUID, int findingId) {
        FalsePositive entry = new FalsePositive(jobUUID, findingId);
        cache.addEntry(jobUUID, entry);
    }

    public void unmarkFalsePositive(UUID jobUUID, int findingId) {
        cache.removeEntry(jobUUID, findingId);
    }

    public Optional<FalsePositive> findFalsePositive(UUID jobUUID, int findingId) {
        requireNonNull(jobUUID, "Parameter 'jobUUID' must not be null");
        if (findingId <= 0) {
            throw new IllegalArgumentException("Parameter 'findingId' must be greater than 0");
        }

        Optional<FalsePositivesList> falsePositiveList = cache.getEntry(jobUUID);

        if (falsePositiveList.isEmpty()) {
            return Optional.empty();
        }

        List<FalsePositive> falsePositives = falsePositiveList.get().getFalsePositives();

        return falsePositives.stream()
                .filter(fp -> fp.getFindingId() == findingId)
                .findFirst();
    }

    public boolean hasMarkedFalsePositives(UUID jobUUID) {
        requireNonNull(jobUUID, "Parameter 'jobUUID' must not be null");
        Optional<FalsePositivesList> falsePositiveList = cache.getEntry(jobUUID);

        if (falsePositiveList.isEmpty()) {
            return false;
        }

        List<FalsePositive> falsePositives = falsePositiveList.get().getFalsePositives();

        if (falsePositives == null || falsePositives.isEmpty()) {
            return false;
        }

        return true;
    }

    public FalsePositiveSyncStatus syncFalsePositives(String projectId, UUID jobUUID, String comment, @Nullable ProgressIndicator progressIndicator) {
        requireNonNull(projectId, "Parameter 'projectId' must not be null");
        requireNonNull(jobUUID, "Parameter 'jobUUID' must not be null");
        requireNonNull(comment, "Parameter 'comment' must not be null");

        Optional<FalsePositivesList> optFalsePositivesList = cache.getEntry(jobUUID);

        if (optFalsePositivesList.isEmpty()) {
            LOG.info("No false positives list found for job UUID {}", jobUUID);
            return FalsePositiveSyncStatus.EMPTY_CACHE;
        }

        FalsePositivesList falsePositivesList = optFalsePositivesList.get();
        List<FalsePositive> falsePositives = falsePositivesList.getFalsePositives();

        if (falsePositives.isEmpty()) {
            LOG.info("No false positives to sync for jobUUID '{}'", jobUUID);
            return FalsePositiveSyncStatus.EMPTY_CACHE;
        }

        falsePositives.forEach(fp -> fp.setComment(comment));
        cache.persist(jobUUID);

        boolean secHubServerAlive = secHubAccess().isSecHubServerAlive();

        if (!secHubServerAlive) {
            LOG.error("SecHub server is not alive");
            return FalsePositiveSyncStatus.SYNC_FAILED;
        }

        if (progressIndicator != null) {
            String progressIndicatorText = "Syncing %d false positives for jobUUID '%s' and projectId '%s'".formatted(falsePositives.size(), jobUUID, projectId);
            progressIndicator.setText(progressIndicatorText);
        }

        FalsePositiveSyncStatus syncStatus = syncFalsePositivesWithServer(projectId, jobUUID, falsePositives);

        if (syncStatus == FalsePositiveSyncStatus.SYNC_OK) {
            cache.clear(jobUUID);
        }

        return syncStatus;
    }

    private FalsePositiveSyncStatus syncFalsePositivesWithServer(String projectId, UUID jobUUID, List<FalsePositive> entries) {
        FalsePositives falsePositives = new FalsePositives();
        falsePositives.apiVersion(apiVersion);
        falsePositives.type(type);
        entries.forEach(entry -> {
            FalsePositiveJobData falsePositiveJobData = new FalsePositiveJobData();
            falsePositiveJobData.setJobUUID(jobUUID);
            falsePositiveJobData.setFindingId(entry.getFindingId());
            falsePositiveJobData.setComment(entry.getComment());
            falsePositives.addJobDataItem(falsePositiveJobData);
        });
        try {
            secHubAccess().markFalsePositive(projectId, falsePositives);
        } catch (Exception e) {
            LOG.error("Failed to sync false positives for projectId '{}' & jobUUID '{}'", projectId, jobUUID, e);
            return FalsePositiveSyncStatus.SYNC_FAILED;
        }

        return FalsePositiveSyncStatus.SYNC_OK;
    }

    private static SecHubAccess secHubAccess() {
        return SecHubAccessFactory.create();
    }

    private static class Cache {

        private static final Map<UUID, FalsePositivesList> runtimeCache = new ConcurrentHashMap<>();
        private static final ObjectMapper mapper = new ObjectMapper();
        private static final Supplier<PropertiesComponent> persistentCacheSupplier = CachedSingletonsRegistry.lazy(PropertiesComponent::getInstance);

        private void persist(UUID jobUUID) {
            if (!runtimeCache.containsKey(jobUUID)) {
                String errMsg = "No cache found for jobUUID: %s".formatted(jobUUID);
                LOG.error(errMsg);
                throw new RuntimeException(errMsg);
            }

            FalsePositivesList falsePositivesList = runtimeCache.get(jobUUID);

            String json;

            try {
                json = mapper.writeValueAsString(falsePositivesList);
            } catch (Exception e) {
                String errMsg = "Failed to serialize false positive container for jobUUID: %s".formatted(jobUUID);
                LOG.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }

            persistentCacheSupplier.get().setValue(jobUUID.toString(), json);
        }

        private Optional<FalsePositivesList> getEntry(UUID jobUUID) {
            requireNonNull(jobUUID, "Parameter 'jobUUID' must not be null");

            FalsePositivesList falsePositivesList = runtimeCache.computeIfAbsent(jobUUID, k -> loadFalsePositives(jobUUID));

            if (falsePositivesList == null) {
                return Optional.empty();
            }

            return Optional.of(falsePositivesList);
        }

        private void addEntry(UUID jobUUID, FalsePositive falsePositive) {
            runtimeCache.computeIfAbsent(jobUUID, k -> loadFalsePositives(jobUUID)).addFalsePositive(falsePositive);
        }

        private void removeEntry(UUID jobUUID, int findingId) {
            requireNonNull(jobUUID, "Parameter 'jobUUID' must not be null");
            if (findingId <= 0) {
                throw new IllegalArgumentException("Parameter 'findingId' must be greater than 0");
            }

            if (!runtimeCache.containsKey(jobUUID)) {
                LOG.debug("No false positives list found for jobUUID: {}", jobUUID);
                return;
            }

            FalsePositivesList falsePositivesList = runtimeCache.get(jobUUID);

            falsePositivesList.getFalsePositives().removeIf(fp -> findingId == fp.getFindingId());

            if (falsePositivesList.getFalsePositives().isEmpty()) {
                runtimeCache.remove(jobUUID);
            }
        }

        /**
         * Attempts to load the {@link FalsePositivesList} from the persistent cache for a given jobUUID.
         * If no entries are found, it returns a new {@link FalsePositivesList} object with an empty list.
         *
         * @param jobUUID the UUID of the job for which to retrieve the cache
         * @return a {@link FalsePositivesList} object
         */
        private FalsePositivesList loadFalsePositives(UUID jobUUID) {
            String persistentCacheEntries = persistentCacheSupplier.get().getValue(jobUUID.toString());

            if (persistentCacheEntries == null || persistentCacheEntries.isEmpty()) {
                return new FalsePositivesList(new ArrayList<>());
            }

            FalsePositivesList falsePositivesList;

            try {
                /* @formatter:off */
                falsePositivesList = mapper.readValue(
                        persistentCacheEntries,
                        new TypeReference<>() {}
                );
                /* @formatter:on */
            } catch (Exception e) {
                String errMsg = "Failed to deserialize false positives list for jobUUID: %s".formatted(jobUUID);
                LOG.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }

            return falsePositivesList;
        }

        private void clear(UUID jobUUID) {
            requireNonNull(jobUUID, "Parameter 'jobUUID' must not be null");
            runtimeCache.remove(jobUUID);
            persistentCacheSupplier.get().unsetValue(jobUUID.toString());
        }
    }

}
