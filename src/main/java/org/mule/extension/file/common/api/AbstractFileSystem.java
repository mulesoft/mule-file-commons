/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api;

import static java.lang.String.format;
import org.mule.extension.file.common.api.command.CopyCommand;
import org.mule.extension.file.common.api.command.CreateDirectoryCommand;
import org.mule.extension.file.common.api.command.DeleteCommand;
import org.mule.extension.file.common.api.command.ListCommand;
import org.mule.extension.file.common.api.command.MoveCommand;
import org.mule.extension.file.common.api.command.ReadCommand;
import org.mule.extension.file.common.api.command.RenameCommand;
import org.mule.extension.file.common.api.command.WriteCommand;
import org.mule.extension.file.common.api.exceptions.FileLockedException;
import org.mule.extension.file.common.api.lock.PathLock;
import org.mule.runtime.api.lock.LockFactory;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;

/**
 * Base class for implementations of {@link FileSystem}
 *
 * @since 1.0
 */
public abstract class AbstractFileSystem<A extends FileAttributes> implements FileSystem<A> {

  private final MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

  @Inject
  private LockFactory lockFactory;

  private final String basePath;

  public AbstractFileSystem(String basePath) {
    this.basePath = basePath;
  }

  /**
   * @return a {@link ListCommand}
   */
  protected abstract ListCommand getListCommand();

  /**
   * @return a {@link ReadCommand}
   */
  protected abstract ReadCommand getReadCommand();

  /**
   * @return a {@link WriteCommand}
   */
  protected abstract WriteCommand getWriteCommand();

  /**
   * @return a {@link CopyCommand}
   */
  protected abstract CopyCommand getCopyCommand();

  /**
   * @return a {@link MoveCommand}
   */
  protected abstract MoveCommand getMoveCommand();

  /**
   * @return a {@link DeleteCommand}
   */
  protected abstract DeleteCommand getDeleteCommand();

  /**
   * @return a {@link RenameCommand}
   */
  protected abstract RenameCommand getRenameCommand();

  /**
   * @return a {@link CreateDirectoryCommand}
   */
  protected abstract CreateDirectoryCommand getCreateDirectoryCommand();

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Result<InputStream, A>> list(FileConnectorConfig config,
                                           String directoryPath,
                                           boolean recursive,
                                           Predicate<A> matcher) {
    return getListCommand().list(config, directoryPath, recursive, matcher);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Result<InputStream, A> read(FileConnectorConfig config, String filePath,
                                     boolean lock) {
    return getReadCommand().read(config, filePath, lock);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(String filePath, InputStream content, FileWriteMode mode,
                    boolean lock, boolean createParentDirectories, String encoding) {
    getWriteCommand().write(filePath, content, mode, lock, createParentDirectories, encoding);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void copy(FileConnectorConfig config, String sourcePath, String targetDirectory, boolean overwrite,
                   boolean createParentDirectories, String renameTo) {
    getCopyCommand().copy(config, sourcePath, targetDirectory, overwrite, createParentDirectories, renameTo);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void move(FileConnectorConfig config, String sourcePath, String targetDirectory, boolean overwrite,
                   boolean createParentDirectories, String renameTo) {
    getMoveCommand().move(config, sourcePath, targetDirectory, overwrite, createParentDirectories, renameTo);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(String filePath) {
    getDeleteCommand().delete(filePath);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void rename(String filePath, String newName, boolean overwrite) {
    getRenameCommand().rename(filePath, newName, overwrite);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createDirectory(String directoryName) {
    getCreateDirectoryCommand().createDirectory(directoryName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final synchronized PathLock lock(Path path) {
    PathLock lock = createLock(path);
    acquireLock(lock);

    return lock;
  }

  /**
   * Attempts to lock the given {@code lock} and throws {@link FileLockedException} if already locked
   *
   * @param lock the {@link PathLock} to be acquired
   * @throws FileLockedException if the {@code lock} is already acquired
   */
  protected void acquireLock(PathLock lock) {
    if (!lock.tryLock()) {
      throw new FileLockedException(
                                    format("Could not lock file '%s' because it's already owned by another process",
                                           lock.getPath()));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MediaType getFileMessageMediaType(FileAttributes attributes) {
    return MediaType.parse(mimetypesFileTypeMap.getContentType(attributes.getPath()));
  }

  /**
   * Try to acquire a lock on a file and release it immediately. Usually used as a quick check to see if another process is still
   * holding onto the file, e.g. a large file (more than 100MB) is still being written to.
   */
  protected boolean isLocked(Path path) {
    PathLock lock = createLock(path);
    try {
      return !lock.tryLock();
    } finally {
      lock.release();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void verifyNotLocked(Path path) {
    if (isLocked(path)) {
      throw new FileLockedException(format("File '%s' is locked by another process", path));
    }
  }

  protected abstract PathLock createLock(Path path);

  /**
   * {@inheritDoc}
   */
  @Override
  public Lock createMuleLock(String lockId) {
    return lockFactory.createLock(lockId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getBasePath() {
    return basePath;
  }
}
