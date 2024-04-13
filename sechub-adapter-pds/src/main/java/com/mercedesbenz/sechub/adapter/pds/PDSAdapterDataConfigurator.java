// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelReducedCloningSupport;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;

public class PDSAdapterDataConfigurator implements PDSAdapterConfigData, PDSAdapterConfigurator {
    private static final String EMPTY_TARGET_TYPE = "";

    private String pdsProductIdentifier;
    private InputStream sourceCodeZipFileInputStreamOrNull;
    private String sourceCodeZipFileChecksumOrNull;
    private InputStream binaryTarFileInputStreamOrNull;
    private UUID secHubJobUUID;
    private Map<String, String> jobParameters;
    private SecHubConfigurationModel secHubConfigurationModel;
    private boolean reusingSecHubStorage;

    private boolean sourceCodeZipFileRequired;
    private boolean binaryTarFileRequired;
    private String targetType = EMPTY_TARGET_TYPE;
    private ScanType scanType;

    private String binariesTarFileChecksumOrNull;

    private boolean pdsScriptTrustsAllCertificates;

    private Long binariesTarFileSizeInBytes;

    private Long sourceCodeZipFileSizeInBytes;

    private int resilienceMaxRetries;

    private long resilienceTimeToWaitBeforeRetryInMilliseconds;

    public void setTargetType(String targetType) {
        if (targetType == null) {
            this.targetType = EMPTY_TARGET_TYPE;
        } else {
            this.targetType = targetType;
        }
    }

    @Override
    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    @Override
    public ScanType getScanType() {
        return scanType;
    }

    @Override
    public Map<String, String> getJobParameters() {
        return Collections.unmodifiableMap(jobParameters);
    }

    public void addJobParameter(String key, String value) {
        jobParameters.put(key, value);
    }

    @Override
    public UUID getSecHubJobUUID() {
        return secHubJobUUID;
    }

    @Override
    public String getPdsProductIdentifier() {
        return pdsProductIdentifier;
    }

    @Override
    public InputStream getSourceCodeZipFileInputStreamOrNull() {
        return sourceCodeZipFileInputStreamOrNull;
    }

    @Override
    public String getSourceCodeZipFileChecksumOrNull() {
        return sourceCodeZipFileChecksumOrNull;
    }

    @Override
    public InputStream getBinaryTarFileInputStreamOrNull() {
        return binaryTarFileInputStreamOrNull;
    }

    @Override
    public String getBinariesTarFileChecksumOrNull() {
        return binariesTarFileChecksumOrNull;
    }

    @Override
    public boolean isReusingSecHubStorage() {
        return reusingSecHubStorage;
    }

    @Override
    public boolean isSourceCodeZipFileRequired() {
        return sourceCodeZipFileRequired;
    }

    @Override
    public boolean isBinaryTarFileRequired() {
        return binaryTarFileRequired;
    }

    @Override
    public SecHubConfigurationModel getSecHubConfigurationModel() {
        return secHubConfigurationModel;
    }

    @Override
    public void setPdsProductIdentifier(String pdsProductIdentifier) {
        this.pdsProductIdentifier = pdsProductIdentifier;
    }

    @Override
    public void setSourceCodeZipFileInputStreamOrNull(InputStream sourceCodeZipFileInputStreamOrNull) {
        this.sourceCodeZipFileInputStreamOrNull = sourceCodeZipFileInputStreamOrNull;
    }

    @Override
    public void setSourceCodeZipFileChecksumOrNull(String sourceCodeZipFileChecksumOrNull) {
        this.sourceCodeZipFileChecksumOrNull = sourceCodeZipFileChecksumOrNull;
    }

    @Override
    public void setBinariesTarFileChecksumOrNull(String binariesTarFileChecksumOrNull) {
        this.binariesTarFileChecksumOrNull = binariesTarFileChecksumOrNull;
    }

    @Override
    public void setBinaryTarFileInputStreamOrNull(InputStream binaryTarFileInputStreamOrNull) {
        this.binaryTarFileInputStreamOrNull = binaryTarFileInputStreamOrNull;
    }

    @Override
    public void setSecHubJobUUID(UUID secHubJobUUID) {
        this.secHubJobUUID = secHubJobUUID;
    }

    @Override
    public void setJobParameters(Map<String, String> jobParameters) {
        this.jobParameters = jobParameters;
    }

    @Override
    public void setSecHubConfigurationModel(SecHubConfigurationModel secHubConfigurationModel) {
        this.secHubConfigurationModel = secHubConfigurationModel;
    }

    public void setReusingSecHubStorage(boolean reusingSecHubStorage) {
        this.reusingSecHubStorage = reusingSecHubStorage;
    }

    public void setSourceCodeZipFileRequired(boolean sourceCodeZipFileRequired) {
        this.sourceCodeZipFileRequired = sourceCodeZipFileRequired;
    }

    public void setBinaryTarFileRequired(boolean binaryTarFileRequired) {
        this.binaryTarFileRequired = binaryTarFileRequired;
    }

    @Override
    public void setPDSScriptTrustsAllCertificates(boolean trustAllCertificates) {
        this.pdsScriptTrustsAllCertificates = trustAllCertificates;
    }

    @Override
    public boolean isPDSScriptTrustingAllCertificates() {
        return pdsScriptTrustsAllCertificates;
    }

    @Override
    public void validateNonCalculatedParts() {
        if (pdsProductIdentifier == null) {
            throw new IllegalStateException("pds product identifier not set!");
        }
        if (jobParameters == null) {
            throw new IllegalStateException("job parameters not set!");
        }
        if (secHubJobUUID == null) {
            throw new IllegalStateException("sechubJobUUID not set!");
        }
        if (scanType == null) {
            throw new IllegalStateException("scanType not set!");
        }
        if (targetType == null) {
            /*
             * remark: the target type is only set by network specific scans like infra
             * scan, or web scan, but not for code or license scans!
             */
            throw new IllegalStateException("targetType may notbe null!");
        }
    }

    @Override
    public void calculate() {
        if (secHubConfigurationModel != null) {
            String reducedConfigJSON = SecHubConfigurationModelReducedCloningSupport.DEFAULT.createReducedScanConfigurationCloneJSON(secHubConfigurationModel,
                    scanType);
            jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION, reducedConfigJSON);
        }
        jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_TYPE, targetType);
    }

    public void setSourceCodeZipFileSizeInBytes(long sourceCodeZipFileSizeInBytes) {
        this.sourceCodeZipFileSizeInBytes = sourceCodeZipFileSizeInBytes;
    }

    public void setBinariesTarFileSizeInBytes(long binariesTarFileSizeInBytes) {
        this.binariesTarFileSizeInBytes = binariesTarFileSizeInBytes;
    }

    @Override
    public Long getBinariesTarFileSizeInBytesOrNull() {
        return binariesTarFileSizeInBytes;
    }

    @Override
    public Long getSourceCodeZipFileSizeInBytesOrNull() {
        return sourceCodeZipFileSizeInBytes;
    }

    @Override
    public void setResilienceMaxRetries(int maxRetries) {
        this.resilienceMaxRetries = maxRetries;
    }

    @Override
    public void setResilienceTimeToWaitBeforeRetryInMilliseconds(long milliseconds) {
        this.resilienceTimeToWaitBeforeRetryInMilliseconds = milliseconds;
    }

    @Override
    public int getResilienceMaxRetries() {
        return resilienceMaxRetries;
    }

    @Override
    public long getResilienceTimeToWaitBeforeRetryInMilliseconds() {
        return resilienceTimeToWaitBeforeRetryInMilliseconds;
    }

}
