package org.fizmo.dropwizard.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.core.HealthCheck;
import org.junit.After;
import org.mockito.ArgumentCaptor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContainerTestBase {

    protected void runContainer(ConfiguredBundle<TestGuiceBundle.TestConfig> bundle) throws Exception {
        Environment environment = mock(Environment.class);
        runContainer(bundle, environment);
    }

    protected void runContainer(Bundle bundle) throws Exception {
        Environment environment = mock(Environment.class);
        runContainer(bundle, environment);
    }

    // TODO. Verify some state in the modules.
    protected void runContainer(ConfiguredBundle<TestGuiceBundle.TestConfig> bundle, Environment environment) throws Exception {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        when(environment.getJerseyResourceConfig()).thenReturn(resourceConfig);

        bundle.run(new TestGuiceBundle.TestConfig(), environment);

        ArgumentCaptor<GuiceContainer> captor = ArgumentCaptor.forClass(GuiceContainer.class);
        verify(environment).setJerseyServletContainer(captor.capture());

        GuiceContainer container = captor.getValue();
        ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameterNames()).thenReturn(Collections.enumeration(Collections.<String>emptyList()));
        ServletContext context = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);

        container.init(config);
    }

    // TODO. Refactor with prior method
    protected void runContainer(Bundle bundle, Environment environment) throws Exception {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        when(environment.getJerseyResourceConfig()).thenReturn(resourceConfig);

        bundle.run(environment);

        ArgumentCaptor<GuiceContainer> captor = ArgumentCaptor.forClass(GuiceContainer.class);
        verify(environment).setJerseyServletContainer(captor.capture());

        GuiceContainer container = captor.getValue();
        ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameterNames()).thenReturn(Collections.enumeration(Collections.<String>emptyList()));
        ServletContext context = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);

        container.init(config);
    }


    /*
         * Since GuiceFilter has a static pipeline, we need to reset it between tests
         * otherwise it will generate warnings after the first test.
         */
    @After
    public void resetGuiceFilterPipeline() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = GuiceFilter.class.getDeclaredMethod("reset");
        m.setAccessible(true);
        m.invoke(null);
    }

    @Path("/")
    public static class RootResource
    {
        private final Environment env;

        @Inject
        public RootResource(Environment env) {

            this.env = env;
        }

        @GET
        public String get() { return "hello, world!"; }
    }

    protected static class TestHealthCheck extends HealthCheck {

        TestHealthCheck() {
            super("test");
        }

        @Override
        protected Result check() throws Exception {
            return Result.healthy();
        }
    }

    protected static class TestConfig extends Configuration {}

    public static class RootResourceModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(RootResource.class);
        }
    }

    protected class HealthCheckModule extends AbstractModule {

        @Override
        protected void configure() {
            final Multibinder<HealthCheck> multibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
            multibinder.addBinding().to(TestHealthCheck.class);
        }
    }
}
