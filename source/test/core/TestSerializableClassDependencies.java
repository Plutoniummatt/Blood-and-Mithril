package core;

import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import javassist.Modifier;

/**
 * Tests classes that are {@link Serializable} that have {@link Inject}ed
 * dependencies marked as transient
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class TestSerializableClassDependencies {

	/**
	 * Uses reflection to get every single class, then verifies that all {@link Serializable} classes
	 * {@link Inject} dependencies that are marked as transient
	 *
	 * Also verifies that non-serializable classes {@link Inject} dependencies are not marked with transient
	 */
	@Test
	public void test() {
		final List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
		classLoadersList.add(ClasspathHelper.contextClassLoader());
		classLoadersList.add(ClasspathHelper.staticClassLoader());

		final Reflections reflections = new Reflections(
			new ConfigurationBuilder()
			.setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
			.setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
			.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("bloodandmithril")))
		);

		final List<String> errors = Lists.newLinkedList();
		reflections.getSubTypesOf(Object.class).stream().sorted((c1, c2) -> {return c1.getName().compareTo(c2.getName());}).forEach(each -> {
			assertTransient(each, errors);
		});

		if (!errors.isEmpty()) {
			errors.forEach(message -> {
				System.out.println(message);
			});
			fail("Errors found, check console output");
		}
	}


	private void assertTransient(final Class<?> clazz, final List<String> errors) {
		if (Serializable.class.isAssignableFrom(clazz)) {
			for (final Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(Inject.class)) {
					if (!Modifier.isTransient(field.getModifiers())) {
						errors.add("Found non-transient dependency on serializable class: " + clazz.getName() + "#" + field.getName());
					}
				}
			}
		} else {
			for (final Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(Inject.class)) {
					if (Modifier.isTransient(field.getModifiers())) {
						errors.add("Found transient dependency on non-serializable class: " + clazz.getName() + "#" + field.getName());
					}
				}
			}
		}
	}
}