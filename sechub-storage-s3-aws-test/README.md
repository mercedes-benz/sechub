<!-- SPDX-License-Identifier: MIT --->
## About sechub-shared-kernel-storage
This is a special testproject having a dependency to s3mock.

S3mock is itself a spring application. So integrating it for junit tests
can have very much side effects when starting from an IDE (e.g. eclipse).

So, this is a dedicated gradle project which has only dependencies to storage
parts (core, s3-aws) and nothing else.

#### What happens with s3mock otherwise
`S3MockApplication` itself is a spring boot application and will have problems
to be ready for junit test execution when we got spring stereotypes inside other
code:

- slow start
- unexpected behaviours
- ...

So we can simply use s3mocking for junit tests without special processes, docker
containers etc.
