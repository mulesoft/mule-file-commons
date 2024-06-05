/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.mule.ftp.engine.api.AbstractConnectedFileInputStreamSupplier;
import org.mule.ftp.engine.api.FileAttributes;
import org.mule.ftp.engine.api.lock.UriLock;
import org.mule.ftp.engine.api.stream.AbstractNonFinalizableFileInputStream;
import org.mule.ftp.engine.api.stream.LazyStreamSupplier;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.connector.ConnectionManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * An {@link AbstractNonFinalizableFileInputStream} implementation which obtains a {@link FtpFileSystem} through a
 * {@link ConnectionManager} and uses it to obtain the contents of a file on a FTP server.
 * <p>
 * When the stream is closed or fully consumed, the {@link FtpFileSystem} is released back to the {@link ConnectionManager}
 *
 * @since 1.0
 */
public abstract class FtpInputStream extends AbstractNonFinalizableFileInputStream {

  protected FtpFileInputStreamSupplier ftpFileInputStreamSupplier;

  protected static ConnectionManager getConnectionManager(FtpConnectorConfig config) throws ConnectionException {
    return config.getConnectionManager();
  }

  protected FtpInputStream(FtpFileInputStreamSupplier ftpFileInputStreamSupplier, UriLock lock) throws ConnectionException {
    super(new LazyStreamSupplier(ftpFileInputStreamSupplier), lock);
    this.ftpFileInputStreamSupplier = ftpFileInputStreamSupplier;
  }

  @Override
  protected void doClose() throws IOException {
    try {
      super.doClose();
    } finally {
      try {
        beforeConnectionRelease();
      } finally {
        ftpFileInputStreamSupplier.releaseConnectionUsedForContentInputStream();
      }
    }
  }

  /**
   * Template method for performing operations just after the stream is closed but before the connection is released. This default
   * implementation is empty.
   *
   * @throws IOException
   */
  protected void beforeConnectionRelease() throws IOException {}

  /**
   * @return {@link Optional} of the {@link FtpFileSystem} used to obtain the stream
   */
  protected Optional<FtpFileSystem> getFtpFileSystem() {
    try {
      return ftpFileInputStreamSupplier.getConnectionUsedForContentInputStream();
    } catch (ConnectionException e) {
      return empty();
    }
  }

  public static class FtpFileInputStreamSupplier extends AbstractConnectedFileInputStreamSupplier<FtpFileSystem> {

    FtpFileInputStreamSupplier(FtpFileAttributes attributes, ConnectionManager connectionManager,
                               Long timeBetweenSizeCheck, FtpConnectorConfig config) {
      super(attributes, connectionManager, timeBetweenSizeCheck, config);
    }

    FtpFileInputStreamSupplier(FtpFileAttributes attributes, Long timeBetweenSizeCheck, FtpFileSystem fileSystem) {
      super(attributes, timeBetweenSizeCheck, fileSystem);
    }

    @Override
    protected FileAttributes getUpdatedAttributes(FtpFileSystem fileSystem) {
      return fileSystem.getFileAttributes(attributes.getPath());
    }

    @Override
    protected InputStream getContentInputStream(FtpFileSystem fileSystem) {
      return fileSystem.retrieveFileContent(attributes);
    }

    @Override
    protected boolean fileWasDeleted(MuleRuntimeException e) {
      return e.getCause() instanceof FileNotFoundException;
    }
  }
}
