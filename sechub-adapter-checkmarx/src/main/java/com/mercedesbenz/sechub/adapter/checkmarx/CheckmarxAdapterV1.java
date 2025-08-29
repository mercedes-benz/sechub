// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AbstractAdapter;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterProfiles;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext.ExecutionType;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxCancelFailedException;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxCancelSupport;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxFullScanNecessaryException;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxOAuthSupport;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxOnlyUnsupportedFilesException;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxProjectSupport;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxScanReportSupport;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxScanSupport;
import com.mercedesbenz.sechub.adapter.checkmarx.support.CheckmarxUploadSupport;

/**
 * This component is able to handle results from
 * <ol>
 * <li>Checkmarx V8.8.0 HF1</li>
 * </ol>
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class CheckmarxAdapterV1 extends AbstractAdapter<CheckmarxAdapterContext, CheckmarxAdapterConfig> implements CheckmarxAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxAdapterV1.class);

    private final CheckmarxCancelSupport cancelSupport = new CheckmarxCancelSupport();

    @Override
    public AdapterExecutionResult execute(CheckmarxAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {

        try {
            assertThreadNotInterrupted();

            CheckmarxContext context = new CheckmarxContext(config, this, runtimeContext);
            context.setFullScan(context.isNewProject() || config.isAlwaysFullScanEnabled());

            CheckmarxOAuthSupport oauthSupport = new CheckmarxOAuthSupport();
            oauthSupport.loginAndGetOAuthToken(context);

            if (ExecutionType.CANCEL.equals(runtimeContext.getType())) {
                cancelSupport.cancelScanInQueue(oauthSupport, context);
                return AdapterExecutionResult.createCancelResult();
            }

            assertThreadNotInterrupted();
            /* ensure project and get project context */
            CheckmarxProjectSupport projectSupport = new CheckmarxProjectSupport();
            projectSupport.ensureProjectExists(context);

            assertThreadNotInterrupted();
            handleUploadSourceCodeAndStartScan(oauthSupport, context);

            assertThreadNotInterrupted();
            CheckmarxScanReportSupport scanReportSupport = new CheckmarxScanReportSupport();
            scanReportSupport.startFetchReport(oauthSupport, context);

            return new AdapterExecutionResult(context.getResult());

        } catch (CheckmarxOnlyUnsupportedFilesException e) {

            LOG.info("Checkmarx was not able to handle the uploaded files, will mark result as canceled");
            /*
             * The cancel operation on Checkmarx side was not possible, but on SecHub side
             * the cancel operation is possible. So we still cancel the job on SecHub side.
             */
            return AdapterExecutionResult.createCancelResult();

        } catch (CheckmarxCancelFailedException e) {
            LOG.warn("Was not possible to cancel the scan on Checkmarx side: {}", e.getCheckmarxMessage());

            return AdapterExecutionResult.createCancelResult();

        } catch (Exception e) {
            if (ExecutionType.CANCEL.equals(runtimeContext.getType())) {
                LOG.warn("Was not able to prepare cancel operation for the scan on Checkmarx side: {}", e.getMessage());
                /*
                 * The cancel operation on Checkmarx side was not possible, but on SecHub side
                 * the cancel operation is possible. So we still cancel the job on SecHub side.
                 */
                return AdapterExecutionResult.createCancelResult();
            }
            throw asAdapterException("Was not able to perform scan!", e, config);
        }

    }

    @Override
    public int getAdapterVersion() {
        return 1;
    }

    private void handleUploadSourceCodeAndStartScan(CheckmarxOAuthSupport oauthSupport, CheckmarxContext context) throws AdapterException {

        try {

            uploadSourceCodeAndStartScan(oauthSupport, context);

        } catch (CheckmarxFullScanNecessaryException e) {

            LOG.info(CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX + "{} (full scan necessary)", e.getCheckmarxMessage());

            context.setFullScan(true);
            uploadSourceCodeAndStartScan(oauthSupport, context);

        }

    }

    private void uploadSourceCodeAndStartScan(CheckmarxOAuthSupport oauthSupport, CheckmarxContext context) throws AdapterException {
        AdapterMetaData metaData = context.getRuntimeContext().getMetaData();
        if (!metaData.getValueAsBoolean(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE)) {
            /* upload source code */
            oauthSupport.refreshBearerTokenWhenNecessary(context);

            CheckmarxUploadSupport uploadSupport = new CheckmarxUploadSupport();
            uploadSupport.uploadZippedSourceCode(context);

            /* after this - mark file upload done, so on a restart we don't need this */
            metaData.setValue(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE, true);
            context.getRuntimeContext().getCallback().persist(metaData);
        } else {
            LOG.info("Reuse existing upload for: {}", context.getTraceID());
        }
        /* start scan */
        CheckmarxScanSupport scanSupport = new CheckmarxScanSupport();
        scanSupport.startNewScan(oauthSupport, context);
    }

    @Override
    protected String getAPIPrefix() {
        return "cxrestapi";
    }
}
