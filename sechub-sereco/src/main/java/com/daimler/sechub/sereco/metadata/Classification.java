// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.metadata;

public class Classification {

	private static final String DEFAULT_MISSING_VALUE = "";
	
	private String owasp = DEFAULT_MISSING_VALUE; // ...clear
	private String wasc = DEFAULT_MISSING_VALUE; // wasc - webapplication consortium?
	private String cwe = DEFAULT_MISSING_VALUE; // common weakness enum
	private String capec = DEFAULT_MISSING_VALUE; // common attack pattern.
	private String pci31 = DEFAULT_MISSING_VALUE; // pcidss payment card data security standard
	private String pci32 = DEFAULT_MISSING_VALUE;
	private String hipaa = DEFAULT_MISSING_VALUE; // health care ... USA
	private String nist = DEFAULT_MISSING_VALUE;// National Institute of Standards and Technology - https://en.wikipedia.org/wiki/National_Institute_of_Standards_and_Technology
	private String fisma = DEFAULT_MISSING_VALUE;// Federal Information Security Modernization Act  (homeland security)
	/**
	 * https://www.owasp.org/index.php/OWASP_Proactive_Controls
	 */
	private String owaspProactiveControls=DEFAULT_MISSING_VALUE; // security techniques that should be used in every project

	public String getOwasp() {
		return owasp;
	}

	public void setOwasp(String owasp) {
		this.owasp = owasp;
	}

	public String getWasc() {
		return wasc;
	}

	public void setWasc(String wasc) {
		this.wasc = wasc;
	}

	public String getCwe() {
		return cwe;
	}

	public void setCwe(String cwe) {
		this.cwe = cwe;
	}

	public String getCapec() {
		return capec;
	}

	public void setCapec(String capec) {
		this.capec = capec;
	}

	public String getPci31() {
		return pci31;
	}

	public void setPci31(String pci31) {
		this.pci31 = pci31;
	}

	public String getPci32() {
		return pci32;
	}

	public void setPci32(String pci32) {
		this.pci32 = pci32;
	}

	public String getHipaa() {
		return hipaa;
	}

	public void setHipaa(String hipaa) {
		this.hipaa = hipaa;
	}

	public String getOwaspProactiveControls() {
		return owaspProactiveControls;
	}

	public void setOwaspProactiveControls(String owaspProactiveControls) {
		this.owaspProactiveControls = owaspProactiveControls;
	}

	public void setNist(String nist) {
		this.nist = nist;
	}
	public String getNist() {
		return nist;
	}
	
	public void setFisma(String fixma) {
		this.fisma = fixma;
	}
	
	public String getFisma() {
		return fisma;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((capec == null) ? 0 : capec.hashCode());
		result = prime * result + ((cwe == null) ? 0 : cwe.hashCode());
		result = prime * result + ((fisma == null) ? 0 : fisma.hashCode());
		result = prime * result + ((hipaa == null) ? 0 : hipaa.hashCode());
		result = prime * result + ((nist == null) ? 0 : nist.hashCode());
		result = prime * result + ((owasp == null) ? 0 : owasp.hashCode());
		result = prime * result + ((owaspProactiveControls == null) ? 0 : owaspProactiveControls.hashCode());
		result = prime * result + ((pci31 == null) ? 0 : pci31.hashCode());
		result = prime * result + ((pci32 == null) ? 0 : pci32.hashCode());
		result = prime * result + ((wasc == null) ? 0 : wasc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Classification other = (Classification) obj;
		if (capec == null) {
			if (other.capec != null)
				return false;
		} else if (!capec.equals(other.capec))
			return false;
		if (cwe == null) {
			if (other.cwe != null)
				return false;
		} else if (!cwe.equals(other.cwe))
			return false;
		if (fisma == null) {
			if (other.fisma != null)
				return false;
		} else if (!fisma.equals(other.fisma))
			return false;
		if (hipaa == null) {
			if (other.hipaa != null)
				return false;
		} else if (!hipaa.equals(other.hipaa))
			return false;
		if (nist == null) {
			if (other.nist != null)
				return false;
		} else if (!nist.equals(other.nist))
			return false;
		if (owasp == null) {
			if (other.owasp != null)
				return false;
		} else if (!owasp.equals(other.owasp))
			return false;
		if (owaspProactiveControls == null) {
			if (other.owaspProactiveControls != null)
				return false;
		} else if (!owaspProactiveControls.equals(other.owaspProactiveControls))
			return false;
		if (pci31 == null) {
			if (other.pci31 != null)
				return false;
		} else if (!pci31.equals(other.pci31))
			return false;
		if (pci32 == null) {
			if (other.pci32 != null)
				return false;
		} else if (!pci32.equals(other.pci32))
			return false;
		if (wasc == null) {
			if (other.wasc != null)
				return false;
		} else if (!wasc.equals(other.wasc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Classification [owasp=" + owasp + ", wasc=" + wasc + ", cwe=" + cwe + ", capec=" + capec + ", pci31="
				+ pci31 + ", pci32=" + pci32 + ", hipaa=" + hipaa + ", owaspProactiveControls=" + owaspProactiveControls
				+ "]";
	}

}
