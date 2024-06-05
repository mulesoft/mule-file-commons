/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp.command;

import org.apache.commons.net.ftp.FTPClient;
import org.mule.ftp.engine.api.command.CreateDirectoryCommand;
import org.mule.ftp.engine.api.ftp.FtpFileSystem;

/**
 * A {@link FtpCommand} which implements the {@link CreateDirectoryCommand}
 *
 * @since 1.0
 */
public final class FtpCreateDirectoryCommand extends FtpCommand implements CreateDirectoryCommand {

  /**
   * {@inheritDoc}
   */
  public FtpCreateDirectoryCommand(FtpFileSystem fileSystem, FTPClient client) {
    super(fileSystem, client);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createDirectory(String directoryPath) {
    super.createDirectory(directoryPath);
  }
}
