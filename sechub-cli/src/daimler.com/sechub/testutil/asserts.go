// SPDX-License-Identifier: MIT
package util

import (
	"encoding/json"
	"reflect"
	"strings"
	"testing"
)

// AssertContains checks wanted string is inside given list
func AssertContains(list []string, wanted string, t *testing.T) {
	if !Contains(list, wanted) {
		t.Fatalf("Did not found %s inside %s", wanted, list)
	}
}

// AssertContainsNot checks unwanted string is NOT inside given list
func AssertContainsNot(list []string, unwanted string, t *testing.T) {
	if Contains(list, unwanted) {
		t.Fatalf("Did found %s inside %s", unwanted, list)
	}
}

// AssertSize checks list has wanted length
func AssertSize(list []string, wantedLength int, t *testing.T) {
	length := len(list)
	if length != wantedLength {
		t.Fatalf("Expected size %d but found %d", length, wantedLength)
	}
}

// AssertJSONEqualsBytes checks expected json equals given json
func AssertJSONEqualsBytes(expected []byte, given []byte, t *testing.T) {
	eq, err := jsonBytesEqual(expected, given)

	if err != nil {
		t.Fatalf("Internal compare failure:%s", err)
	}
	if !eq {

		t.Fatalf("JSON differs:\nExpected:\n-----\n%s\n-----\nGot        :\n-----\n%s\n-----\n", string(expected), string(given))
	}
}

// AssertJSONEquals checks expected json equals given json
func AssertJSONEquals(expected string, given string, t *testing.T) {
	a := []byte(expected)
	b := []byte(given)
	AssertJSONEqualsBytes(a, b, t)
}

// AssertEquals checks expected string equals given
func AssertEquals(expected string, given string, t *testing.T) {
	if expected != given {

		t.Fatalf("Strings differ:\nExpected:\n-----\n%s\n-----\nGot        :\n-----\n%s\n-----\n", expected, given)
	}
}

// AssertNotEquals checks notExpected string equals given
func AssertNotEquals(notExpected string, given string, t *testing.T) {
	if notExpected == given {
		t.Fatalf("Strings do NOT differ:\nUnexpected:%s\nGot     :%s", notExpected, given)
	}
}

// AssertTrue checks given boolean is true
func AssertTrue(found bool, t *testing.T) {
	if !found {
		t.Fatalf("Assert failed - not true, but false")
	}
}

// AssertFalse checks given boolean is false
func AssertFalse(found bool, t *testing.T) {
	if found {
		t.Fatalf("Assert failed - not false, but true")
	}
}

// Check given error is nil, otherwise fails fatal
func Check(err error, t *testing.T) {
	if err != nil {
		t.Fatalf("Error detected:%s", err)
	}
}

// Contains does check if wanted string is contained in list
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

func jsonBytesEqual(a, b []byte) (bool, error) {
	var j, j2 interface{}
	if err := json.Unmarshal(a, &j); err != nil {
		return false, err
	}
	if err := json.Unmarshal(b, &j2); err != nil {
		return false, err
	}
	return reflect.DeepEqual(j2, j), nil
}
