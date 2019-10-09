// SPDX-License-Identifier: MIT
package cli

import (
	"fmt"
	"testing"

	. "daimler.com/sechub/testutil"
)

func Test_newSecHubConfigFromString_works_for_given_json(t *testing.T) {

	jStr := `
    {
        "apiVersion" : "1.2.3",
        "codeScan":{
            "zipDate":"1234",
            "fileSystem": {
                "folders": ["1111","2222"]
            }
        }
    }
    `
	data := map[string]string{}

	var config SecHubConfig = newSecHubConfigFromTemplate(jStr, data)
	fmt.Printf("Loaded config: %s", config)
	AssertEquals("1.2.3", config.APIVersion, t)
	AssertEquals("1111", config.CodeScan.FileSystem.Folders[0], t)
	AssertEquals("2222", config.CodeScan.FileSystem.Folders[1], t)
}

func Test_newSecHubConfigFromString_works_for_given_json_with_template_map(t *testing.T) {

	jStr := `
    {
        "apiVersion" : "1.2.3",
        "codeScan":{
            "zipDate":"1234",
            "fileSystem": {
                "folders": ["{{ .DATA1 }}","{{ .DATA2 }}"]
            }
        }
    }
    `
	data := map[string]string{
		"DATA1": "12345",
		"DATA2": "67890",
	}
	var config SecHubConfig = newSecHubConfigFromTemplate(jStr, data)
	fmt.Printf("Loaded config: %s", config)
	AssertEquals("1.2.3", config.APIVersion, t)
	AssertEquals("12345", config.CodeScan.FileSystem.Folders[0], t)
	AssertEquals("67890", config.CodeScan.FileSystem.Folders[1], t)
}

func Test_envToMap_works_as_expected(t *testing.T) {
	/* execute */
	data, _ := envToMap()

	/* test */
	AssertEquals("", data["THIS_VARIABLE_SHOULD_NEVER_EXIST_12345"], t)
	AssertNotEquals("", data["GOPATH"], t) // must exist, we need GOPATH to run test...

}
