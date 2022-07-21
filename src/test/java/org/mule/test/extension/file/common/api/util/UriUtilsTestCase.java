package org.mule.test.extension.file.common.api.util;

import org.junit.Test;
import org.mule.extension.file.common.api.util.UriUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class UriUtilsTestCase {

    @Test
    public void testNoBasePath(){
        String basePath="";
        String filePath = "test.txt";
        String result = UriUtils.createUri(basePath,filePath).toString();
        assertThat(result,is(equalTo(filePath)));
    }

}
