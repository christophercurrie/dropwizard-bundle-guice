# Dropwizard Guice Bundle

This is a project for integrating Guice with Dropwizard. There are many like it, but this one is mine.

[![Build Status](https://travis-ci.org/fizmo/dropwizard-bundle-guice.png?branch=master)](https://travis-ci.org/fizmo/dropwizard-bundle-guice)

## Motivations

If you're here, you probably already believe in the advantages of Guice, but there are also some stylistic preferences
that motivated its creation.

### Per-request injection of Resource classes

Dropwizard's documentation demonstrates one style of registering Resources, in the form of singleton instances that are
shared by all threads. This module could support that as well (with `@Singleton` annotations), but our preference is
to rely on Jersey's default behavior of creating a new Resource instance per request. This allows for
constructor-injection of Request parameters, which can keep a class DRY, and helps immutability. It also encourages
factoring state outside of the Resource class, into explicitly designed thread-safe state classes or, ideally, to stable
storage.

### Modularity of concerns

Guice modules can help in exposing abstractions without awkward factory methods. Package-private implementations of
public interfaces can be exposed in a public module, enabling late-binding of implementations based on configuration.
It's likely possible to do similar things using Dropwizard's bundles; we simply prefer Guice's mechanisms.

## Binding Resources and Providers

Binding resources or providers is as simple as declaring the binding in a module:


    @Path("/")
    public class MyResource { /* ... */ }

    public class MyResourceModule extends AbstractModule
    {
        @Override
        public void configure() {
            bind(MyResource.class)
        }
    }

An explicit binding is required to inform Guice of the type. The underlying GuiceContainer enables injection of
JAX-RS specific parameters at construction type, if the default per-request lifecycle is used.

## Binding Health Checks

To allow registration of multiple health classes, the bundle looks for a binding to `Set<HealthCheck>`. This binding
can be populated by hand, but it is intended for use with the
[Guice Multibinding extension](https://code.google.com/p/google-guice/wiki/Multibindings):

    public class MyHealthCheck extends HealthCheck { /* ... */ }

    public class MyHealthCheckModule extends AbstractModule
    {
        @Override
        public void configure() {
            final Multibinder<HealthCheck> multibinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
            multibinder.addBinding().to(MyHealthCheck.class);
        }
    }

## Future Directions

Future releases may support additional Dropwizard primitives such as Tasks or Managed lifecycle objects, as may be
appropriate.

## Alternatives

* [HubSpot](http://dev.hubspot.com) has [their own Guice bundle](https://github.com/HubSpot/dropwizard-guice), which
requires HealthCheck classes to inherit from a custom base class, but additionally supports package scanning for
automatic configuration, which this bundle does not.

* [Jared Stehler](http://mindtap.cengage.com/) has [another bundle](https://github.com/jaredstehler/dropwizard-guice)
which requires Service classes to inherit from a custom base class, but additionally supports adding tasks and managed
lifecycle objects, which this bundle does not.

* Dropwizard does support adding classes instead of instances. In this case, you must use Jersey's built-in dependency
injection, if you want any at all.

* Dropwizard also supports Bundles, such as this one, for modularity of implementations, which could meet some of the
needs of Guice modules.

## Questions

