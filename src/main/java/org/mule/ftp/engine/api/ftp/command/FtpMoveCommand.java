/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp.command;

import org.apache.commons.net.ftp.FTPClient;
import org.mule.ftp.engine.api.FileConnectorConfig;
import org.mule.ftp.engine.api.command.MoveCommand;
import org.mule.ftp.engine.api.ftp.FtpFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link FtpCommand} which implements the {@link MoveCommand} contract
 *
 * @since 1.0
 */
public final class FtpMoveCommand extends FtpCommand implements MoveCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(FtpMoveCommand.class);

  /**
   * {@inheritDoc}
   */
  public FtpMoveCommand(FtpFileSystem fileSystem, FTPClient client) {
    super(fileSystem, client);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void move(FileConnectorConfig config, String sourcePath, String targetPath, boolean overwrite,
                   boolean createParentDirectories, String renameTo) {
    copy(config, sourcePath, targetPath, overwrite, createParentDirectories, renameTo, new MoveFtpDelegate(this, fileSystem));
    LOGGER.debug("Moved '{}' to '{}'", sourcePath, targetPath);
  }
}
