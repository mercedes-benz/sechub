// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.scan.Target;
import com.daimler.sechub.domain.scan.TargetType;

@Component
public class IntranetIPpatternResolveStrategy implements InetAdressTargetResolveStrategy {

	public static final String PREFIX = "intranet-ip-pattern:";
	private static final Logger LOG = LoggerFactory.getLogger(IntranetEndsWithURITargetResolveStrategy.class);

	private Set<InetAddressPattern> patterns = new LinkedHashSet<>();

	public boolean initialize(String uriPattern) {
		if (uriPattern == null) {
			return false;
		}
		if (!uriPattern.startsWith(PREFIX)) {
			return false;
		}
		String values = uriPattern.substring(PREFIX.length()).trim();
		if (values.isEmpty()) {
			LOG.warn("{} used, bot no content found!",PREFIX);
			return false;
		}
		String[] uriEndings = values.split(",");
		for (String uriEnding : uriEndings) {
			String pattern = uriEnding.trim();

			/* first try ipv4*/
			IPv4Pattern ipv4Pattern = new IPv4Pattern(pattern);
			if (ipv4Pattern.isValid()) {
				LOG.info("Intranet detection by ipv4 pattern:'{}'",pattern);
				patterns.add(ipv4Pattern);
				continue;
			}
			/* next try ipv6*/
			IPv6Pattern ipv6Pattern = new IPv6Pattern(pattern);
			if (ipv6Pattern.isValid()) {
				LOG.info("Intranet detection by ipv6 pattern:'{}'",pattern);
				patterns.add(ipv6Pattern);
				continue;
			}
			/* we simply ignore not matching patterns, so no special handling*/
			LOG.warn("Invalid pattern:'{}' - is ignored",pattern);
		}
		return true;

	}

	@Override
	public Target resolveTargetFor(InetAddress inetAdress) {
		Target result = null;

		for (InetAddressPattern p: patterns) {
			if (p.isMatching(inetAdress)) {
				result = new Target(inetAdress,TargetType.INTRANET);
				break;
			}
		}
		if (result == null) {
			result = new Target(inetAdress, TargetType.INTERNET);
		}
		return result;
	}

}
