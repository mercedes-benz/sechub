package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.FileStoreAdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConfig;

@Service
public class CheckmarxWrapperScanService {

    @Value("${pds.job.metadata.file}")
    String pdsJobMetaDatafile;

    @Autowired
    CheckmarxAdapter adapter;

    public String startScan() {

        File metaDataFile = new File(pdsJobMetaDatafile);
        AdapterMetaDataCallback adapterMetaDataCallBack = new FileStoreAdapterMetaDataCallback(metaDataFile);

        /* @formatter:off */
        CheckmarxConfig config =
                CheckmarxConfig.builder().
                    setAlwaysFullScan(false).

                build();
        /* @formatter:on */

        try {
            adapter.start(config, adapterMetaDataCallBack);
        } catch (AdapterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
