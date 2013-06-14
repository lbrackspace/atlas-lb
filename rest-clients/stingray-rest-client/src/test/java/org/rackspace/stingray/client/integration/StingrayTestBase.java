package org.rackspace.stingray.client.integration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StingrayTestBase {

    public final String TESTNAME = "i_test";

    public File createTestFile(String fileName, String fileText) throws IOException {
        File fixx = new File(fileName);
        FileWriter fw = new FileWriter(fixx);
        fw.write(fileText);
        fw.close();
        return fixx;
    }

}
