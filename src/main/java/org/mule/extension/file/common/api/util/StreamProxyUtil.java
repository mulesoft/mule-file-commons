/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.util;

import org.mule.extension.file.common.api.stream.LazyStreamSupplier;
import org.mule.runtime.api.exception.MuleRuntimeException;

import java.io.InputStream;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

/**
 * Utility class for creating proxy instances of {@link InputStream} using Byte Buddy.
 * The proxies lazily initialize and delegate method calls to real {@link InputStream} instances.
 *
 * <p>This utility is particularly useful when there's a need to delay the initialization of an {@link InputStream}
 * until it's actually used, for example, in scenarios where the initialization is expensive or requires external resources.</p>
 */
public class StreamProxyUtil {

  public static InputStream getInputStreamFromStreamFactory(LazyStreamSupplier streamFactory) {

    try {
      return new LazyInputStreamProxy(streamFactory);
    } catch (Exception e) {
      throw new MuleRuntimeException(createStaticMessage("Could not create instance of " + InputStream.class), e);
    }
  }


}
