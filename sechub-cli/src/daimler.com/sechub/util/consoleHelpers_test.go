// SPDX-License-Identifier: MIT

package util

import (
	"fmt"
	"log"
	"os"
	"testing"

	sechubTestUtil "daimler.com/sechub/testutil"
)

func Example_readFromConsole() {
	// PREPARE
	fakeStdin, w, err := os.Pipe()
	if err != nil {
		log.Fatal(err)
	}
	originalStdin := os.Stdin
	os.Stdin = fakeStdin

	longString := "1234567890"
	for i := 0; i < 5; i++ {
		longString = longString + longString
	}

	// EXECUTE
	fmt.Fprintln(w, "hello")
	r1, err := ReadFromConsole()

	fmt.Fprintln(w, "hello again")
	fmt.Fprintln(w, "hello again2")
	r2, err := ReadFromConsole()

	fmt.Fprintln(w, longString)
	r3, err := ReadFromConsole() // Should be truncated to 255 chars

	// Restore Stdin
	os.Stdin = originalStdin

	// TEST
	fmt.Println(r1)
	fmt.Println(r2)
	fmt.Println(len(r3), "of", len(longString))
	// Output:
	// hello
	// hello again
	// 255 of 320
}

func TestReadAllowedItemFromConsole(t *testing.T) {
	// PREPARE
	fakeStdin, w, err := os.Pipe()
	if err != nil {
		log.Fatal(err)
	}
	originalStdin := os.Stdin
	os.Stdin = fakeStdin

	itemlist := []ConsoleInputItem{
		{Input: "a", ShortDescription: "option a"},
		{Input: "B", ShortDescription: "option B"},
		{Input: "c", ShortDescription: "option c"},
	}

	// EXECUTE
	fmt.Fprintln(w, "a")
	r1, err := ReadAllowedItemFromConsole("Prompt1", itemlist)

	fmt.Fprintln(w, "b")
	r2, err := ReadAllowedItemFromConsole("Prompt2", itemlist)

	fmt.Fprintln(w, "C")
	r3, err := ReadAllowedItemFromConsole("Prompt3", itemlist)

	// Restore Stdin
	os.Stdin = originalStdin

	// TEST
	sechubTestUtil.AssertEquals("a", r1, t)
	sechubTestUtil.AssertEquals("b", r2, t)
	sechubTestUtil.AssertEquals("c", r3, t)
}
