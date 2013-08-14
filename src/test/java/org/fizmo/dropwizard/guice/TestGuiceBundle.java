package org.fizmo.dropwizard.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Stage;
import com.google.inject.multibindings.Multibinder;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.core.HealthCheck;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Collections;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestGuiceBundle {

    public static class TestConfig extends Configuration {}


    @Path("/")
    public static class RootResource
    {
        @GET
        public String get() { return "hello, world!"; }
    }

    private class RootResourceModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(RootResource.class);
        }
    }

    private static class TestHealthCheck extends HealthCheck {

        private TestHealthCheck() {
            super("test");
        }

        @Override
        protected Result check() throws Exception {
            return Result.healthy();
        }
    }

    private class HealthCheckModule extends AbstractModule {

        @Override
        protected void configure() {
            final Multibinder<HealthCheck> multibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
            multibinder.addBinding().to(TestHealthCheck.class);
        }
    }

    @Test
    public void TestSimpleBundle() throws Exception {
        ConfiguredBundle<TestConfig> bundle = new GuiceBundle.Builder().withModules(new RootResourceModule()).build();
        runContainer(bundle);
    }

    @Test
    public void TestParentedBundle() throws Exception {
        ConfiguredBundle<TestConfig> bundle = new GuiceBundle.Builder().withParent(Guice.createInjector(new RootResourceModule())).build();
        runContainer(bundle);
    }

    @Test
    public void TestStagedBundle() throws Exception {
        ConfiguredBundle<TestConfig> bundle = new GuiceBundle.Builder().withStage(Stage.TOOL).withModules(new RootResourceModule()).build();
        runContainer(bundle);
    }

    @Test
    public void TestHealthChecks() throws Exception {
        ConfiguredBundle<TestConfig> bundle = new GuiceBundle.Builder()
                .withModules(new RootResourceModule(), new HealthCheckModule()).build();

        Environment environment = mock(Environment.class);

        runContainer(bundle, environment);

        ArgumentCaptor<HealthCheck> captor = ArgumentCaptor.forClass(HealthCheck.class);
        verify(environment).addHealthCheck(captor.capture());

        assertTrue(captor.getValue() instanceof TestHealthCheck);
    }

    private void runContainer(ConfiguredBundle<TestConfig> bundle) throws Exception {
        Environment environment = mock(Environment.class);
        runContainer(bundle, environment);
    }

    // TODO. Verify some state in the modules.
    private void runContainer(ConfiguredBundle<TestConfig> bundle, Environment environment) throws Exception {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        when(environment.getJerseyResourceConfig()).thenReturn(resourceConfig);

        bundle.run(new TestConfig(), environment);

        ArgumentCaptor<GuiceContainer> captor = ArgumentCaptor.forClass(GuiceContainer.class);
        verify(environment).setJerseyServletContainer(captor.capture());

        GuiceContainer container = captor.getValue();
        ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameterNames()).thenReturn(Collections.enumeration(Collections.<String>emptyList()));
        ServletContext context = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);

        container.init(config);
    }

}
