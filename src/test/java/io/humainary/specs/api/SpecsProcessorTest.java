// Copyright (c) 2026 William David Louth

package io.humainary.specs.api;

import org.junit.jupiter.api.Test;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.net.URI;
import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Compiles small sources with [SpecsProcessor] attached and reads back what it reported.

final class SpecsProcessorTest {

  private static final String DOC =
    "@io.humainary.specs.api.Specs.SpecDoc(\"https://example.org/spec/blob/1.0.0/SPEC.md\")";

  /// The error messages the processor reports for a source named `Sample`.
  private static List < String > errors (
    final String source
  ) {

    final var compiler = ToolProvider.getSystemJavaCompiler ();
    final var diagnostics = new DiagnosticCollector < JavaFileObject > ();

    final var file =
      new SimpleJavaFileObject ( URI.create ( "string:///Sample.java" ), JavaFileObject.Kind.SOURCE ) {
        @Override
        public CharSequence getCharContent ( final boolean ignoreEncodingErrors ) {
          return source;
        }
      };

    final var task =
      compiler.getTask (
        null,
        null,
        diagnostics,
        List.of ( "-proc:only", "-classpath", System.getProperty ( "java.class.path" ) ),
        null,
        List.of ( file )
      );

    task.setProcessors ( List.of ( new SpecsProcessor () ) );
    task.call ();

    return
      diagnostics.getDiagnostics ().stream ()
        .filter ( diagnostic -> diagnostic.getKind () == ERROR )
        .map ( diagnostic -> diagnostic.getMessage ( null ) )
        .toList ();

  }

  private static void assertReported (
    final String source,
    final String fragment
  ) {

    final var errors = errors ( source );

    assertTrue (
      errors.stream ().anyMatch ( message -> message.contains ( fragment ) ),
      () -> "expected an error containing \"" + fragment + "\", got " + errors
    );

  }

  @Test
  void accepted_referencesInScopeOfEveryKindOfElement () {

    assertEquals (
      List.of (),
      errors (
        DOC + " @io.humainary.specs.api.Specs.SpecRef({\"1.2\", \"registry:locks\", \"substrates:10.1\", \"B.4-1\"})"
          + " class Sample {"
          + "   @io.humainary.specs.api.Specs.SpecRef(\"3\") int field;"
          + "   @io.humainary.specs.api.Specs.SpecRef(\"4\") Sample() {}"
          + "   @io.humainary.specs.api.Specs.SpecRef(\"5\") void method(@io.humainary.specs.api.Specs.SpecRef(\"6\") int p) {}"
          + "   @io.humainary.specs.api.Specs.SpecRef(\"7\") static class Nested {"
          + "     @io.humainary.specs.api.Specs.SpecRef(\"8\") void inner() {}"
          + "   }"
          + " }"
      )
    );

  }

  @Test
  void rejected_referenceOnASiblingTypeTheDocumentDoesNotReach () {

    assertReported (
      DOC + " class Sample {}"
        + " class Sibling { @io.humainary.specs.api.Specs.SpecRef(\"1.2\") void m() {} }",
      "no @SpecDoc"
    );

  }

  @Test
  void rejected_emptyReferenceList () {

    assertReported (
      DOC + " @io.humainary.specs.api.Specs.SpecRef({}) class Sample {}",
      "at least one reference"
    );

  }

  @Test
  void rejected_referencesOutsideTheGrammar () {

    for ( final var reference : List.of ( "§6.2", " 6.2", "a:b:c", "Ns:1", "", "6..2", "6.2." ) ) {
      assertReported (
        DOC + " @io.humainary.specs.api.Specs.SpecRef(\"" + reference + "\") class Sample {}",
        "outside the reference grammar"
      );
    }

  }

  @Test
  void rejected_relativeDocumentUrl () {

    assertReported (
      "@io.humainary.specs.api.Specs.SpecDoc(\"SPEC.md\") class Sample {}",
      "must be absolute"
    );

  }

  @Test
  void rejected_documentUrlNamingABranch () {

    for ( final var branch : List.of ( "main", "master", "HEAD" ) ) {
      assertReported (
        "@io.humainary.specs.api.Specs.SpecDoc(\"https://github.com/org/spec/blob/" + branch + "/SPEC.md\")"
          + " class Sample {}",
        "names a branch"
      );
    }

  }

}
