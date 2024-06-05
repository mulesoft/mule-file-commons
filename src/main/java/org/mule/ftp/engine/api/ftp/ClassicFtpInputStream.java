/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.mule.ftp.engine.api.FileAttributes;
import org.mule.ftp.engine.api.lock.UriLock;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.IOException;

/**
 * Implementation of {@link FtpInputStream} for FTP connections
 *
 * @since 1.0
 */
public class ClassicFtpInputStream extends FtpInputStream {

  /**
   * Establishes the underlying connection and returns a new instance of this class.
   * <p>
   * Instances returned by this method <b>MUST</b> be closed or fully consumed.
   *
   * @param config     the {@link FtpConnectorConfig} which is configuring the connection
   * @param attributes a {@link FileAttributes} referencing the file which contents are to be fetched
   * @param lock       the {@link UriLock} to be used
   * @return a new {@link FtpInputStream}
   * @throws ConnectionException if a connection could not be established
   */
  public static FtpInputStream newInstance(FtpConnectorConfig config, FtpFileAttributes attributes, UriLock lock,
                                           Long timeBetweenSizeCheck)
      throws ConnectionException {
    return new ClassicFtpInputStream(new FtpFileInputStreamSupplier(attributes, getConnectionManager(config),
                                                                    timeBetweenSizeCheck,
                                                                    config),
                                     lock);
  }

  /**
   * Using the given connection ,returns a new instance of this class.
   * <p>
   * Instances returned by this method <b>MUST</b> be closed or fully consumed.
   *
   * @param fileSystem            the {@link FtpFileSystem} to be used to connect to the FTP server
   * @param attributes            a {@link FileAttributes} referencing the file which contents are to be fetched
   * @param lock                  the {@link UriLock} to be used
   * @param timeBetweenSizeCheck  the time to be waited between size checks if configured.
   * @return a mew {@link FtpInputStream}
   * @throws ConnectionException
   */
  public static FtpInputStream newInstance(FtpFileSystem fileSystem, FtpFileAttributes attributes, UriLock lock,
                                           Long timeBetweenSizeCheck)
      throws ConnectionException {
    return new ClassicFtpInputStream(new FtpFileInputStreamSupplier(attributes, timeBetweenSizeCheck, fileSystem),
                                     lock);
  }

  public ClassicFtpInputStream(FtpFileInputStreamSupplier ftpFileInputStreamSupplier, UriLock lock)
      throws ConnectionException {
    super(ftpFileInputStreamSupplier, lock);
  }

  /**
   * Invokes {@link FtpFileSystem#awaitCommandCompletion()} to make sure that the operation is completed before closing the
   * stream
   */
  @Override
  protected void beforeConnectionRelease() throws IOException {
    getFtpFileSystem().ifPresent(ftpFileSystem -> ftpFileSystem.awaitCommandCompletion());
  }
}
