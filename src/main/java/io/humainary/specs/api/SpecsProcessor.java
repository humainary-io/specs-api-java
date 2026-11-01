// Copyright (c) 2026 William David Louth

package io.humainary.specs.api;

import io.humainary.specs.api.Specs.SpecDoc;
import io.humainary.specs.api.Specs.SpecRef;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.regex.Pattern;

import static javax.tools.Diagnostic.Kind.ERROR;

/// Checks the rules [Specs] states, at compile time, so that a regression is a build failure rather
/// than something only a later sweep would find.
///
/// It reports, as compile errors:
///
/// - a [SpecRef] with no [SpecDoc] on itself or on any lexically enclosing type;
/// - a [SpecRef] naming no references, or a reference outside the grammar documented on [SpecRef];
/// - a [SpecDoc] URL that is not absolute, or whose path names a common branch (`main`, `master`,
///   `develop`, `trunk`, `HEAD`) where a pinned revision belongs.
///
/// Whether a URL names a branch cannot be decided from the URL alone, so the last check catches the
/// usual mistake rather than proving permanence. Resolving references against the documents
/// themselves is a matter for the project that publishes them.
///
/// It runs when this artifact is on the compiler's annotation processor path.
///
/// @author William David Louth
/// @since 3.10

@SupportedAnnotationTypes ( {
  "io.humainary.specs.api.Specs.SpecDoc",
  "io.humainary.specs.api.Specs.SpecRef"
} )
public final class SpecsProcessor
  extends AbstractProcessor {

  /// The reference grammar documented on [SpecRef].
  private static final Pattern REFERENCE =
    Pattern.compile ( "(?:[a-z][a-z0-9-]*:)?[A-Za-z0-9]+(?:[.-][A-Za-z0-9]+)*" );

  /// A branch name in the path forms the common hosts use for a file at a revision.
  private static final Pattern BRANCH =
    Pattern.compile ( "/(?:blob|tree|raw|src)/(?:main|master|develop|trunk|HEAD)(?:/|$)" );

  /// Whether a [SpecDoc] is on `element` or on any type lexically enclosing it.
  private static boolean resolvable (
    final Element element
  ) {

    for (
      var scope = element;
      scope != null;
      scope = scope.getEnclosingElement ()
    ) {

      if ( scope instanceof TypeElement && scope.getAnnotation ( SpecDoc.class ) != null ) {
        return true;
      }

    }

    return false;

  }

  @Override
  public SourceVersion getSupportedSourceVersion () {

    return
      SourceVersion.latestSupported ();

  }

  @Override
  public boolean process (
    final Set < ? extends TypeElement > annotations,
    final RoundEnvironment round
  ) {

    for ( final var element : round.getElementsAnnotatedWith ( SpecDoc.class ) ) {
      checkDocument ( element, element.getAnnotation ( SpecDoc.class ).value () );
    }

    for ( final var element : round.getElementsAnnotatedWith ( SpecRef.class ) ) {
      checkReferences ( element, element.getAnnotation ( SpecRef.class ).value () );
    }

    // Not claimed: other processors may read the same annotations.
    return false;

  }

  private void checkDocument (
    final Element element,
    final String url
  ) {

    final URI uri;

    try {
      uri = new URI ( url );
    } catch ( final URISyntaxException malformed ) {
      error ( element, "@SpecDoc URL is malformed: " + malformed.getMessage () );
      return;
    }

    if ( !uri.isAbsolute () || uri.getHost () == null ) {
      error ( element, "@SpecDoc URL must be absolute, naming a host: " + url );
    } else if ( BRANCH.matcher ( uri.getPath () ).find () ) {
      error ( element, "@SpecDoc URL names a branch where a pinned revision belongs: " + url );
    }

  }

  private void checkReferences (
    final Element element,
    final String[] references
  ) {

    if ( references.length == 0 ) {
      error ( element, "@SpecRef must name at least one reference" );
    }

    for ( final var reference : references ) {
      if ( !REFERENCE.matcher ( reference ).matches () ) {
        error ( element, "@SpecRef reference \"" + reference + "\" is outside the reference grammar" );
      }
    }

    if ( !resolvable ( element ) ) {
      error ( element, "@SpecRef has no @SpecDoc on itself or on any enclosing type to resolve against" );
    }

  }

  private void error (
    final Element element,
    final String message
  ) {

    processingEnv
      .getMessager ()
      .printMessage ( ERROR, message, element );

  }

}
