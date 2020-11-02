// SPDX-License-Identifier: MIT

package cli

import (
	"flag"
	"fmt"
	"io"
	"os"
)

// PrintUsage - Print usage help informations
func PrintUsage(w io.Writer) {
	flag.CommandLine.SetOutput(w)

	info := "Usage of " + os.Args[0] + `:
sechub [options] action

You can also define some of the options via environment variables or inside your config file.
But commandline arguments will override environment variables; Environment variables will override config file.

Options:
`
	action := `
action
  Following actions are supported:
   ` + scanAction + ` - start scan, wait for job done, download result report to output folder
   ` + scanAsynchronAction + ` - just trigger scan and return job id as last output line
   ` + getStatusAction + ` - fetch current job status and return result as json
   ` + getReportAction + ` - fetch report as json (result will only exist when job is done)
   ` + getFalsePositivesAction + ` - fetch the project's false-positives list as json
   ` + markFalsePositivesAction + ` - add from a json file to project's false-positives list
   ` + unmarkFalsePositivesAction + ` - remove items from project's false-positives list as defined in json file
   ` + interactiveMarkFalsePositivesAction + ` - interactively define false-positives depending on a json report file
   ` + interactiveUnmarkFalsePositivesAction + ` - interactively remove items from project's false-positives list
`
	example := `
Example for starting a scan which will wait until results are availabe and download the report:
SECHUB_APITOKEN=7536a8c4aa82407da7e06bdbEXAMPLE
sechub scan

Example 'sechub.json' config file which will configure a webscan and also a code scan:

	{
		"apiVersion": "1.0",
		"project"   : "gamechanger",
		"server"    : "https://sechub.example.com:8443",
		"user"      : "alice",
		"codeScan"  : {
			"fileSystem": { "folders": ["gamechanger-android/src", "gamechanger-server/src/main/java"] }
		},
		"webScan"   : {
			"uris": ["https://my-test-gamechanger-server/"]
		}
	}

Please also look into 'sechub-client.pdf' for detailed help, more examples, etc.
`
	fmt.Fprintf(w, info)
	flag.PrintDefaults()
	fmt.Fprintf(w, action)
	fmt.Fprintf(w, example)
}
