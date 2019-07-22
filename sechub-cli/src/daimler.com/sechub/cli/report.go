// SPDX-License-Identifier: MIT
package cli

import (
    "fmt"
    "os"
    "io/ioutil"
    "path/filepath"
    "encoding/json"
    "bytes"
)

type Report struct{
    outputFileName string
    outputFolder string
    serverResult string 
}

func (report *Report) save(context *Context) {
    
    filePath:= report.createReportFilePath(context, true)
    LogDebug(context, fmt.Sprintf("filepath %s:\n", filePath))
    content:=report.serverResult
    if (context.config.reportFormat=="json"){
        content=jsonPrettyPrint(content)
    }
    
    d1 := []byte(content)
    err := ioutil.WriteFile(filePath, d1, 0644)
    HandleError(err)
    fmt.Printf("  SecHub report written to %s\n", filePath)    
}

func (report *Report) createReportFilePath(context *Context, forceDirectory bool) string{
    path:=report.outputFolder
    if (forceDirectory){
        if _, err := os.Stat(path); os.IsNotExist(err) {
            os.MkdirAll(path, os.ModePerm)
        }
    }
    result:= filepath.Join(path,report.outputFileName)
    return result
}

func jsonPrettyPrint(in string) string {
    var out bytes.Buffer
    err := json.Indent(&out, []byte(in), "", "   ")
    if err != nil {
        return in
    }
    return out.String()
}