// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"
	"path/filepath"
	"testing"

	. "mercedes-benz.com/sechub/testutil"
)

func Test_fillTemplate_without_data_keeps_data_as_is(t *testing.T) {

	jStr := `
    {
        "apiVersion" : "1.2.3",
        "codeScan":{
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

func Example_newSecHubConfigFromFile_parses_data_sources_section_correctly() {
	/* prepare */
	sechubJSON := `
		{
			"data": {
				"sources": [
					{
						"name": "reference-name-sources-1",
						"fileSystem": {
							"files": [
								"somewhere/file1.txt",
								"somewhere/file2.txt"
							],
							"folders": [
								"somewhere/subfolder1",
								"somewhere/subfolder2"
							]
						},
						"excludes": [
							"**/mytestcode/**",
							"**/documentation/**"
						],
						"additionalFilenameExtensions": [
							".cplusplus",
							".py9"
						]
					}
				]
			}
		}
	`
	/* execute */
	result := newSecHubConfigFromBytes([]byte(sechubJSON))

	/* test */
	fmt.Printf("%+v\n", result.Data.Sources)
	// Output:
	// [{Name:reference-name-sources-1 FileSystem:{Files:[somewhere/file1.txt somewhere/file2.txt] Folders:[somewhere/subfolder1 somewhere/subfolder2]} Excludes:[**/mytestcode/** **/documentation/**] SourceCodePatterns:[.cplusplus .py9]}]
}

func Example_newSecHubConfigFromFile_parses_data_binary_section_correctly() {
	/* prepare */
	sechubJSON := `
		{
			"data": {
				"binaries": [
					{
						"name": "reference-name-binaries-1",
						"fileSystem": {
							"files": [ "somewhere/file1.dll", "somewhere/file2.a" ],
							"folders": [ "somewhere/bin/subfolder1", "somewhere/bin/subfolder2" ]
						}
					},
					{
						"name": "reference-name-binaries-2",
						"fileSystem": {
							"files": [ "somewhere-else/mylib.so" ],
							"folders": [ "somewhere-else/lib" ]
						}
					}
				]
			}
		}
	`
	/* execute */
	result := newSecHubConfigFromBytes([]byte(sechubJSON))

	/* test */
	fmt.Printf("%+v\n", result.Data.Binaries)
	// Output:
	// [{Name:reference-name-binaries-1 FileSystem:{Files:[somewhere/file1.dll somewhere/file2.a] Folders:[somewhere/bin/subfolder1 somewhere/bin/subfolder2]}} {Name:reference-name-binaries-2 FileSystem:{Files:[somewhere-else/mylib.so] Folders:[somewhere-else/lib]}}]
}

func Example_newSecHubConfigFromFile_parses_codeScan_use_correctly() {
	/* prepare */
	sechubJSON := `
		{
			"data": {
				"sources": [
					{
						"name": "mysources-1",
						"fileSystem": { "folders": [ "src1/" ] }
					},
					{
						"name": "mysources-2",
						"fileSystem": { "folders": [ "src2/" ] }
					}
				]
			},
			"codeScan": {
				"use": [ "mysources-1", "mysources-2" ]
			}
		}
	`
	/* execute */
	result := newSecHubConfigFromBytes([]byte(sechubJSON))

	/* test */
	fmt.Printf("%+v\n", result.CodeScan.Use)
	// Output:
	// [mysources-1 mysources-2]
}
