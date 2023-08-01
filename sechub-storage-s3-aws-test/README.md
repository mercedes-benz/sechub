<!-- SPDX-License-Identifier: MIT --->

## About sechub-storage-s3-aws-test
This is a special test project having a dependency to S3mock.

S3mock is itself a Spring application, which means that integrating it for "normal" junit tests
for existing Spring Boot applications (like SecHub server) will lead to side effects when starting 
from an IDE (e.g. Eclipse) because having multiple Spring Boot Applications inside classpath.

`sechub-storage-s3-aws-test` is a dedicated gradle project which has only dependencies to storage
parts (core, s3-aws) and nothing else. The only Spring Boot Application is `S3MockApplication`.

With this project it is possible to use S3Mock for Junit tests without special processes, docker
containers etc. but also without any side effects.

