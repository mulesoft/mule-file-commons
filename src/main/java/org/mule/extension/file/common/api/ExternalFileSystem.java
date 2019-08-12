/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api;

import org.mule.extension.file.common.api.lock.UriLock;

import java.net.URI;

/**
 * Represents an external file system and the operations which can be performed on it.
 * <p>
 * This interface acts as a facade which allows performing common file operations for files outside a local disk,
 * like in a an FTP server, a cloud storage service, etc, and uses {@link URI} to identify files.
 *
 * @since 1.3.0
 */
public interface ExternalFileSystem extends FileSystem {

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
  UriLock lock(URI uri);

  /**
   * Verify that the given {@code uri} is not locked
   *
   * @param uri the uri to test
   * @throws IllegalStateException if the {@code uri} is indeed locked
   */
  void verifyNotLocked(URI uri);

}
