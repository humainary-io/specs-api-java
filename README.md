# Humainary Specs API

Specification traceability metadata: two source-retained annotations, the rule connecting them, and
an annotation processor that checks it.

## What it is

A Humainary API is the Java projection of a language-neutral specification. These annotations record
which specification, and which part of it, a declaration answers to:

- **`@SpecDoc`** marks a type and names, by URL, the document that references within that type's
  lexical subtree resolve against.
- **`@SpecRef`** names the content in that document which a declaration realizes, documents, or
  verifies.

```java
@SpecDoc ( "https://example.org/widget-spec/blob/1.4.0/SPEC.md" )
public interface Widgets {

  @SpecRef ( {"3.2", "7.1"} )
  interface Widget { … }

}
```

Both are `SOURCE`-retained and have no runtime effect.

## Why it is its own artifact

The annotations belong to neither of the APIs that use them. Keeping them separate means a
specification-bearing project can record its traceability without depending on an unrelated runtime,
and an API need not know which specifications are layered above it. Consumers depend on this at
`provided` scope: compile-only, nothing on the runtime classpath, not transitive.

## Resolution

A bare identifier resolves against the `@SpecDoc` in scope — the annotated element itself when it is
a type, then each lexically enclosing type outward. There is no fallback: a `@SpecRef` with no
`@SpecDoc` in scope is an incomplete annotation, not a reference to a default.

Resolution is **lexical, not per file**. A file may declare sibling top-level types, and a
`@SpecDoc` on one does not reach the others. One annotated root type per file is the simplest way to
satisfy this, and is recommended.

A namespace-qualified identifier (`registry:locks`, `substrates:10.1`) cites a source other than the
one `@SpecDoc` names. This artifact defines the grammar and the resolution rule only; each
specification defines its own namespace tokens, and pins each to a revision as `@SpecDoc` is pinned.

## Permanence

A `@SpecDoc` URL must address a revision its publisher keeps immutable — a release tag or a commit —
never a branch. A released artifact must never resolve against specification text other than the
text it was written for, and a branch URL silently re-points as that branch advances.

Immutable does not mean everlasting. A publisher may retire a superseded release's tag when it
publishes the next: references pinned to the retired tag then stop resolving, which a reader can
see, rather than resolving to different text, which a reader could not. What keeps a tag immutable
is the publisher never moving it or reusing a retired name; the version control system does not.

## Checking

`SpecsProcessor` reports, as compile errors, a `@SpecRef` with no `@SpecDoc` in scope, an empty or
malformed reference, and a `@SpecDoc` URL that is not absolute or names a common branch. It runs when
this artifact is on the compiler's annotation processor path — with Maven:

```xml
<annotationProcessorPaths>
  <path>
    <groupId>io.humainary.specs</groupId>
    <artifactId>humainary-specs-api</artifactId>
    <version>3.15.1</version>
  </path>
</annotationProcessorPaths>
```

## Coordinates

```xml
<dependency>
  <groupId>io.humainary.specs</groupId>
  <artifactId>humainary-specs-api</artifactId>
  <version>3.15.1</version>
  <scope>provided</scope>
</dependency>
```

Versioned in lockstep with the Humainary workspace revision rather than independently: the
annotations are source-retained and change rarely, and a shared version is one fewer coordinate to
reconcile. The module name is `io.humainary.specs.api`.

## Building

The artifact is not published to a public Maven repository. Build and install it from this
repository before depending on it:

```sh
./mvnw install
```

It requires Java 27, the language level of every Humainary artifact; a consumer compiling with an
older JDK will reject its class files.

## License

Copyright © 2025–2026 William David Louth. Licensed under the Apache License, Version 2.0.
