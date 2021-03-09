// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TemplatesDialogData {
    
    private TreeMap<String,TemplateData> map = new TreeMap<>();

    public TemplatesDialogData() {
    }
    public void add(String key, Type type, Necessarity necessarity, String description) {
        add(key,type,necessarity,description,null);
    }
    public void add(String key, Type type, Necessarity necessarity, String description, String example) {
        TemplateData data = new TemplateData();
        data.key=key;
        data.type=type;
        data.necessarity=necessarity;
        data.description=description;
        data.example=example;
        
        map.put(key, data);
    }
    
    public TemplateData getData(String key) {
        return map.get(key);
    }

    
    public List<TemplateData> getMappingData(){
        return getData(Type.MAPPING);
    }
    
    public List<TemplateData> getKeyValueData(){
        return getData(Type.KEY_VALUE);
    }
    
    private List<TemplateData> getData(Type type){
        List<TemplateData> list = new ArrayList<>();
        for (TemplateData data: map.values()) {
            if (data.type.equals(type)) {
                list.add(data);
            }
        }
        return list;
    }
    
    
    public static enum Type{
        MAPPING,
        KEY_VALUE,
        UNKNOWN,
    }
    
    public static enum Necessarity{
        OPTIONAL,
        UNKNOWN,
        MANDATORY,
    }
    
    
    public static class TemplateData{
        public Type type;
        public Necessarity necessarity;
        public String key;
        public String description;
        public String example;
    }


   
}
