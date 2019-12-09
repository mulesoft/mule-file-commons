/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extension.file.common;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mule.extension.file.common.api.AbstractFileSystem;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.command.*;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import org.mule.extension.file.common.api.lock.PathLock;
import org.mule.extension.file.common.api.source.AbstractPostActionGroup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractPostActionGroupTestCase {

  private static final String originalName = "original.txt";
  private static final String renameTo = "renamed.txt";
  private static final String moveToDirectory = "output";

  private FileAttributes fileAttributes;
  private FileConnectorConfig fileConnectorConfig;
  private ConcreteFileSystem fileSystem;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    fileAttributes = mock(FileAttributes.class);
    when(fileAttributes.getName()).thenReturn(originalName);
    fileConnectorConfig = mock(FileConnectorConfig.class);
    fileSystem = new ConcreteFileSystem("");
  }

  @Test
  public void failMoveToDirectory() {
    expectedException.expect(FileAlreadyExistsException.class);
    expectedException.expectMessage("Move");

    fileSystem.setCanMove(false);
    apply(moveToDirectory, null, false);
  }

  @Test
  public void failMoveToDirectoryAndRename() {
    expectedException.expect(FileAlreadyExistsException.class);
    expectedException.expectMessage("Move");

    fileSystem.setCanMove(false);
    apply(moveToDirectory, renameTo, false);
  }

  @Test
  public void failMoveToDirectoryAndAutoDelete() {
    fileSystem.setCanMove(false);
    apply(moveToDirectory, null, true);
  }

  @Test
  public void moveToDirectory() {
    fileSystem.setCanMove(true);
    apply(moveToDirectory, null, false);
  }

  @Test
  public void moveToDirectoryAndAutoDelete() {
    fileSystem.setCanMove(true);
    apply(moveToDirectory, null, true);
  }

  @Test
  public void failRenameTo() {
    expectedException.expect(FileAlreadyExistsException.class);
    expectedException.expectMessage("Rename");

    fileSystem.setCanRename(false);
    apply(null, renameTo, false);
  }

  @Test
  public void failRenameToAndAutoDelete() {
    fileSystem.setCanRename(false);
    apply(null, renameTo, true);
  }

  @Test
  public void renameTo() {
    fileSystem.setCanRename(true);
    apply(null, renameTo, false);
  }

  @Test
  public void renameToAndAutoDelete() {
    fileSystem.setCanRename(true);
    apply(null, renameTo, true);
  }

  private void apply(String moveToDirectory, String renameTo, boolean autoDelete) {
    ConcretePostActionGroup postActionGroup = new ConcretePostActionGroup(moveToDirectory, renameTo, autoDelete);
    postActionGroup.apply(fileSystem, fileAttributes, fileConnectorConfig);
  }

  private class ConcretePostActionGroup extends AbstractPostActionGroup {

    private String moveToDirectory;
    private String renameTo;
    private boolean isAutoDelete;

    public ConcretePostActionGroup(String moveToDirectory, String renameTo, boolean isAutoDelete) {
      this.moveToDirectory = moveToDirectory;
      this.renameTo = renameTo;
      this.isAutoDelete = isAutoDelete;
    }

    @Override
    public boolean isAutoDelete() {
      return isAutoDelete;
    }

    @Override
    public String getMoveToDirectory() {
      return moveToDirectory;
    }

    @Override
    public String getRenameTo() {
      return renameTo;
    }

    @Override
    public boolean isApplyPostActionWhenFailed() {
      return false;
    }
  }

  private class ConcreteFileSystem extends AbstractFileSystem {

    private boolean canRename = false;
    private boolean canMove = false;

    public ConcreteFileSystem(String basePath) {
      super(basePath);
    }

    public void setCanRename(boolean canRename) {
      this.canRename = canRename;
    }

    public void setCanMove(boolean canMove) {
      this.canMove = canMove;
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
      return new ConcreteCommand(canMove);
    }

    @Override
    protected DeleteCommand getDeleteCommand() {
      return new ConcreteCommand(true);
    }

    @Override
    protected RenameCommand getRenameCommand() {
      return new ConcreteCommand(canRename);
    }

    @Override
    protected CreateDirectoryCommand getCreateDirectoryCommand() {
      return null;
    }

    @Override
    protected PathLock createLock(Path path) {
      return null;
    }

    @Override
    public void changeToBaseDir() {

    }

    private class ConcreteCommand implements RenameCommand, MoveCommand, DeleteCommand {

      private boolean available;

      public ConcreteCommand(boolean available) {
        this.available = available;
      }

      @Override
      public void rename(String filePath, String newName, boolean overwrite) {
        if (!this.available)
          throw new FileAlreadyExistsException("Rename");
      }

      @Override
      public void move(FileConnectorConfig config, String sourcePath, String targetPath, boolean overwrite,
                       boolean createParentDirectories, String renameTo) {
        if (!this.available)
          throw new FileAlreadyExistsException("Move");
      }

      @Override
      public void delete(String filePath) {
        if (!this.available)
          throw new IllegalArgumentException("Delete");
      }
    }
  }

}
