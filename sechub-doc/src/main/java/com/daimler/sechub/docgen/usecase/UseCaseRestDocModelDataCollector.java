// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.usecase;

import static com.daimler.sechub.docgen.GeneratorConstants.*;
import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import com.daimler.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;
import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;

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
					"docgen corrupt - did not found sechub-doc folder, cannot determine root folder, i am at :"+new File(".").getAbsolutePath());
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
			restDocEntry.variantId = RestDocPathFactory.createVariantId(restDocEntry.variantOriginValue);
			
			restDocEntry.usecaseEntry = useCaseEntry;
			String path = RestDocPathFactory.createPath(useCaseClass, restDocEntry.variantId);
			
			restDocEntry.identifier=RestDocPathFactory.createIdentifier(useCaseClass);
			restDocEntry.path = path;
			
			File projectRestDocGenFolder = scanForSpringRestDocGenFolder(restDocEntry);
			restDocEntry.copiedRestDocFolder = copyToDocumentationProject(projectRestDocGenFolder, path);
			restDocEntry.wanted=restDoc.wanted();
			
			model.add(restDocEntry);

			/* connect with usecase */
			useCaseEntry.addLinkToRestDoc(restDocEntry);

		}
		return model;
	}
	
	private File copyToDocumentationProject(File projectRestDocGenFolder, String id) {
		File targetFolder = new File(sechHubDoc, "src/docs/asciidoc/"+DOCUMENTS_GEN + id);
		try {
			if (targetFolder.exists() && !FileSystemUtils.deleteRecursively(targetFolder)) {
					throw new IOException("target folder exists but not deletable!");
			}
			FileSystemUtils.copyRecursively(projectRestDocGenFolder, targetFolder);
			return targetFolder;
		} catch (IOException e) {
			throw new IllegalStateException(
					"copy restdoc parts not possible from:\n" + projectRestDocGenFolder + "\nto\n" + targetFolder, e);
		}
	}

	private File scanForSpringRestDocGenFolder(UseCaseRestDocEntry entry) {
		File lastTry=null;
		for (File buildDir : buildDirectories) {
			File expected = new File(buildDir, "generated-snippets/" + entry.path);
			if (expected.exists()) {
				return expected;
			}
			lastTry=expected;
		}
		throw new IllegalStateException("No restdoc found for Usecase:"+entry.usecaseEntry.getAnnotationName()+"\nIt is annotated as @UseCaseRestDoc, but no restdoc files generated!\n"
				+ "Maybe you \n   -forgot to do the documentation parts for the test, or\n   - you did you used accidently another class when calling UseCaseRestDoc.Factory.createPath(...) ?\n\nDetails:\nNo rest doc gen folder not found for id:" + entry.path+",\nlastTry:"+ ( lastTry!=null?lastTry.getAbsolutePath():"null"));
	}

}