<!-- SPDX-License-Identifier: MIT --->
# Contributing

_Contributions are highly welcome!_

This document explains how to contribute to this project.
By contributing you will agree that your contribution will be put under the same license as this repository.

## Table of Contents
- [Communication](#communication)
- [Quick start](#quick-start)
- [Contributor License Agreement (CLA)](#contributor-license-agreement-cla)
- [Create a fork](#create-a-fork)
- [Quality](#quality)

## Communication
For communication please respect our [FOSS Code of Conduct](https://github.com/mercedes-benz/foss/blob/master/CODE_OF_CONDUCT.md).

IMPORTANT: If you want to contribute or report an issue, then first create an [issue in Github](https://github.com/mercedes-benz/sechub/issues).

Transparent and open communication is important to us.<br>
Thus, all project-related communication should happen here and in English.<br>
Issue-related communication should happen within the concerned issue.

## Quick Start
Please look at [First steps Wiki page](https://github.com/mercedes-benz/sechub/wiki/First-steps)

## Contributor License Agreement (CLA)

Upon your first pull request, you will be asked to read and accept the Mercedes-Benz Contributor License Agreement.

When submitting code, please follow the existing conventions and style in order to keep the code as readable as possible.

Before you can contribute, you will need to sign our Contributor License Agreement (CLA). When you create your first pull request, you will be requested by our CLA-assistant to sign this CLA.

If you are new to contributing in Github, [First Contributions](https://github.com/firstcontributions/first-contributions) might be a good starting point.

## Create a fork
If you would like to contribute code you can do so through Mercedes-Benz GitHub by forking the repository and sending a pull request.

1. Fork the repository at `https://github.com/mercedes-benz/sechub.git` via web UI
2. Create a branch (e.g. "feature-868-rename-to-sechub-api-java") in your forked repository
3. Make your changes in this branch
4. Create a pull request from your fork via github.com web ui into our `community` branch (not `develop`!)

Inside the description it's a good way to mention the related issues with "closes #${issue number}" - this will automatically link the issue and the pull request inside the WebUI.<br>
It also will close the linked issue automatically when the pull request is merged!

An example pull request description:
```
This PR
- closes #868
```

### Examples for new contributors
- With [feature issue #633](https://github.com/mercedes-benz/sechub/issues/633) all commits are in a dedicated ordering and contain detailed information about the different steps which were necessary.
- Please look also into the PR description at https://github.com/mercedes-benz/sechub/pull/1029

## Quality
We assume that for every non-trivial contribution, the project has been built and tested prior to the contribution.

### Documentation
Please ensure that for all contributions, the corresponding documentation is in-sync and up-to-date. All documentation is in English language.

Please see [here](https://mercedes-benz.github.io/sechub/latest/sechub-techdoc.html#section-documentation) for details about documentation.


### Commit messages
GitHub has the possibility to link commits automatically inside issues when mentioned with #${issue number} inside the commit headline.<br>
To make things easier to follow/maintain, please always provide commit message in following style:

```
Dedicated short message what was done #${issue number}
```
or
```
Dedicated short message what was done #${issue number}

- detail1
- detail2
- ...
```
NOTE: Here you MUST add an empty second line between headline and details. It's common practice for GIT.

Example:
```
Described commit message format #868

- added examples
- explained purpose
```

### Development tools setup
Of course it depends on you which kind of IDE you use for development. But we have some defaults defined - e.g. for git configuration which should be used.

Please check our [development-setup documentation](https://mercedes-benz.github.io/sechub/latest/sechub-techdoc.html#development-setup) for details.

Please look into https://github.com/mercedes-benz/sechub/issues/168 for source formatter config files and description for usage.

### Coding conventions

Please look at our [coding-conventions documentation](https://mercedes-benz.github.io/sechub/latest/sechub-techdoc.html#section-coding-conventions) for details.
