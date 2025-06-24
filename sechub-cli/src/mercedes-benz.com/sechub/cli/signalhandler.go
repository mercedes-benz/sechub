// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"
	"os/signal"
	"syscall"
)

// Channel for signals
var sigs = make(chan os.Signal, 1)

func initSignalHandler(context *Context) {
	// Intercept SIGINT and SIGTERM signals
	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)

	// Setup signal handler for above
	go func() {
		sig := <-sigs
		fmt.Println()
		fmt.Println("Received",sig)
		processTerminationSignal(context)
		fmt.Println("Exiting program")
		os.Exit(0)
	}()

}

func processTerminationSignal(context *Context) {
	// Act depending on client action
	switch context.config.action {
		case scanAction:
			// Cancel current scan job on termination
			cancelSecHubJob(context)
	}
}
