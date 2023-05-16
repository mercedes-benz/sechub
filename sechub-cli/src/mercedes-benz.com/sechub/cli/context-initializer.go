// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"strings"

	sechubUtil "mercedes-benz.com/sechub/util"
)

// InitializeContext - initialize and validate context.
// creates a new context having configuration values from flags or env entries
// env entries will be overriden by flags (command parameters)
func InitializeContext() *Context {
	/* create config and context */
	configPtr := NewConfigByFlags()
	context := NewContext(configPtr)

	printLogoWithVersion(context)

	/* load configuration file - maybe there are some settings normally done by cli arguments too */
	if context.config.action != showHelpAction {
		loadConfigFile(context)
	}

	// Add labels defined via cmdline args or env var to config JSON (only for scan jobs)
	if len(context.config.labels) > 0 && (context.config.action == scanAction || context.config.action == scanAsynchronAction) {
		err := applyLabelsToConfigJson(context)
		if err != nil {
			sechubUtil.LogError("Error while processing labels: " + err.Error())
		}
	}

	/* assert after load the configuration is valid */
	assertValidConfig(context)

	return context
}

func loadConfigFile(context *Context) {
	var configFromFile SecHubConfig

	configPtr := context.config

	configFromFile, configPtr.configFileRead = newSecHubConfigurationFromFile(context, configPtr.configFilePath)
	context.sechubConfig = &configFromFile

	if configPtr.configFileRead {
		/* override if not set by option or environment variable */
		if configPtr.server == "" {
			debugNotDefinedAsOption(context, "server", configFromFile.Server)
			configPtr.server = configFromFile.Server
		}
		if configPtr.user == "" {
			debugNotDefinedAsOption(context, "user", configFromFile.User)
			configPtr.user = configFromFile.User
		}
		if configPtr.projectID == "" {
			debugNotDefinedAsOption(context, "projectID", configFromFile.ProjectID)
			configPtr.projectID = configFromFile.ProjectID
		}
	}
}

func lowercaseOrNotice(s string, name string) string {
	lowercased := strings.ToLower(s)
	if s != lowercased {
		sechubUtil.LogNotice("Converted " + name + " '" + s + "' to lowercase. Because it contained uppercase characters, which are not accepted by SecHub server.")
	}
	return lowercased
}

func debugNotDefinedAsOption(context *Context, fieldName string, fieldValue string) {
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("'%s' not defined by option or environment variable - using entry from config file: '%s'", fieldName, fieldValue))
}
