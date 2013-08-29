package org.fizmo.dropwizard.guice.service;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.yammer.metrics.core.HealthCheck;
import org.fizmo.dropwizard.guice.service.health.GuiceBundleHealthCheck;
import org.fizmo.dropwizard.guice.service.resources.GuiceBundleResource;

public class GuiceBundleModule extends AbstractModule
{
    @Override
    protected void configure() {
        bind(GuiceBundleResource.class);
        final Multibinder<HealthCheck> multibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
        multibinder.addBinding().to(GuiceBundleHealthCheck.class);
    }
}
