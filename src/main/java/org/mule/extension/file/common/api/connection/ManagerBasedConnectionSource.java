/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.connection;

import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandler;
import org.mule.runtime.core.api.connector.ConnectionManager;

/**
 * Implementation of {@link ConnectionSource} that uses a {@link FileConnectorConfig} and a {@link ConnectionManager} to
 * ask for connections. It is necessary that all instances of this class release all the connections that it asked for.
 */
public class ManagerBasedConnectionSource<T extends FileSystem> implements ConnectionSource<T> {

  private final FileConnectorConfig config;
  private final ConnectionManager connectionManager;
  private ConnectionHandler<T> connectionHandler;
  private T fileSystem;

  public ManagerBasedConnectionSource(FileConnectorConfig config, ConnectionManager connectionManager) {
    this.config = config;
    this.connectionManager = connectionManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T getConnection() throws ConnectionException {
    if (fileSystem == null) {
      connectionHandler = connectionManager.getConnection(config);
      fileSystem = connectionHandler.getConnection();
    }
    return fileSystem;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void releaseConnection() {
    fileSystem = null;
    if (connectionHandler != null) {
      connectionHandler.release();
      connectionHandler = null;
    }
  }
}
