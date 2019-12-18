package com.daimler.sechub.sharedkernel.metadata;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.Profiles;

@Component
@Profile("!"+Profiles.INTEGRATIONTEST)
public class DefaultMetaDataInspector implements MetaDataInspector{

	@Override
	public MetaDataInspection inspect(String id) {
		MetaDataInspection collection = new LoggingMetaDataInspection(id);
		return collection;
	}

}
