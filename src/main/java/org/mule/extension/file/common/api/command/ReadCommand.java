/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.command;

import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.extension.api.runtime.operation.Result;
import java.io.InputStream;

/**
 * Command design pattern for reading files
 *
 * @since 1.0
 */
public interface ReadCommand<A extends FileAttributes> {

  /**
   * Reads files under the considerations of {@link FileSystem#read(FileConnectorConfig, String, boolean)}
   *
   * @param config the config that is parameterizing this operation
   * @param filePath the path of the file you want to read
   * @param lock whether or not to lock the file
   * @return An {@link Result} with an {@link InputStream} with the file's content as payload and a
   *         {@link FileAttributes} object as {@link Message#getAttributes()}
   * @throws IllegalArgumentException if the file at the given path doesn't exist
   */
  @Deprecated
  Result<InputStream, A> read(FileConnectorConfig config, String filePath, boolean lock);

  /**
   * Reads files under the considerations of {@link FileSystem#read(FileConnectorConfig, String, boolean)}
   *
   * @param config the config that is parameterizing this operation
   * @param filePath the path of the file you want to read
   * @param lock whether or not to lock the file
   * @param timeBetweenSizeCheck wait time between size checks to determine if a file is ready to be read in milliseconds.
   * @return An {@link Result} with an {@link InputStream} with the file's content as payload and a {@link FileAttributes} object
   *         as {@link Message#getAttributes()}
   * @throws IllegalArgumentException if the file at the given path doesn't exist
   */
  default Result<InputStream, A> read(FileConnectorConfig config, String filePath, boolean lock, Long timeBetweenSizeCheck) {
    return read(config, filePath, lock);
  }

  /**
   * Reads files under the considerations of {@link FileSystem#read(FileConnectorConfig, String, boolean)}
   *
   * @param config the config that is parameterizing this operation
   * @param filePath the path of the file you want to read
   * @param lock whether or not to lock the file.
   * @param timeBetweenSizeCheck wait time between size checks to determine if a file is ready to be read in milliseconds.
   * @param lockTimeout time in nanoseconds that the operation will spend trying to lock the file.
   * @return An {@link Result} with an {@link InputStream} with the file's content as payload and a {@link FileAttributes} object
   *         as {@link Message#getAttributes()}
   * @throws IllegalArgumentException if the file at the given path doesn't exist
   */
  default Result<InputStream, A> read(FileConnectorConfig config, String filePath, boolean lock, Long timeBetweenSizeCheck,
                                      long lockTimeout) {
    return read(config, filePath, lock);
  }

  /**
   * Reads files under the considerations of {@link FileSystem#read(FileConnectorConfig, String, boolean)} This method can be used
   * instead of {@link ReadCommand#read(FileConnectorConfig, String, boolean, Long)} to avoid extra processing to get the file
   * attributes again if that information is already collected (this is important if the attributes are gathered from a remote
   * server).
   *
   * @param config the config that is parameterizing this operation
   * @param attributes the attributes of the file you want to read
   * @param lock whether or not to lock the file
   * @param timeBetweenSizeCheck wait time between size checks to determine if a file is ready to be read in milliseconds.
   * @return An {@link Result} with an {@link InputStream} with the file's content as payload and a {@link FileAttributes} object
   *         as {@link Message#getAttributes()}
   * @throws IllegalArgumentException if the file at the given path doesn't exist
   */
  default Result<InputStream, A> read(FileConnectorConfig config, A attributes, boolean lock, Long timeBetweenSizeCheck) {
    return read(config, attributes.getPath(), lock, timeBetweenSizeCheck);
  }

}
