package org.fizmo.dropwizard.guice;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.core.HealthCheck;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.google.inject.Stage.PRODUCTION;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestGuiceBundle2 extends ContainerTestBase {

    @Test
    public void TestSimpleBundle() throws Exception {
        final Bundle bundle = new GuiceBundle2(new RootResourceModule());
        runContainer(bundle);
    }

    @Test
    public void TestParentedBundle() throws Exception {
        final Module parentModule = mock(Module.class);

        final Bundle bundle = new GuiceBundle2(Guice.createInjector(parentModule), new RootResourceModule());

        verify(parentModule).configure(any(Binder.class));

        runContainer(bundle);
    }

    @Test
    public void TestStagedBundle() throws Exception {
        final Bundle bundle = new GuiceBundle2(PRODUCTION, new RootResourceModule());
        runContainer(bundle);
    }

    @Test
    public void TestHealthChecks() throws Exception {
        final Bundle bundle = new GuiceBundle2(new RootResourceModule(), new HealthCheckModule());

        Environment environment = mock(Environment.class);

        runContainer(bundle, environment);

        ArgumentCaptor<HealthCheck> captor = ArgumentCaptor.forClass(HealthCheck.class);
        verify(environment).addHealthCheck(captor.capture());

        assertTrue(captor.getValue() instanceof TestHealthCheck);
    }

}
