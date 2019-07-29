/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.command;

import static java.lang.String.format;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;
import org.mule.runtime.api.exception.MuleRuntimeException;

import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;

import org.slf4j.Logger;

public abstract class AbstractFileCommand<F extends FileSystem, I> {

  private static final Logger LOGGER = getLogger(FileCommand.class);

  protected final F fileSystem;

  /**
   * Creates a new instance
   *
   * @param fileSystem the {@link FileSystem} on which the operation is performed
   */
  protected AbstractFileCommand(F fileSystem) {
    this.fileSystem = fileSystem;
  }

  /**
   * Returns true if the given {@code path} exists
   *
   * @param path the path to test
   * @return whether the {@code path} exists
   */
  protected abstract boolean exists(I path);

  protected void assureParentFolderExists(I path, boolean createParentFolder) {
    if (exists(path)) {
      return;
    }

    I parentFolder = getParent(path);
    if (!exists(parentFolder)) {
      if (createParentFolder) {
        mkdirs(parentFolder);
      } else {
        throw new IllegalPathException(format("Cannot write to file '%s' because path to it doesn't exist. Consider setting the 'createParentDirectories' attribute to 'true'",
                                              path));
      }
    }
  }

  protected abstract I getParent(I path);

  /**
   * Creates the directory pointed by {@code directoryPath} also creating any missing parent directories
   *
   * @param directoryPath the path to the directory you want to create
   */
  protected void mkdirs(I directoryPath) {
    Lock lock = fileSystem.createMuleLock(format("%s-mkdirs-%s", getClass().getName(), directoryPath));
    lock.lock();
    try {
      // verify no other thread beat us to it
      if (exists(directoryPath)) {
        return;
      }
      doMkDirs(directoryPath);
    } finally {
      lock.unlock();
    }

    LOGGER.debug("Directory '{}' created", directoryPath);
  }

  protected abstract void doMkDirs(I directoryPath);

  /**
   * Returns an absolute path for the given {@code filePath}
   *
   * @param filePath the relative path to a file or directory
   * @return an absolute path
   */
  protected I resolvePath(String filePath) {
    I path = getBasePath(fileSystem);
    if (filePath != null) {
      path = resolvePath(path, filePath);
    }
    return getAbsolutePath(path);
  }

  /**
   * Returns a path to which all non absolute paths are relative to
   *
   * @param fileSystem the file system that we're connecting to
   * @return a not {@code null} path
   */
  protected abstract I getBasePath(FileSystem fileSystem);

  /**
   * Resolve the given basePath against the filePath.
   *
   * <p> If the {@code filePath} parameter is an absolute
   * path then this method trivially returns {@code filePath}. If {@code filePath}
   * is an <i>empty path</i> then this method trivially returns basePath.
   * Otherwise this method considers the basePath to be a directory and resolves
   * the given filePath against the basePath. In the simplest case, the given filePath
   * does not have a root component, in which case this method
   * <em>joins</em> both paths and returns a resulting path
   * that ends with the given filePath. Where the given filePath has
   * a root component then resolution is highly implementation dependent and
   * therefore unspecified.
   *
   * @param basePath the base path considered as a directory
   * @param filePath the path to resolve against the basePath
   *
   * @return  the resulting path
   *
   */
  protected abstract I resolvePath(I basePath, String filePath);

  /**
   * Returns an object representing the absolute path for the given path.
   *
   * <p> If the given path is already absolute then this method simply returns
   * it. Otherwise, this method resolves the path in an implementation dependent
   * manner, typically by resolving the path against a file system default directory.
   * Depending on the implementation, this method may throw an I/O error if the file
   * system is not accessible.
   *
   * @return the absolute path
   */
  protected abstract I getAbsolutePath(I path);

  /**
   * Similar to {@link #resolvePath(String)} only that it throws a {@link IllegalArgumentException} if the
   * given path doesn't exist.
   * <p>
   * The existence of the obtained path is verified by delegating into {@link #exists(I)}
   *
   * @param filePath the path to a file or directory
   * @return an absolute path
   */
  protected I resolveExistingPath(String filePath) {
    I path = resolvePath(filePath);
    if (!exists(path)) {
      throw pathNotFoundException(path);
    }

    return path;
  }

  /**
   * Returns an {@link IllegalPathException} explaining that a
   * {@link FileSystem#read(FileConnectorConfig, String, boolean)} operation was attempted on a {@code path} pointing to
   * a directory
   *
   * @param path the path on which a read was attempted
   * @return {@link IllegalPathException}
   */
  protected IllegalPathException cannotReadDirectoryException(I path) {
    return new IllegalPathException(format("Cannot read path '%s' since it's a directory", pathToString(path)));
  }

  /**
   * Returns a {@link IllegalPathException} explaining that a
   * {@link FileSystem#list(FileConnectorConfig, String, boolean, Predicate)} operation was attempted on a {@code path}
   * pointing to a file.
   *
   * @param path the path on which a list was attempted
   * @return {@link IllegalPathException}
   */
  protected IllegalPathException cannotListFileException(I path) {
    return new IllegalPathException(format("Cannot list path '%s' because it's a file. Only directories can be listed",
                                           pathToString(path)));
  }

  /**
   * Returns a {@link IllegalPathException} explaining that a
   * {@link FileSystem#list(FileConnectorConfig, String, boolean, Predicate)} operation was attempted on a {@code path}
   * pointing to a file.
   *
   * @param path the on which a list was attempted
   * @return {@link RuntimeException}
   */
  protected IllegalPathException pathNotFoundException(I path) {
    return new IllegalPathException(format("Path '%s' doesn't exist", pathToString(path)));
  }

  /**
   * Returns a {@link IllegalPathException} explaining that an operation is trying to write to the given {@code path} but it
   * already exists and no overwrite instruction was provided.
   *
   * @param path the that the operation tried to modify
   * @return {@link IllegalPathException}
   */
  public FileAlreadyExistsException alreadyExistsException(I path) {
    return new FileAlreadyExistsException(format("'%s' already exists. Set the 'overwrite' parameter to 'true' to perform the operation anyway",
                                                 pathToString(path)));
  }

  protected abstract String pathToString(I path);

  /**
   * Returns a properly formatted {@link MuleRuntimeException} for the given {@code message} and {@code cause}
   *
   * @param message the exception's message
   * @return a {@link RuntimeException}
   */
  public RuntimeException exception(String message) {
    return new MuleRuntimeException(createStaticMessage(message));
  }

  /**
   * Returns a properly formatted {@link MuleRuntimeException} for the given {@code message} and {@code cause}
   *
   * @param message the exception's message
   * @param cause the exception's cause
   * @return {@link RuntimeException}
   */
  public RuntimeException exception(String message, Exception cause) {
    return new MuleRuntimeException(createStaticMessage(message), cause);
  }

  /**
   * @param fileName the name of a file
   * @return {@code true} if {@code fileName} equals to &quot;.&quot; or &quot;..&quot;
   */
  protected boolean isVirtualDirectory(String fileName) {
    return ".".equals(fileName) || "..".equals(fileName);
  }

}
