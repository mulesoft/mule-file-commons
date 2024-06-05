/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.lock;

import java.net.URI;

/**
 * Use to generate a lock on a file referenced by an uri.
 *
 * @since 1.3.0
 */
public interface UriLock extends Lock {

  /**
   * @return The uri to the locked file
   */
  URI getUri();

}
