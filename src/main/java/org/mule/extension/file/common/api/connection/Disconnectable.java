/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.connection;

/**
 * since 1.2.0
 */
public interface Disconnectable {

  /**
   * Frees all the allocated resources of the {@link Disconnectable}, succesive calls to this method must not fail.
   */
  void disconnect();

}
