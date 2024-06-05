/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.matcher;

import static org.mule.ftp.engine.api.PredicateType.LOCAL_FILE_SYSTEM;
import static org.mule.runtime.api.util.Preconditions.checkArgument;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.mule.ftp.engine.api.PredicateType;
import org.mule.runtime.core.api.util.StringUtils;

/**
 * A {@link Predicate} which tests random paths in {@link String} representation to match a specific pattern.
 * <p>
 * The pattern can be either a regex or glob expression. The pattern can qualify itself by using thee &quot;glob:&quot; or
 * &quot;regex:&quot; prefixes. If no prefix is supplied, glob is assumed by default.
 *
 * @since 1.0
 */
public final class PathMatcherPredicate implements Predicate<String> {

  private static final String GLOB_PREFIX = "glob:";
  private static final String REGEX_PREFIX = "regex:";

  private final Predicate<String> delegate;



  /**
   * Creates a new instance using the given pattern
   *
   * @param pattern the pattern to be used to test paths.
   *
   */

  public PathMatcherPredicate(String pattern) {
    delegate = getPredicateForFilename(pattern, LOCAL_FILE_SYSTEM, FileMatcher.DEFAULT_CASE_SENSITIVE);
  }

  /**
   * Creates a new instance using the given pattern
   *
   * @param pattern       the pattern to be used to test paths.
   * @param predicateType if is gonna a match local file system or a remote file system ex:ftp , sftp
   * @param caseSensitive if the predicateType is a external file system predicate this set the case sensitivity
   */

  public PathMatcherPredicate(String pattern, PredicateType predicateType, final boolean caseSensitive) {
    delegate = getPredicateForFilename(pattern, predicateType, caseSensitive);
  }

  /**
   * @param path the path to test
   * @return whether the given {@code path} matches {@code this} instance pattern
   */
  @Override
  public boolean test(String path) {
    checkArgument(!StringUtils.isBlank(path), "Cannot match a blank filename");
    return delegate.test(path);
  }

  private Predicate<String> getPredicateForFilename(String pattern, PredicateType predicateType, final boolean caseSensitive) {
    if (pattern.startsWith(REGEX_PREFIX)) {
      return Pattern.compile(stripRegexPrefix(pattern)).asPredicate();
    } else if (pattern.startsWith(GLOB_PREFIX)) {
      return predicateType.getPredicate(pattern, caseSensitive);
    } else {
      return predicateType.getPredicate(GLOB_PREFIX + pattern, caseSensitive);
    }
  }

  private String stripRegexPrefix(String pattern) {
    return pattern.replaceAll(REGEX_PREFIX, "");
  }
}
