// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"io"
)

func printLogoWithVersion(w io.Writer) {
	printLogoNoNewLine(w)
	fmt.Printf("Client Version %s\n\n", Version())
}
func printLogoNoNewLine(w io.Writer) {
	fmt.Fprintf(w, " _____           _   _       _     \n")
	fmt.Fprintf(w, "/  ___|         | | | |     | |    \n")
	fmt.Fprintf(w, "\\ `--.  ___  ___| |_| |_   _| |__  \n")
	fmt.Fprintf(w, " `--. \\/ _ \\/ __|  _  | | | | '_ \\ \n")
	fmt.Fprintf(w, "/\\__/ /  __/ (__| | | | |_| | |_) |\n")
	fmt.Fprintf(w, "\\____/ \\___|\\___\\_| |_/\\__,_|_.__/ ")

}
