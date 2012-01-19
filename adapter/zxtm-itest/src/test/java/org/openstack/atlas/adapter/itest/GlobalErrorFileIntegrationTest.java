package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Errorpage;

import java.util.List;

public class GlobalErrorFileIntegrationTest extends ZeusTestBase {

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
        setCustomErrorFile();
        deleteErrorFile();
    }

    private void setCustomErrorFile() {
        try {
            final String content = "<html> This is a test error page</html>";
            Errorpage errorpage = new Errorpage();
            errorpage.setContent(content);

            zxtmAdapter.setErrorFile(config, lb.getId(), lb.getAccountId(), content);

            String[] errorFile = getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()});
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
            Assert.assertEquals("global_error.html", fileNameAfterDeletion);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
