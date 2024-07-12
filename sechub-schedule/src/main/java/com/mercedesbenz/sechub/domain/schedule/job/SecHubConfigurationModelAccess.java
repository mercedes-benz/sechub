package com.mercedesbenz.sechub.domain.schedule.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;

@Service
public class SecHubConfigurationModelAccess {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubConfigurationModelAccess.class);

    @Autowired
    ScheduleEncryptionService encryptionService;

    /**
     * Resolves sechub configuration model
     *
     * @param job
     * @return model or <code>null</code> if job contains no encrypted configuration
     */
    public SecHubConfigurationModel resolveUnencryptedConfiguration(ScheduleSecHubJob job) {
        String json = resolveUnencryptedConfigurationasJson(job);

        SecHubConfigurationModel configuration = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);

        return configuration;
    }

    /**
     * Resolves sechub configuration model
     *
     * @param job
     * @return model or <code>null</code> if job contains no encrypted configuration
     */
    public String resolveUnencryptedConfigurationasJson(ScheduleSecHubJob job) {
        if (job == null) {
            throw new IllegalArgumentException("job parameter may not be null!");
        }
        byte[] encryptedConfiguration = job.getEncryptedConfiguration();
        if (encryptedConfiguration == null) {
            LOG.debug("No encrypted sechub configuration found for job: {}!", job.getUUID());
            return null;
        }
        Long encryptionCipherPoolId = job.getEncryptionCipherPoolId();
        InitializationVector initialVector = new InitializationVector(job.getEncryptionInitialVectorData());

        String json = encryptionService.decryptToString(job.getEncryptedConfiguration(), encryptionCipherPoolId, initialVector);

        return json;
    }
}
