// SPDX-License-Identifier: MIT

package cli

import (
	"errors"
	"fmt"
	"strings"
)

func addLabelToList(list map[string]string, labelDefinition string, overrideIfExists bool) (map[string]string, error) {
	if len(labelDefinition) < 3 || !strings.Contains(labelDefinition, "=") {
		return list, errors.New("incorrect label definition. Example: \"key1=value1\"")
	}

	splitted := strings.Split(labelDefinition, "=")

	if splitted[1] == "" {
		return list, errors.New("no value given for key \"" + splitted[0] + "\"")
	}

	if !overrideIfExists && list[splitted[0]] != "" {
		return list, nil
	}

	list[splitted[0]] = splitted[1]
	return list, nil
}

// applyLabelsToConfig extends/creates the `labels` section in the sechub config JSON
func applyLabelsToConfigJson(context *Context) error {
	var err error
	labels := context.sechubConfig.MetaData["labels"]
	l, ok := labels.(map[string]interface{})
	fmt.Println("labels:", labels, " l:", l, " ok:", ok)
	if ok {
		for k, v := range l {
			fmt.Println("- k:", k, "v:", v)
			context.config.labels, err = addLabelToList(context.config.labels, fmt.Sprintf("%v=%v", k, v), false)
			if err != nil {
				return err
			}
		}
	}
	context.sechubConfig.MetaData["labels"] = context.config.labels
	return err
}
