/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extension.file.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.extension.file.common.api.AbstractFileSystem;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.command.CopyCommand;
import org.mule.extension.file.common.api.command.CreateDirectoryCommand;
import org.mule.extension.file.common.api.command.DeleteCommand;
import org.mule.extension.file.common.api.command.ListCommand;
import org.mule.extension.file.common.api.command.MoveCommand;
import org.mule.extension.file.common.api.command.ReadCommand;
import org.mule.extension.file.common.api.command.RenameCommand;
import org.mule.extension.file.common.api.command.WriteCommand;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import org.mule.extension.file.common.api.lock.PathLock;
import org.mule.extension.file.common.api.source.AbstractPostActionGroup;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractPostActionGroupTestCase {

  public static final String DELETE = "delete";
  public static final String MOVE = "move";
  public static final String RENAME = "rename";

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
    expectedException.expectMessage(MOVE);

    fileSystem.setCanMove(false);
    apply(moveToDirectory, null, false);
  }

  @Test
  public void failMoveToDirectoryAndRename() {
    expectedException.expect(FileAlreadyExistsException.class);
    expectedException.expectMessage(MOVE);

    fileSystem.setCanMove(false);
    apply(moveToDirectory, renameTo, false);
  }

  @Test
  public void failMoveToDirectoryAndAutoDelete() {
    fileSystem.clearActions();
    fileSystem.setCanMove(false);
    apply(moveToDirectory, null, true);
    assertThat(fileSystem.getActionExecuted(), is(DELETE));
  }

  @Test
  public void moveToDirectory() {
    fileSystem.clearActions();
    fileSystem.setCanMove(true);
    apply(moveToDirectory, null, false);
    assertThat(fileSystem.getActionExecuted(), is(MOVE));
  }

  @Test
  public void moveToDirectoryAndAutoDelete() {
    fileSystem.clearActions();
    fileSystem.setCanMove(true);
    apply(moveToDirectory, null, true);
    assertThat(fileSystem.getActionExecuted(), is(MOVE));
  }

  @Test
  public void failRenameTo() {
    expectedException.expect(FileAlreadyExistsException.class);
    expectedException.expectMessage(RENAME);

    fileSystem.setCanRename(false);
    apply(null, renameTo, false);
  }

  @Test
  public void failRenameToAndAutoDelete() {
    fileSystem.clearActions();
    fileSystem.setCanRename(false);
    apply(null, renameTo, true);
    assertThat(fileSystem.getActionExecuted(), is(DELETE));
  }

  @Test
  public void renameTo() {
    fileSystem.clearActions();
    fileSystem.setCanRename(true);
    apply(null, renameTo, false);
    assertThat(fileSystem.getActionExecuted(), is(RENAME));
  }

  @Test
  public void renameToAndAutoDelete() {
    fileSystem.clearActions();
    fileSystem.setCanRename(true);
    apply(null, renameTo, true);
    assertThat(fileSystem.getActionExecuted(), is(RENAME));
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

    private Queue<String> actions;
    private boolean canRename = false;
    private boolean canMove = false;
    private Command success;

    public ConcreteFileSystem(String basePath) {
      super(basePath);
      this.actions = new LinkedList<>();
      this.success = new Success(actions);
    }

    public void clearActions() {
      this.actions.clear();
    }

    public String getActionExecuted() {
      return this.actions.element();
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
      return new ConcreteCommand(canMove, success);
    }

    @Override
    protected DeleteCommand getDeleteCommand() {
      return new ConcreteCommand(true, success);
    }

    @Override
    protected RenameCommand getRenameCommand() {
      return new ConcreteCommand(canRename, success);
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

    private class Success implements Command {

      private Queue<String> actions;

      public Success(Queue<String> actions) {
        this.actions = actions;
      }

      @Override
      public void execute(String action) {
        actions.add(action);
      }
    }

    private class ConcreteCommand implements RenameCommand, MoveCommand, DeleteCommand {

      private boolean available;
      private Command successCallback;

      public ConcreteCommand(boolean available, Command successCallback) {
        this.available = available;
        this.successCallback = successCallback;
      }

      @Override
      public void rename(String filePath, String newName, boolean overwrite) {
        if (!this.available)
          throw new FileAlreadyExistsException(RENAME);
        this.successCallback.execute(RENAME);
      }

      @Override
      public void move(FileConnectorConfig config, String sourcePath, String targetPath, boolean overwrite,
                       boolean createParentDirectories, String renameTo) {
        if (!this.available)
          throw new FileAlreadyExistsException(MOVE);
        this.successCallback.execute(MOVE);
      }

      @Override
      public void delete(String filePath) {
        if (!this.available)
          throw new IllegalArgumentException(DELETE);
        this.successCallback.execute(DELETE);
      }
    }
  }

  private interface Command {

    void execute(String action);
  }
}
