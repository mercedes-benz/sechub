package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareStatus;
import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.moduls.PrepareWrapperGitModule;
import com.mercedesbenz.sechub.wrapper.prepare.moduls.PrepareWrapperModule;

@Service
public class PrepareWrapperPreparationService {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperPreparationService.class);

    @Autowired
    PrepareWrapperEnvironment environment;

    @Autowired
    PrepareWrapperContextFactory factory;

    @Autowired
    PrepareWrapperRemoteConfigurationExtractor extractor;

    @Autowired
    PrepareWrapperGitModule gitModule;

    List<PrepareWrapperModule> modules = new ArrayList<>();

    public AdapterExecutionResult startPreparation() throws IOException {
        // TODO: 17.04.24 laura add gitModule to modules - where?

        LOG.debug("Start preparation");
        PrepareWrapperContext context = factory.create(environment);
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = extractor.extract(context.getSecHubConfiguration());

        if (remoteDataConfigurationList.isEmpty()) {
            LOG.warn("No Remote configuration was found");
            PrepareResult result = new PrepareResult(PrepareStatus.OK);
            SecHubMessage message = new SecHubMessage(SecHubMessageType.WARNING, "No Remote Configuration found");
            Collection<SecHubMessage> collection = new ArrayList<>();
            collection.add(message);
            return new AdapterExecutionResult(result.toString(), collection);
        }
        for (PrepareWrapperModule module : modules) {
            SecHubConfigurationModel sechubConfiguration = context.getSecHubConfiguration();
            if (module.isAbleToPrepare(sechubConfiguration)) {
                String folder = context.getEnvironment().getPdsPrepareUploadFolderDirectory();
                module.prepare(sechubConfiguration, folder);
                if (isDownloadedDataInFolder(folder)) {
                    // clean directory if download was successful from unwanted files (e.g. .git
                    // files)
                    module.cleanDirectory(folder);
                } else {
                    PrepareResult result = new PrepareResult(PrepareStatus.FAILED);
                    SecHubMessage message = new SecHubMessage(SecHubMessageType.ERROR, "Download of configured remote data failed");
                    Collection<SecHubMessage> collection = new ArrayList<>();
                    collection.add(message);
                    return new AdapterExecutionResult(result.toString(), collection);
                }
            }
        }
        PrepareResult result = new PrepareResult(PrepareStatus.OK);
        return new AdapterExecutionResult(result.toString());
    }

    private boolean isDownloadedDataInFolder(String folder) throws IOException {
        // check if download folder is not empty
        Path path = Paths.get(folder);
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
                return !directory.iterator().hasNext();
            }
        }
        return false;
    }

}
