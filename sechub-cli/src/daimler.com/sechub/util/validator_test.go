// SPDX-License-Identifier: MIT

package util

import (
	"testing"

	. "daimler.com/sechub/testutil"
)

func Test_emptyJSON_is_json(t *testing.T) {
	string := "{}"
	AssertTrue(IsValidJSON([]byte(string)), t)
}

func Test_json_with_id_is_1_is_json(t *testing.T) {
	string := `{"id":"1"}`
	AssertTrue(IsValidJSON([]byte(string)), t)
}

func Test_json_containing_go_template_keys_is_valid_json(t *testing.T) {
	string := `{
		"apiVersion": "1.0",
		"webScan": {
			"uris": [
				"https://productfailure.demo.example.org"
			],
			"login": {
				"url": "https://productfailure.demo.example.org/login",
				"basic": {
					"user": "${{ .LOGIN_USER }}",
					"password": "${{ .LOGIN_PWD }}",
					"realm": "${{ .LOGIN_REALM }}" 
				}
			}
		}
	}`
	AssertTrue(IsValidJSON([]byte(string)), t)
}

func Test_json_being_corrupt_is_NOT_json(t *testing.T) {
	string := `{'id":"1"}`
	AssertFalse(IsValidJSON([]byte(string)), t)
}

func Test_empty_is_NOT_json(t *testing.T) {
	string := ""
	AssertFalse(IsValidJSON([]byte(string)), t)
}
