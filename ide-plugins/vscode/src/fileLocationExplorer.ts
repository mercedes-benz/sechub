// SPDX-License-Identifier: MIT
import * as path from 'path';
import * as fs from 'fs';

export class FileLocationExplorer {
    searchFolders = new Set<String>();

    /**
     * Searches for given location string. 
     * 
     * For example: 
     * 
     * - location (from report) is         :   
     *   "/java/com/example/TestMe.java" and   
     * - search folder is defined as       :   
     *   "/home/developer/projects/project1"   
     * - project1 does contain structure at:   
     *   "/home/developer/projects/project1/subproject/java/com/example/Testme.java"  
     * 
     * then letter shall be in results.
     * 
     * @param location represents known location to search for inside defined search folders
     * @return set of matching file pathes
     */
    public searchFor(location: string): Set<String> {
        // var fs = require('fs');
        var result = new Set<String>();
        this.searchFolders.forEach((folder) => {
            var strFolder: string = folder.toString();
            this.searchFilesRecursive(location, strFolder, result);
        });


        return result;
    }

    private searchFilesRecursive(location: string, folder: string, result: Set<String>) {
        

        for (const file of fs.readdirSync(folder)) {
            const fullPath = path.resolve(folder,file);
            if(fs.lstatSync(fullPath).isDirectory()){
                this.searchFilesRecursive(location, fullPath, result);
            }else{
                /* check the path is reached */
                if (fullPath.endsWith(location)){
                    /* found */
                    result.add(new String(fullPath));
                    break;
                }

            }
        }
    }

   
}
