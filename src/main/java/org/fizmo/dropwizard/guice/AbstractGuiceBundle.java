package org.fizmo.dropwizard.guice;

import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.core.HealthCheck;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class AbstractGuiceBundle {

    private static final Key<Set<HealthCheck>> HEALTH_CHECKS_KEY = Key.get(new TypeLiteral<Set<HealthCheck>>() {});

    private final Optional<Stage> stageOptional;
    private final Optional<Injector> injectorOptional;

    protected AbstractGuiceBundle()
    {
        this(Optional.<Stage>absent(), Optional.<Injector>absent());
    }

    protected AbstractGuiceBundle(final Stage stage)
    {
        this(Optional.of(checkNotNull(stage)), Optional.<Injector>absent());
    }

    protected AbstractGuiceBundle(final Injector injector)
    {
        this(Optional.<Stage>absent(), Optional.of(checkNotNull(injector)));
    }

    private AbstractGuiceBundle(final Optional<Stage> stageOptional, final Optional<Injector> injectorOptional)
    {
        this.stageOptional = stageOptional;
        this.injectorOptional = injectorOptional;
    }

    protected Injector createInjector(final Iterable<Module> modules)
    {
        if (injectorOptional.isPresent()) {
            return injectorOptional.get().createChildInjector(modules);
        }

        if (stageOptional.isPresent()) {
            return Guice.createInjector(stageOptional.get(), modules);
        }

        return Guice.createInjector(modules);
    }

    protected void addHealthChecks(final Environment environment, final Injector injector) {
        if (!injector.findBindingsByType(HEALTH_CHECKS_KEY.getTypeLiteral()).isEmpty()) {
            final Set<HealthCheck> healthChecks = injector.getInstance(HEALTH_CHECKS_KEY);
            for (HealthCheck hc : healthChecks) {
                environment.addHealthCheck(hc);
            }
        }
    }

    /**
     * Initializes the service bootstrap. This method supports the corresponding methods in
     * {@link com.yammer.dropwizard.Bundle} and {@link com.yammer.dropwizard.ConfiguredBundle},
     * though this class implements neither; it exists so that the concrete bundle classes
     * need not implement it for their respsective interfaces.
     *
     * @param bootstrap the service bootstrap
     */
    public void initialize(Bootstrap<?> bootstrap) {

    }

}