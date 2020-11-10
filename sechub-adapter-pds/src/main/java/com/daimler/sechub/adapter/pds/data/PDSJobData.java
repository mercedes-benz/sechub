// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds.data;

import java.util.ArrayList;
import java.util.List;

public class PDSJobData {

    public String apiVersion="1.0";
    
    public String sechubJobUUID;
    
    public String productId;
    
    public List<PDSJobParameterEntry> parameters = new ArrayList<>();
}
