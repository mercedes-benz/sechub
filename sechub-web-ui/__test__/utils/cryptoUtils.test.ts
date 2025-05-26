// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest'
import { createSha256Checksum } from '../../src/utils/cryptoUtils'

describe('createSha256Checksum', () => {
  it('should create SHA-256 checksum for a file', async () => {
    /* prepare */
    const fileContent = 'Hello, world!'
    const file = new File([fileContent], 'hello.txt', { type: 'text/plain' })
    const expectedChecksum = '315f5bdb76d078c43b8ac0064e4a0164612b1fce77c869345bfc94c75894edd3'

    /* execute */
    const checksum = await createSha256Checksum(file)

    /* test */
    expect(checksum).toBe(expectedChecksum)
  })
})
