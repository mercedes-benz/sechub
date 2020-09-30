// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.net.InetAddress;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.domain.scan.InstallSetup;
import com.daimler.sechub.domain.scan.SecHubAdapterOptionsBuilderStrategy;
import com.daimler.sechub.domain.scan.Target;
import com.daimler.sechub.domain.scan.TargetRegistry;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.resolve.TargetResolver;
import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

/**
 * An abstract product executor implementation which does automatically handle
 * identification of targets by install setup. The different target types are
 * separated and the
 * {@link #executeWithAdapter(SecHubExecutionContext, InstallSetup, TargetRegistryInfo)}
 * method will be called for each found target type once.<br>
 * <br>
 * Implementations have to handle the execution for a target type and also the
 * creation of an {@link InstallSetup}.
 *
 * @author Albert Tregnaghi
 *
 * @param <S>
 */
public abstract class AbstractInstallSetupProductExecutor<S extends InstallSetup> implements ProductExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractInstallSetupProductExecutor.class);

	@Autowired
	protected TargetResolver targetResolver;
	
	/**
	 * @return scan type of this executor - never <code>null</code>
	 */
	protected abstract ScanType getScanType();

	protected SecHubAdapterOptionsBuilderStrategy createAdapterOptionsStrategy(SecHubExecutionContext context) {
		return new SecHubAdapterOptionsBuilderStrategy(context, getScanType());
	}
	
	@Override
	public final List<ProductResult> execute(SecHubExecutionContext context, ProductExecutorContext executorContext ) throws SecHubExecutionException {
		UUIDTraceLogID traceLogId = context.getTraceLogId();

		LOG.debug("Executing {}", traceLogId);

		SecHubConfiguration config = context.getConfiguration();
		S setup = getInstallSetup();

		TargetRegistry registry = createTargetRegistry();

		List<URI> uris = resolveURIsForTarget(config);
		registerURIs(traceLogId, setup, registry, uris);

		List<InetAddress> inetAdresses = resolveInetAdressForTarget(config);
		registerInetAdresses(traceLogId, setup, registry, inetAdresses);

		customRegistration(traceLogId, setup, registry, config);

		try {
			return execute(context, executorContext, registry, setup);
		} catch (SecHubExecutionException e) {
			throw e;
		} catch (Exception e) {
			/*
			 * every other exception is wrapped to a SecHub execution exception which is
			 * handled
			 */
			throw new SecHubExecutionException(getIdentifier() + " execution failed." + traceLogId, e);
		}

	}
	
	protected void customRegistration(UUIDTraceLogID traceLogId, S setup, TargetRegistry registry, SecHubConfiguration config) {
		/* do nothing per default*/
	}

	TargetRegistry createTargetRegistry() {
		return new TargetRegistry();
	}

	private void registerURIs(UUIDTraceLogID traceLogId, S setup, TargetRegistry registry, List<URI> uris) {
		if (uris == null || uris.isEmpty()) {
			return;
		}
		for (URI uri : uris) {
			Target target = targetResolver.resolveTarget(uri);
			if (!setup.isAbleToScan(target.getType())) {
				LOG.error("{}: setup not able to scan target {}", getIdentifier(), target);
				continue;
			}
			LOG.debug("{} register scan target:{}", traceLogId, target);
			registry.register(target);
		}
	}

	private void registerInetAdresses(UUIDTraceLogID traceLogId, S setup, TargetRegistry registry,
			List<InetAddress> inetAdresses) {
		if (inetAdresses == null || inetAdresses.isEmpty()) {
			return;
		}
		for (InetAddress inetAdress : inetAdresses) {
			Target target = targetResolver.resolveTarget(inetAdress);
			if (!setup.isAbleToScan(target.getType())) {
				LOG.error("{}: setup not able to scan target {}", getIdentifier(), target);
				continue;
			}
			LOG.debug("{} register scan target:{}", traceLogId, target);
			registry.register(target);
		}
	}

	/**
	 * Implementation will return the target URIs to use for targets
	 *
	 * @param config
	 * @return uris or <code>null</code>
	 */
	protected abstract List<URI> resolveURIsForTarget(SecHubConfiguration config);

	/**
	 * Implementation will return the IP adresses to use for targets
	 *
	 * @param config
	 * @return ip adresses or <code>null</code>
	 */
	protected abstract List<InetAddress> resolveInetAdressForTarget(SecHubConfiguration config);

	/**
	 * Execute the scan by product
	 *
	 * @param context
	 * @param registry
	 * @param setup
	 * @return result or <code>null</code> (null means the setup is not able to
	 *         scan)
	 * @throws SecHubExecutionException
	 */
	protected List<ProductResult> execute(SecHubExecutionContext context, ProductExecutorContext executorContext,TargetRegistry registry, S setup)
			throws Exception /* NOSONAR */ {
		List<ProductResult> result = new ArrayList<>();

		/* we handle here automatically all known targets and call the adapters */
		for (TargetType type: TargetType.values()) {
			if (!type.isValid()) {
				/* not executable*/
				continue;
			}
			executeAdapterWhenTargetTypeSupported(context, executorContext, registry, setup, result, type);
		}

		return result;

	}

	private void executeAdapterWhenTargetTypeSupported(SecHubExecutionContext context, ProductExecutorContext executorContext ,TargetRegistry registry, S setup,
			List<ProductResult> result, TargetType targetType) throws Exception {
		if (!setup.isAbleToScan(targetType)) {
			LOG.debug("{} Setup says its not able to scan target type {} with {}", context.getTraceLogId(), targetType,
					getIdentifier());
			return;
		}else {
			LOG.debug("{} Setup says it IS able to scan target type {} with {}", context.getTraceLogId(), targetType,
					getIdentifier());
		}
		TargetRegistryInfo registryInfo = registry.createRegistryInfo(targetType);

		if (!registryInfo.containsAtLeastOneTarget()) {
			LOG.debug("{} Did not found any IP, URI, or identifier defined for target type '{}' for {}", context.getTraceLogId(),
					targetType, getIdentifier());
			return;
		}

		LocalDateTime started = LocalDateTime.now();
		List<ProductResult> productResults = executeWithAdapter(context, executorContext, setup, registryInfo);
		LocalDateTime ended = LocalDateTime.now();

		if (productResults != null) {
			for (ProductResult pr: productResults) {
				pr.setStarted(started);
				pr.setEnded(ended);
			}
			result.addAll(productResults);
		}
	}

	/**
	 * At this point the implementation can call the adapter. It is ensured that the
	 * adapter is able to scan the wanted targets.<br>
	 * <br>
	 * The implementation handles the final execution by adapter and must decide if
	 * it uses the adapter in a single call for the given target, or make a loop
	 * call (e.g. when the product is not able to scan multiple URIs or IPs at same
	 * time and produces multiple product results - exotic, but could happen - so we use
	 * a list as result!)
	 *
	 * @param context
	 * @param setup
	 * @param targetData
	 * @return
	 * @throws Exception
	 */
	protected abstract List<ProductResult> executeWithAdapter(SecHubExecutionContext context,ProductExecutorContext executorContext, S setup,
			TargetRegistryInfo targetData) throws Exception/* NOSONAR */;

	/**
	 * Get the install setup which defines the product hosting location and the
	 * supported target types - this should be injected by spring!
	 *
	 * @return install setup, never <code>null</code>
	 */
	protected abstract S getInstallSetup();

}
