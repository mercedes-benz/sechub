// SPDX-License-Identifier: MIT
package util

import "testing"
import "strings"

func AssertContains(list []string, wanted string, t *testing.T) {
	if !Contains(list, wanted) {
		t.Fatalf("Did not found %s inside %s", wanted, list)
	}
}

func AssertContainsNot(list []string, wanted string, t *testing.T) {
	if Contains(list, wanted) {
		t.Fatalf("Did found %s inside %s", wanted, list)
	}
}
func AssertSize(list []string, wantedLength int, t *testing.T) {
	length := len(list)
	if length != wantedLength {
		t.Fatalf("Expected size %d but found %d", length, wantedLength)
	}
}

func AssertEquals(expected string, found string, t *testing.T) {
	if expected != found {
		t.Fatalf("Strings differ:\nExpected:%s\nGot     :%s", expected, found)
	}
}

func AssertTrue(found bool, t *testing.T) {
	if !found {
		t.Fatalf("Assert failed - not true, but false")
	}
}

func AssertFalse(found bool, t *testing.T) {
	if found {
		t.Fatalf("Assert failed - not false, but true")
	}
}


func Check(err error, t *testing.T) {
	if err != nil {
		t.Fatalf("Error detected:%s", err)
	}
}

func Contains(list []string, wanted string) bool {
	for _, found := range list {
		if found == wanted {
			return true
		}
	}
	return false
}

/* converts a path containing windows separators to unix ones */
func ConvertBackslashPath(path string) string {
	return strings.Replace(path, "\\", "/", -1) /* convert all \ to / if on a windows machine */
}
