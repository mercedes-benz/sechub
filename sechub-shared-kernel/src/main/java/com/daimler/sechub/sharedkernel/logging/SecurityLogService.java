// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.UserContextService;

@Service
public class SecurityLogService {

	@Autowired
	UserContextService userContextService;

	private static final Logger LOG = LoggerFactory.getLogger(SecurityLogService.class);

	private static String SECURITY = "[SECURITY] [{}]";
	private static String SECURITY_USERNAME = SECURITY+" ({}) :";

	public void log(SecurityLogType type, String message, Object ...objects ) {
		if (type==null) {
			type=SecurityLogType.UNKNOWN;
			LOG.warn("Security log service was called with no type id! Wrong implemented! Use fallback:{}",type);
		}
		/* convert this to a new list, otherweise slf4j becomes problems with identifying this as list and having wrong output*/
		String userId = userContextService.getUserId();
		List<Object> list = new ArrayList<>();
		list.add(userId);
		list.add(type.getTypeId());
		list.addAll(Arrays.asList(objects));

		Object[] array = list.toArray();
		LOG.info(SECURITY_USERNAME+message, array);
	}

}
