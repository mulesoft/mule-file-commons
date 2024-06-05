/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extension.file.common;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.ftp.engine.api.util.UriUtils.createUri;

import org.mule.ftp.engine.api.AbstractFileSystem;
import org.mule.ftp.engine.api.ExternalFileSystem;
import org.mule.ftp.engine.api.command.CopyCommand;
import org.mule.ftp.engine.api.command.CreateDirectoryCommand;
import org.mule.ftp.engine.api.command.DeleteCommand;
import org.mule.ftp.engine.api.command.ListCommand;
import org.mule.ftp.engine.api.command.MoveCommand;
import org.mule.ftp.engine.api.command.ReadCommand;
import org.mule.ftp.engine.api.command.RenameCommand;
import org.mule.ftp.engine.api.command.WriteCommand;
import org.mule.ftp.engine.api.exceptions.FileLockedException;
import org.mule.ftp.engine.api.lock.Lock;
import org.mule.ftp.engine.api.lock.NullPathLock;
import org.mule.ftp.engine.api.lock.NullUriLock;
import org.mule.ftp.engine.api.lock.PathLock;
import org.mule.ftp.engine.api.lock.UriLock;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.tck.size.SmallTest;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ConcurrentLockTestCase {

  private static final Path PATH = Paths.get("lock");
  private static final URI URI = createUri("lock");
  private static final int TIMEOUT = 5;
  private static final TimeUnit TIMEOUT_UNIT = SECONDS;

  private TestFileSystem fileSystem = new TestFileSystem("");
  private Latch mainThreadLatch;
  private Latch secondaryThreadLatch;
  private CountDownLatch assertionLatch;
  private AtomicInteger failed;
  private AtomicInteger successful;

  @Before
  public void setUp() {
    mainThreadLatch = new Latch();
    secondaryThreadLatch = new Latch();
    assertionLatch = new CountDownLatch(2);
    failed = new AtomicInteger(0);
    successful = new AtomicInteger(0);
    fileSystem.setLock(false);
  }

  @Test
  public void concurrentPathLock() throws Exception {
    concurrentLock(() -> fileSystem.lock(PATH));
  }

  @Test
  public void concurrentUriLock() throws Exception {
    concurrentLock(() -> fileSystem.lock(URI));
  }

  private void concurrentLock(Supplier<Lock> lockSupplier) throws Exception {
    new Thread(() -> {
      try {
        mainThreadLatch.release();
        secondaryThreadLatch.await(TIMEOUT, TIMEOUT_UNIT);
        tryLock(lockSupplier);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    mainThreadLatch.await(TIMEOUT, TIMEOUT_UNIT);
    secondaryThreadLatch.release();
    tryLock(lockSupplier);

    assertionLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(successful.get(), is(1));
    assertThat(failed.get(), is(1));
  }

  private void tryLock(Supplier<Lock> lockSupplier) {
    try {
      if (lockSupplier.get().tryLock()) {
        successful.incrementAndGet();
      } else {
        failed.incrementAndGet();
      }
    } catch (Exception e) {
      failed.incrementAndGet();
    }
    assertionLatch.countDown();
  }

  private class TestFileSystem extends AbstractFileSystem implements ExternalFileSystem {

    private boolean locked = false;

    public void setLock(Boolean lock) {
      locked = lock;
    }

    public TestFileSystem(String basePath) {
      super(basePath);
    }

    @Override
    protected ListCommand getListCommand() {
      return null;
    }

    @Override
    protected ReadCommand getReadCommand() {
      return null;
    }

    @Override
    protected WriteCommand getWriteCommand() {
      return null;
    }

    @Override
    protected CopyCommand getCopyCommand() {
      return null;
    }

    @Override
    protected MoveCommand getMoveCommand() {
      return null;
    }

    @Override
    protected DeleteCommand getDeleteCommand() {
      return null;
    }

    @Override
    protected RenameCommand getRenameCommand() {
      return null;
    }

    @Override
    protected CreateDirectoryCommand getCreateDirectoryCommand() {
      return null;
    }

    @Override
    protected PathLock createLock(Path path) {
      if (locked) {
        PathLock lock = mock(PathLock.class);
        when(lock.tryLock()).thenReturn(false);
        return lock;
      } else {
        locked = true;
        return new NullPathLock(path);
      }
    }

    protected UriLock createLock(URI uri) {
      if (locked) {
        UriLock lock = mock(UriLock.class);
        when(lock.tryLock()).thenReturn(false);
        return lock;
      } else {
        locked = true;
        return new NullUriLock(uri);
      }
    }

    public final synchronized UriLock lock(URI uri) {
      UriLock lock = createLock(uri);
      acquireLock(lock);

      return lock;
    }

    protected void acquireLock(UriLock lock) {
      if (!lock.tryLock()) {
        throw new FileLockedException(
                                      format("Could not lock file '%s' because it's already owned by another process",
                                             lock.getUri().getPath()));
      }
    }

    public void verifyNotLocked(URI uri) {
      if (isLocked(uri)) {
        throw new FileLockedException(format("File '%s' is locked by another process", uri));
      }
    }

    protected boolean isLocked(URI uri) {
      UriLock lock = createLock(uri);
      try {
        return !lock.tryLock();
      } finally {
        lock.release();
      }
    }

    @Override
    public void changeToBaseDir() {}
  }

}
