// SPDX-License-Identifier: MIT

package util

import (
	"encoding/json"
	"os"
	"reflect"
	"strings"
	"testing"
)

// AssertErrorHasExpectedStartMessage checks given error is not null and starts with expected message
func AssertErrorHasExpectedStartMessage(err error, expectedErrMsg string, t *testing.T) {
	AssertError(err, t)

	if !strings.HasPrefix(err.Error(), expectedErrMsg) {
		t.Fatalf("error = \"%s\" but expected beginning with \"%s\"...", err.Error(), expectedErrMsg)
	}
}

// AssertErrorHasExpectedMessage checks given error is not null and contains expected message
func AssertErrorHasExpectedMessage(err error, expectedErrMsg string, t *testing.T) {
	AssertError(err, t)
	if err.Error() != expectedErrMsg {
		t.Fatalf("error = \"%s\" but expected \"%s\".", err.Error(), expectedErrMsg)
	}
}

// AssertError checks given error is not null
func AssertError(err error, t *testing.T) {
	if err == nil {
		t.Fatalf("No error returned!")
	}
}

// AssertNoError checks there is no error
func AssertNoError(err error, t *testing.T) {
	if err != nil {
		t.Fatalf("Error occured: \"%s\"", err.Error())
	}
}

// AssertContains checks wanted string is inside given list
func AssertContains(list []string, wanted string, t *testing.T) {
	if !Contains(list, wanted) {
		t.Fatalf("Did not find %s inside %s", wanted, list)
	}
}

// AssertContainsNot checks unwanted string is NOT inside given list
func AssertContainsNot(list []string, unwanted string, t *testing.T) {
	if Contains(list, unwanted) {
		t.Fatalf("Found %s inside %s which was not expected.", unwanted, list)
	}
}

// AssertSize checks list has wanted length
func AssertSize(list []string, wantedLength int, t *testing.T) {
	length := len(list)
	if length != wantedLength {
		t.Fatalf("Expected size %d but found %d", wantedLength, length)
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

// AssertEquals checks expected value equals given value (any type possible like string, int, ...)
func AssertEquals(expected interface{}, given interface{}, t *testing.T) {
	if expected != given {
		t.Fatalf("Values differ:\n   got: %#v (type %T)\n  want: %#v (type %T)\n", given, given, expected, expected)
	}
}

// AssertNotEquals checks notExpected value equals given value
func AssertNotEquals(notExpected interface{}, given interface{}, t *testing.T) {
	if notExpected == given {
		t.Fatalf("Values do NOT differ:\n  got     : %#v (type %T)\n  unwanted: %#v (type %T)\n", given, given, notExpected, notExpected)
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
		t.Fatalf("Error detected: %q", err)
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

// AssertFileExists - checks if a file exists
func AssertFileExists(filePath string, t *testing.T) {
	_, err := os.Stat(filePath)
	if os.IsNotExist(err) {
		t.Fatalf("File %q expected, but does not exist.", filePath)
	} else {
		Check(err, t)
	}
}

// AssertMinimalFileSize - checks a file's size to be at least `size` bytes
func AssertMinimalFileSize(filePath string, size int64, t *testing.T) {
	fileinfo, err := os.Stat(filePath)
	if fileinfo.Size() < size {
		t.Fatalf("File %q too small: expected >= %d bytes, but has only %d.", filePath, size, fileinfo.Size())
	}
	Check(err, t)
}

// AssertContains checks wanted string is contained in given string s
func AssertStringContains(s string, wanted string, t *testing.T) {
	if !strings.Contains(s, wanted) {
		t.Fatalf("Did not find \"%s\" inside \"%s\"", wanted, s)
	}
}

// AssertStringContainsNot checks unwanted string is not contained in given string s
func AssertStringContainsNot(s string, unwanted string, t *testing.T) {
	if strings.Contains(s, unwanted) {
		t.Fatalf("Found \"%s\" inside \"%s\", but should not be found.", unwanted, s)
	}
}
