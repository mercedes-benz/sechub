// SPDX-License-Identifier: MIT

package cli

import (
	"errors"
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
