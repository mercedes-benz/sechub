<!-- SPDX-License-Identifier: MIT --->
## About sechub-shared-kernel-storage
This is a special "submodule" of `sechub-shared-kernel`

It has only storage specific dependencies but no dependency to any other sechub
project.

### Reason why storage not directly insdie sechub-shared-kernel
Reason for handling this is to have no spring boot dependencies inside this
gradle subproject. This is necessary to executes s3mock test.

#### What happens with s3mock otherwise
`S3MockApplication` itself is a spring boot application and will have problems
to be ready for junit test execution when we got spring stereotypes inside other
code:

- slow start
- unexpected behaviours
- ...

#### Where are the spring boot parts?
Spring boot parts for storage are still inside `sechub-shared-kernel`

So e.g. setup and service will be found there.
