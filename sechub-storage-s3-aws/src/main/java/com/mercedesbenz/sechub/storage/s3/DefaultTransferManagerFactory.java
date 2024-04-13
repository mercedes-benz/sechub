// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.TransferManager;

/**
 * This implementation delegates to the {@link AwsS3TransferManagerFactory} to
 * create the transfer manager - this will always set the upload threshold to 5
 * MiB (done by {@link AwsS3TransferManagerFactory}).
 *
 * @author Albert Tregnaghi
 *
 */
public class DefaultTransferManagerFactory implements TransferManagerFactory {

    public static final DefaultTransferManagerFactory INSTANCE = new DefaultTransferManagerFactory();

    private AwsS3TransferManagerFactory delegate = new AwsS3TransferManagerFactory();

    private DefaultTransferManagerFactory() {
        // make it private, so INSTANCE is always used
    }

    @Override
    public TransferManager createTransferManager(AmazonS3 client) {
        return delegate.create(client);
    }

}
