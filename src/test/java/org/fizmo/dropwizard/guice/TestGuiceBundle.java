package org.fizmo.dropwizard.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.core.HealthCheck;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.google.inject.Stage.PRODUCTION;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestGuiceBundle extends ContainerTestBase {

    protected class RootResourceConfigured extends AbstractModule implements ConfiguredModule<TestConfig> {

        private TestConfig config;

        @Override
        protected void configure() {
            assertTrue(config != null);
            install(new RootResourceModule());
        }

        @Override
        public Module withConfiguration(TestGuiceBundle.TestConfig configuration) {
            config = configuration;
            return this;
        }
    }

    @Test
    public void TestSimpleBundle() throws Exception {
        ConfiguredBundle<TestConfig> bundle = new GuiceBundle.Builder().withModules(new RootResourceModule()).build();
        runContainer(bundle);
    }

    @Test
    public void TestParentedBundle() throws Exception {
        final Module parentModule = mock(Module.class);

        ConfiguredBundle<TestConfig> bundle = new GuiceBundle.Builder()
                .withParent(Guice.createInjector(parentModule))
                .withModules(new RootResourceModule())
                .build();


        verify(parentModule).configure(any(Binder.class));

        runContainer(bundle);
    }

    @Test
    public void TestStagedBundle() throws Exception {
        ConfiguredBundle<TestConfig> bundle = new GuiceBundle.Builder().withStage(PRODUCTION).withModules(new RootResourceModule()).build();
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

    @Test
    public void TestConfiguredModule() throws Exception {
        ConfiguredBundle<TestConfig> bundle = new GuiceBundle.Builder().withModules(new RootResourceConfigured()).build();
        runContainer(bundle);
    }

}
