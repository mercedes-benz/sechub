// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
)

func printLogoWithVersion(context *Context) {
	if context.config.quiet {
		return
	}
	printLogoNoNewLine()
	fmt.Print("Client Version ", Version(), "\n\n")
}

func printLogoNoNewLine() {
	fmt.Print(" _____           _   _       _     \n")
	fmt.Print("/  ___|         | | | |     | |    \n")
	fmt.Print("\\ `--.  ___  ___| |_| |_   _| |__  \n")
	fmt.Print(" `--. \\/ _ \\/ __|  _  | | | | '_ \\ \n")
	fmt.Print("/\\__/ /  __/ (__| | | | |_| | |_) |\n")
	fmt.Print("\\____/ \\___|\\___\\_| |_/\\__,_|_.__/ ")
}
