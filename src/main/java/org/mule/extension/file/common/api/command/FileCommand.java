/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.command;

import static org.slf4j.LoggerFactory.getLogger;
import org.mule.extension.file.common.api.FileSystem;

import java.nio.file.Path;

import org.slf4j.Logger;

/**
 * Base class for implementations of the Command design pattern which performs operations on a file system
 *
 * @param <F> the generic type of the {@link FileSystem} on which the operation is performed
 * @since 1.0
 */
public abstract class FileCommand<F extends FileSystem> extends AbstractFileCommand<F, Path> {

  private static final Logger LOGGER = getLogger(FileCommand.class);

  /**
   * Creates a new instance
   *
   * @param fileSystem the {@link FileSystem} on which the operation is performed
   */
  protected FileCommand(F fileSystem) {
    super(fileSystem);
  }

  protected String pathToString(Path path) {
    return path.toString();
  }

  /**
   * {@inheritDoc}
   *
   * @return an absolute {@link Path}
   */
  protected Path getAbsolutePath(Path path) {
    return path.toAbsolutePath();
  }

  /**
   * {@inheritDoc}
   *
   * @return the parent {@link Path}
   */
  protected Path getParent(Path path) {
    return path.getParent();
  }

  /**
   * {@inheritDoc}
   *
   * @return the resolved {@link Path}
   */
  protected Path resolvePath(Path basePath, String filePath) {
    return basePath.resolve(filePath);
  }

}
