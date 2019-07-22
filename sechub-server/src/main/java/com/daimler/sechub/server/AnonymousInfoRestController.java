// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.sharedkernel.APIConstants;

@RestController
public class AnonymousInfoRestController {

	@Autowired
	private InfoService serverInfoService;

	@RequestMapping(path = APIConstants.API_ANONYMOUS + "info/version", method = RequestMethod.GET, produces = { MediaType.TEXT_PLAIN_VALUE })
	@ResponseBody
	public String getServerVersion() {
		return serverInfoService.getVersionAsString();
	}

}
