// SPDX-License-Identifier: MIT

package util

import (
	"archive/tar"
	"errors"
	"fmt"
	"io"
	"io/fs"
	"os"
	"path/filepath"
	"strings"
)

// TarConfig - contains all necessary information for creating a tar archive for scanning
type TarConfig struct {
	TarFileName string
	TarWriter   *tar.Writer
	PrefixInTar string
	Files       []string
	Folders     []string
	Excludes    []string
	Quiet       bool
	Debug       bool
}

const TarFileHasNoContent = "Binaries tar file is empty! Please check your \"binaries\" section in the config file."
const TarFileNotCreated = "No binaries tar file created. Assuming \"remote\" section is defined."

// TargetTarFileLoop error message when it comes to an infinite loop because the tar file would be part of its own content
const TargetTarFileLoop = "Target tarfile would be part of its own content, leading to infinite loop. Please change target path!"

// Tar - Will tar defined folders and files using given TarConfig.
//
//	Note: TarConfig.TarWriter must be created beforehand!
func Tar(config *TarConfig) (err error) {
	Log(fmt.Sprintf("Creating tar archive for upload (%s)", config.TarFileName), config.Quiet)

	// Add defined folders to tar file
	for _, folder := range config.Folders {
		// tribute to Windows... (convert \ to / )
		folder = ConvertBackslashPath(folder)

		// Remove trailing slash if present
		folder = strings.TrimSuffix(folder, "/")

		// Add folder to tarfile
		err = tarOneFolderRecursively(folder, config)
		if err != nil {
			return err
		}
	}

	// Add defined single files to tar file
	currentWorkingDirectory, _ := filepath.Abs(".")
	var tarPath string
	for _, file := range config.Files {
		file = ConvertBackslashPath(file)
		tarPath, err = normalizeArchivePath(file, currentWorkingDirectory)
		if err != nil {
			return err
		}
		// Add prefix to tarPath
		tarPath = config.PrefixInTar + tarPath
		Log(fmt.Sprintf("Adding file '%s' to tar", file), config.Quiet)

		err = tarOneFile(file, tarPath, config)
		if err != nil {
			return err
		}
	}

	return nil
}

func tarOneFolderRecursively(folder string, config *TarConfig) error {
	currentWorkingDirectory, _ := filepath.Abs(".")
	folderPathAbs, _ := filepath.Abs(folder)
	if _, err := os.Stat(folderPathAbs); os.IsNotExist(err) {
		return errors.New("Folder not found: " + folder + " (" + folderPathAbs + ")")
	}
	Log(fmt.Sprintf("Adding folder '%s' to tar (%s)", folder, folderPathAbs), config.Quiet)

	err := filepath.Walk(folder, func(file string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if info == nil {
			return errors.New("Did not find folder file info of " + folder)
		}
		if info.IsDir() {
			return nil
		}

		var tarPath string
		tarPath, err = normalizeArchivePath(file, currentWorkingDirectory)
		if err != nil {
			return err
		}

		// Filter excludes
		for _, excludePattern := range config.Excludes {
			var path string
			if strings.HasPrefix(excludePattern, "/") {
				path = file
			} else {
				path = tarPath
			}

			if FilePathMatch(path, excludePattern) {
				LogDebug(config.Debug, fmt.Sprintf("%q matches exclude pattern %q -> skip", file, excludePattern))
				return nil
			}
		}

		if file == config.TarFileName {
			// Cannot add tar file to itself
			return errors.New(TargetTarFileLoop)
		}

		// Add prefix to tarPath
		tarPath = config.PrefixInTar + tarPath

		return tarOneFile(file, tarPath, config)
	})
	return err
}

func tarOneFile(file string, tarPath string, config *TarConfig) error {

	if IsSymlink(file) {
		LogNotice("Skipping "+file+" because it is a symlink.")
		return nil
	}

	LogDebug(config.Debug, "Adding "+tarPath)

	fileToAdd, err := os.Open(file)
	if err != nil {
		return err
	}
	defer fileToAdd.Close()

	// Get the file information
	var info fs.FileInfo
	info, err = fileToAdd.Stat()
	if err != nil {
		return err
	}

	// Take file metadata as is (owner, access dates etc.)
	header, _ := tar.FileInfoHeader(info, "symlinks are not supported")

	// Change path in archive to requested tarPath
	header.Name = tarPath

	// Write header data into tar file
	err = config.TarWriter.WriteHeader(header)
	if err != nil {
		return err
	}

	// Write file content into tar file
	_, err = io.Copy(config.TarWriter, fileToAdd)
	if err != nil {
		return err
	}

	return nil
}

// ReadContentOfTarFile - return files inside a tarfile as a list of strings
func ListContentOfTarFile(tarFilePath string) ([]string, error) {
	list := []string{}

	reader, err := os.Open(tarFilePath)
	if err != nil {
		return list, err
	}
	tarReader := tar.NewReader(reader)

	for {
		header, err := tarReader.Next()
		if err == io.EOF {
			break
		} else if err != nil {
			return list, err
		}

		// Skip directories
		if header.FileInfo().IsDir() {
			continue
		}

		// Add file path to list
		list = append(list, header.Name)
	}

	return list, err
}
