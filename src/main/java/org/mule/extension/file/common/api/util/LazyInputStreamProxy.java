/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.util;

import org.mule.extension.file.common.api.stream.LazyStreamSupplier;

import java.io.IOException;
import java.io.InputStream;

public class LazyInputStreamProxy extends InputStream {

  private final LazyStreamSupplier streamSupplier;
  private InputStream delegate;

  public LazyInputStreamProxy(LazyStreamSupplier streamSupplier) {
    this.streamSupplier = streamSupplier;
  }

  private InputStream getDelegate() {
    if (delegate == null) {
      delegate = streamSupplier.get();
    }
    return delegate;
  }

  @Override
  public int read() throws IOException {
    return getDelegate().read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return getDelegate().read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return getDelegate().read(b, off, len);
  }

  @Override
  public long skip(long n) throws IOException {
    return getDelegate().skip(n);
  }

  @Override
  public int available() throws IOException {
    return getDelegate().available();
  }

  @Override
  public void close() throws IOException {
    getDelegate().close();
  }

  @Override
  public synchronized void mark(int readlimit) {
    getDelegate().mark(readlimit);
  }

  @Override
  public synchronized void reset() throws IOException {
    getDelegate().reset();
  }

  @Override
  public boolean markSupported() {
    return getDelegate().markSupported();
  }
}

