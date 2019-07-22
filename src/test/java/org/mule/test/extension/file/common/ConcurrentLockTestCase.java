/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extension.file.common;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;

import org.mule.extension.file.common.api.AbstractFileSystem;
import org.mule.extension.file.common.api.ExternalFileSystem;
import org.mule.extension.file.common.api.command.CopyCommand;
import org.mule.extension.file.common.api.command.CreateDirectoryCommand;
import org.mule.extension.file.common.api.command.DeleteCommand;
import org.mule.extension.file.common.api.command.ListCommand;
import org.mule.extension.file.common.api.command.MoveCommand;
import org.mule.extension.file.common.api.command.ReadCommand;
import org.mule.extension.file.common.api.command.RenameCommand;
import org.mule.extension.file.common.api.command.WriteCommand;
import org.mule.extension.file.common.api.lock.NullPathLock;
import org.mule.extension.file.common.api.lock.PathLock;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.tck.size.SmallTest;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

  private AbstractFileSystem fileSystem = new TestFileSystem("");
  private ExternalFileSystem externalFileSystem = new TestExternalFileSystem("");
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
  }

  @Test
  public void concurrentLock() throws Exception {
    new Thread(() -> {
      try {
        mainThreadLatch.release();
        secondaryThreadLatch.await(TIMEOUT, TIMEOUT_UNIT);
        tryLock();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    mainThreadLatch.await(TIMEOUT, TIMEOUT_UNIT);
    secondaryThreadLatch.release();
    tryLock();

    assertionLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(successful.get(), is(1));
    assertThat(failed.get(), is(1));
  }

  private void tryLock() {
    try {
      if (fileSystem.lock(PATH).tryLock()) {
        successful.incrementAndGet();
      } else {
        failed.incrementAndGet();
      }
    } catch (Exception e) {
      failed.incrementAndGet();
    }
    assertionLatch.countDown();
  }

  private class TestFileSystem extends AbstractFileSystem {

    private boolean locked = false;

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

    @Override
    public void changeToBaseDir() {}
  }

  @Test
  public void concurrentUriLock() throws Exception {
    new Thread(() -> {
      try {
        mainThreadLatch.release();
        secondaryThreadLatch.await(TIMEOUT, TIMEOUT_UNIT);
        tryUriLock();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    mainThreadLatch.await(TIMEOUT, TIMEOUT_UNIT);
    secondaryThreadLatch.release();
    tryUriLock();

    assertionLatch.await(TIMEOUT, TIMEOUT_UNIT);
    assertThat(successful.get(), is(1));
    assertThat(failed.get(), is(1));
  }

  private void tryUriLock() {
    try {
      if (externalFileSystem.lock(URI).tryLock()) {
        successful.incrementAndGet();
      } else {
        failed.incrementAndGet();
      }
    } catch (Exception e) {
      failed.incrementAndGet();
    }
    assertionLatch.countDown();
  }

  private class TestExternalFileSystem extends ExternalFileSystem {

    private boolean locked = false;

    public TestExternalFileSystem(String basePath) {
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
    protected PathLock createLock(URI path) {
      if (locked) {
        PathLock lock = mock(PathLock.class);
        when(lock.tryLock()).thenReturn(false);
        return lock;
      } else {
        locked = true;
        return new NullPathLock(path);
      }
    }

    @Override
    protected PathLock createLock(Path path) {
      throw new IllegalStateException("For external file systems, use createLock(URI uri) instead.");
    }

    @Override
    public void changeToBaseDir() {}
  }
}
