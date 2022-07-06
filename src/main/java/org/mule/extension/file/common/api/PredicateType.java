/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api;

import org.mule.extension.file.common.api.util.UriUtils;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Type of predicate to be used for the file matcher
 *
 * @since 1.4.0
 */
public enum PredicateType {

  /**
   * Custom predicate implementation independent of the file system
   */
  EXTERNAL_FILE_SYSTEM {

    @Override
    public Predicate<String> getPredicate(final String pattern, final boolean caseSensitive) {
      final String regex = UriUtils.toRegexPattern(getPattern(pattern));
      return Pattern.compile(regex, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE).asPredicate();
    }
  },
  /**
   * Default predicate using path marcher provided by FileSystem class
   */
  LOCAL_FILE_SYSTEM {

    @Override
    public Predicate<String> getPredicate(final String pattern, final boolean caseSensitive) {
      PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
      return path -> matcher.matches(Paths.get(path));
    }
  };

  /**
   * @param pattern glob pattern to be converted to predicate
   * @param caseSensitive if the predicate type is EXTERNAL_FILE_SYSTEM set up if it's case-insensitive or case-sensitive
   * @return {@link Predicate} predicate to match the files
   */
  public abstract Predicate<String> getPredicate(final String pattern, final boolean caseSensitive);

  private static String getPattern(final String syntaxAndInput) {
    int pos = syntaxAndInput.indexOf(':');
    return syntaxAndInput.substring(pos + 1);
  }
}
