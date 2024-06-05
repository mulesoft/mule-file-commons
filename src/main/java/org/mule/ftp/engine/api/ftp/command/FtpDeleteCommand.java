/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp.command;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.mule.ftp.engine.api.FileAttributes;
import org.mule.ftp.engine.api.command.DeleteCommand;
import org.mule.ftp.engine.api.ftp.FtpFileAttributes;
import org.mule.ftp.engine.api.ftp.FtpFileSystem;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;

import static java.lang.String.format;
import static org.mule.ftp.engine.api.util.UriUtils.createUri;
import static org.mule.ftp.engine.api.ftp.FtpUtils.normalizePath;
import static org.slf4j.LoggerFactory.getLogger;

public final class FtpDeleteCommand extends FtpCommand implements DeleteCommand {

  private static Logger LOGGER = getLogger(FtpDeleteCommand.class);

  /**
   * {@inheritDoc}
   */
  public FtpDeleteCommand(FtpFileSystem fileSystem, FTPClient client) {
    super(fileSystem, client);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(String filePath) {
    FileAttributes fileAttributes = getExistingFile(filePath);
    boolean isDirectory = fileAttributes.isDirectory();
    URI uri = createUri(fileAttributes.getPath());

    if (isDirectory) {
      LOGGER.debug("Preparing to delete directory '{}'", uri.getPath());
      deleteDirectory(uri);
    } else {
      deleteFile(uri);
    }
  }

  private void deleteFile(URI uri) {
    fileSystem.verifyNotLocked(uri);
    try {
      if (!client.deleteFile(normalizePath(uri.getPath()))) {
        throw exception("Could not delete file " + uri.getPath());
      }
    } catch (Exception e) {
      throw exception("Found Exception while deleting directory " + uri.getPath(), e);
    }
    logDelete(uri);
  }

  private void deleteDirectory(URI uri) {
    changeWorkingDirectory(uri.getPath());
    FTPFile[] files;
    try {
      files = client.listFiles();
    } catch (IOException e) {
      throw exception(format("Could not list contents of directory '%s' while trying to delete it", uri.getPath()), e);
    }

    for (FTPFile file : files) {
      if (file != null && isVirtualDirectory(file.getName())) {
        continue;
      }

      FileAttributes fileAttributes = new FtpFileAttributes(createUri(uri.getPath(), file.getName()), file);

      final URI fileUri = createUri(fileAttributes.getPath());
      if (fileAttributes.isDirectory()) {
        deleteDirectory(fileUri);
      } else {
        deleteFile(fileUri);
      }
    }

    boolean removed;
    try {
      client.changeToParentDirectory();
      removed = client.removeDirectory(uri.getPath());
    } catch (IOException e) {
      throw exception("Found exception while trying to remove directory " + uri.getPath(), e);
    }

    if (!removed) {
      throw exception("Could not remove directory " + uri.getPath());
    }

    logDelete(uri);
  }

  private void logDelete(URI uri) {
    LOGGER.debug("Successfully deleted '{}'", uri.getPath());
  }
}
