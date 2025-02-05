package com.mercedesbenz.sechub.domain.scan.template;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class TemplatesHealthCheckResult implements JSONable<TemplatesHealthCheckResult> {

    private static TemplatesHealthCheckResult IMPORTER = new TemplatesHealthCheckResult();

    private TemplatesHealthCheckStatus status;

    private List<TemplateHealthCheckEntry> entries = new ArrayList<>();

    public void setStatus(TemplatesHealthCheckStatus status) {
        this.status = status;
    }

    public TemplatesHealthCheckStatus getStatus() {
        return status;
    }

    public List<TemplateHealthCheckEntry> getEntries() {
        return entries;
    }

    @Override
    public Class<TemplatesHealthCheckResult> getJSONTargetClass() {
        return TemplatesHealthCheckResult.class;
    }

    public static TemplatesHealthCheckResult fromJson(String json) {
        return IMPORTER.fromJSON(json);
    }

}
