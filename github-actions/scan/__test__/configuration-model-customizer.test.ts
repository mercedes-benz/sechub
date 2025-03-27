// SPDX-License-Identifier: MIT

import { addAdditonalExcludes } from "../src/configuration-model-customizer";


describe('addAdditonalExcludes', function () {
    const EXPECTED_ADDITIONAL_EXCLUDES: string[] = ['**/.sechub-gha/**'];

    it('data section entries updated correctly', function () {
        /* prepare */
        const ORIGINAL_SOURCE_EXCLUDES = ["**/mytestcode/**", "*.config"];
        const ORIGINAL_BINARY_EXCLUDES = ["**/test/**"];
        let sampleJson = {
            "apiVersion": "1.0",
            "data": {
              "sources": [
                {
                  "excludes": [ 
                    "**/mytestcode/**",
                    "*.config"
                  ]
                }
              ],
              "binaries": [
                {
                  "excludes": [ 
                    "**/test/**"
                  ]
                }
              ]
            }
          };

        /* execute */
        addAdditonalExcludes(sampleJson);

        /* test */
        sampleJson.data.sources.forEach((entry: { excludes?: string[]; }) => {
          EXPECTED_ADDITIONAL_EXCLUDES.forEach((exclude) => {
                expect(entry.excludes).toContain(exclude);
            });
          ORIGINAL_SOURCE_EXCLUDES.forEach((exclude) => {
              expect(entry.excludes).toContain(exclude);
          });

        });
        sampleJson.data.binaries.forEach((entry: { excludes?: string[]; }) => {
          EXPECTED_ADDITIONAL_EXCLUDES.forEach((exclude) => {
                expect(entry.excludes).toContain(exclude);
          });
          ORIGINAL_BINARY_EXCLUDES.forEach((exclude) => {
              expect(entry.excludes).toContain(exclude);
          });
        });
    });

    it('data section excludes created and updated correctly', function () {
      /* prepare */
      let sampleJson = {
          "apiVersion": "1.0",
          "data": {
            "sources": [
              {
                 "name": "gamechanger-sources"
              }
            ],
            "binaries": [
              {
                "name": "gamechanger-binaries"
              }
            ]
          }
        } as {
          apiVersion: string;
          data: {
            sources: [
              {
                 name: string;
                 excludes?: string[]; // Add the optional excludes property
              }
            ];
            binaries: [
              {
                name: string;
                excludes?: string[]; // Add the optional excludes property
              }
            ];
          };
        };

      /* execute */
      addAdditonalExcludes(sampleJson);

      /* test */
      sampleJson.data.sources.forEach((entry: { excludes?: string[]; }) => {
        EXPECTED_ADDITIONAL_EXCLUDES.forEach((exclude) => {
              expect(entry.excludes).toContain(exclude);
          });

      });
      sampleJson.data.binaries.forEach((entry: { excludes?: string[]; }) => {
        EXPECTED_ADDITIONAL_EXCLUDES.forEach((exclude) => {
              expect(entry.excludes).toContain(exclude);
          });
      });
  });

    it('deprecated way with existing excludes entries updated correctly', function () {
        /* prepare */
        let sampleJson = {
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
        addAdditonalExcludes(sampleJson);

        /* test */
        EXPECTED_ADDITIONAL_EXCLUDES.forEach((exclude) => {
            expect(sampleJson.codeScan.excludes).toContain(exclude);
        });
    });

    it('deprecated way no excludes entries updated correctly', function () {
        /* prepare */
        let sampleJson = {
            "apiVersion": "1.0",
            "codeScan": {
              "fileSystem": {
                "folders": [
                  "gamechanger-android/src/main/java",
                  "gamechanger-server/src/main/java"
                ]
              }
            }
          } as {
            apiVersion: string;
            codeScan: {
                fileSystem: {
                    folders: string[];
                };
                excludes?: string[]; // Add the optional excludes property
            };
        };

        /* execute */
        addAdditonalExcludes(sampleJson);

        /* test */
        EXPECTED_ADDITIONAL_EXCLUDES.forEach((exclude) => {
            expect(sampleJson.codeScan.excludes).toContain(exclude);
        });
    });

    it('invalid config no entries updated', function () {
        /* prepare */
        let sampleJson = {
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

        let expectedJson = JSON.parse(JSON.stringify(sampleJson));;

        /* execute */
        addAdditonalExcludes(sampleJson);

        /* test */
        expect(expectedJson).toEqual(sampleJson);
    });


    it('combination of deprecated way and data section is updated correctly', function () {
      /* prepare */
      let sampleJson = {
          "apiVersion": "1.0",
          "codeScan": {
            "fileSystem": {
              "folders": [
                "gamechanger-android/src/main/java",
                "gamechanger-server/src/main/java"
              ],
            },
            "excludes": [
              "**/mytestcode/**",
              "*.config"
            ]
          },
          "data": {
            "sources": [
              {
                "excludes": [ 
                  "**/mytestcode/**",
                  "*.config"
                ]
              }
            ],
            "binaries": [
              {
                "excludes": [ 
                    "**/test/**"
                ]
              }
            ]
          }
        }

      /* execute */
      addAdditonalExcludes(sampleJson);

      /* test */
      EXPECTED_ADDITIONAL_EXCLUDES.forEach((exclude) => {
          expect(sampleJson.codeScan.excludes).toContain(exclude);
      });

      sampleJson.data.sources.forEach((entry: { excludes?: string[]; }) => {
        EXPECTED_ADDITIONAL_EXCLUDES.forEach((exclude) => {
              expect(entry.excludes).toContain(exclude);
          });
      });
      sampleJson.data.binaries.forEach((entry: { excludes?: string[]; }) => {
        EXPECTED_ADDITIONAL_EXCLUDES.forEach((exclude) => {
              expect(entry.excludes).toContain(exclude);
          });
      });
  });

});