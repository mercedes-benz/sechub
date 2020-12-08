// SPDX-License-Identifier: MIT

package util

import (
	"bufio"
	"fmt"
	"io"
	"os"
	"strings"
)

// ConsoleInputItem - struct for holding console input and short(1-2 words max) description
// eg: {input:"y", shortDescription:"yes"}
type ConsoleInputItem struct {
	Input            string
	ShortDescription string
}

// ReadAllowedItemFromConsole - read input from Console allowing only defined inputs.
// Keeps prompting until an allowed input is entered or interrupted with e.g. ctrl-C
func ReadAllowedItemFromConsole(prompt string, itemList []ConsoleInputItem) (result string, err error) {
	prompt += " ("
	for i := range itemList {
		// turn string to lowercase so we can compare later ignoring case
		itemList[i].Input = strings.ToLower(itemList[i].Input)

		prompt += fmt.Sprintf("'%s' %s, ", itemList[i].Input, itemList[i].ShortDescription)
	}
	prompt = strings.TrimSuffix(prompt, ", ")
	prompt += ") "

	// Loop until an allowed input is entered
	var input string
	for result == "" {
		fmt.Print(prompt)
		input, err = ReadFromConsole()
		input = strings.ToLower(input)

		for _, item := range itemList {
			if input == item.Input {
				result = input
			}
		}
	}

	return result, err
}

// ReadFromConsole - read a string from Console/stdin
func ReadFromConsole() (result string, err error) {
	reader := bufio.NewReaderSize(io.LimitReader(os.Stdin, 255), 256) // we always limit to 255 characters

	result, err = reader.ReadString('\n')
	return strings.TrimSuffix(result, "\n"), err
}

// PrintDashedLine - used for separation of sections in console output
func PrintDashedLine() {
	fmt.Println("------------------------------------------------------------------")
}
