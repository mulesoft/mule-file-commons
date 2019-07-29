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
import org.mule.extension.file.common.api.FileSystem;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;

import java.net.URI;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;

/**
 * Base class for implementations of the Command design pattern which performs operations on a external file system.
 *
 * @param <F> the generic type of the {@link ExternalFileSystem} on which the operation is performed
 * @since 1.3.0
 */
public abstract class ExternalFileCommand<F extends ExternalFileSystem> extends AbstractFileCommand<F, URI> {

  private static final Logger LOGGER = getLogger(FileCommand.class);

  /**
   * Creates a new instance
   *
   * @param externalFileSystem the {@link ExternalFileSystem} on which the operation is performed
   */
  protected ExternalFileCommand(F externalFileSystem) {
    super(externalFileSystem);
  }

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
   * @param externalFileSystem the file system that we're connecting to
   * @return a not {@code null} {@link URI}
   */
  protected abstract URI getBaseUri(FileSystem externalFileSystem);

  /**
   * Similar to {@link #resolvePathIntoUri(String)} only that it throws a {@link IllegalArgumentException} if the
   * given path doesn't exist.
   * <p>
   * The existence of the obtained path is verified by delegating into {@link #exists(Object)}
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

  protected String pathToString(URI uri) {
    return uri.getPath();
  }

}
