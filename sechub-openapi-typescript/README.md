<!-- SPDX-License-Identifier: MIT --->
# OpenApi Generator for TypeScript

## Usage

To generate the SecHub openAPI Client run the following command in the project root directory.

### Prerequisites

Use correct Node.js version with nvm:

```bash
nvm install 
```

```bash
nvm use
```

### Install dependencies

```bash
npm install
```

### Generate the SecHub OpenAPI Client

```bash
npm run generate-api-client
 ```

### link the generated client to your local npm environment

```bash
npm link
```

## Description

In the src/services overrides for incorrectly generated code is overridden.
As well as shared services (by web-ui and vscode extension) are implemented here.