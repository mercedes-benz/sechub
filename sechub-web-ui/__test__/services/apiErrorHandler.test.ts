// SPDX-License-Identifier: MIT
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ResponseError } from 'sechub-openapi-typescript'
import { handleApiError } from '../../src/services/apiErrorHandler'

// Mock the router
vi.mock('../../src/router/index')

describe('handleApiError', () => {
  let originalLocation: PropertyDescriptor | undefined

  beforeEach(() => {
    vi.clearAllMocks()
    // Mock window.location.href
    originalLocation = Object.getOwnPropertyDescriptor(window, 'location')
    Object.defineProperty(window, 'location', {
      value: { href: '' },
      writable: true,
    })
  })

  afterEach(() => {
    // Restore window.location
    if (originalLocation) {
      Object.defineProperty(window, 'location', originalLocation)
    }
  })

  it('sets window.location.href to "/login" when ResponseError status is 401', () => {
    /* prepare */
    const response = { status: 401 } as Response
    const responseError = new ResponseError(response, 'Unauthorized')

    /* execute */
    handleApiError(responseError)

    /* test */
    expect(window.location.href).toBe('/login')
  })

  it('does not change window.location.href for non-ResponseError errors', () => {
    /* prepare */
    const genericError = new Error('Invalid error type!')

    /* execute */
    handleApiError(genericError)

    /* test */
    expect(window.location.href).toBe('')
  })

  it('does not change window.location.href for ResponseError with status other than 401', () => {
    /* prepare */
    const response = { status: 403 } as Response
    const responseError = new ResponseError(response, 'Forbidden')

    /* execute */
    handleApiError(responseError)

    /* test */
    expect(window.location.href).toBe('')
  })
})
