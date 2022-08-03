/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extension.file.common.api.util;

import static org.mule.extension.file.common.api.util.UriUtils.createUri;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.junit.Test;

public class UriUtilsTestCase {

  @Test
  public void testNoBasePath() {
    String basePath = "";
    String filePath = "test.txt";
    String result = createUri(basePath, filePath).toString();
    assertThat(result, is(equalTo(filePath)));
  }

  @Test
  public void testWithBasePath() {
    String basePath = "/";
    String filePath = "test.txt";
    String result = createUri(basePath, filePath).toString();
    assertThat(result, is(equalTo(basePath + filePath)));
  }

  @Test
  public void testCurrentBasePath() {
    String basePath = ".";
    String filePath = "test.txt";
    String result = createUri(basePath, filePath).toString();
    assertThat(result, is(equalTo(basePath + File.separator + filePath)));
  }

  @Test
  public void testHomeBasePath() {
    String basePath = "~";
    String filePath = "test.txt";
    String result = createUri(basePath, filePath).toString();
    assertThat(result, is(equalTo(basePath + File.separator + filePath)));
  }

  @Test
  public void testRouteWithBackBasePath() {
    String basePath = "~/test/..";
    String filePath = "test.txt";
    String result = createUri(basePath, filePath).toString();
    assertThat(result, is(equalTo(basePath + File.separator + filePath)));
  }

  @Test
  public void testRouteWithSpecialCharacters() {
    String basePath = "test";
    String filePath = "\'test\'.txt";
    String result = createUri(basePath, filePath).toString();
    assertThat(result, is(equalTo(basePath + File.separator + filePath)));
  }

  @Test
  public void testAbsolutePath() {
    String basePath = "/ignore";
    String filePath = "/test/test.txt";
    String result = createUri(basePath, filePath).toString();
    assertThat(result, is(equalTo(filePath)));
  }
}
