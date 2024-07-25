// SPDX-License-Identifier: MIT

package util

// inspired by https://golangcode.com/create-zip-files-in-go/
// inspired by https://stackoverflow.com/questions/49057032/recursively-zipping-a-directory-in-golang

import (
	"archive/zip"
	"errors"
	"fmt"
	"io"
	"io/fs"
	"os"
	"path/filepath"
	"strings"
)

// ZipConfig - contains all necessary things for zipping source code for scanning
type ZipConfig struct {
	ZipFileName        string
	ZipWriter          *zip.Writer
	PrefixInZip        string
	Files              []string
	Folders            []string
	Excludes           []string
	SourceCodePatterns []string
	Quiet              bool
	Debug              bool
}

const ZipFileHasNoContent = "Zipfile has no content! Please check your \"sources\" section in the config file."
const ZipFileNotCreated = "No sources zip file created. Assuming \"remote\" section is defined."

// TargetZipFileLoop error message when it comes to an infinite loop because the zip file would be part of its own content
const TargetZipFileLoop = "Target zipfile would be part of zipped content, leading to infinite loop. Please change target path!"

// Zip - Will zip defined folders and files using given ZipConfig.
//
//	Note: ZipConfig.ZipWriter must be created beforehand!
func Zip(config *ZipConfig) (err error) {

	// Add defined folders to zip file
	for _, folder := range config.Folders {
		// tribute to Windows... (convert \ to / )
		folder = ConvertBackslashPath(folder)

		// Remove trailing slash if present
		folder = strings.TrimSuffix(folder, "/")

		// Add folder to zipfile
		err = zipOneFolderRecursively(folder, config)
		if err != nil {
			return err
		}
	}

	// Add defined single files to zip file
	currentWorkingDirectory, _ := filepath.Abs(".")
	var zipPath string
	for _, file := range config.Files {
		file = ConvertBackslashPath(file)
		zipPath, err = normalizeArchivePath(file, currentWorkingDirectory)
		if err != nil {
			return err
		}
		// Add prefix to zipPath
		zipPath = config.PrefixInZip + zipPath

		err = zipOneFile(file, zipPath, config)
		if err != nil {
			return err
		}
	}

	return nil
}

func zipOneFolderRecursively(folder string, config *ZipConfig) error {
	currentWorkingDirectory, _ := filepath.Abs(".")
	folderPathAbs, _ := filepath.Abs(folder)
	if _, err := os.Stat(folderPathAbs); os.IsNotExist(err) {
		return errors.New("Folder not found: " + folder + " (" + folderPathAbs + ")")
	}
	Log(fmt.Sprintf("Zipping folder: %s (%s)", folder, folderPathAbs), config.Quiet)

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

		var zipPath string
		zipPath, err = normalizeArchivePath(file, currentWorkingDirectory)
		if err != nil {
			return err
		}

		// Only accept source code files
		isSourceCode := false
		for _, srcPattern := range config.SourceCodePatterns {
			if strings.HasSuffix(file, srcPattern) {
				isSourceCode = true
				break
			}
		}

		// no matches above -> ignore file
		if !isSourceCode {
			return nil
		}

		// Filter excludes
		for _, excludePattern := range config.Excludes {
			path := ""
			if strings.HasPrefix(excludePattern, "/") {
				path = file
			} else {
				path = zipPath
			}

			if FilePathMatch(path, excludePattern) {
				LogDebug(config.Debug, fmt.Sprintf("%q matches exclude pattern %q -> skip", file, excludePattern))
				return nil
			}
		}

		if file == config.ZipFileName {
			// Cannot add zip file to itself
			return errors.New(TargetZipFileLoop)
		}

		// Add prefix to zipPath
		zipPath = config.PrefixInZip + zipPath

		return zipOneFile(file, zipPath, config)
	})
	return err
}

func zipOneFile(file string, zipPath string, config *ZipConfig) error {
	LogDebug(config.Debug, "Adding "+zipPath)

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

	header, err := zip.FileInfoHeader(info)
	if err != nil {
		return err
	}

	// Using FileInfoHeader() above only uses the basename of the file. If we want
	// to preserve the folder structure we can overwrite this with the full path.
	header.Name = zipPath

	// Change to deflate to gain better compression
	// see http://golang.org/pkg/archive/zip/#pkg-constants
	header.Method = zip.Deflate

	writer, err := config.ZipWriter.CreateHeader(header)
	if err != nil {
		return err
	}
	if _, err = io.Copy(writer, fileToAdd); err != nil {
		return err
	}

	return nil
}

// ReadContentOfZipFile - return files inside a zipfile as a list of strings
func ReadContentOfZipFile(path string) ([]string, error) {
	list := []string{}

	zipfile, err := zip.OpenReader(path)
	if err != nil {
		return list, err
	}
	defer zipfile.Close()

	for _, file := range zipfile.File {
		name := ConvertBackslashPath(file.Name)
		list = append(list, name)
	}
	return list, err
}
