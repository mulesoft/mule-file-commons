/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.util;

import org.mule.extension.file.common.api.exceptions.IllegalPathException;
import org.mule.runtime.api.exception.MuleRuntimeException;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FilenameUtils;

/**
 * Class for creating and handling URIs.
 *
 * @since 1.3.0
 */
public final class UriUtils {

  private UriUtils() {}

  private static String SEPARATOR = "/";

  /**
   * Creates an {@link URI} for a given path.
   *
   * @param path the path to the file or directory.
   * @return a {@link URI} representing the path in the following format (using the unix path separator): "/directory/subdirectory"
   */
  public static URI createUri(String path) {
    return createUri(path, "");
  }

  /**
   * Creates an {@link URI} for a given basePath and a filePath, resolving them in the process. This means that if the filePath
   * is absolute, then a uri representing it is returned. Otherwise, the basePath and filePath are combined together.
   *
   * @param basePath the path to the base directory.
   * @param filePath the path to the file.
   * @return a {@link URI} representing the resolved Path between the two given arguments.
   */
  public static URI createUri(String basePath, String filePath) {
    String fullPath;
    try {
      if (filePath.length() > 0) {
        if (isAbsolute(filePath)) {
          fullPath = filePath;
        } else {
          fullPath = addSeparator(basePath) + filePath;
        }
      } else {
        fullPath = removeSeparator(basePath);
      }
      return new URI(null, null, fullPath, null);
    } catch (URISyntaxException e) {
      throw new IllegalPathException("Cannot convert given path into a valid Uri", e);
    }
  }

  /**
   * Adds a separator at the end of the given path. If the path already ends with the separator, then
   * this method does nothing.
   */
  private static String addSeparator(String path) {
    return (path.endsWith(SEPARATOR) || path.length() == 1) ? path : path + SEPARATOR;
  }

  /**
   * Removes the separator at the end of the given path. If the path does not end with the separator, then
   * this method does nothing.
   */
  private static String removeSeparator(String path) {
    return (!path.endsWith(SEPARATOR) || path.length() == 1) ? path : path.substring(0, path.length() - 1);
  }

  /**
   * @param path the path to check
   * @return a {@link Boolean} indicating if the path is absolute or not.
   */
  private static Boolean isAbsolute(String path) {
    return (path.length() > 0 && path.startsWith(SEPARATOR));
  }

  /**
   * @param uri the uri to normalize.
   * @return a new normalized uri normalized according to {@link URI#normalize()}.
   */
  public static URI normalizeUri(URI uri) {
    URI normalizedUri = uri.normalize();
    String path = normalizedUri.getPath();
    if (path.endsWith(SEPARATOR)) {
      try {
        normalizedUri = new URI(null, null, path.substring(0, path.length() - 1), null).normalize();
      } catch (URISyntaxException e) {
        throw new MuleRuntimeException(e);
      }
    }
    return normalizedUri;
  }

  /**
   * Creates a new uri by trimming the last path fragment from the given uri. Can also be thought as getting the
   * 'parent' uri.
   *
   * @param uri the uri to trim.
   * @return the trimmed or 'parent' uri.
   */
  public static URI trimLastFragment(URI uri) {
    Integer index = uri.getPath().lastIndexOf(SEPARATOR);
    return index != -1 ? createUri(uri.getPath().substring(0, index)) : null;
  }
}
