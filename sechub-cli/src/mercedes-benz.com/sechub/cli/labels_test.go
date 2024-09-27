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

func Test_empty_key_in_addLabelToList(t *testing.T) {
	// PREPARE
	var err error
	labels := map[string]string{}
	malformedLabel := "=value1"

	// EXECUTE
	_, err = addLabelToList(labels, malformedLabel, false)

	// TEST
	sechubTestUtil.AssertErrorHasExpectedStartMessage(err, "key cannot be empty", t)
}

func Example_addLabelToList_value_contains_equals_signs() {
	// PREPARE
	labels := map[string]string{}
	label_with_equals_signs := "key1=myvalue=123=test"

	// EXECUTE
	labels, _ = addLabelToList(labels, label_with_equals_signs, false)

	// TEST
	fmt.Println(labels)

	// Output:
	// map[key1:myvalue=123=test]
}

func Example_applyLabelsToConfigJson_with_labels_section_in_json() {
	// PREPARE
	var context Context
	var config Config
	context.config = &config

	context.config.configFileRead = true
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
	context.contentToSend = []byte(sechubJSON)

	// These lables must override the above defined ones
	labels := map[string]string{
		"key1": "value1x",
	}
	context.config.labels = labels

	// EXECUTE
	applyLabelsToConfigJson(&context)

	// TEST
	fmt.Printf("labels: %+v\n", context.config.labels)
	fmt.Println("context.contentToSend:", string(context.contentToSend))

	// Output:
	// labels: map[key1:value1x key2:value2]
	// context.contentToSend: {"apiVersion":"1.0","metaData":{"labels":{"key1":"value1x","key2":"value2"}},"project":"myproject"}
}

func Example_applyLabelsToConfigJson_with_labels_section_only_in_json() {
	// PREPARE
	var context Context
	var config Config
	context.config = &config

	context.config.configFileRead = true
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
	context.contentToSend = []byte(sechubJSON)

	// Lables are empty
	labels := map[string]string{}
	context.config.labels = labels

	// EXECUTE
	applyLabelsToConfigJson(&context)

	// TEST
	fmt.Printf("labels: %+v\n", context.config.labels)

	// Output:
	// labels: map[key1:value1 key2:value2]
}

func Example_applyLabelsToConfigJson_without_labels_section_in_json() {
	// PREPARE
	var context Context
	var config Config
	context.config = &config

	context.config.configFileRead = true
	sechubJSON := `
  {
    "apiVersion": "1.0",
    "project": "myproject",
    "metaData": {
      "other": "123"
    }
  }
  `
	context.contentToSend = []byte(sechubJSON)

	// These lables must override the above defined ones
	labels := map[string]string{
		"key1": "value1x",
	}
	context.config.labels = labels

	// EXECUTE
	applyLabelsToConfigJson(&context)

	// TEST
	fmt.Printf("labels: %+v\n", context.config.labels)
	fmt.Println("context.contentToSend:", string(context.contentToSend))

	// Output:
	// labels: map[key1:value1x]
	// context.contentToSend: {"apiVersion":"1.0","metaData":{"labels":{"key1":"value1x"},"other":"123"},"project":"myproject"}
}

func Example_applyLabelsToConfigJson_without_metadata_section_in_json() {
	// PREPARE
	var context Context
	var config Config
	context.config = &config

	context.config.configFileRead = true
	sechubJSON := `
  {
    "apiVersion": "1.0",
    "project": "myproject"
  }
  `
	context.contentToSend = []byte(sechubJSON)

	// These lables must override the above defined ones
	labels := map[string]string{
		"key1": "value1x",
	}
	context.config.labels = labels

	// EXECUTE
	applyLabelsToConfigJson(&context)

	// TEST
	fmt.Printf("labels: %+v\n", context.config.labels)
	fmt.Println("context.contentToSend:", string(context.contentToSend))

	// Output:
	// labels: map[key1:value1x]
	// context.contentToSend: {"apiVersion":"1.0","metaData":{"labels":{"key1":"value1x"}},"project":"myproject"}
}

func Example_applyLabelsToConfigJson_with_no_config_file() {
	// PREPARE
	var context Context
	var config Config
	context.config = &config

	context.config.configFileRead = false

	// These lables must override the above defined ones
	labels := map[string]string{
		"key1": "value1",
	}
	context.config.labels = labels

	// EXECUTE
	applyLabelsToConfigJson(&context)

	// TEST
	fmt.Printf("labels: %+v\n", context.config.labels)
	fmt.Println("context.contentToSend:", string(context.contentToSend))

	// Output:
	// labels: map[key1:value1]
	// context.contentToSend:
}
