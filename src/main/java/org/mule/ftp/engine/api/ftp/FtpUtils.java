/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Utility class for FTP needs
 *
 * @since 1.0
 */
public class FtpUtils {

  private FtpUtils() {}

  /**
   * @param path to be normalized
   * @return a {@link String} representing the path in the following format (using the unix path separator): "directory/subdirectory"
   */
  public static String normalizePath(String path) {
    path = path.length() > 2 && (path.charAt(1) == ':' || path.charAt(2) == ':') ? path.substring(path.indexOf(':') + 1) : path;
    return FilenameUtils.normalize(path, true);
  }

  public static String getReplyErrorMessage(Integer replyCode, String replyString) {
    return FTPReply.isPositiveCompletion(replyCode) ? ""
        : format("FTP reply code is: %d. FTP reply string is: %s", replyCode, replyString);
  }

  public static String getReplyCodeErrorMessage(Integer replyCode) {
    return FTPReply.isPositiveCompletion(replyCode) ? "" : format("FTP reply code is: %d", replyCode);
  }

  public static URL createUrl(FTPClient client, URI uri) throws MalformedURLException {
    return new URL("ftp", client.getRemoteAddress().getHostAddress(), client.getRemotePort(),
                   uri != null ? uri.getPath() : EMPTY);
  }
}
