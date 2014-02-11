# CHANGES.md

## 0.1.1 -> 0.2.0

### new features

- ImplementationResultSet: added: load(...), loadIterable(...), loadDecendingIterable(...)
- MetainfTransformer: added methods: setPieceSeparators, setLineSeparators.

### bug fixes / minor changes

- MetainfTransformer: fixed \r issue.
- moved Util to common.
- moved IterAdapter out of MetainfLookupProvider.
- improved and implemented missing tests.
- added gradle.properties for builds in parallel with daemon.

## 0.1.0 -> 0.1.1

- changed to maven-publish and gradle-shadow instead of fatJar in build.gradle.
- fixed annotations processor bugs.
- written tests for processor.
- ImplementationInformation.Impl constructors now interpret empty ("" string) extras/types as null
