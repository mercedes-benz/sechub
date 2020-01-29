// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapter;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxFullScanNecessaryException;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxOAuthSupport;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxProjectSupport;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxScanReportSupport;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxScanSupport;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxUploadSupport;

/**
 * This component is able to handle results from
 * <ol> 
 * <li>Checkmarx V8.8.0 HF1</li>
 * </ol>
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class CheckmarxAdapterV1 extends AbstractAdapter<CheckmarxAdapterContext, CheckmarxAdapterConfig>
		implements CheckmarxAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(CheckmarxAdapterV1.class);

	@Override
	public String start(CheckmarxAdapterConfig config) throws AdapterException {
		try {
			CheckmarxContext context = new CheckmarxContext(config, this);
			context.setFullScan(context.isNewProject());
			CheckmarxOAuthSupport support = new CheckmarxOAuthSupport();
			support.loginAndGetOAuthToken(context);
			
			/* ensure project and get project context*/
			CheckmarxProjectSupport projectSupport = new CheckmarxProjectSupport();
			projectSupport.ensureProjectExists(context);
			
			handleUploadSourceCodeAndStartScan(context);
			
			CheckmarxScanReportSupport scanReportSupport = new CheckmarxScanReportSupport();
			scanReportSupport.startFetchReport(context);
			
			return context.getResult();
		} catch (Exception e) {
			throw asAdapterException("Was not able to perform scan!", e, config);
		}

	}
	@Override
	public int getAdapterVersion() {
		return 1;
	}

	private void handleUploadSourceCodeAndStartScan(CheckmarxContext context) throws AdapterException {
		try {
			uploadSourceCodeAndStartScan(context);
		}catch(CheckmarxFullScanNecessaryException e) {
			LOG.info("Full scan necessarye bcause of checkmarx message: {}",e.getCheckmarxMessage());
			context.setFullScan(true);
			uploadSourceCodeAndStartScan(context);
			
		}
	}

	private void uploadSourceCodeAndStartScan(CheckmarxContext context) throws AdapterException {
		/* upload source code */
		CheckmarxUploadSupport uploadSupport = new CheckmarxUploadSupport();
		uploadSupport.uploadZippedSourceCode(context);
		
		/* start scan */
		CheckmarxScanSupport scanSupport = new CheckmarxScanSupport();
		scanSupport.startNewScan(context);
	}
	

	@Override
	protected String getAPIPrefix() {
		return "cxrestapi";
	}
}
