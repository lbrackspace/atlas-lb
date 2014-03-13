package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Errorpage;

import java.util.Arrays;

public class GlobalErrorFileIntegrationTest extends ZeusTestBase {
    final String baseContent = "<html> This is a test error page</html>";


    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        setupSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    @Test
    public void testSimpleErrorFileOperations() {
        setCustomErrorFile(baseContent);
        deleteErrorFile();
    }

    @Test
    public void testErrorFileContentAfterCreation() {
        verifyCustomErrorFileContent();
        deleteErrorFile();
    }

    private String[] setCustomErrorFile(String efContent) {
        String[] errorFile = new String[0];
        try {
            Errorpage errorpage = new Errorpage();
            errorpage.setContent(efContent);

            zxtmAdapter.setErrorFile(config, lb, efContent);

            errorFile = getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()});
            boolean doesExist = false;
            for (String fileName : errorFile) {
                if (fileName.equals(errorFileName())) {
                    doesExist = true;
                    break;
                }
            }
            Assert.assertTrue(doesExist);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        return errorFile;
    }

    private void verifyCustomErrorFileContent() {
        String customContent = "<html>I am a custom error file for ZXTM Integration Tests</html>";
            try {
               setCustomErrorFile(customContent);
                String[] errorFile = getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()});
                Assert.assertEquals(customContent, new String(getServiceStubs().getZxtmConfExtraBinding().downloadFile(errorFile[0])));
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

    private void deleteErrorFile() {
        try {
            zxtmAdapter.removeAndSetDefaultErrorFile(config, lb);
            String[] errorFile = getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()});
            String fileNameAfterDeletion = null;

            boolean doesExist = false;
             for (String fileName : errorFile) {
                 fileNameAfterDeletion = errorFile[0];
                if (fileName.equals(errorFileName())) {
                    doesExist = true;
                    break;
                }
            }
            Assert.assertFalse(doesExist);
            Assert.assertEquals("Default", fileNameAfterDeletion);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
