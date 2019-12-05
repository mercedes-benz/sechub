package com.daimler.sechub.domain.scan.product.checkmarx;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import com.daimler.sechub.sharedkernel.resilience.ResilienceConsultant;
import com.daimler.sechub.sharedkernel.resilience.ResilienceContext;
import com.daimler.sechub.sharedkernel.resilience.ResilienceProposal;
import com.daimler.sechub.sharedkernel.resilience.SimpleRetryResilienceProposal;
import com.daimler.sechub.sharedkernel.util.StacktraceUtil;

public class CheckmarxBadRequestConsultant implements ResilienceConsultant {

	private static final Logger LOG = LoggerFactory.getLogger(CheckmarxBadRequestConsultant.class);

	@Override
	public ResilienceProposal consultFor(ResilienceContext context) {
		Objects.requireNonNull(context);
		Throwable rootCause = StacktraceUtil.findRootCause(context.getCurrentError());
		if (rootCause instanceof HttpClientErrorException) {
			HttpClientErrorException hce = (HttpClientErrorException) rootCause;
			int statusCode = hce.getRawStatusCode();
			if (statusCode == 400) {
				/*
				 * BAD request - this can happen for same project scans put to queue because
				 * there can a chmkx server error happen
				 */
				LOG.info("Make retry proposal");
				return new SimpleRetryResilienceProposal("checkmarx bad request handling", 3, 2000);
			}else {
				LOG.info("Can't make proposal for http client error exception:{}",StacktraceUtil.createDescription(rootCause));
			}
		} else {
			LOG.info("Can't make proposal for exception with root cause:{}",StacktraceUtil.createDescription(rootCause));
		}
		return null;
	}

}
