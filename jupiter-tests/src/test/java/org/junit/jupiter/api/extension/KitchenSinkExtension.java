/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

/**
 * <em>Kitchen Sink</em> extension that implements every extension API
 * supported by JUnit Jupiter.
 *
 * <p>This extension should never actually be registered for any tests.
 * Rather, its sole purpose is to help ensure (via visual inspection)
 * that a concrete extension is able to implement all extension APIs
 * supported by JUnit Jupiter without any naming conflicts or
 * ambiguities with regard to method names or method signatures.
 * {@link ExtensionComposabilityTests}, on the other hand, serves
 * the same purpose in a dynamic and automated fashion.
 *
 * @since 5.0
 * @see ExtensionComposabilityTests
 */
// @formatter:off
public class KitchenSinkExtension implements

	// Lifecycle Callbacks
	BeforeAllCallback,
		BeforeClassTemplateInvocationCallback,
			BeforeEachCallback,
				BeforeTestExecutionCallback,
					TestExecutionExceptionHandler,
				AfterTestExecutionCallback,
			AfterEachCallback,
		AfterClassTemplateInvocationCallback,
	AfterAllCallback,

	// Lifecycle methods exception handling
	LifecycleMethodExecutionExceptionHandler,

	// Dependency Injection
	TestInstancePreConstructCallback,
	TestInstanceFactory,
	TestInstancePostProcessor,
	TestInstancePreDestroyCallback,
	ParameterResolver,

	// Conditional Test Execution
	ExecutionCondition,

	// @TestTemplate and @ClassTemplate
	TestTemplateInvocationContextProvider,
	ClassTemplateInvocationContextProvider,

	// Miscellaneous
	TestWatcher,
	InvocationInterceptor,
	PreInterruptCallback

// @formatter:on
{

	// --- Test Class Instantiation --------------------------------------------

	@Override
	public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
		return ExtensionContextScope.TEST_METHOD;
	}

	// --- Lifecycle Callbacks -------------------------------------------------

	@Override
	public void beforeAll(ExtensionContext context) {
	}

	@Override
	public void beforeClassTemplateInvocation(ExtensionContext context) {
	}

	@Override
	public void beforeEach(ExtensionContext context) {
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) {
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
	}

	@Override
	public void afterEach(ExtensionContext context) {
	}

	@Override
	public void afterClassTemplateInvocation(ExtensionContext context) {
	}

	@Override
	public void afterAll(ExtensionContext context) {
	}

	// --- Lifecycle methods exception handling

	@Override
	public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable) {
	}

	@Override
	public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) {
	}

	@Override
	public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) {
	}

	@Override
	public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable) {
	}

	// --- Dependency Injection ------------------------------------------------

	@Override
	public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
	}

	@Override
	public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
	}

	@Override
	public void preDestroyTestInstance(ExtensionContext context) {
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return false;
	}

	@Override
	public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return null;
	}

	// --- Conditional Test Execution ------------------------------------------

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		throw new UnsupportedOperationException();
	}

	// --- @TestTemplate -------------------------------------------------------

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return false;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean mayReturnZeroTestTemplateInvocationContexts(ExtensionContext context) {
		return false;
	}

	// --- @ClassTemplate -------------------------------------------------------

	@Override
	public boolean supportsClassTemplate(ExtensionContext context) {
		return false;
	}

	@Override
	public Stream<ClassTemplateInvocationContext> provideClassTemplateInvocationContexts(ExtensionContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean mayReturnZeroClassTemplateInvocationContexts(ExtensionContext context) {
		return false;
	}

	// --- TestWatcher ---------------------------------------------------------

	@Override
	public void testDisabled(ExtensionContext context, Optional<String> reason) {
	}

	@Override
	public void testSuccessful(ExtensionContext context) {
	}

	@Override
	public void testAborted(ExtensionContext context, @Nullable Throwable cause) {
	}

	@Override
	public void testFailed(ExtensionContext context, @Nullable Throwable cause) {
	}

	// --- InvocationInterceptor -----------------------------------------------

	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
			throws Throwable {
		return InvocationInterceptor.super.interceptTestClassConstructor(invocation, invocationContext,
			extensionContext);
	}

	@Override
	public void interceptBeforeAllMethod(Invocation<@Nullable Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		InvocationInterceptor.super.interceptBeforeAllMethod(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptBeforeEachMethod(Invocation<@Nullable Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		InvocationInterceptor.super.interceptBeforeEachMethod(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptTestMethod(Invocation<@Nullable Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		InvocationInterceptor.super.interceptTestMethod(invocation, invocationContext, extensionContext);
	}

	@Override
	public <T> T interceptTestFactoryMethod(Invocation<T> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		return InvocationInterceptor.super.interceptTestFactoryMethod(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptTestTemplateMethod(Invocation<@Nullable Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		InvocationInterceptor.super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptDynamicTest(Invocation<@Nullable Void> invocation,
			DynamicTestInvocationContext invocationContext, ExtensionContext extensionContext) throws Throwable {
		InvocationInterceptor.super.interceptDynamicTest(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptAfterEachMethod(Invocation<@Nullable Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		InvocationInterceptor.super.interceptAfterEachMethod(invocation, invocationContext, extensionContext);
	}

	@Override
	public void interceptAfterAllMethod(Invocation<@Nullable Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		InvocationInterceptor.super.interceptAfterAllMethod(invocation, invocationContext, extensionContext);
	}

	// --- PreInterruptCallback ------------------------------------------------

	@Override
	public void beforeThreadInterrupt(PreInterruptContext preInterruptContext, ExtensionContext extensionContext)
			throws Exception {

	}
}
