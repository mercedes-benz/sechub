<!-- SPDX-License-Identifier: MIT --->
# Checkmarx Wrapper + PDS

This Helm chart enables one to deploy the [Checkmarx Wrapper](https://checkmarx.com/product/cxsast-source-code-scanning/) and the [SecHub Product Delegation Server (PDS)](https://mercedes-benz.github.io/sechub/latest/sechub-product-delegation-server.html) into a Kubernetes environment. It is recommended to use Checkmarx Wrapper + PDS together with [SecHub](https://mercedes-benz.github.io/sechub/).

NOTE: The Checkmarx Wrapper communicates with a Checkmarx SAST installation. As of 2022, Checkmarx SAST needs to be installed on Windows and it is not possible to run on Linux or Kubernetes. This PDS solution does not contain Checkmarx SAST or any scripts to set Checkmarx SAST up.