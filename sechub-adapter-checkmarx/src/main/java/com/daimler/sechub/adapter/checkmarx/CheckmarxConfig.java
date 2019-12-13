// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import java.io.InputStream;

import com.daimler.sechub.adapter.AbstractCodeScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractCodeScanAdapterConfigBuilder;

public class CheckmarxConfig extends AbstractCodeScanAdapterConfig implements CheckmarxAdapterConfig{

	private String teamIdForNewProjects;
	private InputStream sourceCodeZipFileInputStream;

	private CheckmarxConfig() {
	}

	@Override
	public String getTeamIdForNewProjects() {
		return teamIdForNewProjects;
	}

	@Override
	public InputStream getSourceCodeZipFileInputStream() {
		return sourceCodeZipFileInputStream;
	}

	public static CheckmarxConfigBuilder builder() {
		return new CheckmarxConfigBuilder();
	}

	public static class CheckmarxConfigBuilder extends AbstractCodeScanAdapterConfigBuilder<CheckmarxConfigBuilder, CheckmarxConfig>{

		private String teamIdForNewProjects;
		private InputStream sourceCodeZipFileInputStream;

		/**
		 * When we create a new project this is the team ID to use
		 * @param teamId
		 * @return
		 */
		public CheckmarxConfigBuilder setTeamIdForNewProjects(String teamId){
			this.teamIdForNewProjects=teamId;
			return this;
		}
		public CheckmarxConfigBuilder setSourceCodeZipFileInputStream(InputStream sourceCodeZipFileInputStream){
			this.sourceCodeZipFileInputStream=sourceCodeZipFileInputStream;
			return this;
		}

		@Override
		protected void customBuild(CheckmarxConfig config) {
			config.teamIdForNewProjects=teamIdForNewProjects;
			config.sourceCodeZipFileInputStream=sourceCodeZipFileInputStream;
		}

		@Override
		protected CheckmarxConfig buildInitialConfig() {
			return new CheckmarxConfig();
		}

		@Override
		protected void customValidate() {
			assertUserSet();
			assertPasswordSet();
			assertProjectIdSet();
			assertTeamIdSet();
		}

		protected void assertTeamIdSet() {
			if (teamIdForNewProjects == null) {
				throw new IllegalStateException("no team id given");
			}
		}
	}


}
