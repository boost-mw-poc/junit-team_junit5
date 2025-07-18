/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.execution;

import static java.util.Objects.requireNonNullElse;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.CancellationToken;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.vintage.engine.Constants;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageEngineDescriptor;

/**
 * @since 5.12
 */
@SuppressWarnings({ "deprecation", "RedundantSuppression" })
@API(status = INTERNAL, since = "5.12")
public class VintageExecutor {

	private static final Logger logger = LoggerFactory.getLogger(VintageExecutor.class);

	private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
	private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

	private final VintageEngineDescriptor engineDescriptor;
	private final EngineExecutionListener engineExecutionListener;
	private final ConfigurationParameters configurationParameters;

	private final boolean parallelExecutionEnabled;
	private final boolean classes;
	private final boolean methods;

	public VintageExecutor(VintageEngineDescriptor engineDescriptor, EngineExecutionListener engineExecutionListener,
			ConfigurationParameters configurationParameters) {
		this.engineDescriptor = engineDescriptor;
		this.engineExecutionListener = engineExecutionListener;
		this.configurationParameters = configurationParameters;
		this.parallelExecutionEnabled = configurationParameters.getBoolean(Constants.PARALLEL_EXECUTION_ENABLED).orElse(
			false);
		this.classes = configurationParameters.getBoolean(Constants.PARALLEL_CLASS_EXECUTION).orElse(false);
		this.methods = configurationParameters.getBoolean(Constants.PARALLEL_METHOD_EXECUTION).orElse(false);
	}

	public void executeAllChildren(CancellationToken cancellationToken) {

		if (!parallelExecutionEnabled) {
			executeClassesAndMethodsSequentially(cancellationToken);
			return;
		}

		if (!classes && !methods) {
			logger.warn(() -> "Parallel execution is enabled but no scope is defined. "
					+ "Falling back to sequential execution.");
			executeClassesAndMethodsSequentially(cancellationToken);
			return;
		}

		boolean wasInterrupted = executeInParallel(cancellationToken);
		if (wasInterrupted) {
			Thread.currentThread().interrupt();
		}
	}

	private void executeClassesAndMethodsSequentially(CancellationToken cancellationToken) {
		RunnerExecutor runnerExecutor = new RunnerExecutor(engineExecutionListener, cancellationToken);
		for (Iterator<TestDescriptor> iterator = engineDescriptor.getModifiableChildren().iterator(); iterator.hasNext();) {
			runnerExecutor.execute((RunnerTestDescriptor) iterator.next());
			iterator.remove();
		}
	}

	private boolean executeInParallel(CancellationToken cancellationToken) {
		ExecutorService executorService = Executors.newWorkStealingPool(getThreadPoolSize());
		RunnerExecutor runnerExecutor = new RunnerExecutor(engineExecutionListener, cancellationToken);

		List<RunnerTestDescriptor> runnerTestDescriptors = collectRunnerTestDescriptors(executorService);

		if (!classes) {
			executeClassesSequentially(runnerTestDescriptors, runnerExecutor);
			return false;
		}

		return executeClassesInParallel(runnerTestDescriptors, runnerExecutor, executorService);
	}

	private int getThreadPoolSize() {
		Optional<String> optionalPoolSize = configurationParameters.get(Constants.PARALLEL_POOL_SIZE);
		if (optionalPoolSize.isPresent()) {
			try {
				int poolSize = Integer.parseInt(optionalPoolSize.get());
				if (poolSize > 0) {
					return poolSize;
				}
				logger.warn(() -> "Invalid value for parallel pool size: " + poolSize);
			}
			catch (NumberFormatException e) {
				logger.warn(() -> "Invalid value for parallel pool size: " + optionalPoolSize.get());
			}
		}
		return DEFAULT_THREAD_POOL_SIZE;
	}

	private List<RunnerTestDescriptor> collectRunnerTestDescriptors(ExecutorService executorService) {
		return engineDescriptor.getModifiableChildren().stream() //
				.map(RunnerTestDescriptor.class::cast) //
				.map(it -> methods ? parallelMethodExecutor(it, executorService) : it) //
				.toList();
	}

	private RunnerTestDescriptor parallelMethodExecutor(RunnerTestDescriptor runnerTestDescriptor,
			ExecutorService executorService) {
		runnerTestDescriptor.setExecutorService(executorService);
		return runnerTestDescriptor;
	}

	private void executeClassesSequentially(List<RunnerTestDescriptor> runnerTestDescriptors,
			RunnerExecutor runnerExecutor) {
		for (RunnerTestDescriptor runnerTestDescriptor : runnerTestDescriptors) {
			runnerExecutor.execute(runnerTestDescriptor);
		}
	}

	private boolean executeClassesInParallel(List<RunnerTestDescriptor> runnerTestDescriptors,
			RunnerExecutor runnerExecutor, ExecutorService executorService) {
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (RunnerTestDescriptor runnerTestDescriptor : runnerTestDescriptors) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(
				() -> runnerExecutor.execute(runnerTestDescriptor), executorService);
			futures.add(future);
		}

		CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
		boolean wasInterrupted = false;
		try {
			allOf.get();
		}
		catch (InterruptedException e) {
			logger.warn(e, () -> "Interruption while waiting for parallel test execution to finish");
			wasInterrupted = true;
		}
		catch (ExecutionException e) {
			throw ExceptionUtils.throwAsUncheckedException(requireNonNullElse(e.getCause(), e));
		}
		finally {
			shutdownExecutorService(executorService);
		}
		return wasInterrupted;
	}

	private void shutdownExecutorService(ExecutorService executorService) {
		try {
			executorService.shutdown();
			if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				logger.warn(() -> "Executor service did not terminate within the specified timeout");
				executorService.shutdownNow();
			}
		}
		catch (InterruptedException e) {
			logger.warn(e, () -> "Interruption while waiting for executor service to shut down");
			Thread.currentThread().interrupt();
		}
	}

}
