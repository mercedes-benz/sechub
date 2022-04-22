// SPDX-License-Identifier: MIT

package util

// inspired by https://golangcode.com/create-zip-files-in-go/
// inspired by https://stackoverflow.com/questions/49057032/recursively-zipping-a-directory-in-golang

import (
	"archive/zip"
	"errors"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"
)

type zipcontext struct {
	atLeastOneFileZipped bool
	filename             string
	config               *ZipConfig
}

// ZipConfig - contains all necessary things for zipping source code for scanning
type ZipConfig struct {
	Folders            []string
	Excludes           []string
	SourceCodePatterns []string
	Debug              bool
}

// ZipFileHasNoContent error message saying zip file has no content
const ZipFileHasNoContent = "Zipfile has no content!"

// TargetZipFileLoop error message when it comes to an infinite lopp because target inside zipped content
const TargetZipFileLoop = "Target zipfile would be part of zipped content, leading to infinite loop. Please change target path!"

// ZipFolders - Will zip given content of given folders into given filePath.
// E.g. when filePath contains subfolder sub1/text1.txt, sub2/text1.txt, sub2/sub3/text1.txt the
// zip of sub1 and sub2 will result in "sub1/text1.txt, sub2/text1.txt, sub2/sub3/text1.txt"
//
func ZipFolders(filePath string, config *ZipConfig, silent bool) (err error) {
	filename, _ := filepath.Abs(filePath)

	/* create zip file */
	newZipFile, err := os.Create(filename)
	if err != nil {
		return err
	}
	defer newZipFile.Close()

	/* create zip writer */
	zipWriter := zip.NewWriter(newZipFile)
	defer zipWriter.Close()

	/* define zip context */
	zipcontext := new(zipcontext)
	zipcontext.filename = filename
	zipcontext.config = config

	/* For each folder */
	for _, folder := range config.Folders {
		// tribute to Windows... (convert \ to / )
		folder = ConvertBackslashPath(folder)

		// Remove trailing slash if present
		folder = strings.TrimSuffix(folder, "/")

		// Add folder to zipfile
		err = zipOneFolderRecursively(zipWriter, folder, zipcontext, silent)

		if err != nil {
			return err
		}
	}
	if !zipcontext.atLeastOneFileZipped {
		return errors.New(ZipFileHasNoContent)
	}

	return nil
}

func zipOneFolderRecursively(zipWriter *zip.Writer, folder string, zipContext *zipcontext, silent bool) error {
	folderPathAbs, _ := filepath.Abs(folder)
	if _, err := os.Stat(folderPathAbs); os.IsNotExist(err) {
		return errors.New("Folder not found: " + folder + " (" + folderPathAbs + ")")
	}
	Log(fmt.Sprintf("Zipping folder: %s (%s)", folder, folderPathAbs), silent)

	err := filepath.Walk(folder, func(file string, info os.FileInfo, err error) error {
		if info == nil {
			return errors.New("Did not find folder file info " + folder)
		}
		if info.IsDir() {
			return nil
		}
		if err != nil {
			return err
		}
		if zipContext.filename == file {
			return errors.New(TargetZipFileLoop)
		}

		fileAbs, err := filepath.Abs(file)
		if err != nil {
			return err
		}
		pwdAbs, _ := filepath.Abs(".")
		// Make zip path relative to current working directory (the usual case)
		zipPath := strings.TrimPrefix(fileAbs, pwdAbs)

		// Change to a Unix-Style path if on Windows
		zipPath = ConvertToUnixStylePath(zipPath)

		// If we still have an absolute path: use the non-absolute file path stripped from "./" and "../"
		if strings.HasPrefix(zipPath, "/") {
			zipPath = file
			zipPath = ConvertToUnixStylePath(zipPath)

			zipPath = strings.ReplaceAll(zipPath, "../", "")
			zipPath = strings.ReplaceAll(zipPath, "./", "")
			// Remove leading / from zip path
			zipPath = strings.TrimPrefix(zipPath, "/")
		}

		// Only accept source code files
		isSourceCode := false
		for _, srcPattern := range zipContext.config.SourceCodePatterns {
			if strings.HasSuffix(zipPath, srcPattern) {
				LogDebug(zipContext.config.Debug, fmt.Sprintf("%q matches %q -> is source code", file, srcPattern))
				isSourceCode = true
				break
			}
		}

		// no matches above -> ignore file
		if !isSourceCode {
			LogDebug(zipContext.config.Debug, fmt.Sprintf("%q has no match with supported file extensions -> skip", zipPath))
			return nil
		}

		// Filter excludes
		for _, excludePattern := range zipContext.config.Excludes {
			if Filepathmatch(zipPath, excludePattern) {
				LogDebug(zipContext.config.Debug, fmt.Sprintf("%q matches exclude pattern %q -> skip", file, excludePattern))
				return nil
			}
		}

		LogDebug(zipContext.config.Debug, "Adding "+zipPath+" <- "+fileAbs)

		/* handle */
		fileToAdd, err := os.Open(file)
		if err != nil {
			return err
		}
		defer fileToAdd.Close()

		// Get the file information
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

		writer, err := zipWriter.CreateHeader(header)
		if err != nil {
			return err
		}
		if _, err = io.Copy(writer, fileToAdd); err != nil {
			return err
		}

		/* done */
		zipContext.atLeastOneFileZipped = true
		return nil
	})
	return err
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
