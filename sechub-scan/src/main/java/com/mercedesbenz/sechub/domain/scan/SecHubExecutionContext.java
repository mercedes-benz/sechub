// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.sharedkernel.TypedKey;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

/**
 * Execution context with scope of SecHub, means knows {@link SecHubJob} ID and
 * {@link SecHubConfiguration}
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubExecutionContext {

    private static final SecHubExecutionOperationType DEFAULT_OPERATION_TYPE = SecHubExecutionOperationType.SCAN;

    private static final Logger LOG = LoggerFactory.getLogger(SecHubExecutionContext.class);

    private SecHubExecutionHistory executionHistory;
    private UUID sechubJobUUID;
    private SecHubConfiguration configuration;
    private UUIDTraceLogID traceLogId;
    private String executedBy;
    private Map<String, Object> dataMap = new HashMap<>();

    private boolean cancelRequested;

    private UUID executionUUID;

    private SecHubExecutionOperationType operationType;

    private LocalDateTime executionStarted;

    private AnalyticData analyticData;

    private boolean prepareFailed;

    private boolean suspended;

    private List<TemplateDefinition> templateDefinitions = new ArrayList<>();
    private Set<ScanType> usedPublicScanTypes = new LinkedHashSet<>();

    public SecHubExecutionContext(UUID sechubJobUUID, SecHubConfiguration configuration, String executedBy, UUID executionUUID) {
        this(sechubJobUUID, configuration, executedBy, executionUUID, null);
    }

    public SecHubExecutionContext(UUID sechubJobUUID, SecHubConfiguration configuration, String executedBy, UUID executionUUID,
            SecHubExecutionOperationType operationType) {
        this.executionStarted = LocalDateTime.now();
        this.analyticData = new AnalyticData();

        this.sechubJobUUID = sechubJobUUID;
        this.executionUUID = executionUUID;

        this.configuration = configuration;
        this.executedBy = executedBy;
        this.traceLogId = UUIDTraceLogID.traceLogID(sechubJobUUID);
        this.operationType = operationType == null ? DEFAULT_OPERATION_TYPE : operationType;
        this.executionHistory = new SecHubExecutionHistory();
    }

    /**
     * @return unmodifiable set of all used public scan types in this SecHub
     *         execution
     */
    public Set<ScanType> getUsedPublicScanTypes() {
        return Collections.unmodifiableSet(usedPublicScanTypes);
    }

    public void rememberIfPublicScanType(ScanType scanType) {
        if (scanType == null) {
            return;
        }
        if (scanType.isInternalScanType()) {
            return;
        }
        usedPublicScanTypes.add(scanType);
    }

    public SecHubExecutionOperationType getOperationType() {
        return operationType;
    }

    public void markCancelRequested() {
        cancelRequested = true;
    }

    public boolean isCancelRequested() {
        return cancelRequested;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public UUID getSechubJobUUID() {
        return sechubJobUUID;
    }

    public SecHubConfiguration getConfiguration() {
        return configuration;
    }

    public UUIDTraceLogID getTraceLogId() {
        return traceLogId;
    }

    public String getTraceLogIdAsString() {
        return traceLogId.toString();
    }

    /**
     * Add additional data by typed key
     *
     * @param <V>
     * @param id
     * @param value
     */
    public <V> void putData(TypedKey<V> id, V value) {
        if (id == null) {
            return;
        }
        dataMap.put(id.getId(), value);
    }

    /**
     * Get additional data by typed key
     *
     * @param <V>
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V> V getData(TypedKey<V> id) {
        if (id == null) {
            return null;
        }
        Object value = dataMap.get(id.getId());
        if (value == null) {
            return null;
        }
        if (id.getValueClass().isAssignableFrom(value.getClass())) {
            return (V) value;
        }
        LOG.error("Wrong usage in code: found entry for key '{}', but type '{}' found instead of wanted '{}'", id.getId(), value.getClass(),
                id.getValueClass());
        return null;
    }

    public boolean isDeleteFormerResultsWanted() {
        return false;
    }

    SecHubExecutionHistory getExecutionHistory() {
        return executionHistory;
    }

    public SecHubExecutionHistoryElement remember(ProductExecutor productExecutor, ProductExecutorData data) {
        return executionHistory.remember(productExecutor, data);
    }

    public void forget(SecHubExecutionHistoryElement historyElement) {
        executionHistory.forget(historyElement);
    }

    /**
     * An execution uuid is a uuid which is unique for this context. A job can be
     * executed many times (because of restarts) and will still have the same job
     * uuid. But the execution uuid will be different! It is unique and shared
     * inside the scan while it is executed.
     *
     * @return the execution uuid
     */
    public UUID getExecutionUUID() {
        return executionUUID;
    }

    public LocalDateTime getExecutionStarted() {
        return executionStarted;
    }

    public AnalyticData getAnalyticData() {
        return analyticData;
    }

    public void markPrepareFailed() {
        this.prepareFailed = true;
    }

    public boolean hasPrepareFailed() {
        return prepareFailed;
    }

    public void markSuspended() {
        this.suspended = true;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public List<TemplateDefinition> getTemplateDefinitions() {
        return templateDefinitions;
    }

}
