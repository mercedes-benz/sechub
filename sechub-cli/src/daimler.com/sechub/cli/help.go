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

action
  Following actions are supported:
   ` + scanAction + ` - start scan, wait for job done, download resulting report to output folder
   ` + scanAsynchronAction + ` - just trigger scan and return job id as last output line
   ` + getStatusAction + ` - fetch current job status and return result as json
   ` + getReportAction + ` - fetch report as json (result will only exist when job is done)
   ` + getFalsePositivesAction + ` - fetch the project's false-positives list as json
   ` + markFalsePositivesAction + ` - add from a json file to project's false-positives list
   ` + unmarkFalsePositivesAction + ` - remove items from project's false-positives list as defined in json file
   ` + interactiveMarkFalsePositivesAction + ` - interactively define false-positives depending on a json report file
   ` + interactiveUnmarkFalsePositivesAction + ` - interactively remove items from project's false-positives list

Options:
`

	optionsFooter := `
You can also define some of the options via environment variables or inside your config file.
But commandline arguments will override environment variables; Environment variables will override config file.
`

	example := `
Example for starting a scan which will wait until results are availabe and download the report:
  export ` + SechubUserIDEnvVar + `=myUserName
  export ` + SechubApitokenEnvVar + `=NTg5YSMkGRkM2Uy00NDJjLTkYTY4NjEXAMPLE
  export ` + SechubServerEnvVar + `=https://sechub.example.com:8443
  sechub scan

Example 'sechub.json' config file which will configure a code scan and also a webscan:
  {
    "apiVersion": "1.0",
    "project": "my_project",
    "codeScan": {
      "fileSystem": { "folders": ["src-server/", "src-client/"] }
    }
    "webScan"  : {
      "uris": ["https://www.myproject"]
    }
  }

Please also look into 'sechub-client.pdf' for detailed help, more examples, etc.
`
	fmt.Fprintf(w, info)
	flag.PrintDefaults()
	fmt.Fprintf(w, optionsFooter)
	fmt.Fprintf(w, example)
}
