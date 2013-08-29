package org.fizmo.dropwizard.guice;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.yammer.dropwizard.config.Environment;

class DropwizardModule extends AbstractModule
{
    private final Optional<Object> configuration;
    private final Environment environment;

    public DropwizardModule(Environment environment)
    {
        this(null, environment);
    }

    public DropwizardModule(Object configuration, Environment environment)
    {
        this.configuration = Optional.fromNullable(configuration);
        this.environment = environment;
    }

    @SuppressWarnings("unchecked")
    protected void configure()
    {
        install(new JerseyServletModule());
        bind(Environment.class).toInstance(environment);
        bind(GuiceContainer.class).to(DropwizardGuiceContainer.class).asEagerSingleton();

        if (configuration.isPresent()) {
            final Object config = configuration.get();
            final Class configClass = config.getClass();
            bind(configClass).toInstance(config);
        }
    }

}
