// SPDX-License-Identifier: MIT

package cli

import (
	"flag"
	"fmt"
	"io"
	"os"
	"strconv"
)

// PrintUsage - Print usage help informations
func PrintUsage(w io.Writer) {
	flag.CommandLine.SetOutput(w)

	info := "Usage of " + os.Args[0] + `:

sechub [options] action

action
  Choose one action:
   ` + scanAction + ` - start scan, wait for job done, download resulting report to output folder
   ` + scanAsynchronAction + ` - just trigger scan and return job id as last output line
   ` + getStatusAction + ` - fetch current job status and return result as json
   ` + getReportAction + ` - fetch report as json (a report will only exist when job has finished)
   ` + listJobsAction + ` - list the ` + strconv.Itoa(SizeOfJobList) + ` latest scan jobs
   ` + cancelAction + ` - cancel the scan job provided with -` + jobUUIDOption + `
   ` + defineFalsePositivesAction + ` - define the project's false-positives list from a json file
   ` + getFalsePositivesAction + ` - fetch the project's false-positives list as json
   ` + markFalsePositivesAction + ` - add from a json file to project's false-positives list
   ` + unmarkFalsePositivesAction + ` - remove items from project's false-positives list as defined in json file
   ` + interactiveMarkFalsePositivesAction + ` - interactively define false-positives depending on a json report file
   ` + interactiveUnmarkFalsePositivesAction + ` - interactively remove items from project's false-positives list
   ` + showHelpAction + ` - show help and terminate
   ` + showVersionAction + ` - show version and terminate

Options:
`

	optionsFooter := `
You can also define some of the options via environment variables or inside your config file.
But commandline arguments will override environment variables; Environment variables will override config file.
`

	example := `
See https://mercedes-benz.github.io/sechub/latest/sechub-client.html#section-client-configuration-file for help on configuration.
`
	fmt.Fprint(w, info)
	flag.PrintDefaults()
	fmt.Fprint(w, optionsFooter)
	fmt.Fprint(w, example)
}
