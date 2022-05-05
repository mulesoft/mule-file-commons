/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.matcher;

import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class PathMatcherPredicateTestCase {

  @Test
  public void globPattern1() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("*.txt", true, "/");
    assertTrue(pmp.test("hello.txt"));
    assertFalse(pmp.test("hello.TXT"));
    assertTrue(pmp.test(".txt"));
    assertFalse(pmp.test("hello.csv"));
    assertFalse(pmp.test("hello@txt"));
  }

  @Test
  public void globPattern2() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("s?t.txt", true, "/");
    assertTrue(pmp.test("sat.txt"));
    assertTrue(pmp.test("sit.txt"));
    assertFalse(pmp.test("st.txt"));
  }

  @Test
  public void globPatternWithSpecialCharacters() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("\\()|\\{.txt", true, "/");
    assertTrue(pmp.test("()|{.txt"));
    assertFalse(pmp.test("x|{.txt"));
  }

  @Test
  public void globPatternStarsAndPathSeparator() {
    PathMatcherPredicate pmp;

    pmp = new PathMatcherPredicate("*", true, "/");
    assertFalse(pmp.test("a/b"));
    assertTrue(pmp.test("a\\b"));
    pmp = new PathMatcherPredicate("**", true, "/");
    assertTrue(pmp.test("a/b"));

    pmp = new PathMatcherPredicate("*", true, "\\");
    assertFalse(pmp.test("a\\b"));
    assertTrue(pmp.test("a/b"));
    pmp = new PathMatcherPredicate("**", true, "\\");
    assertTrue(pmp.test("a\\b"));
  }

  @Test
  public void globPatternWithBraces() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("*.{java,clas?}", true, "/");
    assertTrue(pmp.test("File.java"));
    assertTrue(pmp.test("File.class"));
    assertTrue(pmp.test("File.clase"));
    assertFalse(pmp.test("File.txt"));
  }

  @Test
  public void globPatternWithEscapes() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("\\{a,b}", true, "/");
    assertTrue(pmp.test("{a,b}"));
  }

  @Test
  public void globPatternWithBrackets() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("b[aei{}]d", true, "/");
    assertTrue(pmp.test("bad"));
    assertTrue(pmp.test("bed"));
    assertTrue(pmp.test("bid"));
    assertTrue(pmp.test("b{d"));
    assertFalse(pmp.test("b.d"));
  }

  @Test
  public void globPatternWithNegativeBrackets() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("b[!aei]d", true, "/");
    assertFalse(pmp.test("bad"));
    assertFalse(pmp.test("bed"));
    assertFalse(pmp.test("bid"));
    assertTrue(pmp.test("b.d"));
  }

  @Test
  public void globPatternWithBracketsAndEscape() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("b[\\]]d", true, "/");
    assertTrue(pmp.test("b]d"));
    assertFalse(pmp.test("bad"));
  }

  @Test
  public void globPatternWithBracketsRegexQuoting() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("\\Qa", true, "/");
    assertTrue(pmp.test("Qa"));
    assertFalse(pmp.test("a"));
  }

  @Test
  public void globPatternWithBracketsAndMisplacedRegex() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("a[[:digit:]]z", true, "/");
    assertFalse(pmp.test("a9z"));
    assertTrue(pmp.test("ad]z"));
  }

  @Test
  public void regexPattern1() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("regex:.*\\.txt", true, "/");
    assertTrue(pmp.test("hello.txt"));
    assertTrue(pmp.test(".txt"));
    assertFalse(pmp.test("hello.csv"));
  }

  @Test
  public void caseInsensitiveGlobPattern() {
    PathMatcherPredicate pmp = new PathMatcherPredicate("*.txt", false, "/");
    assertTrue(pmp.test("hello.txt"));
    assertTrue(pmp.test("hello.TXT"));
  }

}
