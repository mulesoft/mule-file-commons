/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extension.file.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.extension.file.common.api.matcher.MatchPolicy.REQUIRE;
import static org.mule.extension.file.common.api.matcher.MatchPolicy.EXCLUDE;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.matcher.FileMatcher;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.PatternSyntaxException;

@SmallTest
public class FileMatcherContractTestCase<T extends FileMatcher, A extends FileAttributes>
    extends AbstractMuleTestCase {

  private static final String FILENAME = "Mule.java";
  private static final String PATH = "a/b/c/" + FILENAME;
  private static final long SIZE = 1024;

  protected T builder = createPredicateBuilder();
  protected A attributes;

  protected int failures;

  @Rule
  public ExpectedException expectedException = none();

  @Before
  public void before() {
    attributes = mock(getFileAttributesClass());
    when(attributes.getName()).thenReturn(FILENAME);
    when(attributes.getPath()).thenReturn(PATH);
    when(attributes.getSize()).thenReturn(SIZE);
    when(attributes.isRegularFile()).thenReturn(true);
    when(attributes.isSymbolicLink()).thenReturn(false);
    when(attributes.isDirectory()).thenReturn(false);
  }

  private class TestFileMatcher extends FileMatcher {

  }

  protected T createPredicateBuilder() {
    return (T) new TestFileMatcher();
  }

  protected Class<A> getFileAttributesClass() {
    return (Class<A>) FileAttributes.class;
  }

  @Test
  public void matchesAll() {
    builder.setFilenamePattern("glob:*.{java, js}")
        .setPathPattern("glob:**.{java, js}")
        .setRegularFiles(REQUIRE)
        .setDirectories(EXCLUDE)
        .setSymLinks(EXCLUDE)
        .setMinSize(1L)
        .setMaxSize(1024L);

    assertMatch();
  }

  @Test
  public void matchesManyButFailsOne() {
    matchesAll();
    builder.setMaxSize(1L);

    assertReject();
  }

  @Test
  public void matchFilenameLiterally() {
    builder.setFilenamePattern(FILENAME);
    assertMatch();
  }

  @Test
  public void rejectFilenameLiterally() {
    builder.setFilenamePattern("fail.pdf");
    assertReject();
  }

  @Test
  public void matchFilenameByGlob() {
    builder.setFilenamePattern("glob:*.{java, js}");
    assertMatch();
  }

  @Test
  public void rejectFilenameByGlob() {
    builder.setFilenamePattern("glob:*.{pdf}");
    assertReject();
  }

  @Test
  public void matchFilenameByRegex() {
    when(attributes.getName()).thenReturn("20060101_test.csv");
    builder.setFilenamePattern("regex:[0-9]*_test.csv");
    assertMatch();
  }

  @Test
  public void rejectFilenameByRegex() {
    when(attributes.getName()).thenReturn("20060101_TEST.csv");
    builder.setFilenamePattern("regex:[0-9]*_test.csv");
    assertReject();
  }

  @Test
  public void matchPathLiterally() {
    builder.setPathPattern(PATH);
    assertMatch();
  }

  @Test
  public void rejectPathLiterally() {
    builder.setPathPattern("a/b/d/Mule.pdf");
    assertReject();
  }

  @Test
  public void matchPathByGlob() {
    builder.setPathPattern("glob:**.{java, js}");
    assertMatch();
  }

  @Test
  public void rejectPathByGlob() {
    builder.setPathPattern("glob:*.{java, js}");
    assertReject();
  }

  @Test
  public void matchPathByRegex() {
    when(attributes.getPath()).thenReturn("a/b/c/20060101_test.csv");
    builder.setPathPattern("regex:a/b/c/[0-9]*_test.csv");
    assertMatch();
  }

  @Test
  public void correctInputs() {
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
    assertMatch("foo-bar", "foo[-a-z]bar");     // match dash
    assertMatch("foo.html", "foo[!-]html");     // match !dash

    // groups of subpattern with bracket expressions
    assertMatch("foo.html", "[f]oo.{[h]tml,class}");
    assertMatch("foo.html", "foo.{[a-z]tml,class}");
    assertMatch("foo.html", "foo.{[!a-e]tml,.class}");

    // assume special characters are allowed in file names
    assertMatch("{foo}.html", "\\{foo*");
    assertMatch("{foo}.html", "*\\}.html");
    assertMatch("[foo].html", "\\[foo*");
    assertMatch("[foo].html", "*\\].html");

    //Windows paths
    assertMatch("C:\\foo", "C:\\\\f*");
    //assertMatch("C:\\FOO", "c:\\\\f*");
    assertMatch("C:\\foo\\bar\\gus", "C:\\\\**\\\\gus");
    assertMatch("C:\\foo\\bar\\gus", "C:\\\\**");

    assertMatch("/tmp/foo", "/tmp/*");
    assertMatch("/tmp/foo/bar", "/tmp/**");

    // some special characters not allowed on Windows
    assertMatch("myfile?", "myfile\\?");
    assertMatch("one\\two", "one\\\\two");
    assertMatch("one*two", "one\\*two");

    assertBadPattern("foo.html", "*[a--z]");            // bad range
    assertBadPattern("foo.html", "*[a--]");             // bad range
    assertBadPattern("foo.html", "*[a-z");              // missing ]
    assertBadPattern("foo.html", "*{class,java");       // missing }
    assertBadPattern("foo.html", "*.{class,{.java}}");  // nested group
    assertBadPattern("foo.html", "*.html\\");           // nothing to escape

    assertRegExMatch("foo.html", ".*\\.html");
    assertRegExMatch("foo012", "foo\\d+");
    assertRegExMatch("fo o", "fo\\so");
    assertRegExMatch("foo", "\\w+");

    try {
      System.out.format("Test unknown syntax");
      FileSystems.getDefault().getPathMatcher("grep:foo");
      System.out.println(" ==> NOT EXPECTED TO COMPILE");
      failures++;
    } catch (UnsupportedOperationException e) {
      System.out.println(" OKAY");
    }

    if (failures > 0)
      throw new RuntimeException(failures +
              " sub-test(s) failed - see log for details");

  }

  @Test
  public void incorrect(){
    assertBadPattern("foo.html", "*{class,java");
  }


  @Test
  public void rejectPathByRegex() {
    when(attributes.getName()).thenReturn("20060101_TEST.csv");
    builder.setFilenamePattern("regex:[0-9]*_test.csv");
    assertReject();
  }

  @Test
  public void minSize() {
    builder.setMinSize(1L);
    assertMatch();
  }

  @Test
  public void maxSize() {
    builder.setMaxSize(1024L);
    assertMatch();
  }

  @Test
  public void rejectMinSize() {
    builder.setMinSize(2048L);
    assertReject();
  }

  @Test
  public void rejectMaxSize() {
    builder.setMaxSize(500L);
    assertReject();
  }

  @Test
  public void regularFile() {
    when(attributes.isRegularFile()).thenReturn(true);
    builder.setRegularFiles(REQUIRE);
    assertMatch();
  }

  @Test
  public void rejectNotRegularFile() {
    when(attributes.isRegularFile()).thenReturn(false);
    builder.setRegularFiles(REQUIRE);
    assertReject();
  }

  @Test
  public void rejectRegularFile() {
    when(attributes.isRegularFile()).thenReturn(true);
    builder.setRegularFiles(EXCLUDE);
    assertReject();
  }

  @Test
  public void isDirectory() {
    when(attributes.isDirectory()).thenReturn(true);
    builder.setDirectories(REQUIRE);
    assertMatch();
  }

  @Test
  public void rejectNotDirectory() {
    when(attributes.isDirectory()).thenReturn(false);
    builder.setDirectories(REQUIRE);
    assertReject();
  }

  @Test
  public void rejectDirectory() {
    when(attributes.isDirectory()).thenReturn(true);
    builder.setDirectories(EXCLUDE);
    assertReject();
  }


  @Test
  public void isSymbolicLink() {
    when(attributes.isSymbolicLink()).thenReturn(true);
    builder.setSymLinks(REQUIRE);
    assertMatch();
  }

  @Test
  public void rejectNotSymbolicLink() {
    when(attributes.isSymbolicLink()).thenReturn(false);
    builder.setSymLinks(REQUIRE);
    assertReject();
  }

  @Test
  public void rejectSymbolicLink() {
    when(attributes.isSymbolicLink()).thenReturn(true);
    builder.setSymLinks(EXCLUDE);
    assertReject();
  }

  @Test
  public void failOnInvalidMinSize() {
    expectedException.expect(IllegalArgumentException.class);
    builder.setMinSize(-1L);
    builder.build();
  }

  @Test
  public void failOnInvalidMaxSize() {
    expectedException.expect(IllegalArgumentException.class);
    builder.setMaxSize(-1L);
    builder.build();
  }

  protected void assertMatch() {
    assertThat(builder.build().test(attributes), is(true));
  }

  protected void assertReject() {
    assertThat(builder.build().test(attributes), is(false));
  }

  public void assertMatch(String path, String pattern) {
    when(attributes.getName()).thenReturn(path);
    builder.setFilenamePattern(pattern);
    builder.setFileSeparator("/");
    assertMatch();
  }

  public void assertNotMatch(String path, String pattern) {
    when(attributes.getName()).thenReturn(path);
    builder.setFilenamePattern(pattern);
    assertReject();
  }

  public void assertBadPattern(String path, String pattern) {
    System.out.format("Compile bad pattern %s\t", pattern);
    try {
      when(attributes.getName()).thenReturn(path);
      builder.setFilenamePattern(pattern);
      builder.setFileSeparator("/");
      assertMatch();
      System.out.println("Compiled ==> UNEXPECTED RESULT!");
      failures++;
    } catch (PatternSyntaxException e) {
      System.out.println("Failed to compile ==> OKAY");
    }
  }

  public void assertRegExMatch(String path, String pattern) {
    System.out.format("Test regex pattern: %s", pattern);
    when(attributes.getName()).thenReturn(path);
    builder.setFilenamePattern("regex:" + pattern);
    assertMatch();
  }
}
