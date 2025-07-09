import { Configuration } from "sechub-openapi-ts-client";

export default function createApiConfig(basePath: string, username: string, password: string): Configuration {
    return new Configuration({
        basePath: basePath,
        username: username,
        password: password,
        headers: {
            'Content-Type': 'application/json',
        },
    });
}
