/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api;

import static java.lang.String.format;

import org.mule.extension.file.common.api.exceptions.FileLockedException;
import org.mule.extension.file.common.api.lock.PathLock;
import org.mule.extension.file.common.api.lock.URLPathLock;

import java.net.URI;

/**
 * Interface that allows to obtain a {@link PathLock} on a path given as a {@link URI}.
 *
 * @since 1.2.6
 */
public abstract class ExternalFileSystem extends AbstractFileSystem {

  public ExternalFileSystem(String basePath) {
    super(basePath);
  }

  /**
   * Acquires and returns lock over the given {@code path}.
   * <p>
   * Depending on the underlying filesystem, the extent of the lock will depend on the implementation. If a lock can not be
   * acquired, then an {@link IllegalStateException} is thrown.
   * <p>
   * Whoever request the lock <b>MUST</b> release it as soon as possible.
   *
   * @param path   the uri to the file you want to lock
   * @return an acquired {@link PathLock}
   * @throws IllegalArgumentException if a lock could not be acquired
   */
  public final synchronized PathLock lock(URI path) {
    PathLock lock = createLock(path);
    acquireLock(lock);

    return lock;
  }

  /**
   * Try to acquire a lock on a file and release it immediately. Usually used as a quick check to see if another process is still
   * holding onto the file, e.g. a large file (more than 100MB) is still being written to.
   */
  protected boolean isLocked(URI path) {
    PathLock lock = createLock(path);
    try {
      return !lock.tryLock();
    } finally {
      lock.release();
    }
  }

  /**
   * Verify that the given {@code path} is not locked
   *
   * @param path the path to test
   * @throws IllegalStateException if the {@code path} is indeed locked
   */
  public void verifyNotLocked(URI path) {
    if (isLocked(path)) {
      throw new FileLockedException(format("File '%s' is locked by another process", path));
    }
  }

  /**
   * @param uri of the file to lock.
   *
   * @return a {@link URLPathLock} on the given uri.
   */
  protected abstract PathLock createLock(URI uri);

}
