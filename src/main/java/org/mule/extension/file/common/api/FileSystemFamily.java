/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api;

import org.apache.commons.io.FilenameUtils;
import org.mule.extension.file.common.api.util.UriUtils;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public enum FileSystemFamily {
  UNIX {
    @Override
    public Predicate<String> getPredicate(final String pattern) {
      final String regex = UriUtils.toUnixRegexPattern(getPattern(pattern));
      return Pattern.compile(regex).asPredicate();
    }
  },
  WINDOWS {
    @Override
    public Predicate<String> getPredicate(final String pattern) {
      final String regex = UriUtils.toWindowsRegexPattern(getPattern(pattern));
      return Pattern.compile(regex).asPredicate();
    }
  },
  DEFAULT {
    @Override
    public Predicate<String> getPredicate(final String pattern) {
      PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
      return path -> matcher.matches(Paths.get(path));
    }
  };

  public abstract Predicate<String> getPredicate(final String pattern);

  private static String getPattern(final String syntaxAndInput) {
    int pos = syntaxAndInput.indexOf(':');
    return syntaxAndInput.substring(pos + 1);
  }
}
