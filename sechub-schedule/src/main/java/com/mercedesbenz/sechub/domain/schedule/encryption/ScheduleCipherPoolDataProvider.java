package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides cipher pool data
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class ScheduleCipherPoolDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleCipherPoolDataProvider.class);

    @Autowired
    ScheduleCipherPoolDataRepository repository;

    /**
     * Resolves all existing cipher pool data from database. If none is available, a
     * fallback will be persisted additionally.
     *
     * @return list of cipher pool data, never empty or <code>null</code>
     */
    public List<ScheduleCipherPoolData> ensurePoolDataAvailable() {
        List<ScheduleCipherPoolData> allPoolDataEntries = repository.findAll();

        if (allPoolDataEntries.isEmpty()) {

            ScheduleCipherPoolData fallbackEntry = createFallback();
            repository.save(fallbackEntry);

            LOG.warn(
                    "No pool data entry found - created and stored fallback with algorithm: {}. Remark: This may only happen inside tests! Real server have default initialized by SQL scripts!",
                    fallbackEntry.getAlgorithm());
            allPoolDataEntries = List.of(fallbackEntry);
        }

        if (allPoolDataEntries.isEmpty()) {
            throw new IllegalStateException("Found no pool data entry - should never happen");
        }
        return allPoolDataEntries;
    }

    private ScheduleCipherPoolData createFallback() {
        ScheduleCipherPoolData fallbackEntry = new ScheduleCipherPoolData();
        fallbackEntry.id = Long.valueOf(0);
        fallbackEntry.cipherPasswordSourceType = CipherPasswordSourceType.NONE;
        fallbackEntry.testText = "fallback";
        fallbackEntry.testEncrypted = "fallback".getBytes();
        fallbackEntry.algorithm = CipherAlgorithm.NONE;
        fallbackEntry.created = LocalDateTime.now();
        fallbackEntry.createdFrom = null;
        fallbackEntry.testInitialVector = null;

        return fallbackEntry;
    }
}
