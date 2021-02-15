package com.daimler.sechub.developertools.admin.ui;

import com.daimler.sechub.developertools.admin.ui.action.adapter.ShowProductExecutorTemplatesDialogAction;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.Necessarity;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.Type;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;

public class ShowProductExecutorTemplatesDialogActionFactory {

    public static ShowProductExecutorTemplatesDialogAction createCheckmarxV1Action(UIContext context) {
        TemplatesDialogData data = new TemplatesDialogData();
        data.add(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId(),Type.MAPPING,Necessarity.OPTIONAL,"Here we can define presetid mappings for new projects");
        data.add(MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId(),Type.MAPPING,Necessarity.MANDATORY,"Here we must define teamId mapping for new projects");
        
        data.add("checkmarx.engineConfigurationName",Type.KEY_VALUE,Necessarity.OPTIONAL,"The engine configuration name. If not set default will be used","somevalue");
        data.add("checkmarx.clientSecret",Type.KEY_VALUE,Necessarity.OPTIONAL,"Normally a static 'secret' - if it ever changes, we can change this here");
        data.add("checkmarx.fullscan.always",Type.KEY_VALUE,Necessarity.OPTIONAL,"If enabled, Checkmarx will always do a fullscan. If disabled, checkmarx will do a delta scan and in case it does not work, a fullscan will be performed. Default value is: false","true");
        
        ShowProductExecutorTemplatesDialogAction action = new ShowProductExecutorTemplatesDialogAction(context, ProductIdentifier.CHECKMARX, 1, data);
        return action;
    }
}
