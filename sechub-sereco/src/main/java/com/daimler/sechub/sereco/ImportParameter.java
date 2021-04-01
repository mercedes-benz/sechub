// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco;

public class ImportParameter {
	private String productId;
	private String importData;
	private String importId;

	public static ImportParamBuilder builder() {
		return new ImportParamBuilder();
	}

	public String getProductId() {
		return productId;
	}

	public String getImportData() {
		return importData;
	}

	public String getImportId() {
		return importId;
	}

	/**
	 * Builder for input parameters
	 * @author Albert Tregnaghi
	 *
	 */
	public static class ImportParamBuilder {

		private ImportParameter param;

		private ImportParamBuilder() {
			clear();
		}

		private void clear() {
			this.param = new ImportParameter();
		}

		public ImportParamBuilder productId(String productId) {
			param.productId = productId;
			return this;
		}

		public ImportParamBuilder importData(String importData) {
			param.importData = importData;
			return this;
		}

		public ImportParamBuilder importId(String importId) {
			param.importId = importId;
			return this;
		}

		public ImportParameter build() {
			ImportParameter result = param;
			clear();
			return result;
		}

	}
}