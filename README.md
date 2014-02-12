# fimpl API

By @toxbee

fimpl - short for __F__ind __I__mplementation for the JVM is a unified
way of providing and finding concrete implementations for an "interface".

It is divided into 2 primary pieces - one which automatically writes data to
locations like `META-INF/services`: __fimpl-annotate__.
The other important piece is __fimpl-api__ which is the querying tool.

## Richer meta-data

An important difference compared to other tools such as ServiceLoader
is that the meta-data fimpl can read and provide is richer if wanted.

In addition to the implementing class, you can query and provide:

- the priority, `[max(int), min(int)]`, of the implementation in the chain.
  0 by default - for same priority no particular order applies.
- a specified type (String) of some kind, not unique.
- extras - an object which may contain anything - for extensibility.

## Highly customizable

Most of the core parts of the library consists of interfaces.
It's up to you where the meta-data for the "interfaces" resides,
and how to read them. But there are standard implementations for everything
provided that should work well for most users.

## Usage

Assume you have an interface:
```java
public interface ImageViewer { ... }
```

### Generating meta-data

You can generate meta-data for an implementation for this class:
```java
@ProvidedImplementation(priority = Integer.MAX_VALUE, type = "image/png")
public class PngViewer implements ImageViewer { ... }
```

When you directly extend or implement more than 1 interface/class,
you must explicitly declare which class you are implementing:

```java
@ProvidedImplementation(of = ImageViewer.class, priority = Integer.MAX_VALUE, type = "image/png")
public class PngViewer extends BasePngViewer implements ImageViewer { ... }
```

Using the `ProvidedImplementationProcessor` you can automatically generate the meta-data.
If you use gradle, this is done when you assemble your project.

### Finding implementations

If you use the __fimpl-metainf__ bindings for __fimpl-api__ you can retrieve the
first implementation that honors `type = "image/png"`:

```java
ImplementationFactory factory = new ImplementationFactoryImpl(new MetainfReader());
ImplementationFinder finder = new ImplementationFinder(factory);
ImplementationResultSet.Impl<ImageViewer> found = finder.find( ImageViewer.class );
Class<? extends ImageViewer> first = found.type( "image/png" ).first();
// Do something with clazz.
```

`ImplementationResultSet` uses a fluent interface and provides many neat querying capabilities.

### Installation

With gradle, you can add dependencies like so:

```groovy
repositories {
    maven {
        url 'https://github.com/toxbee/mvn-repo/raw/master/maven-deploy'
    }
    mavenCentral()
}

dependencies {
  compile 'se.toxbee.fimpl:fimpl-annotate:0.1' // for annotations
  compile 'se.toxbee.fimpl:fimpl-metainf:0.1' // for meta-inf implementation of API.
}
```

## Why not [ServiceLoader](http://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html)?

`ServiceLoader` has a few inherent problems, among them being:

1. it's not agnostic as to where the meta-data comes from or how it is formatted.
2. it always creates objects of the implementations.
3. you can only use the default-constructor.

## Changelog

See [CHANGES.md](CHANGES.md).

## Contributing & Issues

If you've found a bug or would like a feature to be added, please make an issue.
We'll be restrictive when it comes to adding new implementations,
you can always create a new repository / project that depends on this one.

If you want to contribute, fork the project and make a pull-request.

## License

Licensed under the Apache License, Version 2.0

See [LICENSE.md](LICENSE.md) for details.

Copyright 2014 toxbee.se
