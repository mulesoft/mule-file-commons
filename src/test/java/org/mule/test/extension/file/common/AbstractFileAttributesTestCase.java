/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extension.file.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;

import org.mule.extension.file.common.api.AbstractFileAttributes;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AbstractFileAttributesTestCase {

  @Test
  public void bothConstructorAssignEqualFileNames() throws Exception {
    List<String> paths = new ArrayList<String>();
    paths.add("");
    paths.add(" ");
    paths.add("/.");
    paths.add("/..");
    paths.add("/root");
    paths.add("/root/");
    paths.add("/root/.");
    paths.add("/root/..");
    paths.add("/root/./$%@");
    paths.add("/root/myFile");
    paths.add("/root/myFile.txt");
    paths.add("/root/my:File");
    paths.add("/root/my:File.txt");
    paths.add("/root/ /myFile");
    paths.add("/root/@/myFile");
    paths.add("/root/./myFile");
    paths.add("/root/../myFile");
    paths.add("/root/./");
    paths.add("/root/./ ");
    for (String path : paths) {
      ConcreteFileAttributes pathAttributes = new ConcreteFileAttributes(Paths.get(path));
      ConcreteFileAttributes uriAttributes = new ConcreteFileAttributes(createUri(path));

      assertThat(pathAttributes.getName(), equalTo(uriAttributes.getName()));
    }
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
