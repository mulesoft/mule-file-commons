/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.stream;

import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.extension.file.common.api.lock.Lock;
import org.mule.extension.file.common.api.lock.PathLock;
import org.mule.extension.file.common.api.util.StreamProxyUtil;
import org.mule.runtime.api.exception.MuleRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.apache.commons.io.input.AutoCloseInputStream;

/**
 * Base class for {@link InputStream} instances returned by connectors which operate over a {@link FileSystem}.
 * <p>
 * It's an {@link AutoCloseInputStream} which also contains the concept of a {@link PathLock} which is released when the stream is
 * closed or fully consumed.
 * <p>
 * Because in most implementations the actual reading of the stream requires initialising/maintaining a connection, instances are
 * created through a {@link LazyStreamSupplier}. This allows such connection/resource to be provisioned lazily. This is very
 * useful in cases such as {@link FileSystem#list(FileConnectorConfig, String, boolean, Predicate)}. Being able to only lazily
 * establish the connections, prevents the connector from opening many connections at the same time, at the risk that many of them
 * might end up not being necessary at the same time.
 *
 * @since 1.0
 *
 * @deprecated Use {@link AbstractNonFinalizableFileInputStream} instead.
 */
@Deprecated
public abstract class AbstractFileInputStream extends AutoCloseInputStream {

  private static InputStream createLazyStream(LazyStreamSupplier streamFactory) {
    return StreamProxyUtil.getInputStreamFromStreamFactory(streamFactory);
  }

  private final LazyStreamSupplier streamSupplier;
  private final Lock lock;
  private final AtomicBoolean closed = new AtomicBoolean(false);

  public AbstractFileInputStream(LazyStreamSupplier streamSupplier, PathLock lock) {
    this(streamSupplier, (Lock) lock);
  }

  public AbstractFileInputStream(LazyStreamSupplier streamSupplier, Lock lock) {
    super(createLazyStream(streamSupplier));
    this.lock = lock;
    this.streamSupplier = streamSupplier;
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
    super.close();
  }

  public boolean isLocked() {
    return lock.isLocked();
  }
}
