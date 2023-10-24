// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

import static com.mercedesbenz.sechub.docgen.GeneratorConstants.*;
import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import com.mercedesbenz.sechub.docgen.reflections.Reflections;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;

/**
 * Collector - inspired by
 * https://github.com/de-jcup/code2doc/blob/master/code2doc-core/src/main/java/de/jcup/code2doc/core/internal/collect/TechInfoLinkAnnotationDataCollector.java
 *
 * @author Albert Tregnaghi
 */
public class UseCaseRestDocModelDataCollector {

    public static final String DOCUMENTS_GEN = "documents/gen/";

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseRestDocModelDataCollector.class);

    private Reflections reflections;

    List<File> buildDirectories = new ArrayList<>();

    private File sechHubDoc;

    public UseCaseRestDocModelDataCollector(Reflections reflections) {
        notNull(reflections, "reflections must not be null!");
        this.reflections = reflections;

        fetchGradleBuildDirectories();

    }

    private void fetchGradleBuildDirectories() {
        File file = new File("sechub-doc");
        File rootFolder = null;
        if (file.exists()) {
            rootFolder = file.getParentFile();
        } else {
            rootFolder = new File("./..");
        }
        sechHubDoc = new File(rootFolder, "sechub-doc");
        if (!sechHubDoc.exists()) {
            throw new IllegalStateException(
                    "docgen corrupt - did not found sechub-doc folder, cannot determine root folder, i am at :" + new File(".").getAbsolutePath());
        }
        File[] subDirs = rootFolder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (File subDir : subDirs) {
            File[] foundBuildDirs = subDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    return file.isDirectory() && file.getName().equals("build");
                }
            });
            buildDirectories.addAll(Arrays.asList(foundBuildDirs));
        }
    }

    public UseCaseRestDocModel collect(UseCaseModel useCaseModel) {
        if (DEBUG) {
            LOG.info("start collecting");
        }
        UseCaseRestDocModel model = new UseCaseRestDocModel(useCaseModel);
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(UseCaseRestDoc.class);
        if (annotatedMethods.size() == 0) {
            throw new IllegalStateException("Criticial:Cannot generate REST documentation!\n"
                    + "Reflections did not find any method annotated with @UseCaseRestDoc!\n"
                    + "- If you started the collector from your IDE, please do not start directly, but instead start `AsciidocGeneratorManualTest.java`.\n"
                    + "  Doing this, the classpath will contain automatically all necessary resources.\n"
                    + "- If you are inside a gradle build this is a criticial problem, because the documentation would not be generated correctly! ");
        }
        if (DEBUG) {
            LOG.info("> will collect for:{} - {}", annotatedMethods.size(), annotatedMethods);
        }

        for (Method method : annotatedMethods) {
            UseCaseRestDoc[] annos = method.getAnnotationsByType(UseCaseRestDoc.class);
            if (annos.length == 0) {
                continue;
            }
            if (annos.length > 1) {
                throw new IllegalStateException("UseCaseRestDoc annotation may only added one time to test method!");
            }
            UseCaseRestDoc restDoc = annos[0];
            Class<? extends Annotation> useCaseClass = restDoc.useCase();
            if (DEBUG) {
                LOG.info("inspect method:{}\n - usecase found:{}", method, useCaseClass);
            }
            UseCaseEntry useCaseEntry = useCaseModel.ensureUseCase(useCaseClass);

            /* create and prepare rest doc entry */
            UseCaseRestDocEntry restDocEntry = new UseCaseRestDocEntry();
            restDocEntry.variantOriginValue = restDoc.variant();
            restDocEntry.variantId = RestDocFactory.createVariantId(restDocEntry.variantOriginValue);
            restDocEntry.restDocTestMethod = method;

            restDocEntry.usecaseEntry = useCaseEntry;
            String path = RestDocFactory.createPath(useCaseClass, restDocEntry.variantId);

            restDocEntry.identifier = RestDocFactory.createIdentifier(useCaseClass);
            restDocEntry.path = path;

            File projectRestDocGenFolder = scanForSpringRestDocGenFolder(restDocEntry);
            restDocEntry.copiedRestDocFolder = copyToDocumentationProject(projectRestDocGenFolder, path);
            restDocEntry.wanted = restDoc.wanted();

            model.add(restDocEntry);

            /* connect with usecase */
            useCaseEntry.addLinkToRestDoc(restDocEntry);

        }
        return model;
    }

    private File copyToDocumentationProject(File projectRestDocGenFolder, String id) {
        File targetFolder = new File(sechHubDoc, "src/docs/asciidoc/" + DOCUMENTS_GEN + id);
        try {
            if (targetFolder.exists() && !FileSystemUtils.deleteRecursively(targetFolder)) {
                throw new IOException("target folder exists but not deletable!");
            }
            FileSystemUtils.copyRecursively(projectRestDocGenFolder, targetFolder);
            return targetFolder;
        } catch (IOException e) {
            throw new IllegalStateException("copy restdoc parts not possible from:\n" + projectRestDocGenFolder + "\nto\n" + targetFolder, e);
        }
    }

    private File scanForSpringRestDocGenFolder(UseCaseRestDocEntry entry) {
        StringBuilder searchedBuildDirsSb = new StringBuilder();
        for (File buildDir : buildDirectories) {
            File expected = new File(buildDir, "generated-snippets/" + entry.path);
            if (expected.exists()) {
                return expected;
            }
            try {
                String canonicalPath = expected.getCanonicalPath();
                if (canonicalPath.contains("sechub-doc")) {
                    String topCandidate = "Top candidate: " + canonicalPath;
                    topCandidate = topCandidate + "\n" + ("-".repeat(topCandidate.length())) + "\n\n";

                    searchedBuildDirsSb.insert(0, topCandidate);
                } else {
                    searchedBuildDirsSb.append(canonicalPath).append("\n");
                }
            } catch (IOException e) {
                LOG.error("Cannot fetch canonical path for {}", expected);
            }
        }
        String annotationName = atLeastEmptyString(entry.usecaseEntry.getAnnotationName());
        String underlineAnnotationName = "-".repeat(annotationName.length());
        String variantName = atLeastEmptyString(entry.variantOriginValue);

        String message = """
                 Missing RESTDOC snippets

                 RESTDOC problem detected for usecase: %s
                                                       %s
                                              variant: %s

                                         RestDOC test: %s
                                               method: %s

                 There is a RESTDOC test which is annotated as @UseCaseRestDoc, but no/not enough RESTDOC snippet files were generated!

                 Maybe you
                    - forgot to implement the RESTDOC test for the usecase or for one of its variants
                    - used two differet names for the variant inside your test (annotation + code in test method)
                    - forgot to add the documentation calls inside a RESTDOC test, or
                    - you accidently used another class when calling UseCaseRestDoc.Factory.createPath(...) or
                    - executed not `gradlew sechub-doc:test` before

                Details:
                No rest doc gen folder found for id: %s

                Searched at following locations:
                %s
                """.formatted(annotationName, underlineAnnotationName, variantName, entry.restDocTestMethod.getDeclaringClass().getSimpleName(),
                entry.restDocTestMethod.getName(), entry.path, searchedBuildDirsSb.toString());

        throw new IllegalStateException(message);
    }

    private String atLeastEmptyString(String annotationName) {
        if (annotationName != null) {
            return annotationName;
        }
        return "";
    }

}