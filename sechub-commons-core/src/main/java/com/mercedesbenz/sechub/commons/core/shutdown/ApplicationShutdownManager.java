package com.mercedesbenz.sechub.commons.core.shutdown;

public interface ApplicationShutdownManager {

    void register(AutoCloseable closeable);

    void shutdown();

}
