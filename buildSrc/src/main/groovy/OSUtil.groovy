// SPDX-License-Identifier: MIT
class OSUtil{
     public static final boolean isWindows(){
          String osName = System.getProperty("os.name").toLowerCase();
          return osName.contains("windows");
     }
}

