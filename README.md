# Humainary Specs API

Specification traceability metadata: two source-retained annotations, and the rule connecting them.

## What it is

A Humainary API is the Java projection of a language-neutral specification. These annotations record
which specification, and which part of it, a declaration answers to:

- **`@SpecDoc`** marks a type and names, by URL, the document that references within that type's
  lexical subtree resolve against.
- **`@SpecRef`** names the content in that document which a declaration realizes, documents, or
  verifies.

```java
@SpecDoc ( "https://github.com/humainary-io/substrates-api-spec/blob/3.1.2/SPEC.md" )
public interface Substrates {

  @SpecRef ( {"10.4", "16.2"} )
  interface Bank < R > { … }

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
specification defines its own namespace tokens and documents what they address.

## Permanence

A `@SpecDoc` URL must address an immutable revision — a release tag or a commit — never a branch. A
released artifact has to resolve against the specification text it was written for, and a branch URL
silently re-points as that branch advances.

## Coordinates

```xml
<dependency>
  <groupId>io.humainary.specs</groupId>
  <artifactId>humainary-specs-api</artifactId>
  <version>3.1.2</version>
  <scope>provided</scope>
</dependency>
```

Versioned in lockstep with the Humainary workspace revision rather than independently: the
annotations are source-retained and change rarely, and a shared version is one fewer coordinate to
reconcile.

## License

Copyright © 2025–2026 William David Louth. Licensed under the Apache License, Version 2.0.
