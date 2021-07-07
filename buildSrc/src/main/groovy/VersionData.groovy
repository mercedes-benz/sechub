// SPDX-License-Identifier: MIT
class VersionData{

    private static final String ID_PDS = "pds";
    private static final String ID_CLIENT = "client"
    private static final String ID_SERVER = "server"
    private static final String ID_WEBSITE = "website"
    
    private static Map<String,VersionInfo> map = new HashMap<>();
    
    
    public static class VersionInfo{
        
        String fullVersion
        String shortVersion
        String id
        String text
        
        public String describe(){
            return "- "+text+" :"+shortVersion+" ["+fullVersion+"]"
        }
        
    }
    
    static{
        /* initialize */
        initialize(ID_SERVER, "Server ")
        initialize(ID_CLIENT, "Client ")
        initialize(ID_PDS,    "PDS    ")
        initialize(ID_WEBSITE,"Website")
    }
    
    
    static void initialize(String id,String text){
        VersionInfo info = new VersionInfo()
        info.id=id;
        info.text=text;
        info.fullVersion="undefined-long-"+id+"version"
        info.shortVersion="undefined-"+id+"version"
        map.put(id, info)
    }
    
    static boolean containingAtLeastOneDirtyReleaseVersion
    static boolean containingAtLeastOneRealReleaseVersion
    
    public static VersionInfo defineVersion(String versionType, String fullVersion){
        
        VersionInfo info = map.get(versionType.toLowerCase());
        if (info==null){
            throw new IllegalArgumentException("unsupported version type:"+versionType);
        }
        inspectReleaseVersion(fullVersion);
        info.shortVersion = simplifiedVersion(fullVersion);
        info.fullVersion= fullVersion
        
        return info;
    }
    
    /**
     * Convenience method, returns short name 
     */
    public static String getServerVersion(){
        return map.get(ID_SERVER).getShortVersion()
    }
    
    /**
     * Convenience method, returns short name 
     */
    public static String getClientVersion(){
        return map.get(ID_CLIENT).getShortVersion()
    }
    
    /**
     * Convenience method, returns short name 
     */
    public static String getPdsVersion(){
        return map.get(ID_PDS).getShortVersion()
    }
    
      /**
     * Convenience method, returns short name 
     */
    public static String getWebsiteVersion(){
        return map.get(ID_WEBSITE).getShortVersion()
    }
    
    
    
    /**
     * Inpsect versoin - if not starting with 0.0.0 this means it's a release ando
     *                   a "dirty" may not be contained inside long version name
     */
    private static void inspectReleaseVersion(String longVersionName){
        if (longVersionName.startsWith("0.0.0")){
            /* not a correct release version so ignore */
            return
        }
        containingAtLeastOneDirtyReleaseVersion=containingAtLeastOneDirtyReleaseVersion || longVersionName.contains("dirty")
        containingAtLeastOneRealReleaseVersion=true
    }
    
    
    /**
     * Simplifies given version string . e.g. 0.4.1-b74 will be reduced to 0.4.1
     */
    private static String simplifiedVersion(String fullVersion){
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