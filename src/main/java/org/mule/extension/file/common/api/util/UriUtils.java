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

public final class UriUtils {

  public static URI createUri(String path) {
    return createUri(path, "");
  }

  public static URI createUri(String path, String filePath) {
    try {
      if (isAbsolute(filePath)) {
        return new URI(null, null, filePath, null);
      }
      String separator = "/";
      if (!path.endsWith(separator) && filePath.length() > 0) {
        path = path + separator;
      }
      return new URI(null, null, path + filePath, null);
    } catch (URISyntaxException e) {
      throw new IllegalPathException("Cannot convert given path into a valid Uri", e);
    }
  }

  private static Boolean isAbsolute(String path) {
    return (path.length() > 0 && path.startsWith("/"));
  }

  public static URI normalizeUri(URI uri) {
    String path = uri.normalize().getPath();
    if (path.endsWith("/")) {
      try {
        uri = new URI(null, null, path.substring(0, path.length() - 1), null);
      } catch (URISyntaxException e) {
        throw new MuleRuntimeException(e);
      }
    }
    return uri.normalize();
  }

  public static URI trimLastFragment(URI uri) {
    Integer index = uri.getPath().lastIndexOf("/");
    return createUri(uri.getPath().substring(0, index), "");
  }
}
