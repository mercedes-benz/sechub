package com.daimler.sechub.domain.scan.product.checkmarx;

import java.util.Objects;

import org.springframework.web.client.HttpClientErrorException;

import com.daimler.sechub.sharedkernel.resilience.ResilienceConsultant;
import com.daimler.sechub.sharedkernel.resilience.ResilienceContext;
import com.daimler.sechub.sharedkernel.resilience.ResilienceProposal;
import com.daimler.sechub.sharedkernel.resilience.SimpleRetryResilienceProposal;
import com.daimler.sechub.sharedkernel.util.StacktraceUtil;

public class CheckmarxBadRequestConsultant implements ResilienceConsultant {

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
				return new SimpleRetryResilienceProposal("checkmarx bad request handling", 3, 2000);

			}
		}
		return null;
	}

}
