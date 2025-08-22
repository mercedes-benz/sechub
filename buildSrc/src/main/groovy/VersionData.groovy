// SPDX-License-Identifier: MIT
class VersionData{

    private static final String ID_CLIENT = "client"
    private static final String ID_LIBRARIES = "libraries"
    private static final String ID_PDS = "pds";
    private static final String ID_PDS_TOOLS = "pds-tools"
    private static final String ID_SERVER = "server"
    private static final String ID_WEB_SERVER = "web server"
    private static final String ID_WRAPPER_CHECKMARX = "checkmarx wrapper"
    private static final String ID_WRAPPER_OWASPZAP = "owasp-zap wrapper"
    private static final String ID_WRAPPER_PREPARE= "prepare wrapper"
    private static final String ID_WRAPPER_SECRETVALIDATION= "secretvalidation wrapper"
    private static final String ID_WRAPPER_XRAY= "xray wrapper"
    private static final String ID_WRAPPER_INFRALIGHT= "infralight wrapper"

    private StringBuilder debugInfo = new StringBuilder();

    private Map<String,VersionInfo> map = new HashMap<>();

    boolean containingAtLeastOneDirtyReleaseVersion
    boolean containingAtLeastOneRealReleaseVersion

    public VersionData(){

        /* initialize */
        initialize(ID_CLIENT,   "Client")
        initialize(ID_LIBRARIES,"Libraries")
        initialize(ID_PDS,      "PDS")
        initialize(ID_PDS_TOOLS,"PDS-Tools")
        initialize(ID_SERVER,   "Server")
        initialize(ID_WEB_SERVER,    "Web Server")
        initialize(ID_WRAPPER_CHECKMARX,        "Checkmarx Wrapper")
        initialize(ID_WRAPPER_OWASPZAP,         "OWASP-ZAP Wrapper")
        initialize(ID_WRAPPER_PREPARE,          "Prepare Wrapper")
        initialize(ID_WRAPPER_SECRETVALIDATION, "SecretValidation Wrapper")
        initialize(ID_WRAPPER_XRAY,             "Xray Wrapper")
        initialize(ID_WRAPPER_INFRALIGHT,       "Infralight Wrapper")
    }

    public class VersionInfo{

        String id
        String text
        String fullVersion
        String shortVersion
        String shortVersionForDocs

        public String describe(){
            return text.padLeft(24)+": "+shortVersion+" ("+fullVersion+") docs: "+shortVersionForDocs
        }

    }

    void initialize(String id,String text){

        VersionInfo info = new VersionInfo()

        info.id = id;
        info.text = text;
        info.fullVersion = "undefined-long-"+id+"version"
        info.shortVersion = "undefined-"+id+"version"
        info.shortVersionForDocs = info.shortVersion
        map.put(id, info)
    }

    public VersionInfo defineVersion(String versionType, String fullVersion, String shortVersionForDocs){

        VersionInfo info = map.get(versionType.toLowerCase());
        if (info==null){
            throw new IllegalArgumentException("unsupported version type:"+versionType);
        }
        inspectReleaseVersion(versionType, fullVersion);
        info.shortVersion = simplifiedVersion(fullVersion);
        info.fullVersion = fullVersion
        info.shortVersionForDocs = shortVersionForDocs

        return info;
    }

    /**
     * Convenience methods: return short version
     */

    public String getCheckmarxWrapperVersion(){
        return map.get(ID_WRAPPER_CHECKMARX).getShortVersion()
    }

    public String getClientVersion(){
        return map.get(ID_CLIENT).getShortVersion()
    }
    public String getClientDocsVersion(){
        return map.get(ID_CLIENT).getShortVersionForDocs()
    }

    public String getLibrariesVersion(){
        return map.get(ID_LIBRARIES).getShortVersion()
    }

    public String getOwaspzapWrapperVersion(){
        return map.get(ID_WRAPPER_OWASPZAP).getShortVersion()
    }

    public String getPdsVersion(){
        return map.get(ID_PDS).getShortVersion()
    }
    public String getPdsDocsVersion(){
        return map.get(ID_PDS).getShortVersionForDocs()
    }

    public String getPdsToolsVersion(){
        return map.get(ID_PDS_TOOLS).getShortVersion()
    }

    public String getServerVersion(){
        return map.get(ID_SERVER).getShortVersion()
    }
    
    public String getServerDocsVersion(){
        return map.get(ID_SERVER).getShortVersionForDocs()
    }

    public String getWebServerVersion(){
        return map.get(ID_WEB_SERVER).getShortVersion()
    }

    public String getXrayWrapperVersion(){
        return map.get(ID_WRAPPER_XRAY).getShortVersion()
    }

    public String getPrepareWrapperVersion(){
        return map.get(ID_WRAPPER_PREPARE).getShortVersion()
    }

    public String getSecretvalidationWrapperVersion(){
        return map.get(ID_WRAPPER_SECRETVALIDATION).getShortVersion()
    }
    
    public String getInfralightWrapperVersion(){
        return map.get(ID_WRAPPER_INFRALIGHT).getShortVersion()
    }

    public String getDebugInfo(){

        return "Debug info:\ncontainingAtLeastOneDirtyReleaseVersion=$containingAtLeastOneDirtyReleaseVersion\ncontainingAtLeastOneRealReleaseVersion=$containingAtLeastOneRealReleaseVersion\n\n$debugInfo";
    }


    /**
     * Inspect version - if not starting with 0.0.0 this means it's a release, so
     *                   a "dirty" may not be contained inside long version name
     */
    private void inspectReleaseVersion(String versionType, String longVersionName){
        debugInfo.append("\ninspect $versionType release version: long version=$longVersionName\n")
        debugInfo.append("- at least one release found : $containingAtLeastOneRealReleaseVersion, one release dirty: $containingAtLeastOneDirtyReleaseVersion\n")

        if (longVersionName.startsWith("0.0.0")){
            /* not a correct release version so ignore */
            return
        }
        containingAtLeastOneDirtyReleaseVersion=containingAtLeastOneDirtyReleaseVersion || longVersionName.contains("dirty")
        containingAtLeastOneRealReleaseVersion=true

        debugInfo.append("- updated data")
        debugInfo.append("- at least one release found : $containingAtLeastOneRealReleaseVersion, one release dirty: $containingAtLeastOneDirtyReleaseVersion\n")
    }

    /**
     * Simplifies given version string . e.g. 0.4.1-b74 will be reduced to 0.4.1
     */
    private String simplifiedVersion(String fullVersion){
        if (fullVersion==null){
            return "0.0.0";
        }
        int index = fullVersion.indexOf('-');
        if (index==-1){
            return fullVersion;
        }
        return fullVersion.substring(0,index);
    }

}