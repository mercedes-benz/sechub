// SPDX-License-Identifier: MIT
import { ResponseError } from '@/generated-sources/openapi/runtime'
import router from '@/router/index'


export function handleApiError(error: any) {
    if(!(error instanceof ResponseError)) {
        return;
    }
    const responseError = error as ResponseError;
    if(!responseError.response){
        return;
    }
    handle401UnauthorizedError(responseError);
}

function handle401UnauthorizedError(responseError: ResponseError) {
    if(responseError.response.status === 401){
        router.push('/login')
    }
}