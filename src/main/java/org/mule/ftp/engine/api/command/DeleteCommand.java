/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.command;

import org.mule.ftp.engine.api.FileConnectorConfig;
import org.mule.ftp.engine.api.FileSystem;

/**
 * Command design pattern for deleting files
 *
 * @since 1.0
 */
public interface DeleteCommand {

  /**
   * Deletes a file under the considerations of {@link FileSystem#delete(FileConnectorConfig, String)}
   *
   * @param filePath the path to the file to be deleted
   * @throws IllegalArgumentException if {@code filePath} doesn't exist or is locked
   */
  void delete(String filePath);
}
