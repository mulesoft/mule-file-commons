/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.command;

import org.mule.ftp.engine.api.FileSystem;

/**
 * Command design pattern for creating directories
 *
 * @since 1.0
 */
public interface CreateDirectoryCommand {

  /**
   * Creates a directory under the considerations of {@link FileSystem#createDirectory(String)}
   *
   * @param directoryName the new directory's new name
   */
  void createDirectory(String directoryName);
}
