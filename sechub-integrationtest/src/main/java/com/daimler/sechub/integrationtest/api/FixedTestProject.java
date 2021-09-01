package com.daimler.sechub.integrationtest.api;

import com.daimler.sechub.integrationtest.internal.TestScenario;

public class FixedTestProject extends TestProject{

    private String fixedProjectId;
    
    public FixedTestProject(String projectId){
        super("fixed project with id:"+projectId);
        this.fixedProjectId=projectId;
    }
    
    @Override
    public String getProjectId() {
        return fixedProjectId;
    }
    
    @Override
    public void prepare(TestScenario scenario) {
        /* we do not prepare fixed ones */
    }
}
