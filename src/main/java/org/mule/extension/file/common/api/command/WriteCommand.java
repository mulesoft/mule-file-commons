/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.command;

import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.extension.file.common.api.FileWriteMode;

import java.io.InputStream;

/**
 * Command design pattern for writing files
 *
 * @since 1.0
 */
public interface WriteCommand {

  String IS_A_DIRECTORY_MESSAGE = "Is a directory";

  /**
   * @deprecated @{@link #write(String, InputStream, FileWriteMode, boolean, boolean)} must be used instead.
   *
   * Writes a file under the considerations of
   * {@link FileSystem#write(String, InputStream, FileWriteMode, boolean, boolean, String)}
   *
   * @param filePath the path of the file to be written
   * @param content the content to be written into the file
   * @param mode a {@link FileWriteMode}
   * @param lock whether or not to lock the file
   * @param createParentDirectory whether or not to attempt creating the parent directory if it doesn't exist.
   * @param encoding when {@@code content} is a {@link String}, this attribute specifies the encoding to be used when writing. If
   *        not set, then it defaults to {@link FileConnectorConfig#getDefaultWriteEncoding()}
   * @throws IllegalArgumentException if an illegal combination of arguments is supplied
   */
  @Deprecated
  void write(String filePath, InputStream content, FileWriteMode mode, boolean lock, boolean createParentDirectory,
             String encoding);

  /**
   * Writes a file under the considerations of {@link FileSystem#write(String, InputStream, FileWriteMode, boolean, boolean)}
   *
   * @param filePath the path of the file to be written
   * @param content the content to be written into the file
   * @param mode a {@link FileWriteMode}
   * @param lock whether or not to lock the file
   * @param createParentDirectory whether or not to attempt creating the parent directory if it doesn't exist.
   * @throws IllegalArgumentException if an illegal combination of arguments is supplied
   */
  default void write(String filePath, InputStream content, FileWriteMode mode, boolean lock, boolean createParentDirectory) {
    write(filePath, content, mode, lock, createParentDirectory, null);
  }
}
