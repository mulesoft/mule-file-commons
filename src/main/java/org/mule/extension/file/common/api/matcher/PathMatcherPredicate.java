/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.matcher;

import static org.mule.runtime.api.util.Preconditions.checkArgument;
import org.mule.runtime.core.api.util.StringUtils;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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
   * Creates a new instance using the given pattern using the native implementation and file system rules.
   *
   * @param pattern the pattern to be used to test paths.
   */
  public PathMatcherPredicate(String pattern) {
    delegate = getPredicateForFilename(pattern);
  }

  /**
   * Creates a new instance using the given pattern.
   *
   * @param pattern         the pattern to be used to test paths.
   * @param caseSensitive   whether matching is case-sensitive
   * @param fileSeparator   what separates a path components
   */
  public PathMatcherPredicate(String pattern, boolean caseSensitive, String fileSeparator) {
    delegate = getPredicateForFilename(pattern, caseSensitive, fileSeparator, false);
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

  private Predicate<String> getPredicateForFilename(String pattern) {
    return getPredicateForFilename(pattern, true, "ignored", true);
  }

  private Predicate<String> getPredicateForFilename(String pattern, boolean caseSensitive, String fileSeparator,
                                                    boolean nativeMatcher) {
    String regex;
    if (pattern.startsWith(REGEX_PREFIX)) {
      regex = pattern.substring(REGEX_PREFIX.length());
    } else {
      String globPattern;
      if (pattern.startsWith(GLOB_PREFIX)) {
        globPattern = pattern.substring(GLOB_PREFIX.length());
      } else {
        globPattern = pattern;
      }
      if (nativeMatcher)
        return getGlobPredicate(GLOB_PREFIX + globPattern);
      regex = globPatternToRegex(globPattern, fileSeparator);
    }
    return Pattern.compile(regex, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE).asPredicate();
  }

  private Predicate<String> getGlobPredicate(String pattern) {
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
    return path -> matcher.matches(Paths.get(path));
  }

  /** Turns a glob pattern into a regular expression pattern.
   *
   *  The glob pattern syntax supported is the one defined for {@link java.nio.file.FileSystem#getPathMatcher(String)}.
   *
   * @param globPattern the input glob pattern
   * @param fileSeparator what's used to separate path components (i.e. "/" or "\")
   * @return a regular expression
   */
  private static String globPatternToRegex(String globPattern, String fileSeparator) {
    if (fileSeparator.equals("\\"))
      fileSeparator = "\\\\";
    int length = globPattern.length();
    StringBuilder sb = new StringBuilder(length + length / 2).append('^');
    int bracesLevel = 0;
    boolean inBracketExpression = false;
    for (int i = 0; i < length; i++) {
      char ch = globPattern.charAt(i);
      if (inBracketExpression) {
        if (ch == '[')
          sb.append('\\'); // this is so character classes (e.g. [:alnum:] ) don't work
        sb.append(ch);
        if (ch == ']') {
          inBracketExpression = false;
        }
        continue;
      }
      switch (ch) {
        case '\\':
          if (i + 1 < length) {
            char nextCh = globPattern.charAt(++i);
            sb.append(Pattern.quote(String.valueOf(nextCh)));
          }
          break;
        case '?':
          sb.append('.');
          break;
        case '*':
          if (i + 1 < length && globPattern.charAt(i + 1) == '*') {
            sb.append(".*");
            i++;
          } else {
            sb.append("[^").append(fileSeparator).append("]*");
          }
          break;
        case '{':
          if (globPattern.substring(i + 1).indexOf('}') == -1) {
            sb.append("\\{");
          } else {
            sb.append("(");
            bracesLevel++;
          }
          break;
        case '}':
          if (bracesLevel == 0) {
            sb.append(ch);
          } else {
            sb.append(')');
            bracesLevel--;
          }
          break;
        case ',':
          if (bracesLevel == 0)
            sb.append(ch);
          else
            sb.append('|');
          break;
        case '[':
          inBracketExpression = true;
          sb.append(ch);
          if (i + 1 < length && globPattern.charAt(i + 1) == '!') {
            sb.append('^');
            i++;
          }
          break;
        case '.':
        case '(':
        case ')':
        case '|':
          sb.append('\\');
        default:
          sb.append(ch);
      }
    }
    sb.append('$');
    return sb.toString();
  }
}
