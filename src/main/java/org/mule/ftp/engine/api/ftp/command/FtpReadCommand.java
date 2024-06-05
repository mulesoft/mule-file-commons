/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp.command;

import org.apache.commons.net.ftp.FTPClient;
import org.mule.ftp.engine.api.FileConnectorConfig;
import org.mule.ftp.engine.api.command.ReadCommand;
import org.mule.ftp.engine.api.ftp.ClassicFtpInputStream;
import org.mule.ftp.engine.api.ftp.FtpConnectorConfig;
import org.mule.ftp.engine.api.lock.NullUriLock;
import org.mule.ftp.engine.api.lock.UriLock;
import org.mule.ftp.engine.api.ftp.FtpFileAttributes;
import org.mule.ftp.engine.api.ftp.FtpFileSystem;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.net.URI;

import static java.lang.String.format;
import static org.mule.ftp.engine.api.util.UriUtils.createUri;
import static org.mule.runtime.core.api.util.IOUtils.closeQuietly;

/**
 * A {@link FtpCommand} which implements the {@link FtpReadCommand}
 *
 * @since 1.0
 */
public final class FtpReadCommand extends FtpCommand implements ReadCommand<FtpFileAttributes> {

  /**
   * {@inheritDoc}
   */
  public FtpReadCommand(FtpFileSystem fileSystem, FTPClient client) {
    super(fileSystem, client);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Result<InputStream, FtpFileAttributes> read(FileConnectorConfig config, String filePath, boolean lock) {
    return read(config, filePath, lock, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Result<InputStream, FtpFileAttributes> read(FileConnectorConfig config, String filePath, boolean lock,
                                                     Long timeBetweenSizeCheck) {
    FtpFileAttributes attributes = getExistingFile(filePath);
    if (attributes.isDirectory()) {
      throw cannotReadDirectoryException(createUri(attributes.getPath()));
    }

    return read(config, attributes, lock, timeBetweenSizeCheck, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Result<InputStream, FtpFileAttributes> read(FileConnectorConfig config, FtpFileAttributes attributes, boolean lock,
                                                     Long timeBetweenSizeCheck) {
    return read(config, attributes, lock, timeBetweenSizeCheck, false);
  }

  private Result<InputStream, FtpFileAttributes> read(FileConnectorConfig config, FtpFileAttributes attributes, boolean lock,
                                                      Long timeBetweenSizeCheck, boolean useCurrentConnection) {
    URI uri = createUri(attributes.getPath());
    UriLock uriLock = lock ? fileSystem.lock(uri) : new NullUriLock(uri);

    InputStream payload = null;
    try {
      payload = getFileInputStream((FtpConnectorConfig) config, attributes, uriLock, timeBetweenSizeCheck, useCurrentConnection);
      return Result.<InputStream, FtpFileAttributes>builder().output(payload)
          .mediaType(fileSystem.getFileMessageMediaType(attributes))
          .attributes(attributes).build();
    } catch (Exception e) {
      uriLock.release();
      closeQuietly(payload);
      throw exception(format("Could not fetch file '%s'. %s", uri.getPath(), e.getMessage()), e);
    }
  }

  private InputStream getFileInputStream(FtpConnectorConfig config, FtpFileAttributes attributes, UriLock uriLock,
                                         Long timeBetweenSizeCheck, boolean useCurrentConnection)
      throws ConnectionException {
    if (useCurrentConnection) {
      return ClassicFtpInputStream.newInstance(fileSystem, attributes, uriLock, timeBetweenSizeCheck);
    } else {
      return ClassicFtpInputStream.newInstance(config, attributes, uriLock, timeBetweenSizeCheck);
    }
  }

}
