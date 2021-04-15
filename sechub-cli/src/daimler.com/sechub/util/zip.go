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
// E.g. when filePath contains subfolder sub1/text1.txt, sub2/text2.txt, sub2/sub3/text3.txt the
// zip of sub1 and sub2 will result in "text1.txt,text2.txt,sub3/text3.txt" !
// This is optimized for sourcecode zipping when having multiple source folders
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

	/* for each folder */
	for _, folder := range config.Folders {
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

func zipOneFolderRecursively(zipWriter *zip.Writer, folder string, zContext *zipcontext, silent bool) error {
	filepathAbs, err := filepath.Abs(folder)
	if _, err := os.Stat(filepathAbs); os.IsNotExist(err) {
		return errors.New("Folder not found: " + folder + " (" + filepathAbs + ")")
	}
	Log(fmt.Sprintf("Zipping folder: %s (%s)", folder, filepathAbs), silent)

	err = filepath.Walk(folder, func(filePath string, info os.FileInfo, err error) error {
		if info == nil {
			return errors.New("Did not found folder file info " + folder)
		}
		if info.IsDir() {
			return nil
		}
		if err != nil {
			return err
		}
		if zContext.filename == filePath {
			return errors.New(TargetZipFileLoop)
		}
		/* folder : e.g. "./../../../../build/go-zip/source-for-zip/sub1" */
		folderAbs, err := filepath.Abs(folder)           /* e.g. /home/project/build/go-zip/source-for-zip/sub1"*/
		folderAbs = filepath.Clean(folderAbs)            /* remove if trailing / is there*/
		folderAbs = folderAbs + string(os.PathSeparator) /* append always a trailing slash at the end..., so it will be removed on relative path */
		if err != nil {
			return err
		}
		fileAbs, err := filepath.Abs(filePath)
		if err != nil {
			return err
		}
		relPathFromFolder := filepath.Clean(strings.TrimPrefix(fileAbs, folderAbs)) /* e.g. "/sub1"*/

		// tribute to Windows... (convert \ to / )
		relPathFromFolder = ConvertBackslashPath(relPathFromFolder)

		// Only accept source code files
		isSourceCode := false
		for _, srcPattern := range zContext.config.SourceCodePatterns {
			if strings.HasSuffix(relPathFromFolder, srcPattern) {
				LogDebug(zContext.config.Debug, fmt.Sprintf("%q matches %q -> is source code", relPathFromFolder, srcPattern))
				isSourceCode = true
				break
			}
		}

		// no matches above -> ignore file
		if !isSourceCode {
			LogDebug(zContext.config.Debug, fmt.Sprintf("%q no match with source code patterns -> skip", relPathFromFolder))
			return nil
		}

		// Filter excludes
		for _, excludePattern := range zContext.config.Excludes {
			if Filepathmatch(relPathFromFolder, excludePattern) {
				LogDebug(zContext.config.Debug, fmt.Sprintf("%q matches exclude pattern %q -> skip", relPathFromFolder, excludePattern))
				return nil
			}
		}

		/* handle */
		zipfile, err := os.Open(filePath)
		if err != nil {
			return err
		}
		defer zipfile.Close()

		// Get the file information
		info, err = zipfile.Stat()
		if err != nil {
			return err
		}

		header, err := zip.FileInfoHeader(info)
		if err != nil {
			return err
		}

		// Using FileInfoHeader() above only uses the basename of the file. If we want
		// to preserve the folder structure we can overwrite this with the full path.
		header.Name = relPathFromFolder

		// Change to deflate to gain better compression
		// see http://golang.org/pkg/archive/zip/#pkg-constants
		header.Method = zip.Deflate

		writer, err := zipWriter.CreateHeader(header)
		if err != nil {
			return err
		}
		if _, err = io.Copy(writer, zipfile); err != nil {
			return err
		}

		/* done */
		zContext.atLeastOneFileZipped = true
		return nil
	})
	return err
}
