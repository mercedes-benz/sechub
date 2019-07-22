// SPDX-License-Identifier: MIT
package cli

import (
	. "daimler.com/sechub/testutil"
	"fmt"
	"testing"
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
	var config SecHubConfig = newSecHubConfigFromString(jStr)
	fmt.Printf("Loaded config: %s", config)
	AssertEquals("1.2.3", config.APIVersion, t)
	AssertEquals("1111", config.CodeScan.FileSystem.Folders[0], t)
}
