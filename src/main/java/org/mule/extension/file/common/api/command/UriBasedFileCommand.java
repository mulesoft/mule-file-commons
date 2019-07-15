/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.command;

import static java.lang.String.format;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static org.mule.extension.file.common.api.util.UriUtils.trimLastFragment;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.file.common.api.ExternalFileSystem;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;

import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;

import org.slf4j.Logger;

public abstract class UriBasedFileCommand<T extends ExternalFileSystem> extends FileCommand<T> {

  private static final Logger LOGGER = getLogger(FileCommand.class);

  protected final T externalFileSystem;

  /**
   * Creates a new instance
   *
   * @param externalFileSystem the {@link FileSystem} on which the operation is performed
   */
  protected UriBasedFileCommand(T externalFileSystem) {
    super(externalFileSystem);
    this.externalFileSystem = externalFileSystem;
  }

  /**
   * Returns true if the given {@code path} exists
   *
   * @param path the {@link URI} to test
   * @return whether the {@code path} exists
   */
  protected abstract boolean exists(URI path);

  protected void assureParentFolderExists(URI uri, boolean createParentFolder) {
    if (exists(uri)) {
      return;
    }
    URI parentUri = trimLastFragment(uri);
    if (!exists(parentUri)) {
      if (createParentFolder) {
        mkdirs(parentUri);
      } else {
        throw new IllegalPathException(format("Cannot write to file '%s' because path to it doesn't exist. Consider setting the 'createParentDirectories' attribute to 'true'",
                                              uri));
      }
    }
  }

  /**
   * Creates the directory pointed by {@code directoryUri} also creating any missing parent directories
   *
   * @param directoryUri the {@link URI} to the directory you want to create
   */
  protected final void mkdirs(URI directoryUri) {
    Lock lock = fileSystem.createMuleLock(format("%s-mkdirs-%s", getClass().getName(), directoryUri));
    lock.lock();
    try {
      // verify no other thread beat us to it
      if (exists(directoryUri)) {
        return;
      }
      doMkDirs(directoryUri);
    } finally {
      lock.unlock();
    }

    LOGGER.debug("Directory '{}' created", directoryUri);
  }

  protected abstract void doMkDirs(URI directoryUri);

  /**
   * Returns an absolute {@link URI} to the given {@code filePath}
   *
   * @param filePath the path to a file or directory
   * @return an absolute {@link URI}
   */
  protected URI resolvePathIntoUri(String filePath) {
    URI baseUri = getBaseUri(fileSystem);
    if (filePath != null) {
      baseUri = createUri(baseUri.getPath(), filePath);
    }
    return baseUri;
  }

  /**
   * Returns a {@link URI} to which all non absolute paths are relative to
   *
   * @param fileSystem the file system that we're connecting to
   * @return a not {@code null} {@link URI}
   */
  protected abstract URI getBaseUri(FileSystem fileSystem);

  /**
   * Similar to {@link #resolvePathIntoUri(String)} only that it throws a {@link IllegalArgumentException} if the
   * given path doesn't exist.
   * <p>
   * The existence of the obtained path is verified by delegating into {@link #exists(URI)}
   *
   * @param filePath the uri to a file or directory
   * @return an absolute {@link URI}
   */
  protected URI resolveExistingPathIntoUri(String filePath) {
    URI uri = resolvePathIntoUri(filePath);
    if (!exists(uri)) {
      throw pathNotFoundException(uri);
    }
    return uri;
  }

  /**
   * Returns an {@link IllegalPathException} explaining that a
   * {@link FileSystem#read(FileConnectorConfig, String, boolean, Long)} operation was attempted on a {@code path} pointing to
   * a directory
   *
   * @param path the {@link URI} on which a read was attempted
   * @return {@link IllegalPathException}
   */
  protected IllegalPathException cannotReadDirectoryException(URI path) {
    return new IllegalPathException(format("Cannot read path '%s' since it's a directory", path.getPath()));
  }

  /**
   * Returns a {@link IllegalPathException} explaining that a
   * {@link FileSystem#list(FileConnectorConfig, String, boolean, Predicate, Long)} operation was attempted on a {@code path}
   * pointing to a file.
   *
   * @param path the {@link URI} on which a list was attempted
   * @return {@link IllegalPathException}
   */
  protected IllegalPathException cannotListFileException(URI path) {
    return new IllegalPathException(format("Cannot list path '%s' because it's a file. Only directories can be listed",
                                           path.getPath()));
  }

  /**
   * Returns a {@link IllegalPathException} explaining that a
   * {@link FileSystem#list(FileConnectorConfig, String, boolean, Predicate, Long)} operation was attempted on a {@code path}
   * pointing to a file.
   *
   * @param path the {@link URI} on which a list was attempted
   * @return {@link RuntimeException}
   */
  protected IllegalPathException pathNotFoundException(URI path) {
    return new IllegalPathException(format("Path '%s' doesn't exist", path.getPath()));
  }

  /**
   * Returns a {@link IllegalPathException} explaining that an operation is trying to write to the given {@code path} but it
   * already exists and no overwrite instruction was provided.
   *
   * @param path the {@link URI} that the operation tried to modify
   * @return {@link IllegalPathException}
   */
  public FileAlreadyExistsException alreadyExistsException(URI path) {
    return new FileAlreadyExistsException(format("'%s' already exists. Set the 'overwrite' parameter to 'true' to perform the operation anyway",
                                                 path.getPath()));
  }

}
