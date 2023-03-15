// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"time"
)

func Example_computeNextWaitInterval() {
	/* prepare */
	max := int64(60 * time.Second)

	/* execute */
	result1 := computeNextWaitInterval(int64(100*time.Second), max)
	result2 := computeNextWaitInterval(int64(10*time.Second), max)

	/* test */
	fmt.Println(result1)
	fmt.Println(result2)
	// Output:
	// 60000000000
	// 15000000000
}
