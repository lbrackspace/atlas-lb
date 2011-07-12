package org.apache.hadoop.fs;

import java.net.URI;

public class LocalUnitTestFileSystem extends LocalFileSystem {

    public static final URI NAME = URI.create("file://" + System.getProperty("user.dir") + "/");
}
