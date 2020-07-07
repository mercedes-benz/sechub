// SPDX-License-Identifier: MIT
package cli

import (
	"fmt"

	. "daimler.com/sechub/util"
)

// InitializeContext - initialize and validate context
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
	if configPtr.projectId == "" {
		debugNotDefinedAsOption(context, "projectId", configFromFile.ProjectId)
		configPtr.projectId = configFromFile.ProjectId
	}

	context.sechubConfig = &configFromFile
}

func debugNotDefinedAsOption(context *Context, fieldName string, fieldValue string) {
	if !context.config.debug {
		return
	}
	LogDebug(context.config.debug, fmt.Sprintf("'%s' not defined by option - use entry from config file:'%s'", fieldName, fieldValue))
}
