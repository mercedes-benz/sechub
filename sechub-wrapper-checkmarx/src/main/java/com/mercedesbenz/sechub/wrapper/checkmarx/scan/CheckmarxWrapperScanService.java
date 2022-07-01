package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;

@Service
public class CheckmarxWrapperScanService {
    
    @Autowired
    CheckmarxAdapter adapter;

    public String startScan() {
        
        AdapterMetaDataCallback adapterMetaDataCallBack = null;
        
        CheckmarxAdapterConfig config = null;
        
        try {
            adapter.start(config, adapterMetaDataCallBack);
        } catch (AdapterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
