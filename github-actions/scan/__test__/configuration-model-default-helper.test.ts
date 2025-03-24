// SPDX-License-Identifier: MIT

import { addDefaultExcludesToSecHubConfig, DEFAULT_EXCLUDES } from "../src/configuration-model-default-helper";


describe('addDefaultExcludes', function () {
    it('data section entries updated correctly', function () {
        /* prepare */
        const sampleJson = {
            "apiVersion": "1.0",
            "data": {
              "sources": [
                {
                  "excludes": [ 
                    "**/mytestcode/**",
                    "*.config"
                  ]
                },
                {
                   "name": "gamechanger-sources"
                }
              ],
              "binaries": [
                {
                  "excludes": [ 
                    "**/test/**"
                  ]
                },
                {
                  "name": "gamechanger-binaries"
                }
              ]
            }
          };

        /* execute */
        const updatedJson = addDefaultExcludesToSecHubConfig(sampleJson);

        /* test */
        updatedJson.data.sources.forEach((entry: { excludes?: string[]; }) => {
            DEFAULT_EXCLUDES.forEach((exclude) => {
                expect(entry.excludes).toContain(exclude);
            });
        });
        updatedJson.data.binaries.forEach((entry: { excludes?: string[]; }) => {
            DEFAULT_EXCLUDES.forEach((exclude) => {
                expect(entry.excludes).toContain(exclude);
            });
        });
    });

    it('deprecated way with existing excludes entries updated correctly', function () {
        /* prepare */
        const sampleJson = {
            "apiVersion": "1.0",
            "codeScan": {
              "fileSystem": {
                "folders": [
                  "gamechanger-android/src/main/java",
                  "gamechanger-server/src/main/java"
                ]
              },
              "excludes": [
                "**/mytestcode/**",
                "*.config"
              ]
            }
          };

        /* execute */
        const updatedJson = addDefaultExcludesToSecHubConfig(sampleJson);

        /* test */
        DEFAULT_EXCLUDES.forEach((exclude) => {
            expect(updatedJson.codeScan.excludes).toContain(exclude);
        });
    });

    it('deprecated way no excludes entries updated correctly', function () {
        /* prepare */
        const sampleJson = {
            "apiVersion": "1.0",
            "codeScan": {
              "fileSystem": {
                "folders": [
                  "gamechanger-android/src/main/java",
                  "gamechanger-server/src/main/java"
                ]
              }
            }
          };

        /* execute */
        const updatedJson = addDefaultExcludesToSecHubConfig(sampleJson);

        /* test */
        DEFAULT_EXCLUDES.forEach((exclude) => {
            expect(updatedJson.codeScan.excludes).toContain(exclude);
        });
    });

    it('invalid config no entries updated', function () {
        /* prepare */
        const sampleJson = {
            "apiVersion": "1.0",
            "licenseScan": {
              "fileSystem": {
                "folders": [
                  "gamechanger-android/src/main/java",
                  "gamechanger-server/src/main/java"
                ]
              }
            }
          };

        /* execute */
        const updatedJson = addDefaultExcludesToSecHubConfig(sampleJson);

        /* test */
        expect(updatedJson).toEqual(sampleJson);
    });

});