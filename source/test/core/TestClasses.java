package core;

import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.Component;
import bloodandmithril.util.CursorBoundTask;
import javassist.Modifier;

/**
 * Tests classes that are {@link Serializable} that have {@link Inject}ed
 * dependencies marked as transient
 *
 * Tests all classes are marked with {@link Copyright}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class TestClasses {

	/**
	 * Uses reflection to get every single class and test them
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
		final Set<Class<? extends Object>> classesToTest = reflections.getSubTypesOf(Object.class);
		classesToTest.stream().sorted((c1, c2) -> {return c1.getName().compareTo(c2.getName());}).forEach(each -> {
			testClass(each, errors);
		});

		if (!errors.isEmpty()) {
			errors.forEach(message -> {
				System.out.println(message);
			});
			fail("Errors found, check console output");
		}

		System.out.println("Test passed " + classesToTest.size() + " classes.");
	}


	private void testClass(final Class<?> clazz, final List<String> errors) {
		if (Serializable.class.isAssignableFrom(clazz)) {
			for (final Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(Inject.class)) {
					if (!Modifier.isTransient(field.getModifiers())) {
						errors.add("Found non-transient dependency on serializable class: " + clazz.getName() + "#" + field.getName());
					}
				}
			}
		} else {
			boolean hasInjectedDependencies = false;
			
			for (final Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(Inject.class)) {
					if (Modifier.isTransient(field.getModifiers())) {
						errors.add("Found transient dependency on non-serializable class: " + clazz.getName() + "#" + field.getName());
					}
					
					hasInjectedDependencies = true;
				}
			}
			
			if (hasInjectedDependencies) {
				if (!clazz.isAnnotationPresent(Singleton.class) && 
					!Component.class.isAssignableFrom(clazz) &&
					!CursorBoundTask.class.isAssignableFrom(clazz) &&
					clazz != BloodAndMithrilClient.class) {
					errors.add("Found class with injected dependencies without @Singleton annotation: " + clazz.getName());
				}
			}
		}

		if (AITask.class.isAssignableFrom(clazz)) {
			if (!clazz.isAnnotationPresent(ExecutedBy.class)) {
				errors.add("Found AITask not annotated with @ExecutedBy: " + clazz.getName());
			}
		}

		if (clazz.isAnnotationPresent(ExecutedBy.class)) {
			if (!AITask.class.isAssignableFrom(clazz)) {
				errors.add("Found non AITask annotated with @ExecutedBy: " + clazz.getName());
			}
		}

		if (RoutineTask.class.isAssignableFrom(clazz)) {
			if (!clazz.isAnnotationPresent(RoutineContextMenusProvidedBy.class)) {
				errors.add("Found RoutineTask not annotated with @RoutineTaskContextMenuProvider: " + clazz.getName());
			}
		}

		if (clazz.isAnnotationPresent(RoutineContextMenusProvidedBy.class)) {
			if (!RoutineTask.class.isAssignableFrom(clazz)) {
				errors.add("Found non RoutineTask annotated with @RoutineTaskContextMenuProvider: " + clazz.getName());
			}
		}

		// Filter out:
		// Static classes
		// Member classes
		// Local classes
		// Anonymous classes
		if (!Modifier.isStatic(clazz.getModifiers()) && !clazz.isMemberClass() && !clazz.isLocalClass() && !clazz.isAnonymousClass()) {
			if (!clazz.isAnnotationPresent(Copyright.class)) {
				errors.add("Found class without Copyright annotation: " + clazz.getName());
			}
		}
	}
}