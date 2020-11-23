package org.openstack.atlas.adapter.vtm;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.adapter.helpers.TrafficScriptHelper;

import java.io.File;
import java.io.IOException;

public class TrafficScriptHelperTest {

    @Before
    public void setUp() {

    }

    @Test
    public void shouldCreateAndRemoveTempFile() throws IOException {
        File f = TrafficScriptHelper.createRuleFile("testrule", "Test-File");
        Assert.assertNotNull(f);
        Assert.assertEquals("Test-File", FileUtils.readFileToString(f));
        Assert.assertTrue(f.delete());
    }
}
