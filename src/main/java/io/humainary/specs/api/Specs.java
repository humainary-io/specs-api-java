// Copyright (c) 2025 William David Louth

package io.humainary.specs.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/// Specification traceability metadata, shared by every Humainary API that is the projection of a
/// language-neutral specification.
///
/// Two annotations, and a rule connecting them. [SpecDoc] marks a type, and names the document that
/// references within that type's lexical subtree resolve against; [SpecRef] names the content within
/// that document which a declaration realizes, documents, or verifies. Neither has any runtime
/// effect.
///
/// They live in their own artifact rather than in any one API because they belong to none of them.
/// A specification-bearing project should be able to record its traceability without taking a
/// dependency on an unrelated runtime, and the API a projection implements should not have to know
/// which other specifications are layered above it.
///
/// ## Versioning
///
/// This artifact releases in lockstep with the Humainary workspace revision, and carries the same
/// version as the APIs that depend on it. It is not independently versioned: the annotations are
/// source-retained and change rarely, and a shared version is one fewer coordinate for a consumer
/// to reconcile.
///
/// @author William David Louth
/// @since 3.0

public interface Specs {

  /// Names the specification document against which the bare identifiers in nested [SpecRef]
  /// annotations are resolved.
  ///
  /// This is a **resolution base and nothing more**. It asserts no conformance and makes no claim
  /// that the annotated type realizes, or is governed by, anything in the named document — a type
  /// carrying it may be a pure projection affordance with no normative content at all. Only a
  /// [SpecRef] claims coverage; this annotation merely says which document that claim is read
  /// against.
  ///
  /// Declaring it once on a type serves every declaration in that type's lexical subtree, so no
  /// individual reference has to name its document. Granularity is per type rather than per module
  /// deliberately: one module's types may resolve against different documents — a vocabulary whose
  /// membership is fixed by a registry, next to a type whose contract is fixed by the specification
  /// proper — and only the type knows which applies.
  ///
  /// **Resolution is lexical, not per file.** A bare identifier on an element resolves against the
  /// first [SpecDoc] found by examining, in order: the element itself when it is a type, then each
  /// lexically enclosing type from innermost to outermost. A reference placed on the same type that
  /// carries the [SpecDoc] — the common case for an interface or a vocabulary class — therefore
  /// resolves against it.
  ///
  /// The invariant is therefore about scope rather than about files: every [SpecRef] MUST sit on,
  /// or be lexically enclosed by, a type carrying a [SpecDoc]. A file may declare sibling top-level
  /// types, and a [SpecDoc] on one of them does not reach the others. Declaring one annotated root
  /// type per file is the simplest way to satisfy this, and is RECOMMENDED.
  ///
  /// A reference with no [SpecDoc] in scope is an incomplete annotation, not a reference to some
  /// default.
  ///
  /// **Permanence.** The URL MUST address an immutable revision — a release tag or a commit — and
  /// never a branch. A released artifact has to resolve against the specification text it was
  /// written for, and a branch URL silently re-points as that branch advances.
  ///
  /// Which of the two to prefer is a project's own convention, not a rule here. A release tag says
  /// which release the reference was written against, where a commit says only that the text has
  /// not moved; a project that publishes releases will usually want the former.

  @Documented
  @Retention ( SOURCE )
  @Target ( TYPE )
  @interface SpecDoc {

    /// URL of the document, pinned to an immutable revision.
    ///
    /// @return an absolute URL naming the document, not a repository or a directory

    String value ();

  }


  /// Identifies normative specification content realized, documented, or verified by the annotated
  /// element.
  ///
  /// ## Reference grammar
  ///
  /// A reference is an identifier, optionally qualified by a namespace:
  ///
  /// ```text
  /// reference ::= [ namespace ":" ] identifier
  /// namespace ::= <token naming a normative source, defined by the governing specification>
  /// ```
  ///
  /// A namespace token may name another specification, or a companion normative document belonging
  /// to the governing one — a registry of vocabularies, a schedule of error codes. The governing
  /// specification defines its own tokens and says what each addresses.
  ///
  /// An **unqualified** identifier names content in the document resolved through [SpecDoc]. There
  /// is no fallback: a reference whose resolution finds no [SpecDoc] is incomplete, because the
  /// document it refers to would otherwise depend on where the declaration happened to sit.
  ///
  /// This is deliberate. The governing document is a property of the enclosing type, not of each
  /// reference, so naming it once removes ceremony from every annotation without losing anything;
  /// nearly all references address the document that [SpecDoc] names.
  ///
  /// A **namespace-qualified** identifier is the exception, for citing a source other than the one
  /// [SpecDoc] names — a companion document of the same specification, or a different specification
  /// such as the one a layered contract builds on. This annotation defines the grammar and the
  /// resolution rule only; it does not enumerate namespace tokens, since that is the business of the
  /// specifications that define them.
  ///
  /// Identifiers address whatever a specification defines as addressable — typically its numbered
  /// sections and subsections. They do not address positions within a numbered list, which are not
  /// stable across revisions: annotate the containing section and identify the item in prose.

  @Documented
  @Retention ( SOURCE )
  @Target ( {TYPE, METHOD, CONSTRUCTOR, FIELD} )
  @interface SpecRef {

    /// Normative specification references, in the grammar documented on this annotation.
    ///
    /// @return one or more references, each an optionally namespace-qualified identifier

    String[] value ();

  }

}
