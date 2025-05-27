// SPDX-License-Identifier: MIT
import { describe, it, beforeEach, expect, vi } from 'vitest';
import { ResponseError } from '../../src/generated-sources/openapi/runtime';
import router from '../../src/router/index'
import { handleApiError } from '../../src/services/apiErrorHandler';

// Mock the router
vi.mock('../../src/router/index');

describe('handleApiError', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  })

  it('router.push is called with "/login" when ResponseError status is 401', () => {
    /* prepare */
    const response = {status: 401} as Response;
    const responseError = new ResponseError(response, "Unauthorized");

    /* execute */
    handleApiError(responseError);

    /* test */
    expect(router.push).toHaveBeenCalledWith('/login');
  });

  it('router.push is not called for non-ResponseError errors', () => {
    /* prepare */
    const genericError = new Error("Invalid error type!")

    /* execute */
    handleApiError(genericError);

    /* test */
    expect(router.push).not.toHaveBeenCalled();
  });

  it('router.push is not called for ResponseError with status other than 401', () => {
    /* prepare */
    const response = {status: 403} as Response;
    const responseError = new ResponseError(response, "Forbidden");

    /* execute */
    handleApiError(responseError);

    /* test */
    expect(router.push).not.toHaveBeenCalled();
  });
  
});