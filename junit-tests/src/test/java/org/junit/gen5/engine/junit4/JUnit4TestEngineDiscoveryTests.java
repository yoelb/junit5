/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import static java.text.MessageFormat.format;
import static java.util.Collections.singleton;
import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.gen5.commons.util.FunctionUtils.where;
import static org.junit.gen5.engine.ClassFilters.classNameMatches;
import static org.junit.gen5.engine.dsl.ClassTestPlanSpecificationElementBuilder.forClass;
import static org.junit.gen5.engine.dsl.ClasspathTestPlanSpecificationElementBuilder.allTests;
import static org.junit.gen5.engine.dsl.MethodTestPlanSpecificationElementBuilder.forMethod;
import static org.junit.gen5.engine.dsl.PackageTestPlanSpecificationElementBuilder.forPackage;
import static org.junit.gen5.engine.dsl.TestPlanSpecificationBuilder.testPlanSpecification;
import static org.junit.gen5.engine.dsl.UniqueIdTestPlanSpecificationElementBuilder.forUniqueId;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit4.samples.PlainOldJavaClassWithoutAnyTest;
import org.junit.gen5.engine.junit4.samples.junit3.JUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails;
import org.junit.gen5.engine.junit4.samples.junit3.PlainJUnit3TestCaseWithSingleTestWhichFails;
import org.junit.gen5.engine.junit4.samples.junit4.*;
import org.junit.gen5.engine.junit4.samples.junit4.Categories.Failing;
import org.junit.gen5.engine.junit4.samples.junit4.Categories.Plain;
import org.junit.gen5.engine.junit4.samples.junit4.Categories.Skipped;
import org.junit.gen5.engine.junit4.samples.junit4.Categories.SkippedWithReason;

class JUnit4TestEngineDiscoveryTests {

	JUnit4TestEngine engine = new JUnit4TestEngine();

	@Test
	void resolvesSimpleJUnit4TestClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		TestPlanSpecification specification = buildClassSpecification(testClass);

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "failingTest", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesIgnoredJUnit4TestClass() {
		Class<?> testClass = IgnoredJUnit4TestCase.class;
		TestPlanSpecification specification = buildClassSpecification(testClass);

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertFalse(runnerDescriptor.isContainer());
		assertTrue(runnerDescriptor.isTest());
		assertEquals(testClass.getName(), runnerDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName(), runnerDescriptor.getUniqueId());
		assertThat(runnerDescriptor.getChildren()).isEmpty();
	}

	@Test
	void resolvesJUnit4TestClassWithCustomRunner() throws Exception {
		Class<?> testClass = SingleFailingTheoryTestCase.class;
		TestPlanSpecification specification = buildClassSpecification(testClass);

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "theory", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesJUnit3TestCase() throws Exception {
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;
		TestPlanSpecification specification = buildClassSpecification(testClass);

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "test", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesJUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails() throws Exception {
		Class<?> suiteClass = JUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails.class;
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;
		TestPlanSpecification specification = buildClassSpecification(suiteClass);

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertTrue(suiteDescriptor.isContainer());
		assertFalse(suiteDescriptor.isTest());
		assertThat(suiteDescriptor.getDisplayName()).startsWith("TestSuite with 1 tests");
		assertEquals("junit4:" + suiteClass.getName(), suiteDescriptor.getUniqueId());
		assertClassSource(suiteClass, suiteDescriptor);

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertContainerTestDescriptor(testClassDescriptor, "junit4:" + suiteClass.getName() + "/", testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "test",
			"junit4:" + suiteClass.getName() + "/" + testClass.getName() + "/");
	}

	@Test
	void resolvesJUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored() throws Exception {
		Class<?> suiteClass = JUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored.class;
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichIsIgnored.class;
		TestPlanSpecification specification = buildClassSpecification(suiteClass);

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(suiteDescriptor, suiteClass);

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertContainerTestDescriptor(testClassDescriptor, "junit4:" + suiteClass.getName() + "/", testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "ignoredTest",
			"junit4:" + suiteClass.getName() + "/" + testClass.getName() + "/");
	}

	@Test
	void resolvesJUnit4TestCaseWithOverloadedMethod() {
		Class<?> testClass = JUnit4TestCaseWithOverloadedMethod.class;
		TestPlanSpecification specification = buildClassSpecification(testClass);

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		List<TestDescriptor> testMethodDescriptors = new ArrayList<>(runnerDescriptor.getChildren());
		assertThat(testMethodDescriptors).hasSize(2);

		TestDescriptor testMethodDescriptor = testMethodDescriptors.get(0);
		assertEquals("theory", testMethodDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName() + "/theory" + "(" + testClass.getName() + ")[0]",
			testMethodDescriptor.getUniqueId());
		assertClassSource(testClass, testMethodDescriptor);

		testMethodDescriptor = testMethodDescriptors.get(1);
		assertEquals("theory", testMethodDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName() + "/theory" + "(" + testClass.getName() + ")[1]",
			testMethodDescriptor.getUniqueId());
		assertClassSource(testClass, testMethodDescriptor);
	}

	@Test
	void doesNotResolvePlainOldJavaClassesWithoutAnyTest() {
		assertYieldsNoDescriptors(PlainOldJavaClassWithoutAnyTest.class);
	}

	@Test
	void doesNotResolveClassRunWithJUnit5() {
		assertYieldsNoDescriptors(TestCaseRunWithJUnit5.class);
	}

	@Test
	void resolvesAllTestsSpecification() throws Exception {
		File root = getClasspathRoot(PlainJUnit4TestCaseWithSingleTestWhichFails.class);
		TestPlanSpecification specification = testPlanSpecification().withElements(allTests(singleton(root))).build();
		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		// @formatter:off
		assertThat(engineDescriptor.getChildren())
			.extracting(TestDescriptor::getDisplayName)
			.contains(PlainJUnit4TestCaseWithSingleTestWhichFails.class.getName())
			.contains(PlainJUnit3TestCaseWithSingleTestWhichFails.class.getName())
			.doesNotContain(PlainOldJavaClassWithoutAnyTest.class.getName());
		// @formatter:on
	}

	@Test
	void resolvesApplyingClassFilters() throws Exception {
		File root = getClasspathRoot(PlainJUnit4TestCaseWithSingleTestWhichFails.class);

		TestPlanSpecification specification = testPlanSpecification().withElements(
			allTests(singleton(root))).withEngineFilters(classNameMatches(".*JUnit4.*"),
				classNameMatches(".*Plain.*")).build();

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		// @formatter:off
		assertThat(engineDescriptor.getChildren())
			.extracting(TestDescriptor::getDisplayName)
			.contains(PlainJUnit4TestCaseWithSingleTestWhichFails.class.getName())
			.doesNotContain(JUnit4TestCaseWithOverloadedMethod.class.getName())
			.doesNotContain(PlainJUnit3TestCaseWithSingleTestWhichFails.class.getName());
		// @formatter:on
	}

	@Test
	void resolvesPackageSpecificationForJUnit4SamplesPackage() {
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;

		TestPlanSpecification specification = testPlanSpecification().withElements(
			forPackage(testClass.getPackage().getName())).build();

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		// @formatter:off
		assertThat(engineDescriptor.getChildren())
			.extracting(TestDescriptor::getDisplayName)
			.contains(testClass.getName())
			.doesNotContain(PlainJUnit3TestCaseWithSingleTestWhichFails.class.getName());
		// @formatter:on
	}

	@Test
	void resolvesPackageSpecificationForJUnit3SamplesPackage() {
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;

		TestPlanSpecification specification = testPlanSpecification().withElements(
			forPackage(testClass.getPackage().getName())).build();

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		// @formatter:off
		assertThat(engineDescriptor.getChildren())
			.extracting(TestDescriptor::getDisplayName)
			.contains(testClass.getName())
			.doesNotContain(PlainJUnit4TestCaseWithSingleTestWhichFails.class.getName());
		// @formatter:on
	}

	@Test
	void resolvesClassesWithInheritedMethods() throws Exception {
		Class<?> superclass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		Class<?> testClass = PlainJUnit4TestCaseWithSingleInheritedTestWhichFails.class;
		TestPlanSpecification specification = buildClassSpecification(testClass);

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertEquals(testClass.getName(), runnerDescriptor.getDisplayName());
		assertClassSource(testClass, runnerDescriptor);

		TestDescriptor testDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertEquals("failingTest", testDescriptor.getDisplayName());
		assertMethodSource(superclass.getMethod("failingTest"), testDescriptor);
	}

	@Test
	void resolvesCategoriesIntoTags() {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestPlanSpecification specification = buildClassSpecification(testClass);

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(runnerDescriptor.getTags()).containsOnly(new TestTag(Plain.class.getName()));

		TestDescriptor failingTest = findChildByDisplayName(runnerDescriptor, "failingTest");
		assertThat(failingTest.getTags()).containsOnly(//
			new TestTag(Plain.class.getName()), //
			new TestTag(Failing.class.getName()));

		TestDescriptor ignoredWithoutReason = findChildByDisplayName(runnerDescriptor, "ignoredTest1_withoutReason");
		assertThat(ignoredWithoutReason.getTags()).containsOnly(//
			new TestTag(Plain.class.getName()), //
			new TestTag(Skipped.class.getName()));

		TestDescriptor ignoredWithReason = findChildByDisplayName(runnerDescriptor, "ignoredTest2_withReason");
		assertThat(ignoredWithReason.getTags()).containsOnly(//
			new TestTag(Plain.class.getName()), //
			new TestTag(Skipped.class.getName()), //
			new TestTag(SkippedWithReason.class.getName()));
	}

	@Test
	void resolvesMethodSpecificationForSingleMethod() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestPlanSpecification specification = testPlanSpecification().withElements(
			forMethod(testClass, testClass.getMethod("failingTest"))).build();

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "failingTest", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesMethodSpecificationForTwoMethodsOfSameClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestPlanSpecification specification = testPlanSpecification().withElements(
			forMethod(testClass, testClass.getMethod("failingTest")),
			forMethod(testClass, testClass.getMethod("successfulTest"))).build();

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		List<TestDescriptor> testMethodDescriptors = new ArrayList<>(runnerDescriptor.getChildren());
		assertThat(testMethodDescriptors).hasSize(2);

		TestDescriptor failingTest = testMethodDescriptors.get(0);
		assertTestMethodDescriptor(failingTest, testClass, "failingTest", "junit4:" + testClass.getName() + "/");

		TestDescriptor successfulTest = testMethodDescriptors.get(1);
		assertTestMethodDescriptor(successfulTest, testClass, "successfulTest", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesUniqueIdSpecificationForSingleMethod() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestPlanSpecification specification = testPlanSpecification().withElements(forUniqueId(
			"junit4:org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods/failingTest(org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods)")).build();

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "failingTest", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesUniqueIdSpecificationForSingleClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestPlanSpecification specification = testPlanSpecification().withElements(forUniqueId(
			"junit4:org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods")).build();

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		assertThat(runnerDescriptor.getChildren()).hasSize(5);
	}

	private TestDescriptor findChildByDisplayName(TestDescriptor runnerDescriptor, String displayName) {
		// @formatter:off
		Set<? extends TestDescriptor> children = runnerDescriptor.getChildren();
		return children
				.stream()
				.filter(where(TestDescriptor::getDisplayName, isEqual(displayName)))
				.findAny()
				.orElseThrow(() ->
					new AssertionError(format("No child with display name \"{0}\" in {1}", displayName, children)));
		// @formatter:on
	}

	private File getClasspathRoot(Class<?> testClass) throws Exception {
		URL location = testClass.getProtectionDomain().getCodeSource().getLocation();
		return new File(location.toURI());
	}

	private void assertYieldsNoDescriptors(Class<?> testClass) {
		TestPlanSpecification specification = buildClassSpecification(testClass);

		TestDescriptor engineDescriptor = engine.discoverTests(specification);

		assertThat(engineDescriptor.getChildren()).isEmpty();
	}

	private static void assertRunnerTestDescriptor(TestDescriptor runnerDescriptor, Class<?> testClass) {
		assertContainerTestDescriptor(runnerDescriptor, "junit4:", testClass);
	}

	private static void assertTestMethodDescriptor(TestDescriptor testMethodDescriptor, Class<?> testClass,
			String methodName, String uniqueIdPrefix) throws Exception {
		assertTrue(testMethodDescriptor.isTest());
		assertFalse(testMethodDescriptor.isContainer());
		assertEquals(methodName, testMethodDescriptor.getDisplayName());
		assertEquals(uniqueIdPrefix + methodName + "(" + testClass.getName() + ")", testMethodDescriptor.getUniqueId());
		assertThat(testMethodDescriptor.getChildren()).isEmpty();
		assertMethodSource(testClass.getMethod(methodName), testMethodDescriptor);
	}

	private static void assertContainerTestDescriptor(TestDescriptor containerDescriptor, String uniqueIdPrefix,
			Class<?> testClass) {
		assertTrue(containerDescriptor.isContainer());
		assertFalse(containerDescriptor.isTest());
		assertEquals(testClass.getName(), containerDescriptor.getDisplayName());
		assertEquals(uniqueIdPrefix + testClass.getName(), containerDescriptor.getUniqueId());
		assertClassSource(testClass, containerDescriptor);
	}

	private static void assertClassSource(Class<?> expectedClass, TestDescriptor testDescriptor) {
		assertThat(testDescriptor.getSource()).containsInstanceOf(JavaSource.class);
		JavaSource classSource = (JavaSource) testDescriptor.getSource().get();
		assertThat(classSource.getJavaClass()).hasValue(expectedClass);
		assertThat(classSource.getJavaMethodName()).isEmpty();
		assertThat(classSource.getJavaMethodParameterTypes()).isEmpty();
	}

	private static void assertMethodSource(Method expectedMethod, TestDescriptor testDescriptor) {
		assertThat(testDescriptor.getSource()).containsInstanceOf(JavaSource.class);
		JavaSource methodSource = (JavaSource) testDescriptor.getSource().get();
		assertThat(methodSource.getJavaClass()).hasValue(expectedMethod.getDeclaringClass());
		assertThat(methodSource.getJavaMethodName()).hasValue(expectedMethod.getName());
		assertThat(methodSource.getJavaMethodParameterTypes()).isPresent();
		assertThat(methodSource.getJavaMethodParameterTypes().get()).containsExactly(
			expectedMethod.getParameterTypes());
	}

	private static TestPlanSpecification buildClassSpecification(Class<?> testClass) {
		return testPlanSpecification().withElements(forClass(testClass)).build();
	}
}
