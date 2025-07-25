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

### Install and Build Step by Step

#### Install dependencies

```bash
npm install
```

#### Generate the SecHub OpenAPI Client

```bash
npm run generate-api-client
 ```

#### build the generated client

```bash
npm run build
```

### Install and Build by script

Run the build script to install dependencies and build the project:

```bash
./build-typescript-client.sh
```

After building the client, you can find the generated code in the `dist` directory.
The module can be now imported into your TypeScript project.