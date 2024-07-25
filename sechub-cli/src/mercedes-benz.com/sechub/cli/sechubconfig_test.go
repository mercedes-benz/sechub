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
						},
						"excludes": [ "*.test" ]
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
	// [{Name:reference-name-binaries-1 FileSystem:{Files:[somewhere/file1.dll somewhere/file2.a] Folders:[somewhere/bin/subfolder1 somewhere/bin/subfolder2]} Excludes:[*.test]} {Name:reference-name-binaries-2 FileSystem:{Files:[somewhere-else/mylib.so] Folders:[somewhere-else/lib]} Excludes:[]}]
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

func Example_adjustSourceFilterPatterns_respects_whitelistAll_false() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.whitelistAll = false

	// Override global DefaultSourceCodeAllowedFilePatterns to get reproducable results
	DefaultSourceCodeAllowedFilePatterns = []string{".c", ".d", ".e"}

	sechubJSON := `
		{
			"data": {
				"sources": [
					{
						"name": "sources-1",
						"fileSystem": { "folders": [ "." ] },
						"additionalFilenameExtensions": [
							".a",
							".b"
						]
					},
					{
						"name": "sources-2",
						"fileSystem": { "folders": [ "." ] },
						"additionalFilenameExtensions": [
							".m",
							".n"
						]
					}
				]
			},
			"codeScan":{
        "use": [ "sources-1", "sources-2" ],
				"fileSystem": { "folders": [ "." ] },
				"additionalFilenameExtensions": [
					".y",
					".z"
				]
			}
		}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.SourceCodePatterns)
	}
	fmt.Printf("%+v\n", context.sechubConfig.CodeScan.SourceCodePatterns)
	// Output:
	// [.a .b .c .d .e]
	// [.m .n .c .d .e]
	// [.y .z .c .d .e]
}

func Example_adjustSourceFilterPatterns_respects_whitelistAll_true() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.whitelistAll = true

	// Override global DefaultSourceCodeAllowedFilePatterns to get reproducable results
	DefaultSourceCodeAllowedFilePatterns = []string{".c", ".d", ".e"}

	sechubJSON := `
		{
			"data": {
				"sources": [
					{
						"name": "sources-1",
						"fileSystem": { "folders": [ "." ] },
						"additionalFilenameExtensions": [
							".a",
							".b"
						]
					},
					{
						"name": "sources-2",
						"fileSystem": { "folders": [ "." ] },
						"additionalFilenameExtensions": [
							".m",
							".n"
						]
					}
				]
			},
			"codeScan":{
        "use": [ "sources-1", "sources-2" ],
				"fileSystem": { "folders": [ "." ] },
				"additionalFilenameExtensions": [
					".y",
					".z"
				]
			}
		}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	fmt.Printf("%+v\n", context.sechubConfig.CodeScan.SourceCodePatterns)
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.SourceCodePatterns)
	}
	// Output:
	// []
	// []
	// []
}

func Example_adjustSourceFilterPatterns_respects_Excludes() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.ignoreDefaultExcludes = false

	// Override global DefaultSourceCodeAllowedFilePatterns to get reproducable results
	DefaultSourceCodeExcludeDirPatterns = []string{"**/default-exclude1/**", "**/default-exclude2/**"}

	sechubJSON := `
		{
			"data": {
				"sources": [
					{
						"name": "sources-1",
						"fileSystem": { "folders": [ "." ] },
						"excludes": [
							"**/data-exclude1/**",
							"**/data-exclude2/**"
						]
					}
				]
			},
			"codeScan":{
        "use": ["sources-1"],
				"fileSystem": { "folders": [ "." ] },
				"excludes": [
					"**/old-exclude1/**",
					"**/old-exclude2/**"
				]
			}
		}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	fmt.Printf("%+v\n", context.sechubConfig.CodeScan.Excludes)
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.Excludes)
	}
	// Output:
	// [**/old-exclude1/** **/old-exclude2/** **/default-exclude1/** **/default-exclude2/**]
	// [**/data-exclude1/** **/data-exclude2/** **/default-exclude1/** **/default-exclude2/**]
}

func Example_adjustSourceFilterPatterns_respects_Excludes_no_default() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.ignoreDefaultExcludes = true

	// Override global DefaultSourceCodeAllowedFilePatterns to get reproducable results
	DefaultSourceCodeExcludeDirPatterns = []string{"**/default-exclude1/**", "**/default-exclude2/**"}

	sechubJSON := `
		{
			"data": {
				"sources": [
					{
						"name": "sources-1",
						"fileSystem": { "folders": [ "." ] },
						"excludes": [
							"**/data-exclude1/**",
							"**/data-exclude2/**"
						]
					}
				]
			},
			"codeScan":{
				"fileSystem": { "folders": [ "." ] },
				"excludes": [
					"**/old-exclude1/**",
					"**/old-exclude2/**"
				]
			}
		}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	fmt.Printf("%+v\n", context.sechubConfig.CodeScan.Excludes)
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.Excludes)
	}
	// Output:
	// [**/old-exclude1/** **/old-exclude2/**]
	// [**/data-exclude1/** **/data-exclude2/**]
}

func Example_adjustSourceFilterPatterns_works_with_old_format() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.whitelistAll = false

	// Override global DefaultSourceCodeAllowedFilePatterns to get reproducable results
	DefaultSourceCodeAllowedFilePatterns = []string{".c", ".d", ".e"}

	sechubJSON := `
		{
			"codeScan":{
				"fileSystem": { "folders": [ "." ] }
			}
		}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	fmt.Printf("%+v\n", context.sechubConfig.CodeScan.SourceCodePatterns)
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.SourceCodePatterns)
	}
	// Output:
	// [.c .d .e]
}

func Example_adjustSourceFilterPatterns_codeScan_works_with_data_section_format() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.whitelistAll = false

	// Override global DefaultSourceCodeAllowedFilePatterns to get reproducable results
	DefaultSourceCodeAllowedFilePatterns = []string{".c", ".d", ".e"}

	sechubJSON := `
	{
		"data": {
			"sources": [
				{
					"name": "mysources",
					"fileSystem": { "folders": [ "src1/" ] }
				}
			]
		},
		"codeScan": { "use": [ "mysources" ] }
	}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	fmt.Printf("%+v\n", context.sechubConfig.CodeScan.SourceCodePatterns)
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.Name, i.SourceCodePatterns)
	}
	// Output:
	// []
	// mysources [.c .d .e]
}

func Example_adjustSourceFilterPatterns_secretScan_does_not_filterSourceCodePatterns() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.whitelistAll = false

	// Override global DefaultSourceCodeAllowedFilePatterns to get reproducable results
	DefaultSourceCodeAllowedFilePatterns = []string{".c", ".d", ".e"}

	sechubJSON := `
	{
		"data": {
			"sources": [
				{
					"name": "mysources",
					"fileSystem": { "folders": [ "src1/" ] }
				}
			]
		},
		"secretScan": { "use": [ "mysources" ] }
	}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.Name, i.SourceCodePatterns)
	}
	// Output:
	// mysources []
}

func Example_adjustSourceFilterPatterns_secretScan_overrides_codeScan() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.whitelistAll = false

	// Override global DefaultSourceCodeAllowedFilePatterns to get reproducable results
	DefaultSourceCodeAllowedFilePatterns = []string{".c", ".d", ".e"}

	sechubJSON := `
	{
		"data": {
			"sources": [
				{
					"name": "mysources-1",
					"fileSystem": { "folders": [ "src1/" ] }
				}
			]
		},
		"codeScan": {	"use": [ "mysources-1" ] },
		"secretScan": {	"use": [ "mysources-1" ] }
	}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.Name, i.SourceCodePatterns)
	}
	// The list must be empty because secretScan is involved

	// Output:
	// mysources-1 []
}

func Example_adjustSourceFilterPatterns_Secretscan_Excludes() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.ignoreDefaultExcludes = false

	// Override global definitions to get reproducable results:
	DefaultSCMDirPatterns = []string{"**/.git/**"}
	DefaultSourceCodeUnwantedDirPatterns = []string{"**/test/**", "**/node_modules/**"}
	DefaultSecretScanUnwantedFilePatterns = []string{"*.a", "*.so"}

	sechubJSON := `
		{
			"data": {
				"sources": [
					{
						"name": "sources",
						"fileSystem": { "folders": [ "." ] },
						"excludes": [
							"**/src-exclude1/**",
							"**/src-exclude2/**"
						]
					}
				]
			},
			"codeScan": { "use": [ "sources" ] },
			"secretScan": { "use": [ "sources" ] }
		}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.Excludes)
	}
	// Output:
	// [**/src-exclude1/** **/src-exclude2/** **/test/** **/node_modules/** **/.git/** *.a *.so]
}

func Example_adjustSourceFilterPatterns_Secretscan_SCMHistory() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.ignoreDefaultExcludes = false

	// This makes `DefaultSCMDirPatterns` NOT being excluded
	config.addSCMHistory = true

	// Override global definitions to get reproducable results:
	DefaultSCMDirPatterns = []string{"**/.git/**"}
	DefaultSourceCodeUnwantedDirPatterns = []string{"**/test/**", "**/node_modules/**"}
	DefaultSecretScanUnwantedFilePatterns = []string{"*.a", "*.so"}

	sechubJSON := `
		{
			"data": {
				"sources": [
					{
						"name": "sources",
						"fileSystem": { "folders": [ "." ] },
						"excludes": [
							"**/src-exclude1/**",
							"**/src-exclude2/**"
						]
					}
				]
			},
			"codeScan": { "use": [ "sources" ] },
			"secretScan": { "use": [ "sources" ] }
		}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.Excludes)
	}
	// Output:
	// [**/src-exclude1/** **/src-exclude2/** **/test/** **/node_modules/** *.a *.so]
}

func Example_adjustSourceFilterPatterns_licenseScan() {
	/* prepare */
	var context Context
	var config Config
	context.config = &config
	config.whitelistAll = false

	// Override global DefaultSourceCodeAllowedFilePatterns to get reproducable results
	DefaultSourceCodeAllowedFilePatterns = []string{".a", ".b", ".c"}

	sechubJSON := `
	{
		"data": {
			"sources": [
				{
					"name": "mysources",
					"fileSystem": { "folders": [ "." ] }
				}
			]
		},
		"licenseScan": {	"use": [ "mysources" ] }
	}
	`
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	/* execute */
	adjustSourceFilterPatterns(&context)

	/* test */
	for _, i := range context.sechubConfig.Data.Sources {
		fmt.Println(i.Name, i.SourceCodePatterns)
	}
	// The list must contain DefaultSourceCodeAllowedFilePatterns

	// Output:
	// mysources [.a .b .c]
}
