/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.lock;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation of the Null Object design pattern for the {@link PathLock} interface
 *
 * @since 1.0
 */
public final class NullPathLock implements PathLock {

  private final Path path;
  private final URI uri;

  public NullPathLock(Path path) {
    this.path = path;
    this.uri = path.toUri();
  }

  public NullPathLock(URI uri) {
    this.uri = uri;
    this.path = Paths.get(uri.getPath());
  }

  /**
   * Does nothing and always returns {@code true}
   *
   * @return {@code true}
   */
  @Override
  public boolean tryLock() {
    return true;
  }

  /**
   * @return {@code false}
   */
  @Override
  public boolean isLocked() {
    return false;
  }

  /**
   * Does nothing regardless of how many invokations the {@link #tryLock()} method has received
   */
  @Override
  public void release() {
    // no-op
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Path getPath() {
    return path;
  }
}
