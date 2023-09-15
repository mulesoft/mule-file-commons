/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.stream;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static org.apache.commons.io.IOUtils.EOF;
import static org.mule.extension.file.common.api.stream.AbstractFileInputStream.getInputStreamFromStreamFactory;

import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.extension.file.common.api.lock.Lock;
import org.mule.extension.file.common.api.lock.PathLock;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.io.input.ClosedInputStream;
import org.apache.commons.io.input.ProxyInputStream;


/**
 * Base class for {@link InputStream} instances returned by connectors which operate over a {@link FileSystem}.
 * <p>
 * It's a {@link ProxyInputStream} which also contains the concept of a {@link PathLock} which is released when the stream is
 * closed or fully consumed. Also has the functionality of {@link AutoCloseInputStream} but without the {@link #finalize()}
 * implementation.
 * <p>
 * Because in most implementations the actual reading of the stream requires initialising/maintaining a connection, instances are
 * created through a {@link LazyStreamSupplier}. This allows such connection/resource to be provisioned lazily. This is very
 * useful in cases such as {@link FileSystem#list(FileConnectorConfig, String, boolean, Predicate)}. Being able to only lazily
 * establish the connections, prevents the connector from opening many connections at the same time, at the risk that many of them
 * might end up not being necessary at the same time.
 *
 * @since 1.0
 */
public abstract class AbstractNonFinalizableFileInputStream extends ProxyInputStream {

  private static InputStream createLazyStream(LazyStreamSupplier streamFactory) {
    return getInputStreamFromStreamFactory(streamFactory);
  }

  private final LazyStreamSupplier streamSupplier;
  private final Lock lock;
  private final AtomicBoolean closed = new AtomicBoolean(false);

  public AbstractNonFinalizableFileInputStream(LazyStreamSupplier streamSupplier, PathLock lock) {
    this(streamSupplier, (Lock) lock);
  }

  public AbstractNonFinalizableFileInputStream(LazyStreamSupplier streamSupplier, Lock lock) {
    super(createLazyStream(streamSupplier));
    this.lock = lock;
    this.streamSupplier = streamSupplier;
  }

  /**
   * Automatically closes the stream if the end of stream was reached.
   *
   * @param n number of bytes read, or -1 if no more bytes are available
   * @throws IOException if the stream could not be closed
   * @since 2.0
   */
  @Override
  protected void afterRead(final int n) throws IOException {
    if (n == EOF) {
      close();
    }
  }

  /**
   * Closes the stream and invokes {@link PathLock#release()} on the {@link #lock}.
   * <p>
   * Because the actual stream is lazily opened, the possibility exists for this method being invoked before the
   * {@link #streamSupplier} is used. In such case, this method will not fail.
   *
   * @throws IOException in case of error
   */
  @Override
  public final synchronized void close() throws IOException {
    try {
      if (closed.compareAndSet(false, true) && streamSupplier.isSupplied()) {
        doClose();
      }
    } finally {
      lock.release();
    }
  }

  protected void doClose() throws IOException {
    in.close();
    in = new ClosedInputStream();
  }

  public boolean isLocked() {
    return lock.isLocked();
  }
}
