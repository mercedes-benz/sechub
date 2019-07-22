// SPDX-License-Identifier: MIT
package cli

import "fmt"

func LogError(text string){
    var result string = "ERROR: "+text+"\n"//fmt.Sprintf(text,a)
    fmt.Printf(result)
}

func LogVerbose(text string){
    var result string = "VERBOSE: "+text+"\n"//fmt.Sprintf(text,a)
    fmt.Printf(result)
}

func LogDebug(context *Context, text string){
    if (! context.config.debug){
        return
    }
    var result string = "DEBUG: "+text+"\n"//fmt.Sprintf(text,a)
    fmt.Printf(result)
}