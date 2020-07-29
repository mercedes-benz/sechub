<!-- SPDX-License-Identifier: MIT --->

# About this project

sechub-doc contains all documentation about the sechub:

- user handbook
- architecture
- techdoc
- operations

All is done with `asciidoc` except those little tech info files as this one, which are done in `markdown`
because GitHub was not able to render asciidoc files 100% when the project started (e.g. includes). 
GitHub has strongly improved the asciidoc support, but we keep the convention.

## Documentation content
More information can be found inside the documents.
Please refer to

- /sechub-doc/src/docs/asciidoc/sechub-architecture.adoc
- /sechub-doc/src/docs/asciidoc/sechub-operations.adoc
- /sechub-doc/src/docs/asciidoc/sechub-techdoc.adoc
- /sechub-doc/src/docs/asciidoc/sechub-user.adoc
- /sechub-doc/src/docs/asciidoc/sechub-product-delegation-server.adoc

Some document parts are complete generated , e.g.

- messaging diagrams (generated plantuml files)
- system property descriptions
- schedule descriptions
- use case documentation

More details about generation can be found inside `sechub-techdoc` document. There is a special documentation chapter inside.

## Create documentation output files and generated content
When you want to create the documentation files and also the generated content, just enter at repository root folder:

- `./gradlew documentation` _(if you have already called `./gradlew integrationtest` before at least one time)_ otherwise call
- `./gradlew integrationtest documentation` _(this generate some runtime information used inside documentation)_

After the build you find `HTML` and also `PDF` output files at `sechub-doc/build/asciidoc/`

On build server you will find the documents also for each build so a local generation is normally not necessary -
and because its asciidoc the files can read directly as well.


## Tool support for documentation
For major IDEs you can use dedicated plugins for showing/editing `asciidoc`.

**Eclipse:**
- [https://marketplace.eclipse.org/content/asciidoctor-editor](https://marketplace.eclipse.org/content/asciidoctor-editor)
- [https://github.com/de-jcup/eclipse-asciidoctor-editor](https://github.com/de-jcup/eclipse-asciidoctor-editor)

**IntelliJ:** 
- [https://intellij-asciidoc-plugin.ahus1.de](https://intellij-asciidoc-plugin.ahus1.de)
- [https://github.com/asciidoctor/asciidoctor-intellij-plugin](https://github.com/asciidoctor/asciidoctor-intellij-plugin)

**VSCode / VSCodium**
- [https://github.com/asciidoctor/asciidoctor-vscode](https://github.com/asciidoctor/asciidoctor-vscode)

