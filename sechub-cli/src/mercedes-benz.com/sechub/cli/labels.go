// SPDX-License-Identifier: MIT

package cli

import (
	"encoding/json"
	"errors"
	"fmt"
	"strings"

	sechubUtil "mercedes-benz.com/sechub/util"
)

func addLabelToList(list map[string]string, labelDefinition string, overrideIfExists bool) (map[string]string, error) {
	if len(labelDefinition) < 3 || !strings.Contains(labelDefinition, "=") {
		return list, errors.New("incorrect label definition: \"" + labelDefinition + "\". Example: \"key1=value1\"")
	}

	key, value, _ := strings.Cut(labelDefinition, "=")

	if key == "" {
		return list, errors.New("key cannot be empty: \"" + labelDefinition + "\". Example: \"key1=value1\"")
	}

	if value == "" {
		return list, errors.New("no value given for key \"" + key + "\". Example: \"key1=value1\"")
	}

	if !overrideIfExists && list[key] != "" {
		return list, nil
	}

	list[key] = value
	return list, nil
}

// applyLabelsToConfigJson extends/creates the `labels` section
// in the sechub config JSON (context.contentToSend) by context.config.labels
func applyLabelsToConfigJson(context *Context) error {
	var err error

	// Unmarshal the JSON into a map structure
	// because when using SecHubConfig struct then all new serverside json options have to be added to the client struct
	var data map[string]interface{}
	err = json.Unmarshal(context.contentToSend, &data)
	if err != nil {
		sechubUtil.LogError(fmt.Sprintf("Could not unmarshal json: %s", err))
		return err
	}

	metaData := data["metaData"]
	m, ok := metaData.(map[string]interface{})
	if ok {
		labels := m["labels"]
		var l map[string]interface{}
		l, ok = labels.(map[string]interface{})
		if ok {
			// Add labels from JSON to context.config.labels (no override)
			for key, value := range l {
				context.config.labels, err = addLabelToList(context.config.labels, fmt.Sprintf("%v=%v", key, value), false)
				if err != nil {
					return err
				}
			}
		}
	} else {
		// initialize m
		m = map[string]interface{}{}
	}

	// Create/update labels in `data` map
	m["labels"] = context.config.labels
	data["metaData"] = m

	// Build JSON
	context.contentToSend, err = json.Marshal(data)
	return err
}
