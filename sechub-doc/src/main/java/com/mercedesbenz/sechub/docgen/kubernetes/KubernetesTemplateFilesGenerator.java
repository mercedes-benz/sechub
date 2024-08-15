// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.kubernetes;

import static com.mercedesbenz.sechub.docgen.util.DocGeneratorUtil.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mercedesbenz.sechub.docgen.DocAnnotationData;
import com.mercedesbenz.sechub.docgen.Generator;
import com.mercedesbenz.sechub.docgen.spring.SpringScheduleExtractor;
import com.mercedesbenz.sechub.docgen.spring.SpringScheduleExtractor.SpringSchedule;
import com.mercedesbenz.sechub.docgen.spring.SpringValueExtractor;
import com.mercedesbenz.sechub.docgen.spring.SpringValueExtractor.SpringValue;
import com.mercedesbenz.sechub.docgen.util.ClasspathDataCollector;
import com.mercedesbenz.sechub.docgen.util.DocGenTextFileWriter;

class KubernetesTemplateFilesGenerator implements Generator {

    private static final String OPTION_SECRET_FILE_NAME = "secretFileName";

    private static final String SECHUB_KUBERNETES_GENO_TARGET_ROOT = "SECHUB_KUBERNETES_GENO_TARGET_ROOT";
    // https://kubernetes.io/docs/concepts/configuration/secret/#using-secrets-as-environment-variables

    SpringValueExtractor springValueExtractor;
    SpringScheduleExtractor springScheduledExtractor;
    DocGenTextFileWriter writer;

    public KubernetesTemplateFilesGenerator() {
        this.springValueExtractor = new SpringValueExtractor();
        this.springScheduledExtractor = new SpringScheduleExtractor();
        this.writer = new DocGenTextFileWriter();
    }

    public static void main(String[] args) throws Exception {
        KubernetesFiles files = new KubernetesFiles();
        new KubernetesTemplateFilesGenerator().generate(files, new ClasspathDataCollector().fetchMustBeDocumentParts());
    }

    public void generate(KubernetesFiles result, List<DocAnnotationData> list) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        /* additional parts: */
        /* database: */
        list.add(newSecret("spring.datasource.username", "database", "Define userid for database access"));
        list.add(newSecret("spring.datasource.password", "database", "The password for database access"));
        list.add(newSecret("spring.datasource.patroni.password", "database", "The password for patroni sync etc."));

        /* database-backup: */
        list.add(newSecret("spring.datasource.backup.username", "database-backup", "The user for database backup access"));
        list.add(newSecret("spring.datasource.backup.password", "database-backup", "The password for database backup access"));

        /* ssl: */
        list.add(newSecret("sechub.server.ssl.keystore.password", "ssl", "The password for server ssl certificate"));
        list.add(newSecret("sechub.server.ssl.keystore.file", "ssl", "The server ssl certificate file", "server-certificate.p12"));

        /*
         * configuration (normally unnecessary because automatical generated, but we
         * want .json as file ending, so here necessary
         */
        list.add(newSecret("sechub.scan.config.initial", "configuration", "The initial scan configuration", "sechub_scan_config_initial.json"));

        Collections.sort(list);
        generateDeploymentFilePart(result, list);
        /* now secrets etc. are well known */

        generateSecretShellScriptsAndMissingSecretFiles(result);
        /* last but not least create output file */
        File generatedDeploymentFilePart = new File(ensureKubernetesGenFolder(), "sechub-server-environment-deployment-parts.gen.yaml");
        writer.writeTextToFile(generatedDeploymentFilePart, result.serverDeploymentYaml);
        output("Written generated deployment file:" + generatedDeploymentFilePart);

    }

    /**
     * Creates annotation data so generator will create deployment code and secret
     * files + shell scripts
     *
     * @param key         - key to use for a) ENV entry (uppercased + . replaced by
     *                    _) b) secret files containing data (same as env entr but
     *                    lowercased +.txt)
     * @param scope       - used for shellscript filenames
     * @param description
     * @return data
     */
    private DocAnnotationData newSecret(String key, String scope, String description) {
        return newSecret(key, scope, description, null);
    }

    /**
     * Creates annotation data so generator will create deployment code and secret
     * files + shell scripts
     *
     * @param key            - key to use for a) ENV entry (uppercased + . replaced
     *                       by _) b) secret files containing data (same as env entr
     *                       but lowercased +.txt)
     * @param scope          - used for shellscript filenames
     * @param description
     * @param secretFileName
     * @return data
     */
    private DocAnnotationData newSecret(String key, String scope, String description, String secretFileName) {
        return newAnnotationData(key, scope, description, true, secretFileName);
    }

    private DocAnnotationData newAnnotationData(String springValue, String scope, String description, boolean secret, String secretFileName) {
        DocAnnotationData data = new DocAnnotationData();
        data.isSecret = secret;
        data.description = description;
        data.scope = scope;
        data.springValue = springValue;
        data.options.put(OPTION_SECRET_FILE_NAME, secretFileName);
        return data;
    }

    private void generateDeploymentFilePart(KubernetesFiles result, List<DocAnnotationData> list) {
        for (DocAnnotationData data : list) {
            generateDeploymentCode(result, data);
        }
        String generatorName = getClass().getSimpleName();

        StringBuilder all = new StringBuilder();
        newLine(all, "# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ #");
        newLine(all, "# + Next kubernetes source is generated by: " + generatorName);
        newLine(all, "# + when your code changes and you have new keys etc. please generate again and replace block  ");
        newLine(all, "# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ #");
        for (StringBuilder code : result.deploymentMap.values()) {
            all.append(code);
        }
        newLine(all, "# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ #");
        newLine(all, "# + End of generated source from: " + getClass().getSimpleName());
        newLine(all, "# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ #");

        result.serverDeploymentYaml = all.toString();
    }

    private void generateDeploymentCode(KubernetesFiles result, DocAnnotationData data) {
        StringBuilder code = result.getDeployment(data);

        String inspect = findKey(data);
        if (inspect == null || inspect.isEmpty()) {
            return;
        }
        String springENV = inspect.replace('.', '_').toUpperCase();
        String description = data.description;
        if (description == null || description.isEmpty()) {
            description = "No description available";
        }
        String[] lines = description.split("\n");
        for (String line : lines) {
            newLine(code, "        # " + line);
        }
        newLine(code, "        - name: " + springENV);
        if (data.isSecret) {
            String secretName = createSecretName(data);
            String secretKey = springENV.toLowerCase();
            result.getSecretKeys(secretName).add(secretKey);

            newLine(code, "          valueFrom:");
            newLine(code, "            secretKeyRef:");
            newLine(code, "               name: " + secretName);
            newLine(code, "               key: " + secretKey);

            /* handle custom secret file names */
            result.secretFileNameMapping.put(secretKey, data.options.get(OPTION_SECRET_FILE_NAME));
        } else {
            String springValue = data.springValue;
            if (springValue != null && !springValue.isEmpty()) {
                SpringValue extract = springValueExtractor.extract(springValue);
                newLine(code, "          value: \"" + extract.getDefaultValue() + "\"");
            } else if (data.springScheduled != null) {
                newLine(code, "          value: \"" + springScheduledExtractor.extract(data.springScheduled).getScheduleDefaultValue() + "\"");
            }
        }

    }

    private void generateSecretShellScriptsAndMissingSecretFiles(KubernetesFiles result) throws IOException {
        createSecretShellScriptAndFiles(result, ensureSecretFolder());
    }

    private void createSecretShellScriptAndFiles(KubernetesFiles result, File targetFolder) throws IOException {

        List<String> allShellScriptNames = new ArrayList<>();
        for (String secretName : result.getSecretNames()) {
            StringBuilder sb = new StringBuilder();
            newLine(sb, "#!/bin/bash");
            newLine(sb, "# ------------------------------------------------------------------  #");
            newLine(sb, "# Generated shellscript for creating/updating secret");
            newLine(sb, "# " + secretName + " in namespace :' {{ .NAMESPACE_NAME }} '");
            newLine(sb, "#");
            newLine(sb, "# Generator: " + getClass().getSimpleName());
            newLine(sb, "# ------------------------------------------------------------------  #");
            // @formatter:off
			// https://stackoverflow.com/questions/45879498/how-can-i-update-a-secret-on-kuberenetes-when-it-is-generated-from-a-file/45881259
			// https://stackoverflow.com/a/45881259/2590615
			// kubectl create secret generic production-tls --from-file=./tls.key --from-file=./tls.crt --dry-run -o yaml | kubectl apply -f -
			// @formatter:on
            sb.append("kubectl create secret generic ");
            sb.append(secretName);
            sb.append(" --namespace={{ .NAMESPACE_NAME }}");
            for (String key : result.getSecretKeys(secretName)) {
                // https://medium.com/platformer-blog/using-kubernetes-secrets-5e7530e0378a
                String fileName = result.getFileNameForSecretKey(key);
                sb.append(" --from-file=").append(key).append("=./" + fileName);
                ensureSecretFile(targetFolder, fileName);
            }
            sb.append(" --dry-run -o yaml | kubectl apply -f -");
            String shellName = createShellScriptName(secretName);
            allShellScriptNames.add(shellName);
            File targetFile = new File(targetFolder, shellName);
            writer.writeTextToFile(targetFile, sb.toString(), true);
            output("Updated/Created:" + targetFile);

        }

        /* create a "one fetch all" shell script: */
        StringBuilder sb = new StringBuilder();
        // add robot parts. deploy-secrets-secret-sechub-robot.sh is manual created and
        // exists
        // only sechub-robot-secret.yaml must copied to templates folder manual and is
        // not inside GIT
        allShellScriptNames.add(0, "deploy-secrets-secret-sechub-robot.sh");

        newLine(sb, "#!/bin/bash");
        newLine(sb, "# ------------------------------------------------------------------  #");
        newLine(sb, "# Generated shellscript for creating/updating all secrets");
        newLine(sb, "#");
        newLine(sb, "# Generator: " + getClass().getSimpleName());
        newLine(sb, "# ------------------------------------------------------------------  #");
        newLine(sb, "");
        sb.append("echo \"+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\"\n");
        sb.append("echo \"+++ Updating all secrets for namespace :' {{ .NAMESPACE_NAME }} '\"\n");
        sb.append("echo \"+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\"\n");
        newLine(sb, "# SecHub robot secret");
        newLine(sb, "# ===================");
        newLine(sb, "# This file is never generated and must be downloaded from quay");
        newLine(sb, "# and put into this templates folder");
        newLine(sb, "# ");
        newLine(sb, "echo \"Upload creddentials for sechub robot by yaml file from server\"");
        newLine(sb, "echo \"(if not existing: download from Registry and copy to template folder)\"");
        newLine(sb, "# ");
        newLine(sb, "");
        newLine(sb, "# Generated secrets");
        newLine(sb, "# ===================");
        newLine(sb, "# These files are generated and must be filled if not existed before");
        for (String shellScriptName : allShellScriptNames) {
            sb.append("echo \"Executing:").append(shellScriptName).append("\"\n");
            sb.append("./").append(shellScriptName).append("\n");
        }
        File targetFile = new File(targetFolder, createShellScriptName("all"));
        writer.writeTextToFile(targetFile, sb.toString(), true);
        output("Updated/Created:" + targetFile);
    }

    private String createShellScriptName(String secretName) {
        return "deploy-secrets-" + secretName + ".sh";
    }

    private String findKey(DocAnnotationData data) {
        if (data.springValue != null) {
            SpringValue extracted = springValueExtractor.extract(data.springValue);
            if (extracted != null) {
                return extracted.getKey();
            }
        }
        if (data.springScheduled == null) {
            return null;
        }
        SpringSchedule extracted = springScheduledExtractor.extract(data.springScheduled);
        return extracted.getScheduleKey();
    }

    private String createSecretName(DocAnnotationData data) {
        return "secret-" + data.scope;
    }

    private void ensureSecretFile(File targetFolder, String fileName) throws IOException {
        File secretFile = new File(targetFolder, fileName);
        if (!secretFile.exists()) {
            if (!secretFile.createNewFile()) {
                throw new IOException("Was not able to create secret file:" + secretFile);
            } else {
                output("created empty secret file:" + secretFile);
            }
        } else {
            output("found existing secret file:" + secretFile);
        }
    }

    private File ensureSecretFolder() {
        return assertExists(secretsFolder());
    }

    private File secretsFolder() {
        return new File(ensureKubernetesFolder(), "kubegen/env-setup/templates/secrets/");
    }

    private File ensureKubernetesGenFolder() {
        File genFolder = new File(ensureKubernetesFolder(), "build/gen/copy-templates/");
        genFolder.mkdirs();
        return assertExists(genFolder);
    }

    private File ensureKubernetesFolder() {
        String targetKubernetesFolder = System.getenv(SECHUB_KUBERNETES_GENO_TARGET_ROOT);
        if (targetKubernetesFolder == null || targetKubernetesFolder.isEmpty()) {
            throw new IllegalStateException(SECHUB_KUBERNETES_GENO_TARGET_ROOT + " must be set");
        }
        return assertExists(new File(targetKubernetesFolder));
    }

    private File assertExists(File folder) {
        if (!folder.exists()) {
            throw new IllegalStateException("folder does not exist:" + folder.getAbsolutePath());
        }
        return folder;
    }

    private void output(String string) {
        /* NOSONAR */System.out.println(string);
    }

}
