// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;

public class UseCaseRestDocModel {

    private Map<String, List<UseCaseRestDocEntry>> map = new TreeMap<>();
    private UseCaseModel useCaseModel;

    public UseCaseRestDocModel(UseCaseModel useCaseModel) {
        if (useCaseModel == null) {
            throw new IllegalArgumentException();
        }
        this.useCaseModel = useCaseModel;
    }

    public UseCaseModel getUseCaseModel() {
        return useCaseModel;
    }

    public List<UseCaseRestDocEntry> getRestDocEntries(UseCaseEntry entry) {
        return map.get(entry.getId());
    }

    public void add(UseCaseRestDocEntry restDocEntry) {
        String key = restDocEntry.usecaseEntry.getId();
        List<UseCaseRestDocEntry> list = map.computeIfAbsent(key, k -> new ArrayList<>());
        list.add(restDocEntry);
    }

}
