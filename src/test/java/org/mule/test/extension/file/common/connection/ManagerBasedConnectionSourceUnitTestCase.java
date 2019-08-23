/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extension.file.common.connection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.extension.file.common.api.connection.ManagerBasedConnectionSource;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandler;
import org.mule.runtime.core.api.connector.ConnectionManager;

import org.junit.Before;
import org.junit.Test;

public class ManagerBasedConnectionSourceUnitTestCase {

  private FileConnectorConfig fileConnectorConfig = mock(FileConnectorConfig.class);

  private ConnectionManager connectionManager = mock(ConnectionManager.class);

  public ConnectionHandler currentConnectionHandler;

  private ManagerBasedConnectionSource managerBasedConnectionSource;

  @Before
  public void doSetup() throws ConnectionException {
    managerBasedConnectionSource = new ManagerBasedConnectionSource(fileConnectorConfig, connectionManager);
    when(connectionManager.getConnection(any())).thenAnswer(invocationOnMock -> {
      currentConnectionHandler = mock(ConnectionHandler.class);
      when(currentConnectionHandler.getConnection()).thenReturn(mock(FileSystem.class));
      return currentConnectionHandler;
    });
  }

  @Test
  public void multipleGetConnectionCalls() throws ConnectionException {
    FileSystem firstFileSystem = managerBasedConnectionSource.getConnection();
    verify(connectionManager, times(1)).getConnection(any());
    FileSystem secondFileSystem = managerBasedConnectionSource.getConnection();
    verify(connectionManager, times(1)).getConnection(any());
    assertThat(firstFileSystem, is(secondFileSystem));
  }

  @Test
  public void connectionsAreReleased() throws ConnectionException {
    managerBasedConnectionSource.getConnection();
    managerBasedConnectionSource.releaseConnection();
    verify(currentConnectionHandler, times(1)).release();
  }

  @Test
  public void afterReleasedAnotherConnectionIsGiven() throws ConnectionException {
    FileSystem firstFileSystem = managerBasedConnectionSource.getConnection();
    managerBasedConnectionSource.releaseConnection();
    FileSystem secondFileSystem = managerBasedConnectionSource.getConnection();
    assertThat(firstFileSystem, is(not(secondFileSystem)));
  }

}
