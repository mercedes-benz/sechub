// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"testing"

	sechubTestUtil "mercedes-benz.com/sechub/testutil"
)

func Example_addLabelToList() {
	// PREPARE
	var err error
	labels := map[string]string{
		"key1": "value1 original",
		"key2": "value2 original",
	}
	newLabel1 := "key1=value1 new"
	newLabel2 := "key2=value2 new"
	newLabel3 := "key3=value3 with blanks"

	// EXECUTE
	labels, err = addLabelToList(labels, newLabel1, false)
	labels, err = addLabelToList(labels, newLabel2, true)
	labels, err = addLabelToList(labels, newLabel3, false)

	// TEST
	if err != nil {
		fmt.Println("Error while testing addLabelToList()")
	}

	fmt.Printf("\"key1\": \"%s\"\n", labels["key1"])
	fmt.Printf("\"key2\": \"%s\"\n", labels["key2"])
	fmt.Printf("\"key3\": \"%s\"\n", labels["key3"])

	// Output:
	// "key1": "value1 original"
	// "key2": "value2 new"
	// "key3": "value3 with blanks"
}

func Test_no_equals_sign_in_addLabelToList(t *testing.T) {
	// PREPARE
	var err error
	labels := map[string]string{}
	malformedLabel := "key1:value1 new"

	// EXECUTE
	_, err = addLabelToList(labels, malformedLabel, false)

	// TEST
	sechubTestUtil.AssertErrorHasExpectedStartMessage(err, "incorrect label definition", t)
}

func Test_empty_definition_in_addLabelToList(t *testing.T) {
	// PREPARE
	var err error
	labels := map[string]string{}
	malformedLabel := ""

	// EXECUTE
	_, err = addLabelToList(labels, malformedLabel, false)

	// TEST
	sechubTestUtil.AssertErrorHasExpectedStartMessage(err, "incorrect label definition", t)
}

func Test_key_with_empty_value_in_addLabelToList(t *testing.T) {
	// PREPARE
	var err error
	labels := map[string]string{}
	malformedLabel := "key1="

	// EXECUTE
	_, err = addLabelToList(labels, malformedLabel, false)

	// TEST
	sechubTestUtil.AssertErrorHasExpectedStartMessage(err, "no value given for key", t)
}

func Example_labels_section_is_read_as_map() {
	// PREPARE
	var context Context
	var config Config
	context.config = &config

	sechubJSON := `
  {
    "apiVersion": "1.0",
    "project": "myproject",
    "metaData": {
      "labels": {
        "key1": "value1",
        "key2": "value2"
      }
    }
  }
  `
	// EXECUTE
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	// TEST
	fmt.Printf("%+v\n", context.sechubConfig.MetaData["labels"])

	// Output:
	// map[key1:value1 key2:value2]
}

func Example_non_existing_labels_section() {
	// PREPARE
	var context Context
	var config Config
	context.config = &config

	sechubJSON := `
  {
    "apiVersion": "1.0",
    "project": "myproject"
  }
  `
	// EXECUTE
	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	// TEST
	fmt.Printf("%+v\n", context.sechubConfig.MetaData["labels"])

	// Output:
	// <nil>
}

func Example_applyLabelsToConfigJson_with_labels_section_in_json() {
	// PREPARE
	var context Context
	var config Config
	context.config = &config

	sechubJSON := `
  {
    "apiVersion": "1.0",
    "project": "myproject",
    "metaData": {
      "labels": {
        "key1": "value1",
        "key2": "value2"
      }
    }
  }
  `
	// These lables must override the above defined ones
	labels := map[string]string{
		"key1": "value1x",
	}
	context.config.labels = labels

	sechubConfig := newSecHubConfigFromBytes([]byte(sechubJSON))
	context.sechubConfig = &sechubConfig

	// EXECUTE
	applyLabelsToConfigJson(&context)

	// TEST
	fmt.Printf("labels: %+v\n", context.config.labels)
	fmt.Printf("sechubConfig: %+v\n", context.sechubConfig.MetaData["labels"])

	// Output:
	// map[key1:value1x key2:value2]
}
