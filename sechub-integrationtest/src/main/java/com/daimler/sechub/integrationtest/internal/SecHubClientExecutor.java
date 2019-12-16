// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.integrationtest.api.TestUser;
import com.daimler.sechub.test.TestUtil;

public class SecHubClientExecutor {

	public enum ClientAction {

		START_ASYNC("scanAsync"), START_SYNC("scan"), GET_REPORT("getReport"), GET_STATUS("getStatus"),

		;
		private String command;

		private ClientAction(String command) {
			this.command = command;
		}

		public String getCommand() {
			return command;
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(SecHubClientExecutor.class);

	public class ExecutionResult {
		private int exitCode;
		private String lastOutputLine;
		private File outputFolder;
		public UUID sechubJobUUD;
		public String[] lines;

		public int getExitCode() {
			return exitCode;
		}

		public File getOutputFolder() {
			return outputFolder;
		}

		public String getLastOutputLine() {
			if (lastOutputLine == null) {
				lastOutputLine = lines[lines.length - 1];
			}
			return lastOutputLine;
		}

		public UUID getSechubJobUUD() {
			if (sechubJobUUD == null) {
				for (String line : lines) {
					if (sechubJobUUD != null) {
						break;
					}
					int index = line.indexOf("job ");
					if (index != -1) {
						try {
							String remaining = line.substring(index + 4).split(" ")[0];
							sechubJobUUD = UUID.fromString(remaining);
							break;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return sechubJobUUD;
		}
	}

	public ExecutionResult execute(File file, TestUser user, ClientAction action, Map<String,String> environmentVariables, String... options) {
		String path = "sechub-cli/build/go/platform/";
		List<String> commandsAsList = new ArrayList<>();
		String sechubExeName = null;
		if (TestUtil.isWindows()) {
			sechubExeName = "sechub.exe";
			path += "windows-386";
			commandsAsList.add("cmd.exe");
			commandsAsList.add("/C");
			commandsAsList.add(sechubExeName);
		} else {
			sechubExeName = "sechub";
			commandsAsList.add("./" + sechubExeName);
			path += "linux-386";
		}
		File pathToExecutable = new File(IntegrationTestFileSupport.getTestfileSupport().getRootFolder(), path);
		File executable = new File(pathToExecutable, sechubExeName);
		if (!executable.exists()) {
			throw new SecHubClientNotFoundException(executable);
		}

		if (file != null) {
			commandsAsList.add("-configfile");
			if (TestUtil.isWindows()) {
				commandsAsList.add("\"" + file.getAbsolutePath() + "\"");
			} else {
				commandsAsList.add(file.getAbsolutePath());
			}
		}
		if (user != null) {
			commandsAsList.add("-user");
			commandsAsList.add(user.getUserId());

			commandsAsList.add("-apitoken");
			commandsAsList.add(user.getApiToken());
		}

		commandsAsList.addAll(Arrays.asList(options));

		if (action != null) {
			commandsAsList.add(action.getCommand());
		}

		try {
			/* create temp file for output */
			File tmpGoOutputFile = File.createTempFile("sechub-client-test-", ".txt");
			tmpGoOutputFile.deleteOnExit();
			LOG.info("Temporary go output at:{}", tmpGoOutputFile);

			/* setup process */
			ProcessBuilder pb = new ProcessBuilder(commandsAsList);
			pb.redirectErrorStream(true);
			pb.redirectOutput(tmpGoOutputFile);
			Map<String, String> environment = pb.environment();
			environment.put("SECHUB_TRUSTALL", "true");
			environment.put("SECHUB_DEBUG", "true");
			if (TestUtil.isKeepingTempfiles()) {
				environment.put("SECHUB_KEEP_TEMPFILES", "true");
			}
			if (environmentVariables!=null) {
				environment.putAll(environmentVariables);
			}
			pb.directory(pathToExecutable);

			StringBuilder sb = new StringBuilder();
			Iterator<String> it  = commandsAsList.iterator();
			while (it.hasNext()) {
				sb.append(it.next());
				if (it.hasNext()) {
					sb.append(" ");
				}
			}
			LOG.info("Execute command '{}'", sb.toString());

			/* start process and wait for execution done */
			Process process = pb.start();
			int exitCode = process.waitFor();

			/* show go output */
			String output = IntegrationTestFileSupport.getTestfileSupport().loadTextFile(tmpGoOutputFile, "\n");
			LOG.info("Command output:\n{}", output);

			/* prepare and return result */
			ExecutionResult result = new ExecutionResult();
			result.lines = output.trim().split("\\n");

			result.exitCode = exitCode;

			return result;
		} catch (IOException e) {
			LOG.error("io failure on command execution", e);
			throw new IllegalStateException("Execution failed", e);
		} catch (InterruptedException e) {
			LOG.error("interrupted command execution", e);
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Execution failed", e);
		}
	}

	public static void main(String[] args) {
		new SecHubClientExecutor().execute(null, null, null,null, "-help");
	}

	public class SecHubClientNotFoundException extends IllegalStateException {
		private static final long serialVersionUID = 1L;

		public SecHubClientNotFoundException(File executable) {
			super("SecHub client not available, did you forget to build the client with `gradlew buildGo` ?\nExpected:" + executable);
		}
	}
}
