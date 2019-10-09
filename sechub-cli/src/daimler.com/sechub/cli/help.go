// SPDX-License-Identifier: MIT
package cli

import (
	"flag"
	"fmt"
	"os"
)

func initHelp() {
	flag.Usage = func() {
		w := flag.CommandLine.Output()
		fmt.Fprintf(w, "Usage of %s:\nsechub [options] action\n", os.Args[0])

		info := "\nYou can define most of the options also inside your config file! But commandline arguments will override those settings.\nOptions:\n"
		fmt.Fprintf(w, info)

		flag.PrintDefaults()
		action := "action\n"
		action += "  following actions are supported:\n"
		action += "   '" + ActionExecuteSynchron + "' will start scan, wait for job done, fetch automatifcally result report to output folder\n"
		action += "   '" + ActionExecuteAsynchron + "' will just trigger scan and return job id in json\n"
		action += "   '" + ActionExecuteGetStatus + "' will fetch current job status and return result as json\n"
		action += "   '" + ActionExecuteGetReport + "' will fetch report as json (result will only exist when job is done)\n"

		fmt.Fprintf(w, "Arguments:\n %s", action)
		fmt.Fprintln(w)
		fmt.Fprint(w, "Example for starting a scan which will block until results are availabe:\n")
		fmt.Fprint(w, "   sechub -apitoken 7536a8c4aa82407da7e06bdbf8dd772f scan\n")
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
		fmt.Fprintf(w, "      	}\n")
		fmt.Fprintf(w, "      	\"codeScan\"  : {\n")
		fmt.Fprintf(w, "      	    \"fileSystem\"  : {\n")
		fmt.Fprintf(w, "      		    \"folders\": [\"gamechanger-android/src/main/java\",\"gamechanger-server/src/main/java\"]\n")
		fmt.Fprintf(w, "      	    },\n")
		fmt.Fprintf(w, "            \"excludes\": [\"**/*.log\",\"README.md\"]\n")
		fmt.Fprintf(w, "      	}\n")

		fmt.Fprintf(w, "      }\n")
		fmt.Fprintf(w, "\nPlease look into 'sechub-user.pdf' for detailed help, correct sechub server url, more examples, etc.\n")
	}
}

func showHelpAndExit() {
	flag.Usage()
	os.Exit(0)
}
func showVersionInfoAndExit() {
	printLogoWithVersion(os.Stdout)
	os.Exit(0)
}
