package org.fizmo.dropwizard.guice;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;

import java.util.Arrays;
import java.util.List;

public class GuiceBundle2 extends AbstractGuiceBundle implements Bundle {

    private final Iterable<Module> modules;

    public GuiceBundle2(Module... modules) {
        this(Arrays.asList(modules));
    }

    public GuiceBundle2(Iterable<Module> modules) {
        this.modules = modules;
    }

    public GuiceBundle2(Injector injector, Module... modules) {
        this(injector, Arrays.asList(modules));
    }

    public GuiceBundle2(Injector injector, Iterable<Module> modules) {
        super(injector);
        this.modules = modules;
    }

    public GuiceBundle2(Stage stage, Module... modules) {
        this(stage, Arrays.asList(modules));
    }

    public GuiceBundle2(Stage stage, Iterable<Module> modules) {
        super(stage);
        this.modules = modules;
    }

    @Override
    public void run(Environment environment) {
        final List<Module> modules = Lists.newArrayList();
        modules.add(new DropwizardModule(environment));
        Iterables.addAll(modules, this.modules);

        final Injector injector = createInjector(modules);
        addHealthChecks(environment, injector);
    }
}
