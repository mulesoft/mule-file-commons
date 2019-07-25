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

  UriLock lock(URI uri);

  void verifyNotLocked(URI uri);

}
