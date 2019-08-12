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
import org.mule.extension.file.common.api.lock.UriLock;

import java.net.URI;
import java.nio.file.Path;

/**
 * Interface that allows to obtain a {@link UriLock} on a uri given as a {@link URI}.
 *
 * @since 1.3.0
 */
public abstract class AbstractExternalFileSystem extends AbstractFileSystem implements ExternalFileSystem {

  public AbstractExternalFileSystem(String baseUri) {
    super(baseUri);
  }

  /**
   * Acquires and returns lock over the given {@code uri}.
   * <p>
   * Depending on the underlying filesystem, the extent of the lock will depend on the implementation. If a lock can not be
   * acquired, then an {@link IllegalStateException} is thrown.
   * <p>
   * Whoever request the lock <b>MUST</b> release it as soon as possible.
   *
   * @param uri   the uri to the file you want to lock
   * @return an acquired {@link UriLock}
   * @throws IllegalArgumentException if a lock could not be acquired
   */
  public final synchronized UriLock lock(URI uri) {
    UriLock lock = createLock(uri);
    acquireLock(lock);

    return lock;
  }

  /**
   * Attempts to lock the given {@code lock} and throws {@link FileLockedException} if already locked
   *
   * @param lock the {@link UriLock} to be acquired
   * @throws FileLockedException if the {@code lock} is already acquired
   */
  protected void acquireLock(UriLock lock) {
    if (!lock.tryLock()) {
      throw new FileLockedException(
                                    format("Could not lock file '%s' because it's already owned by another process",
                                           lock.getUri().getPath()));
    }
  }

  /**
   * Try to acquire a lock on a file and release it immediately. Usually used as a quick check to see if another process is still
   * holding onto the file, e.g. a large file (more than 100MB) is still being written to.
   */
  protected boolean isLocked(URI uri) {
    UriLock lock = createLock(uri);
    try {
      return !lock.tryLock();
    } finally {
      lock.release();
    }
  }

  /**
   * Verify that the given {@code uri} is not locked
   *
   * @param uri the uri to test
   * @throws IllegalStateException if the {@code uri} is indeed locked
   */
  public void verifyNotLocked(URI uri) {
    if (isLocked(uri)) {
      throw new FileLockedException(format("File '%s' is locked by another process", uri));
    }
  }

  /**
   * @param uri of the file to lock.
   *
   * @return a {@link UriLock} on the given uri.
   */
  protected abstract UriLock createLock(URI uri);

  @Override
  public synchronized PathLock lock(Path path) {
    throw new UnsupportedOperationException("This method is not supported for an External File System. Use lock(URI uri) instead.");
  }

  @Override
  public final void verifyNotLocked(Path path) {
    throw new UnsupportedOperationException("This method is not supported for an External File System. Use verifyNotLocked(URI uri) instead.");
  }

  @Override
  protected final boolean isLocked(Path path) {
    throw new UnsupportedOperationException("This method is not supported for an External File System. Use isLocked(URI uri) instead.");
  }

  @Override
  protected final void acquireLock(PathLock path) {
    throw new UnsupportedOperationException("This method is not supported for an External File System. Use acquireLock(URI uri) instead.");
  }

  @Override
  protected final PathLock createLock(Path path) {
    throw new UnsupportedOperationException("This method is not supported for an External File System. Use createLock(URI uri) instead.");
  }

}
