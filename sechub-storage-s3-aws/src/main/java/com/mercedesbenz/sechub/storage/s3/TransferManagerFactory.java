// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.TransferManager;

/**
 * An interface to decouple transfer manager creation. Makes it easier to mock
 * for testing but also to customize transfer manager setup.
 *
 * @author Albert Tregnaghi
 *
 */
public interface TransferManagerFactory {

    /**
     * Creates a transfer manager for the given client
     *
     * @param client
     * @return
     */
    public TransferManager createTransferManager(AmazonS3 client);
}
