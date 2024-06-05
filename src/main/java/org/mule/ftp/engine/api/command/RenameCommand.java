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
 * Command design pattern for reading files
 *
 * @since 1.0
 */
public interface RenameCommand {

  /**
   * Renames a file under the considerations of {@link FileSystem#rename(FileConnectorConfig, String, String, boolean)}
   *
   * @param filePath the path to the file to be renamed
   * @param newName the file's new name
   * @param overwrite whether to overwrite the target file if it already exists
   */
  void rename(String filePath, String newName, boolean overwrite);
}
