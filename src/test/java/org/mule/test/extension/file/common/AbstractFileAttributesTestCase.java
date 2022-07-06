/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extension.file.common;

import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;

import org.mule.extension.file.common.api.AbstractFileAttributes;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class AbstractFileAttributesTestCase {

  private static String path;

  @Test
  public void bothConstructorAssignEqualFileNamesForEmptyPath() {
    path = "";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForSpacePath() {
    path = " ";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForCurrentDirectoryPath() {
    path = "/.";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForParentDirectoryPath() {
    path = "/..";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForSimplePath() {
    path = "/root";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForTrailingSlashPath() {
    path = "/root/";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForCurrentDirectoryWithParentPath() {
    path = "/root/.";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForParentDirectoryWithParentPath() {
    path = "/root/..";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForSpecialCharactersPath() {
    assumeTrue(!IS_OS_WINDOWS);
    path = "/root/./$%@<>";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForLongPath() {
    path = "/root/myFile";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForPathWithColon() {
    assumeTrue(!IS_OS_WINDOWS);
    path = "/rootWith:/myFile";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForFileWithExtensionPath() {
    path = "/root/myFile.txt";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForComplexPathWithSpaceDirectory() {
    path = "/root/ /myFile";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForComplexPathWithSpecialCharacterDirectory() {
    assumeTrue(!IS_OS_WINDOWS);
    path = "/root/@/myFile";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForComplexPathWithCurrentDirectory() {
    path = "/root/./myFile";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForComplexPathWithParentDirectory() {
    path = "/root/../myFile";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForCurrentDirectoryWithTrailingSlashAndParentPath() {
    path = "/root/./";
    assertFileName(path);
  }

  @Test
  public void bothConstructorAssignEqualFileNamesForParentDirectoryWithTrailingSlashAndParentPath() {
    path = "/root/../";
    assertFileName(path);
  }

  private void assertFileName(String path) {
    ConcreteFileAttributes pathAttributes = new ConcreteFileAttributes(Paths.get(path));
    ConcreteFileAttributes uriAttributes = new ConcreteFileAttributes(createUri(path));

    assertThat(pathAttributes.getName(), equalTo(uriAttributes.getName()));
  }


  @Test
  public void bothConstructorWithBasePathInHome() {
    assertBasePathAndFileName("~", "myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAssignInCurrent() {
    assertBasePathAndFileName(".", "myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAssignInSpecificFolder() {
    assertBasePathAndFileName("/root", "myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAssignInSpecificFolderWithSeparator() {
    assertBasePathAndFileName("/root/", "myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAssignInSpecificFolderWithSeparator2() {
    ConcreteFileAttributes pathAttributes = new ConcreteFileAttributes(Paths.get("/myFile.txt"));
    // Basepath parameter will be ignored when tne filepath is absolute (when it start with slash)
    ConcreteFileAttributes uriAttributes = new ConcreteFileAttributes(createUri("/root/", "/myFile.txt"));
    assertThat(pathAttributes.getName(), equalTo(uriAttributes.getName()));
  }

  @Test
  public void bothConstructorWithBasePathAssignInSpecificFolderWithoutSeparators() {
    assertBasePathAndFileName("root", "myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAssignInSpecificFoldersWithoutSeparators() {
    assertBasePathAndFileName("root/test", "myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAssignInRoot() {
    assertBasePathAndFileName("/", "myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAssignInParent() {
    assertBasePathAndFileName("..", "myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAssignInParentSpeficFolder() {
    assertBasePathAndFileName("/root", "../myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAEmpty() {
    assertBasePathAndFileName("", "myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAEmptyAndSeparator() {
    assertBasePathAndFileName("", "/myFile.txt");
  }

  @Test
  public void bothConstructorWithBasePathAEmptyAndFilePathEmpty() {
    assertBasePathAndFileName("", "");
  }

  @Test
  public void bothConstructorWithBasePathWithSpaceAndFilePathEmpty() {
    assertBasePathAndFileName(" ", "");
  }

  private void assertBasePathAndFileName(String basePath, String filePath) {
    ConcreteFileAttributes pathAttributes = new ConcreteFileAttributes(Paths.get(basePath, filePath));
    ConcreteFileAttributes uriAttributes = new ConcreteFileAttributes(createUri(basePath, filePath));
    assertThat(pathAttributes.getName(), equalTo(uriAttributes.getName()));
    assertThat(pathAttributes.getPath(), equalTo(uriAttributes.getPath()));
  }

  private class ConcreteFileAttributes extends AbstractFileAttributes {

    public ConcreteFileAttributes(Path path) {
      super(path);
    }

    public ConcreteFileAttributes(URI uri) {
      super(uri);
    }

    @Override
    public long getSize() {
      return 0;
    }

    @Override
    public boolean isRegularFile() {
      return false;
    }

    @Override
    public boolean isDirectory() {
      return false;
    }

    @Override
    public boolean isSymbolicLink() {
      return false;
    }
  }

}
