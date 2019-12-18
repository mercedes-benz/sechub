package com.daimler.sechub.sharedkernel.metadata;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.Profiles;

@Component
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestMetaDataInspector implements MetaDataInspector{

	private List<MapStorageMetaDataInspection> inspections = new ArrayList<MapStorageMetaDataInspection>();

	public void clear() {
		inspections.clear();
	}

	@Override
	public MetaDataInspection inspect(String id) {
		MapStorageMetaDataInspection collection = new MapStorageMetaDataInspection(id);
		inspections.add(collection);
		return collection;
	}

	public List<MapStorageMetaDataInspection> getInspections(){
		return inspections;
	}

}
