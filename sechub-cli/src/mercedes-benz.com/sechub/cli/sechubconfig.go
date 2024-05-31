// SPDX-License-Identifier: MIT

package cli

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"slices"
	"strings"
	"text/template"

	sechubUtil "mercedes-benz.com/sechub/util"
)

// SecHubConfig is the sechub configuration JSON pendant in Go. But we do only necessary parts from JSON file!
// so Webscan, InfraScan are not handled here (but still uploaded)
// Only code scan is necessary, because determination necessary if there is an upload necessary or not.
type SecHubConfig struct {
	APIVersion  string                `json:"apiVersion"`
	User        string                `json:"user"`
	ProjectID   string                `json:"project"`
	Server      string                `json:"server"`
	CodeScan    CodeScanConfig        `json:"codeScan"`
	LicenseScan LicenseScanConfig     `json:"licenseScan"`
	SecretScan  SecretScanConfig      `json:"secretScan"`
	Data        DataSectionScanConfig `json:"data"`
}

type DataSectionScanConfig struct {
	Binaries []NamedBinariesScanConfig `json:"binaries"`
	Sources  []NamedCodeScanConfig     `json:"sources"`
}

// NamedCodeScanConfig contains a filesystem scope for code scans with optional excludes and additionalFilenameExtensions
type NamedCodeScanConfig struct {
	Name               string           `json:"name"`
	FileSystem         FileSystemConfig `json:"fileSystem"`
	Excludes           []string         `json:"excludes"`
	SourceCodePatterns []string         `json:"additionalFilenameExtensions"`
}

// NamedCodeScanConfig contains a filesystem scope for binary scans
type NamedBinariesScanConfig struct {
	Name       string           `json:"name"`
	FileSystem FileSystemConfig `json:"fileSystem"`
	Excludes   []string         `json:"excludes"`
}

// CodeScanConfig - definition of a code scan
type CodeScanConfig struct {
	Use []string `json:"use"`
	////////////////////////////////
	// From here: Deprecated/legacy
	// Kept for backward compatibility. Take "use" in conjunction with data section.
	FileSystem         FileSystemConfig `json:"fileSystem"`
	Excludes           []string         `json:"excludes"`
	SourceCodePatterns []string         `json:"additionalFilenameExtensions"`
	////////////////////////////////
}

// SecretScanConfig - definition of a secrets scan
type LicenseScanConfig struct {
	Use []string `json:"use"`
}

// SecretScanConfig - definition of a secrets scan
type SecretScanConfig struct {
	Use []string `json:"use"`
}

// FileSystemConfig contains data for defined files+folders
type FileSystemConfig struct {
	Files   []string `json:"files"`
	Folders []string `json:"folders"`
}

// fillTemplate - Fill in environment variables via go-templating
// templateSource: content of json config file
// data: environment variables
func fillTemplate(templateSource string, data map[string]string) []byte {
	var tpl bytes.Buffer
	t := template.Must(template.New("sechubConfig").Parse(templateSource))

	err := t.Execute(&tpl, data)

	if err != nil {
		sechubUtil.LogError(fmt.Sprintf("SecHub configuration json is not a valid template:\n%s", err))
		showHelpHint()
		os.Exit(ExitCodeMissingConfigFile)
	}
	return tpl.Bytes()
}

func newSecHubConfigFromBytes(bytes []byte) SecHubConfig {
	var sechubConfig SecHubConfig

	/* transform text to json */
	err := json.Unmarshal(bytes, &sechubConfig)
	if err != nil {
		sechubUtil.LogError(fmt.Sprintf("SecHub configuration json is not valid json:\n%s", err))
		showHelpHint()
		os.Exit(ExitCodeMissingConfigFile)
	}
	return sechubConfig
}

func showHelpHint() {
	fmt.Fprint(os.Stderr, "\nHint: Call sechub with \"help\" to show usage options.\n")
}

func newSecHubConfigurationFromFile(context *Context, filePath string) (SecHubConfig, bool) {
	fileWasRead := false

	/* open file and check exists */
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Loading config file: '%s'", filePath))
	jsonFile, err := os.Open(filePath)
	if err != nil {
		emptyConfig := SecHubConfig{}
		return emptyConfig, fileWasRead
	}
	defer jsonFile.Close()

	/* read text content as "unfilled byte value". This will be used for debug outputs,
	   so we do not have passwords etc. accidently leaked. We limit read to maximum allowed bytes */
	context.inputForContentProcessing, err = io.ReadAll(io.LimitReader(jsonFile, MaximumBytesOfSecHubConfig))

	if sechubUtil.HandleIOError(err) {
		showHelpHint()
		os.Exit(ExitCodeMissingConfigFile)
	}

	if len(context.inputForContentProcessing) >= MaximumBytesOfSecHubConfig {
		sechubUtil.LogError("Given SecHub config file '" + context.config.configFilePath + "' is too big!")
		os.Exit(ExitCodeInvalidConfigFile)
	}

	if !json.Valid(context.inputForContentProcessing) {
		sechubUtil.LogError("Given SecHub config file '" + context.config.configFilePath + "' is not correct JSON!")
		os.Exit(ExitCodeInvalidConfigFile)
	} else {
		fileWasRead = true
	}

	// Apply Go templating to config file
	data, _ := envToMap()
	context.contentToSend = fillTemplate(string(context.inputForContentProcessing), data)

	return newSecHubConfigFromBytes(context.contentToSend), fileWasRead
}

// envToMap - read all environment variables from OS into a map structure
func envToMap() (map[string]string, error) {
	envMap := make(map[string]string)
	var err error

	for _, v := range os.Environ() {
		splitted := strings.Split(v, "=")
		envMap[splitted[0]] = splitted[1]
	}

	return envMap, err
}

func adjustSourceFilterPatterns(context *Context) {
	for i, item := range context.sechubConfig.Data.Sources {

		if slices.Contains(context.sechubConfig.SecretScan.Use, item.Name) {
			// Clear upload filter for secrets scans
			context.sechubConfig.Data.Sources[i].SourceCodePatterns =
				adjustSourceFilterPatternsWhitelistAll(item.SourceCodePatterns, true)
		} else if slices.Contains(context.sechubConfig.CodeScan.Use, item.Name) {
			// Append default source code patterns for code scans
			context.sechubConfig.Data.Sources[i].SourceCodePatterns =
				adjustSourceFilterPatternsWhitelistAll(item.SourceCodePatterns, context.config.whitelistAll)
		} else if slices.Contains(context.sechubConfig.LicenseScan.Use, item.Name) {
			// Append default source code patterns for license scans
			context.sechubConfig.Data.Sources[i].SourceCodePatterns =
				adjustSourceFilterPatternsWhitelistAll(item.SourceCodePatterns, context.config.whitelistAll)
		}

		if !context.config.ignoreDefaultExcludes {
			excludePatterns := computeSourceExcludePatterns(context, item)
			// add exclude patterns to exclude list
			context.sechubConfig.Data.Sources[i].Excludes = append(item.Excludes, excludePatterns...)
		}
	}

	////////////////////////////////////////////////
	// Old/legacy support
	// We still support the old/legacy format directly in context.sechubConfig.CodeScan:
	if len(context.sechubConfig.CodeScan.FileSystem.Folders) > 0 {
		context.sechubConfig.CodeScan.SourceCodePatterns =
			adjustSourceFilterPatternsWhitelistAll(context.sechubConfig.CodeScan.SourceCodePatterns, context.config.whitelistAll)

		if !context.config.ignoreDefaultExcludes {
			context.sechubConfig.CodeScan.Excludes = append(context.sechubConfig.CodeScan.Excludes, DefaultSourceCodeExcludeDirPatterns...)
		}
	}
	////////////////////////////////////////////////
}

func adjustSourceFilterPatternsWhitelistAll(sourceCodePatterns []string, whitelistAll bool) []string {
	if whitelistAll {
		return []string{""}
	}

	// build list of source code file patterns
	return append(sourceCodePatterns, DefaultSourceCodeAllowedFilePatterns...)
}

func computeSourceExcludePatterns(context *Context, config NamedCodeScanConfig) []string {
	var result []string
	if slices.Contains(context.sechubConfig.SecretScan.Use, config.Name) {
		// On secrets scan we add a bunch of exclude patterns (binaries, image files etc.)
		result = DefaultSourceCodeUnwantedDirPatterns
		if !context.config.addSCMHistory {
			result = append(result, DefaultSCMDirPatterns...)
		}
		result = append(result, DefaultSecretScanUnwantedFilePatterns...)
	} else if slices.Contains(context.sechubConfig.CodeScan.Use, config.Name) {
		result = DefaultSourceCodeExcludeDirPatterns
	}
	return result
}
