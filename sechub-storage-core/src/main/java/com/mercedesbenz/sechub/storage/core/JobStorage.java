// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface JobStorage {

    /**
     * Stores given stream for this job
     *
     * @param name   name for storage object
     * @param stream origin data stream
     */
    public void store(String name, InputStream stream) throws IOException;

    /**
     * Stores given stream for this job
     *
     * @param name                 name for storage object
     * @param stream               origin data stream
     * @param contentLengthInBytes content length of the stream in bytes
     */
    public void store(String name, InputStream stream, long contentLengthInBytes) throws IOException;

    /**
     * Fetch object with given name
     *
     * @param name
     * @return input stream
     * @throws IOException
     */
    public InputStream fetch(String name) throws IOException;

    /*
     * Deletes all content of this job
     */
    public void deleteAll() throws IOException;

    /**
     * Checks if an object with given name exists
     *
     * @param name
     * @return
     * @throws IOException
     */
    public boolean isExisting(String name) throws IOException;

    /**
     *
     * @return list with all object names
     * @throws IOException
     */
    public Set<String> listNames() throws IOException;

}
