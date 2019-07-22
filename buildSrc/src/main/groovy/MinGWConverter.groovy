// SPDX-License-Identifier: MIT
class MinGWConverter{
    public String convert(String path){
          if (path==null) {
              return "";
          }
          String replaced = path.replaceAll("\\\\", "/");
          if (replaced.indexOf(':')==1) {
              StringBuilder sb = new StringBuilder();
              sb.append('/');
              sb.append(replaced.substring(0,1));
              sb.append(replaced.substring(2));
              return sb.toString();
          }
          return replaced;
    }

}
