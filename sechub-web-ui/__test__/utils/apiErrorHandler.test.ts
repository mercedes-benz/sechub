// SPDX-License-Identifier: MIT
import { ResponseError } from '../../src/generated-sources/openapi/runtime';
import router from '../../src/router/index';
import { handleApiError } from '../../src/utils/apiErrorHandler';

// Mock the router
jest.mock('../../src/router/index', () => ({
  push: jest.fn(),
}));

describe('handleApiError', () => {
  beforeEach(() => {
    // Clear mocks
    jest.clearAllMocks();
  });

  it('router.push is called with "/login" when ResponseError status is 401', () => {
    /* prepare */
    const apiResponse = {
      status: 401
    } as Response;

    const responseError = {
      response: apiResponse,
    } as ResponseError;

    /* execute */
    handleApiError(responseError);

    /* test */
    expect(router.push).toHaveBeenCalledWith('/login');
  });

  it('router.push is not called for non-ResponseError errors', () => {
    /* prepare */
    const genericError = {
      message:'Generic error'
    } as Error;

    /* execute */
    handleApiError(genericError);

    /* test */
    expect(router.push).not.toHaveBeenCalled();
  });

  it('router.push is not called for ResponseError with status other than 401', () => {
    /* prepare */
    const apiResponse = {
      status: 403
    } as Response;

    const responseError = {
      response: apiResponse,
    } as ResponseError;

    /* execute */
    handleApiError(responseError);

    /* test */
    expect(router.push).not.toHaveBeenCalled();
  });
  
});