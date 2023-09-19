/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.stream;

import junit.framework.TestCase;
import org.mule.extension.file.common.api.lock.PathLock;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileInputStreamTest extends TestCase {

  public void testAbstractNonFinalizableFileInputStreamIsCalled() throws IOException {
    Supplier supplier = mock(Supplier.class);
    LazyStreamSupplier lazyStreamSupplier = new LazyStreamSupplier(supplier);
    PathLock pathLock = mock(PathLock.class);
    InputStream inputStream = mock(InputStream.class);

    when(inputStream.read()).thenReturn(5, 6, -1);
    when(supplier.get()).thenReturn(inputStream);

    try (TestClassAbstractNonFinalizableFileInputStream nffis =
        new TestClassAbstractNonFinalizableFileInputStream(lazyStreamSupplier, pathLock)) {

      int read = nffis.read();
      verify(inputStream, times(1)).read();
      assertEquals(5, read);
    }
  }

  public void testAbstractFileInputStreamIsCalled() throws IOException {
    Supplier supplier = mock(Supplier.class);
    LazyStreamSupplier lazyStreamSupplier = new LazyStreamSupplier(supplier);
    PathLock pathLock = mock(PathLock.class);
    InputStream inputStream = mock(InputStream.class);

    when(inputStream.read()).thenReturn(5, 6, -1);
    when(supplier.get()).thenReturn(inputStream);

    try (TestClassAbstractFileInputStream testClassAbstractFileInputStream =
        new TestClassAbstractFileInputStream(lazyStreamSupplier, pathLock)) {

      int read = testClassAbstractFileInputStream.read();
      verify(inputStream, times(1)).read();
      assertEquals(5, read);
    }
  }

  private static class TestClassAbstractNonFinalizableFileInputStream extends AbstractNonFinalizableFileInputStream {

    public TestClassAbstractNonFinalizableFileInputStream(LazyStreamSupplier streamSupplier,
                                                          PathLock lock) {
      super(streamSupplier, lock);
    }
  }


  private static class TestClassAbstractFileInputStream extends AbstractFileInputStream {

    public TestClassAbstractFileInputStream(LazyStreamSupplier streamSupplier, PathLock lock) {
      super(streamSupplier, lock);
    }
  }
}
