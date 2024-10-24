// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProvider;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.domain.scan.access.ScanAccessCountService;
import com.mercedesbenz.sechub.domain.scan.admin.FullScanData;
import com.mercedesbenz.sechub.domain.scan.admin.FullScanDataService;
import com.mercedesbenz.sechub.domain.scan.config.ScanConfigService;
import com.mercedesbenz.sechub.domain.scan.config.ScanMapping;
import com.mercedesbenz.sechub.domain.scan.config.ScanMappingConfigurationService;
import com.mercedesbenz.sechub.domain.scan.config.ScanMappingRepository;
import com.mercedesbenz.sechub.domain.scan.config.UpdateScanMappingConfigurationService;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultCountService;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultService;
import com.mercedesbenz.sechub.domain.scan.product.config.DefaultProductExecutorConfigInfo;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfileRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigInfo;
import com.mercedesbenz.sechub.domain.scan.product.config.WithoutProductExecutorConfigInfo;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigRepository;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportCountService;
import com.mercedesbenz.sechub.sharedkernel.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingIdentifier;

/**
 * Contains additional rest call functionality for integration tests on scan
 * domain
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestScanRestController {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestScanRestController.class);

    @Autowired
    private ScanAccessCountService scanAccessCountService;

    @Autowired
    private ProductResultCountService productResultCountService;

    @Autowired
    private ScanReportCountService scanReportCountService;

    @Autowired
    private UpdateScanMappingConfigurationService updateScanMappingService;

    @Autowired
    private ScanMappingRepository scanMappingrepository;

    @Autowired
    private ScanMappingConfigurationService scanMappingConfigurationService;

    @Autowired
    private IntegrationTestScanJobListener scanJobCancelService;

    @Autowired
    private ProductResultService productResultService;

    @Autowired
    private FullScanDataService fullScanDataService;

    @Autowired
    ProductResultRepository productResultRepository;

    @Autowired
    ProductExecutionProfileRepository profileRepository;

    @Autowired
    private ScanConfigService scanConfigService;

    @Autowired
    private ScanProjectConfigRepository scanProjectConfigRepository;

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/autocleanup/inspection/scan/days", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public long fetchScheduleAutoCleanupConfiguredDays() {
        return scanConfigService.getAutoCleanupInDays();
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/config/execution/profile/{profileId}/exists", method = RequestMethod.GET, produces = {
            MediaType.TEXT_PLAIN_VALUE })
    public String executionProfileExists(@PathVariable("profileId") String profileId) {
        return "" + profileRepository.existsById(profileId);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/project/{projectId}/scan/access/count", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public long countProjectAccess(@PathVariable("projectId") String projectId) {
        return scanAccessCountService.countProjectAccess(projectId);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/project/{projectId}/scan/report/count", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public long countProductResults(@PathVariable("projectId") String projectId) {
        return scanReportCountService.countProjectProductResults(projectId);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/project/{projectId}/scan/productresult/count", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public long countScanResults(@PathVariable("projectId") String projectId) {
        return productResultCountService.countProjectScanResults(projectId);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/job/{jobUUID}/fullscandata", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public FullScanData countScanResults(@PathVariable("jobUUID") UUID secHubJobUUID) {
        return fullScanDataService.getFullScanData(secHubJobUUID);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS
            + "integrationtest/project/{projectId}/scan/productresult/all-shrinked/{maxLength}", method = RequestMethod.GET, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    public List<ProductResult> fetchScanResults(@PathVariable("projectId") String projectId, @PathVariable("maxLength") int maxLength) {
        if (maxLength < 10) {
            maxLength = 10;
        }
        List<ProductResult> originResults = productResultService.fetchAllResultsInProject(projectId);
        List<ProductResult> shrinkedResults = new ArrayList<ProductResult>();
        for (ProductResult originProductResult : originResults) {
            String result = originProductResult.getResult();
            if (result.length() > maxLength) {
                result = result.substring(0, maxLength - 3) + "...";
            }
            ProductExecutorConfigInfo info = new DefaultProductExecutorConfigInfo(originProductResult.getProductIdentifier(),
                    originProductResult.getProductExecutorConfigUUID());
            ProductResult shrinked = new ProductResult(originProductResult.getSecHubJobUUID(), originProductResult.getProjectId(), info, result);
            shrinkedResults.add(shrinked);
        }
        return shrinkedResults;
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS
            + "integrationtest/project/{projectId}/job/{sechubJobUUID}/scan/productresult/{productIdentifier}", method = RequestMethod.PUT, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    public void changeScanResults(@RequestBody String body, @PathVariable("projectId") String projectId, @PathVariable("sechubJobUUID") UUID sechubJobUUID,
            @PathVariable("productIdentifier") ProductIdentifier productIdentifier) {

        List<ProductResult> originResults = productResultService.fetchAllResultsForJob(sechubJobUUID);

        ProductResult result = null;
        for (ProductResult originProductResult : originResults) {
            if (productIdentifier.equals(originProductResult.getProductIdentifier())) {
                result = originProductResult;
                break;
            }
        }
        ProductResult resultToPersist = null;
        if (result != null) {
            resultToPersist = result;
        } else {
            // We do not know which executor this has been done - it's a new one, so we
            // create just a new configuration info without UUID
            WithoutProductExecutorConfigInfo info = new WithoutProductExecutorConfigInfo(productIdentifier);
            resultToPersist = new ProductResult(sechubJobUUID, projectId, info, body);

        }
        resultToPersist.setResult(body);
        productResultRepository.save(resultToPersist);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/job/{sechubJobUUID}/productresults", method = RequestMethod.DELETE, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public void deleteAllJobResults(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        productResultService.deleteAllResultsForJob(sechubJobUUID);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/job/{sechubJobUUID}/productresults-count", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public String countProductresults(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        long result = productResultService.fetchAllResultsForJob(sechubJobUUID).size();
        return "" + result;
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/scan/cancel/jobs", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public long cancelAllJobs() {
        LOG.info("Integration test request a cancel for all Jobs");
        /* return amount of canceled jobs */
        return scanJobCancelService.cancelAll();
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/config/scan/mapping/{mappingId}", method = RequestMethod.PUT)
    public void updateScanMapping(@PathVariable("mappingId") String mappingId, @RequestBody MappingData mappingData) {
        updateScanMappingService.updateScanMapping(mappingId, mappingData);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/config/scan/mapping/{mappingId}", method = RequestMethod.GET)
    public MappingData fetchScanMappingData(@PathVariable("mappingId") String mappingId) {
        Optional<ScanMapping> found = scanMappingrepository.findById(mappingId);
        if (found.isPresent()) {
            MappingData data = MappingData.fromString(found.get().getData());
            return data;
        }
        return null;

    }

    @SuppressWarnings("deprecation")
    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/config/namepattern/{namePatternProviderId}/{name}", method = RequestMethod.GET)
    public String getIdForNameByProvider(@PathVariable("namePatternProviderId") String namePatternProviderId, @PathVariable("name") String name) {
        MappingIdentifier mappingIdentifier = MappingIdentifier.valueOf(namePatternProviderId);
        NamePatternIdProvider provider = scanMappingConfigurationService.getNamePatternIdProvider(mappingIdentifier);
        if (provider == null) {
            return null;
        }
        String id = provider.getIdForName(name);
        return id;

    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/job/{jobUUID}/pds/uuids", method = RequestMethod.GET)
    public List<UUID> getPDSJobUUIDSForSecHubJOob(@PathVariable("jobUUID") UUID sechubJob) {
        List<UUID> list = new ArrayList<>();
        List<ProductResult> productResults = productResultService.fetchAllResultsForJob(sechubJob);
        boolean pdsResultFound = false;
        for (ProductResult productResult : productResults) {
            ProductIdentifier identifier = productResult.getProductIdentifier();
            if (!identifier.toString().startsWith("PDS_")) {
                continue;
            }
            pdsResultFound = true;
            String json = productResult.getMetaData();
            if (json == null) {
                continue;
            }
            try {
                AdapterMetaData metaData = JSONConverter.get().fromJSON(AdapterMetaData.class, json);
                String valueOrNull = metaData.getValueAsStringOrNull("PDS_JOB_UUID");
                if (valueOrNull == null) {
                    LOG.trace("No PDS_JOB_UUID inside adapter metadata - product result:{}", productResult.getUUID());
                    continue;
                }
                UUID pdsJobUUID = UUID.fromString(valueOrNull);
                list.add(pdsJobUUID);
                LOG.info("Found PDS job uuid: {} for sechub job:{}", pdsJobUUID, sechubJob);
            } catch (RuntimeException e) {
                LOG.error("Was not able to convert to adapter meta data  - product result uuid:{}, metaData={}", productResult.getUUID(), json);
            }
        }
        LOG.info("PDS product result found in database: {}. list = {}", pdsResultFound, list);
        return list;
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/project-scanconfig/{projectId}", method = RequestMethod.GET)
    public List<ScanProjectConfig> fetchScanConfigValue(@PathVariable("projectId") String projectId) {
        return scanProjectConfigRepository.findAllForProject(projectId);
    }

}
