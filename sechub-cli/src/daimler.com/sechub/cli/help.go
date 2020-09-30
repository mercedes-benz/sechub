// SPDX-License-Identifier: MIT

package cli

import (
	"flag"
	"fmt"
	"os"
)

// PrintUsage - Print usage help informations
func PrintUsage() {
	w := flag.CommandLine.Output()
	fmt.Fprintf(w, "Usage of %s:\nsechub [options] action\n", os.Args[0])

	info := "\nYou can define most of the options also inside your config file! But commandline arguments will override those settings.\nOptions:\n"
	fmt.Fprintf(w, info)

	flag.PrintDefaults()
	action := "action\n"
	action += "  following actions are supported:\n"
	action += "   " + ActionExecuteSynchron + " - start scan, wait for job done, fetch automatifcally result report to output folder\n"
	action += "   " + ActionExecuteAsynchron + " - just trigger scan and return job id in json\n"
	action += "   " + ActionExecuteGetStatus + " - fetch current job status and return result as json\n"
	action += "   " + ActionExecuteGetReport + " - fetch report as json (result will only exist when job is done)\n"
	action += "   " + ActionExecuteGetFalsePositives + " - fetch the project's false-positives list as json\n"
	action += "   " + ActionExecuteMarkFalsePositives + " - add from a json file to project's false-positives list\n"
	action += "   " + ActionExecuteUnmarkFalsePositives + " - remove items from project's false-positives list as defined in json file\n"
	action += "   " + ActionExecuteInteractiveMarkFalsePositives + " - interactively define false-positives depending on a json report\n"
	action += "   " + ActionExecuteInteractiveUnmarkFalsePositives + " - interactively remove items from project's false-positives list\n"

	fmt.Fprintf(w, action)
	fmt.Fprintln(w)
	fmt.Fprint(w, "Example for starting a scan which will block until results are availabe:\n")
	fmt.Fprint(w, "   SECHUB_APITOKEN=7536a8c4aa82407da7e06bdbEXAMPLE\n")
	fmt.Fprint(w, "   sechub scan\n")
	fmt.Fprint(w, "\n")
	fmt.Fprint(w, "Example '"+DefaultSecHubConfigFile+"' config file which will configure a webscan and also source scan:\n\n")

	fmt.Fprintf(w, "      {\n")
	fmt.Fprintf(w, "      	\"apiVersion\": \"1.0\",\n")
	fmt.Fprintf(w, "\n")
	fmt.Fprintf(w, "      	\"server\"   : \"https://$SECHUB_SERVER/\",\n")
	fmt.Fprintf(w, "      	\"user\"     : \"alice\",\n")
	fmt.Fprintf(w, "\n")
	fmt.Fprintf(w, "      	\"project\"  : \"gamechanger\",\n")
	fmt.Fprintf(w, "\n")
	fmt.Fprintf(w, "      	\"webScan\"  : {\n")
	fmt.Fprintf(w, "      		\"uris\": [\"http://$SCAN_TARGET_SERVER/\"]\n")
	fmt.Fprintf(w, "        },\n")
	fmt.Fprintf(w, "      	\"codeScan\"  : {\n")
	fmt.Fprintf(w, "      	    \"fileSystem\"  : {\n")
	fmt.Fprintf(w, "      		    \"folders\": [\"gamechanger-android/src/main/java\",\"gamechanger-server/src/main/java\"]\n")
	fmt.Fprintf(w, "      	    },\n")
	fmt.Fprintf(w, "            \"excludes\": [\"**/*.log\",\"README.md\"]\n")
	fmt.Fprintf(w, "      	}\n")

	fmt.Fprintf(w, "      }\n")
	fmt.Fprintf(w, "\nPlease look into 'sechub-user.pdf' for detailed help, correct sechub server url, more examples, etc.\n")
}

func showHelpAndExit() {
	PrintUsage()
	os.Exit(0)
}

func showVersionInfoAndExit() {
	printLogoWithVersion(os.Stdout)
	os.Exit(0)
}
