package org.fizmo.dropwizard.guice;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Module;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;

/**
 * A ModuleFactory is used to create instances of Guice modules that need the Dropwizard configuration to create
 * their bindings. Common needs are bindings for database access.
 *
 * @param <T>
 */
public interface ModuleFactory<T> {

    Module create(T configuration);

    static class Builder {

        /**
         * A helper method to create a ModuleFactory from a class object.
         *
         * The class must implement @{com.google.inject.Module}, and have a public, single-argument constructor that
         * takes an instance of the configuration class as its argument.
         *
         * @param moduleClass The class of the module.
         * @param <T> The type of the configuration object.
         * @return A ModuleFactory that will create instances of the module, given a configuration.
         */
        static <T> ModuleFactory<T> fromClass(final Class<? extends Module> moduleClass)
        {
            return new ModuleFactory<T>() {
                @Override
                public Module create(T configuration) {
                    final Class<?> configClass = configuration.getClass();
                    try {
                        final Constructor<? extends Module> ctor = moduleClass.getConstructor(configClass);
                        return ctor.newInstance(configuration);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        /**
         * A helper function to create a list of ModuleFactories from a list of Module classes.
         *
         * @param moduleClasses The class of the module.
         * @param <T> The type of the configuration object.
         * @return A ModuleFactory that will create instances of the module, given a configuration.
         */
        static <T> Iterable<ModuleFactory<T>> fromClasses(final Iterable<Class<? extends Module>> moduleClasses)
        {
            return Iterables.transform(moduleClasses, new Function<Class<? extends Module>, ModuleFactory<T>>() {
                @Nullable
                @Override
                public ModuleFactory<T> apply(@Nullable Class<? extends Module> moduleClass) {
                    return Builder.fromClass(moduleClass);
                }
            });
        }

    }
}
