// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"
	"path/filepath"
	"testing"

	. "daimler.com/sechub/testutil"
)

func Test_fillTemplate_without_data_keeps_data_as_is(t *testing.T) {

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

	result := fillTemplate(jStr, data)

	AssertJSONEquals(string(result), jStr, t)

}

func Test_fillTemplate_with_data_changes_template_content(t *testing.T) {

	jStrA := `
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
	jStrB := `
	{ 
        "apiVersion" : "1.2.3",
        "codeScan":{
            "zipDate":"1234",
            "fileSystem": {
                "folders": ["12345","67890"]
            }
        }
    }
	`
	data := map[string]string{
		"DATA1": "12345",
		"DATA2": "67890",
	}

	result := fillTemplate(jStrA, data)

	AssertJSONEquals(string(result), jStrB, t)
}

func Test_envToMap_works_as_expected(t *testing.T) {
	/* execute */
	data, _ := envToMap()

	/* test */
	AssertEquals("", data["THIS_VARIABLE_SHOULD_NEVER_EXIST_12345"], t)
	AssertNotEquals("", data["GOPATH"], t) // must exist, we need GOPATH to run test...

}

func Test_newSecHubConfigFromFile_does_resolve_map_entries(t *testing.T) {
	configPtr := NewConfigByFlags()
	context := NewContext(configPtr)

	os.Setenv("SHTEST_VERSION", "1.0")
	os.Setenv("SHTEST_FOLDERS1", "testProject1/src/java")

	path := filepath.Join("testdata", "sechub-testfile1.json") // relative path

	var config SecHubConfig
	config, _ = newSecHubConfigurationFromFile(context, path)
	fmt.Printf("Loaded config: %s", config)
	AssertEquals("1.0", config.APIVersion, t)
	AssertEquals("testProject1/src/java", config.CodeScan.FileSystem.Folders[0], t)

}
