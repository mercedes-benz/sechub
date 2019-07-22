// SPDX-License-Identifier: MIT
package util

import (
	"fmt"
	"path/filepath"
	"strings"
)

//
// This method provides ANT like selectors.
// For example: "**/a*.txt" will accept
//               - "/home/tester/xyz/a1234.txt
//               - "/root/a1b.txt
//
func Filepathmatch(path string, pattern string) (result bool) {
	/* ------------------------------ */
	/* Explanation - how it works ... */
	/* ------------------------------ */
	// - we separate pattern into different path patterns by path wildcards into elements
	// - then the last element is inspected to be a file wildcard
	// - file wildcards are resolved /tried by golang filepath.Match method
	// - if this is okay we do our special path wildcard matching


	/* separate patterns containing path wildcards */
	subPattern := strings.Split(pattern, "**/")
//	fmt.Printf("\n%s - seperated into %q", pattern, subPattern)
	length := len(subPattern)
	potentialFilePattern := subPattern[length-1]

	/* ------------------------ */
	/* handle file name pattern */
	/* ------------------------ */
	substring := path
	if strings.Index(potentialFilePattern, "*") != -1 {
		/* found a file pattern in last entry */
		/* change length - so slice not changed but last entry no longer handled later */
		length--

		/* check if file patter is applicable */
		lastPathIndex := strings.LastIndex(path, "/")
		fileName := path
		indexForCut := lastPathIndex
		if lastPathIndex != -1 {
			fileName = path[lastPathIndex:]
		} else {
			indexForCut = 0
		}
		/* we just use the golang integrated filename matcher:*/
		matches, err := filepath.Match(potentialFilePattern, fileName)
		if err != nil {
			fmt.Println(err)
		}
		if !matches {
			return false
		}

		/* remove filename for further path inspection*/
		substring = path[indexForCut : len(path)-indexForCut]
	}

	/* -------------------- */
	/* handle path patterns */
	/* -------------------- */
	lastOneWasAsterisk := false
	for i := 0; i < length; i++ {
		currentSearch := subPattern[i]
		if currentSearch == "" {
			/* this is double asterisk at the begining*/
			/* so we just accept all here*/
			lastOneWasAsterisk=true
			continue
		}
		/* search for next occurrence */
		index := strings.Index(substring, currentSearch)
		if index == -1 {
			/* search not found*/
			return false
		}
		if !lastOneWasAsterisk && index != 0 {
			/* found but not starting with it*/
			/* when last pattern was not an asterisk ...*/
			return false
		}
		substring = substring[index+len(currentSearch):]
		/* after first entry we have always path asterisks between - even
		 * when we got no longer "" between (happens only on first entry)
		 */
		lastOneWasAsterisk=true
	}
	return true
}
