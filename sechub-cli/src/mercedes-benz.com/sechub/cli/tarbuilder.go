// SPDX-License-Identifier: MIT

package cli

import (
	"archive/tar"
	"errors"
	"fmt"
	"os"
	"path/filepath"

	sechubUtil "mercedes-benz.com/sechub/util"
)

// createBinariesTarFile - compress all defined binaries into one single tar archive
func createBinariesTarFile(context *Context) error {
	tarFile, _ := filepath.Abs(context.binariesTarFileName)

	// create tar file
	newTarFile, err := os.Create(tarFile)
	if err != nil {
		return err
	}

	// create tar writer
	tarWriter := tar.NewWriter(newTarFile)

	// Add everything in Data.Binaries to tar file
	tarFileShouldHaveContent := false
	for _, item := range context.sechubConfig.Data.Binaries {
		if len(item.FileSystem.Files) > 0 || len(item.FileSystem.Folders) > 0 {
			err = appendToBinariesTarFile(tarFile, tarWriter, item, context.config.quiet, context.config.debug)
			if err != nil {
				return err
			}
			tarFileShouldHaveContent = true
		}
	}

	tarWriter.Close()
	newTarFile.Close()

	// Check if context.binariesTarFileName is an empty tar
	// For performance reasons we only look deeper into very small files
	if tarFileShouldHaveContent {
		context.binariesTarUploadNeeded = true
		if sechubUtil.GetFileSize(tarFile) < 10000 {
			tarFileContent, _ := sechubUtil.ListContentOfTarFile(tarFile)
			if len(tarFileContent) == 0 {
				return errors.New(sechubUtil.TarFileHasNoContent)
			}
		}
	} else {
		context.binariesTarUploadNeeded = false
		sechubUtil.LogNotice(sechubUtil.TarFileNotCreated)
	}

	return nil
}

func appendToBinariesTarFile(tarFile string, tarWriter *tar.Writer, config NamedBinariesScanConfig, quiet bool, debug bool) error {
	prefix := ""
	if config.Name != "" {
		// e.g.: __data__/example-name-1/
		prefix = fmt.Sprintf("%s/%s/", archiveDataPrefix, config.Name)
	}
	tarConfig := sechubUtil.TarConfig{
		TarFileName: tarFile,
		TarWriter:   tarWriter,
		PrefixInTar: prefix,
		Files:       config.FileSystem.Files,
		Folders:     config.FileSystem.Folders,
		Excludes:    config.Excludes,
		Quiet:       quiet,
		Debug:       debug,
	}

	amountOfFolders := len(config.FileSystem.Folders)
	amountOfFiles := len(config.FileSystem.Files)

	sechubUtil.LogDebug(debug, fmt.Sprintf("appendToBinariesTarFile - %d folders defined: %+v", amountOfFolders, config.FileSystem.Folders))
	sechubUtil.LogDebug(debug, fmt.Sprintf("appendToBinariesTarFile - %d files defined: %+v", amountOfFiles, config.FileSystem.Files))
	sechubUtil.LogDebug(debug, fmt.Sprintf("appendToBinariesTarFile - Excludes: %+v", config.Excludes))

	if amountOfFolders == 0 && amountOfFiles == 0 { // nothing defined, so nothing to do
		return nil
	}

	return sechubUtil.Tar(&tarConfig)
}
