// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"

	sechubUtil "daimler.com/sechub/util"
)

// InitializeContext - initialize and validate context.
// creates a new context having configuration values from flags or env entries
// env entries will be overriden by flags (command parameters)
func InitializeContext() *Context {
	/* create config and context */
	configPtr := NewConfigByFlags()
	context := NewContext(configPtr)

	/* load configuration file - maybe there are some settings normally done by cli arguments too */
	loadConfigFile(context)

	/* assert after load the configuration is valid */
	assertValidConfig(configPtr)

	return context
}

/**
* Loads config file.
 */
func loadConfigFile(context *Context) {
	configPtr := context.config
	filePath := configPtr.configFilePath

	configFromFile := newSecHubConfigurationFromFile(context, filePath)

	/* override if not set before */
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

	context.sechubConfig = &configFromFile
}

func debugNotDefinedAsOption(context *Context, fieldName string, fieldValue string) {
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("'%s' not defined by option - using entry from config file: '%s'", fieldName, fieldValue))
}
