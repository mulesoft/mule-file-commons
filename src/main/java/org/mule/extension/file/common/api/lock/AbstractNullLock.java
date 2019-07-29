/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.lock;

/**
 * Implementation of the Null Object design pattern for the {@link NullLock} interface
 *
 * @since 1.3.0
 */
public abstract class AbstractNullLock implements Lock {

  /**
   * Does nothing and always returns {@code true}
   *
   * @return {@code true}
   */
  public boolean tryLock() {
    return true;
  }

  /**
   * @return {@code false}
   */
  public boolean isLocked() {
    return false;
  }

  /**
   * Does nothing regardless of how many invokations the {@link #tryLock()} method has received
   */
  public void release() {
    // no-op
  }
}
