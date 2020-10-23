// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapter;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.AdapterRuntimeContext;
import com.daimler.sechub.adapter.pds.data.PDSJobCreateResult;
import com.daimler.sechub.adapter.pds.data.PDSJobData;
import com.daimler.sechub.adapter.pds.data.PDSJobParameterEntry;

/**
 * This component is able to handle PDS API V1
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class PDSAdapterV1 extends AbstractAdapter<PDSAdapterContext, PDSAdapterConfig> implements PDSAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(PDSAdapterV1.class);

    @Override
    public int getAdapterVersion() {
        return 1;
    }

    @Override
    protected String execute(PDSAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        PDSContext context = new PDSContext(config, this, runtimeContext);

        createNewPDSJOB(context);

        uploadJobData(context);

        throw asAdapterException("pds adapter needs implementation...", config);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Upload.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void uploadJobData(PDSContext context) throws AdapterException {

        PDSAdapterConfig config = context.getConfig();
        if (!(config instanceof PDSSourceZipConfig)) {
            /* no upload necessary */
            return;
        }
        PDSSourceZipConfig sourceZipConfig = (PDSSourceZipConfig) config;
        AdapterMetaData metaData = context.getRuntimeContext().getMetaData();
        if (!metaData.hasValue(PDSAdapterConstants.METADATA_KEY_FILEUPLOAD_DONE, true)) {
            /* upload source code */
            PDSUploadSupport uploadSupport = new PDSUploadSupport();
            uploadSupport.uploadZippedSourceCode(context, sourceZipConfig);

            /* after this - mark file upload done, so on a restart we don't need this */
            metaData.setValue(PDSAdapterConstants.METADATA_KEY_FILEUPLOAD_DONE, true);
            context.getRuntimeContext().getCallback().persist(metaData);
        } else {
            LOG.info("Reuse existing upload for:{}", context.getTraceID());
        }
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Create New Job.................. + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void createNewPDSJOB(PDSContext context) throws AdapterException {

        String json = createJobDataJSON(context);
        String url = context.getUrlBuilder().buildCreateJob();

        String jsonResult = context.getRestSupport().postJSON(url, json);
        PDSJobCreateResult result = context.getJsonSupport().fromJSON(PDSJobCreateResult.class, jsonResult);
        context.setPDSJobUUID(result.jobUUID);
    }

    private String createJobDataJSON(PDSContext context) throws AdapterException {
        PDSJobData jobData = createJobData(context);

        String json = context.getJsonSupport().toJSON(jobData);
        return json;
    }

    private PDSJobData createJobData(PDSContext context) {
        PDSAdapterConfig config = context.getConfig();
        Map<String, String> parameters = config.getJobParameters();

        PDSJobData jobData = new PDSJobData();
        for (String key : parameters.keySet()) {
            PDSJobParameterEntry parameter = new PDSJobParameterEntry();
            parameter.key = key;
            parameter.value = parameters.get(key);

            jobData.parameters.add(parameter);
        }

        UUID secHubJobUUID = config.getSecHubJobUUID();
        jobData.sechubJobUUID = secHubJobUUID.toString();
        jobData.productId=config.getPdsProductIdentifier();
        
        return jobData;
    }

    @Override
    protected String getAPIPrefix() {
        return "/api/";
    }
}
