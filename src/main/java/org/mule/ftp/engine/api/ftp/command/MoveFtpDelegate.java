/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp.command;

import org.mule.ftp.engine.api.FileAttributes;
import org.mule.ftp.engine.api.FileConnectorConfig;
import org.mule.ftp.engine.api.ftp.FtpCopyDelegate;
import org.mule.ftp.engine.api.ftp.FtpFileSystem;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static java.lang.String.format;

public class MoveFtpDelegate implements FtpCopyDelegate {

  private FtpCommand command;
  private FtpFileSystem fileSystem;
  private static final Logger LOGGER = LoggerFactory.getLogger(MoveFtpDelegate.class);

  public MoveFtpDelegate(FtpCommand command, FtpFileSystem fileSystem) {
    this.command = command;
    this.fileSystem = fileSystem;
  }

  @Override
  public void doCopy(FileConnectorConfig config, FileAttributes source, URI targetUri, boolean overwrite) {
    String path = source.getPath();
    try {
      if (command.exists(targetUri)) {
        if (overwrite) {
          fileSystem.delete(targetUri.getPath());
        } else {
          command.alreadyExistsException(targetUri);
        }
      }

      command.rename(path, targetUri.getPath(), overwrite);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Moved file {} to {}", path, targetUri.getPath());
      }
    } catch (ModuleException e) {
      throw e;
    } catch (Exception e) {
      throw command.exception(format("Found exception copying file '%s' to '%s'", path, targetUri.getPath()), e);
    }
  }
}
