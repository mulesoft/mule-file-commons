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

import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;
import org.slf4j.Logger;

/**
 * Extension of {@link AbstractFileCommand} for local file systems which use {@link Path} to identify and manage
 * files and directories.
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

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   *
   * @return whether the {@code Path} exists
   */
  protected abstract boolean exists(Path path);

  /**
   * {@inheritDoc}
   *
   * @return whether the {@code Path} exists
   */
  protected abstract Path getBasePath(FileSystem fileSystem);

  /**
   * {@inheritDoc}
   *
   * @return whether the {@code Path} exists
   */
  protected abstract void doMkDirs(Path directoryPath);

  /**
   * {@inheritDoc}
   *
   * @return whether the {@code Path} exists
   */
  protected void assureParentFolderExists(Path path, boolean createParentFolder) {
    super.assureParentFolderExists(path, createParentFolder);
  }

  /**
   * {@inheritDoc}
   *
   * @return whether the {@code Path} exists
   */
  protected void mkdirs(Path directoryPath) {
    super.mkdirs(directoryPath);
  }

  /**
   * {@inheritDoc}
   *
   * @return whether the {@code Path} exists
   */
  protected Path resolvePath(String filePath) {
    return super.resolvePath(filePath);
  }

  /**
   * {@inheritDoc}
   *
   * @return an absolute {@code Path}
   */
  protected Path resolveExistingPath(String filePath) {
    return super.resolveExistingPath(filePath);
  }

  /**
   * {@inheritDoc}
   *
   * @return {@link IllegalPathException}
   */
  protected IllegalPathException cannotReadDirectoryException(Path path) {
    return super.cannotReadDirectoryException(path);
  }

  /**
   * {@inheritDoc}
   *
   * @return {@link IllegalPathException}
   */
  protected IllegalPathException cannotListFileException(Path path) {
    return super.cannotListFileException(path);
  }

  /**
   * {@inheritDoc}
   *
   * @return {@link IllegalPathException}
   */
  protected IllegalPathException pathNotFoundException(Path path) {
    return super.pathNotFoundException(path);
  }

  /**
   * {@inheritDoc}
   *
   * @return {@link IllegalPathException}
   */
  public FileAlreadyExistsException alreadyExistsException(Path path) {
    return super.alreadyExistsException(path);
  }
}
