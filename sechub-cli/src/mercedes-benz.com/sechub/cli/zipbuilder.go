// SPDX-License-Identifier: MIT

package cli

import (
	"archive/zip"
	"errors"
	"fmt"
	"os"
	"path/filepath"

	sechubUtil "mercedes-benz.com/sechub/util"
)

// createSouceCodeZipFile - compress all defined sources into one single zip file
func createSouceCodeZipFile(context *Context) error {
	zipFile, _ := filepath.Abs(context.sourceZipFileName)

	// create zip file
	newZipFile, err := os.Create(zipFile)
	if err != nil {
		return err
	}

	// create zip writer
	zipWriter := zip.NewWriter(newZipFile)

	// Add everything in Data.Sources to zip file
	zipFileShouldHaveContent := false
	for _, item := range context.sechubConfig.Data.Sources {
		if len(item.FileSystem.Files) > 0 || len(item.FileSystem.Folders) > 0 {
			err = appendToSourceCodeZipFile(zipFile, zipWriter, item, context.config.quiet, context.config.debug)
			if err != nil {
				return err
			}
			zipFileShouldHaveContent = true
		}
	}

	// Also support legacy definition:
	if len(context.sechubConfig.CodeScan.FileSystem.Folders) > 0 {
		namedCodeScanConfig := NamedCodeScanConfig{
			Name:               "",
			FileSystem:         context.sechubConfig.CodeScan.FileSystem,
			Excludes:           context.sechubConfig.CodeScan.Excludes,
			SourceCodePatterns: context.sechubConfig.CodeScan.SourceCodePatterns,
		}
		err = appendToSourceCodeZipFile(zipFile, zipWriter, namedCodeScanConfig, context.config.quiet, context.config.debug)
		if err != nil {
			return err
		}
		zipFileShouldHaveContent = true
	}

	zipWriter.Close()
	newZipFile.Close()

	// Check if context.sourceZipFileName is an empty zip
	// For performance reasons we only look deeper into very small files
	if zipFileShouldHaveContent {
		context.sourceZipUploadNeeded = true
		if sechubUtil.GetFileSize(zipFile) < 300 {
			zipFileContent, _ := sechubUtil.ReadContentOfZipFile(zipFile)
			if len(zipFileContent) == 0 {
				return errors.New(sechubUtil.ZipFileHasNoContent)
			}
		}
	} else {
		context.sourceZipUploadNeeded = false
		sechubUtil.LogNotice(sechubUtil.ZipFileNotCreated)
	}

	return nil
}

func appendToSourceCodeZipFile(zipFile string, zipWriter *zip.Writer, config NamedCodeScanConfig, quiet bool, debug bool) error {
	prefix := ""
	if config.Name != "" {
		// e.g.: __data__/example-name-1/
		prefix = fmt.Sprintf("%s/%s/", archiveDataPrefix, config.Name)
	}
	zipConfig := sechubUtil.ZipConfig{
		ZipFileName:        zipFile,
		ZipWriter:          zipWriter,
		PrefixInZip:        prefix,
		Files:              config.FileSystem.Files,
		Folders:            config.FileSystem.Folders,
		Excludes:           config.Excludes,
		SourceCodePatterns: config.SourceCodePatterns,
		Quiet:              quiet,
		Debug:              debug,
	}

	amountOfFolders := len(config.FileSystem.Folders)
	amountOfFiles := len(config.FileSystem.Files)

	sechubUtil.LogDebug(debug, fmt.Sprintf("appendToSourceCodeZipFile - %d folders defined: %+v", amountOfFolders, config.FileSystem.Folders))
	sechubUtil.LogDebug(debug, fmt.Sprintf("appendToSourceCodeZipFile - %d files defined: %+v", amountOfFiles, config.FileSystem.Files))
	sechubUtil.LogDebug(debug, fmt.Sprintf("appendToSourceCodeZipFile - Excludes: %+v", config.Excludes))
	sechubUtil.LogDebug(debug, fmt.Sprintf("appendToSourceCodeZipFile - SourceCodePatterns: %+v", config.SourceCodePatterns))

	if amountOfFolders == 0 && amountOfFiles == 0 { // nothing defined, so nothing to do
		return nil
	}

	return sechubUtil.Zip(&zipConfig)
}
