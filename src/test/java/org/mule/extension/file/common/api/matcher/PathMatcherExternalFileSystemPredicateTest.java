/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.file.common.api.matcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.mule.extension.file.common.api.PredicateType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.PatternSyntaxException;

import org.junit.Test;

/**
 * Path marcher external file system tests
 * 
 * @since 1.4.0
 */
public class PathMatcherExternalFileSystemPredicateTest {

  public int failures;

  @Test
  public void globPattern1() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("*.txt", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertTrue(pmp.test("hello.txt"));
    assertFalse(pmp.test("hello.TXT"));
    assertTrue(pmp.test(".txt"));
    assertFalse(pmp.test("hello.csv"));
    assertFalse(pmp.test("hello@txt"));
  }

  @Test
  public void globPattern2() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("s?t.txt", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertTrue(pmp.test("sat.txt"));
    assertTrue(pmp.test("sit.txt"));
    assertFalse(pmp.test("st.txt"));
  }

  @Test
  public void globPatternWithSpecialCharacters() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("\\()|\\{.txt", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertTrue(pmp.test("()|{.txt"));
    assertFalse(pmp.test("x|{.txt"));
  }

  @Test
  public void globPatternStarsAndPathSeparator() {
    PathMatcherPredicate pmp;

    pmp = new PathMatcherPredicate("*", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertFalse(pmp.test("a/b"));
    assertTrue(pmp.test("a\\b"));

    pmp = new PathMatcherPredicate("**", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertTrue(pmp.test("a/b"));
  }

  @Test
  public void globPatternWithBraces() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("*.{java,clas?}", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertTrue(pmp.test("File.java"));
    assertTrue(pmp.test("File.class"));
    assertTrue(pmp.test("File.clase"));
    assertFalse(pmp.test("File.txt"));
  }

  @Test
  public void globPatternWithEscapes() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("\\{a,b}", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertTrue(pmp.test("{a,b}"));
  }

  @Test
  public void globPatternWithBrackets() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("b[aei{}]d", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertTrue(pmp.test("bad"));
    assertTrue(pmp.test("bed"));
    assertTrue(pmp.test("bid"));
    assertTrue(pmp.test("b{d"));
    assertFalse(pmp.test("b.d"));
  }

  @Test
  public void globPatternWithNegativeBrackets() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("b[!aei]d", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertFalse(pmp.test("bad"));
    assertFalse(pmp.test("bed"));
    assertFalse(pmp.test("bid"));
    assertTrue(pmp.test("b.d"));
  }

  @Test
  public void globPatternWithBracketsRegexQuoting() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("\\Qa", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertTrue(pmp.test("Qa"));
    assertFalse(pmp.test("a"));
  }

  @Test
  public void globPatternWithBracketsAndMisplacedRegex() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("a[[:digit:]]z", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertFalse(pmp.test("a9z"));
    assertTrue(pmp.test("ad]z"));
  }

  @Test
  public void regexPattern1() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("regex:.*\\.txt", PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertTrue(pmp.test("hello.txt"));
    assertTrue(pmp.test(".txt"));
    assertFalse(pmp.test("hello.csv"));
  }

  @Test
  public void caseInsensitiveGlobPattern() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("*.txt", PredicateType.EXTERNAL_FILE_SYSTEM, false);
    assertTrue(pmp.test("hello.txt"));
    assertTrue(pmp.test("hello.TXT"));
  }

  @Test
  public void jdkPathMatcherTests() {
    assertMatch("foo.html", "foo.html");
    assertNotMatch("foo.html", "foo.htm");
    assertNotMatch("foo.html", "bar.html");

    // match zero or more characters
    assertMatch("foo.html", "f*");
    assertMatch("foo.html", "*.html");
    assertMatch("foo.html", "foo.html*");
    assertMatch("foo.html", "*foo.html");
    assertMatch("foo.html", "*foo.html*");
    assertNotMatch("foo.html", "*.htm");
    assertNotMatch("foo.html", "f.*");

    // match one character
    assertMatch("foo.html", "?oo.html");
    assertMatch("foo.html", "??o.html");
    assertMatch("foo.html", "???.html");
    assertMatch("foo.html", "???.htm?");
    assertNotMatch("foo.html", "foo.???");

    // group of subpatterns
    assertMatch("foo.html", "foo{.html,.class}");
    assertMatch("foo.html", "foo.{class,html}");
    assertNotMatch("foo.html", "foo{.htm,.class}");

    // bracket expressions
    assertMatch("foo.html", "[f]oo.html");
    assertMatch("foo.html", "[e-g]oo.html");
    assertMatch("foo.html", "[abcde-g]oo.html");
    assertMatch("foo.html", "[abcdefx-z]oo.html");
    assertMatch("foo.html", "[!a]oo.html");
    assertMatch("foo.html", "[!a-e]oo.html");
    assertMatch("foo-bar", "foo[-a-z]bar"); // match dash
    assertMatch("foo.html", "foo[!-]html"); // match !dash

    // groups of subpattern with bracket expressions
    assertMatch("foo.html", "[f]oo.{[h]tml,class}");
    assertMatch("foo.html", "foo.{[a-z]tml,class}");
    assertMatch("foo.html", "foo.{[!a-e]tml,.class}");

    // assume special characters are allowed in file names
    assertMatch("{foo}.html", "\\{foo*");
    assertMatch("{foo}.html", "*\\}.html");
    assertMatch("[foo].html", "\\[foo*");
    assertMatch("[foo].html", "*\\].html");

    assertBadPattern("foo.html", "*[a--z]"); // bad range
    assertBadPattern("foo.html", "*[a--]"); // bad range
    assertBadPattern("foo.html", "*[a-z"); // missing ]
    assertBadPattern("foo.html", "*{class,java"); // missing }
    assertBadPattern("foo.html", "*.{class,{.java}}"); // nested group
    assertBadPattern("foo.html", "*.html\\"); // nothing to escape

    assertMatch("C:\\foo", "C:\\\\f*");
    // assertMatch("C:\\FOO", "c:\\\\f*");
    assertMatch("C:\\foo\\bar\\gus", "C:\\\\**\\\\gus");
    assertMatch("C:\\foo\\bar\\gus", "C:\\\\**");


    assertMatch("/tmp/foo", "/tmp/*");
    assertMatch("/tmp/foo/bar", "/tmp/**");

    // some special characters not allowed on Windows
    assertMatch("myfile?", "myfile\\?");
    assertMatch("one\\two", "one\\\\two");
    assertMatch("one*two", "one\\*two");


    assertRegExMatch("foo.html", ".*\\.html");
    assertRegExMatch("foo012", "foo\\d+");
    assertRegExMatch("fo o", "fo\\so");
    assertRegExMatch("foo", "\\w+");

    if (failures > 0)
      throw new RuntimeException(failures +
          " sub-test(s) failed - see log for details");
  }

  private void assertBadPattern(String path, String pattern) {
    System.out.format("Compile bad pattern %s\t", pattern);
    try {
      PathMatcherPredicate pmp = new PathMatcherPredicate(pattern, PredicateType.EXTERNAL_FILE_SYSTEM, true);
      pmp.test(path);
      System.out.println("Compiled ==> UNEXPECTED RESULT!");
      failures++;
    } catch (PatternSyntaxException e) {
      System.out.println("Failed to compile ==> OKAY");
    }
  }

  public void assertRegExMatch(String path, String pattern) {
    System.out.format("Test regex pattern: %s", pattern);
    Path file = Paths.get(path);
    PathMatcherPredicate pmp = new PathMatcherPredicate("regex:" + pattern, PredicateType.EXTERNAL_FILE_SYSTEM, true);
    boolean matched = pmp.test(path);
    if (matched) {
      System.out.println(" OKAY");
    } else {
      System.out.println(" ==> UNEXPECTED RESULT!");
      failures++;
    }
  }


  private void assertMatch(final String path, final String pattern) {
    PathMatcherPredicate pmp = new PathMatcherPredicate(pattern, PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertTrue(pmp.test(path));
  }

  private void assertNotMatch(final String path, final String pattern) {
    PathMatcherPredicate pmp = new PathMatcherPredicate(pattern, PredicateType.EXTERNAL_FILE_SYSTEM, true);
    assertFalse(pmp.test(path));
  }

}
