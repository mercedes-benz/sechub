// SPDX-License-Identifier: MIT
package cli

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
)

// The configuration pendant in Go. But we do only necessary parts from JSON file!
// so Webscan, InfraScan are not handled here (but still uploaded)
// Only code scan is necessary, because determination necessary if there is an upload necessary or not.
type SecHubConfig struct {
	APIVersion  string         `json:"apiVersion"`
	User        string         `json:"user"`
	ProjectId string         `json:"project"`
	Server      string         `json:"server"`
	CodeScan    CodeScanConfig `json:"codeScan"`
}

type CodeScanConfig struct {
	FileSystem FileSystemConfig `json:"fileSystem"`
	Excludes []string `json:"excludes"`
}

type FileSystemConfig struct {
	Folders []string `json:"folders"`
}

func newSecHubConfigFromString(json string) SecHubConfig {
	return newSecHubConfigFromBytes([]byte(json))
}

func newSecHubConfigFromBytes(bytes []byte) SecHubConfig {
	var sechubConfig SecHubConfig

	/* transform text to json */
	err := json.Unmarshal(bytes, &sechubConfig)
	if err != nil {
		fmt.Println("sechub confiuration json is not valid json")
		showHelpHint()
		os.Exit(EXIT_CODE_MISSING_CONFIGFILE)
	}
	return sechubConfig
}

func showHelpHint() {
	fmt.Println("Call sechub with --help option to show correct usage and examples")
}

func newSecHubConfigurationFromFile(context *Context, filePath string) SecHubConfig {
	LogDebug(context, fmt.Sprintf("Loading config file: '%s'\n", filePath))

	/* open file and check exists */
	jsonFile, err := os.Open(filePath)
	defer jsonFile.Close()

	if err != nil {
		fmt.Println(err)
		showHelpHint()
		os.Exit(EXIT_CODE_MISSING_CONFIGFILE)
	}

	/* read text content */
	context.byteValue, err = ioutil.ReadAll(jsonFile)
	if err != nil {
		fmt.Println(err)
		showHelpHint()
		os.Exit(EXIT_CODE_MISSING_CONFIGFILE)
	}

	return newSecHubConfigFromBytes(context.byteValue)
}
