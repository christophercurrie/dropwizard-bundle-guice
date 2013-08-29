package org.fizmo.dropwizard.guice;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Environment;

import java.util.List;

public class ConfiguredGuiceBundle<T> extends AbstractGuiceBundle implements ConfiguredBundle<T> {

    private final Iterable<ModuleFactory<T>> moduleFactories;

    public ConfiguredGuiceBundle(Iterable<ModuleFactory<T>> moduleFactories)
    {
        this.moduleFactories = moduleFactories;
    }

    public ConfiguredGuiceBundle(Injector injector, Iterable<ModuleFactory<T>> moduleFactories)
    {
        super(injector);
        this.moduleFactories = moduleFactories;
    }

    public ConfiguredGuiceBundle(Stage stage, Iterable<ModuleFactory<T>> moduleFactories)
    {
        super(stage);
        this.moduleFactories = moduleFactories;
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        final List<Module> modules = Lists.newArrayList();
        modules.add(new DropwizardModule(environment));
        for (ModuleFactory<T> f : moduleFactories)
        {
            modules.add(f.create(configuration));
        }

        final Injector injector = createInjector(modules);
        addHealthChecks(environment, injector);
    }

}
