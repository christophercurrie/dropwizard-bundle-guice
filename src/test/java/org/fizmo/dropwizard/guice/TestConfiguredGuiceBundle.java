package org.fizmo.dropwizard.guice;

import com.google.inject.AbstractModule;
import com.yammer.dropwizard.ConfiguredBundle;
import org.junit.Test;

public class TestConfiguredGuiceBundle extends ContainerTestBase {

    public static class RootResourceConfigured extends AbstractModule {

        private final TestConfig config;

        public RootResourceConfigured(TestConfig config) {
            this.config = config;
        }

        @Override
        protected void configure() {
            install(new RootResourceModule());
        }

    }

    @Test
    public void TestConfiguredModule() throws Exception {
        ConfiguredBundle<TestConfig> bundle = new ConfiguredGuiceBundle<TestConfig>(ModuleFactory.Builder.<TestConfig>fromClass(RootResourceConfigured.class));
        runContainer(bundle);
    }

}
