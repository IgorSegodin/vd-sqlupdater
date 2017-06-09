package de.vdata.sqlupdater.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author isegodin
 */
public class FileUtilTest {

    @Test
    public void testGetPathToFolder() {
        Assert.assertEquals("/", FileUtil.getPathToFolder("/folderWithoutSlash"));
        Assert.assertEquals("", FileUtil.getPathToFolder(""));
        Assert.assertEquals("/", FileUtil.getPathToFolder("/"));
        Assert.assertEquals("D:", FileUtil.getPathToFolder("D:"));

        Assert.assertEquals("folder", FileUtil.getPathToFolder("folder/"));
        Assert.assertEquals("folder", FileUtil.getPathToFolder("folder/subFolderWithoutSlash"));
        Assert.assertEquals("/folder", FileUtil.getPathToFolder("/folder/"));

        Assert.assertEquals("/folder/subFolder", FileUtil.getPathToFolder("/folder/subFolder/"));
        Assert.assertEquals("/folder", FileUtil.getPathToFolder("/folder/test.txt"));
        Assert.assertEquals("/folder/subFolder", FileUtil.getPathToFolder("/folder/subFolder/file.txt"));

        Assert.assertEquals("D:", FileUtil.getPathToFolder("D:\\folder"));
        Assert.assertEquals("D:\\folder", FileUtil.getPathToFolder("D:\\folder\\"));
        Assert.assertEquals("D:\\folder", FileUtil.getPathToFolder("D:\\folder\\file.txt"));
    }

}
