/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
package org.openstack.atlas.api.validate;

import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import java.util.Set;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class AccessListValidatorTest_ {

    public static class WhenValidatingaccesslist {

        private NetworkItemType[] networkItemTypes;
        private Set<NetworkItemType> networkItemTypeSet;
        private Set<NetworkItemType> badnetworkItemTypeSet;
        private NetworkItem invalidHost;
        private NetworkItem invalidSubnet;
        private NetworkItem validHost;
        private NetworkItem validSubnet;
        private NetworkItem noIp;
        private NetworkItem noType;
        private NetworkItem noTypeorIP;
        private NetworkItem noIpVersion;
        private AccessList emptyaccesslist;
        private AccessList singleItemaccesslist;
        private AccessList multiItemaccesslist;
        private AccessList invalidHostInaccesslist;
        private AccessList invalidSubnetInaccesslist;
        private AccessList nullaccesslist;
        private AccessList noIpInaccesslist;
        private AccessList noTypeInaccesslist;
        private AccessList noTypeOrIpInaccesslist;
        private AccessList noIpVersionInaccesslist;

        public NetworkItem NetWorkItemTypeInit(String Address, String nType, String ipversion) {
            NetworkItem out = new NetworkItem();
            out.setAddress(Address);
            out.setType(NetworkItemType.valueOf(nType));
            out.setIpVersion(IpVersion.valueOf(ipversion));
            return out;
        }

        @Before
        public void standUp() {
            noTypeorIP = new NetworkItem();
            noTypeOrIpInaccesslist = new AccessList();
            noTypeOrIpInaccesslist.getNetworkItems().add(noTypeorIP);
            
            invalidHost = NetWorkItemTypeInit("www.google.com", "ALLOW","IPV_4");
            invalidSubnet = NetWorkItemTypeInit("www.google.com/24", "ALLOW","IPV_4");
            validHost = NetWorkItemTypeInit("192.168.3.51", "ALLOW","IPV_4");
            validSubnet = NetWorkItemTypeInit("192.168.2.0/24", "DENY","IPV_4");

            nullaccesslist = null;

            emptyaccesslist = new AccessList();

            singleItemaccesslist = new AccessList();
            singleItemaccesslist.getNetworkItems().add(validHost);

            multiItemaccesslist = new AccessList();
            multiItemaccesslist.getNetworkItems().add(validHost);
            multiItemaccesslist.getNetworkItems().add(validSubnet);

            invalidHostInaccesslist = new AccessList();
            invalidHostInaccesslist.getNetworkItems().add(invalidHost);
            invalidHostInaccesslist.getNetworkItems().add(validHost);

            invalidSubnetInaccesslist = new AccessList();
            invalidSubnetInaccesslist.getNetworkItems().add(invalidSubnet);
            invalidSubnetInaccesslist.getNetworkItems().add(validSubnet);

            noIp = new NetworkItem();
            noIp.setType(NetworkItemType.ALLOW);
            noIp.setIpVersion(IpVersion.IPV_4);

            noIpVersion = new NetworkItem();
            noIpVersion.setAddress("127.0.0.2");
            noIpVersion.setType(NetworkItemType.ALLOW);

            noType = new NetworkItem();
            noType.setAddress("127.0.0.1");
            noType.setIpVersion(IpVersion.IPV_4);

            noIpInaccesslist = new AccessList();
            noIpInaccesslist.getNetworkItems().add(noIp);

            noTypeInaccesslist = new AccessList();
            noTypeInaccesslist.getNetworkItems().add(noType);

            noIpVersionInaccesslist = new AccessList();
            noIpVersionInaccesslist.getNetworkItems().add(noIpVersion);
        }

        @Test
        public void ShouldHaveIPinEachNetworkItem() {

            assertFalse("Expected rejection of NetworkItem with no IP", new AccessListValidator_()
                    .validate(noIpInaccesslist).passedValidation());
            assertTrue("Expected Acceptence of NetworkItem with IP",new AccessListValidator_()
                    .validate(multiItemaccesslist).passedValidation());
            assertFalse("Expected rejection of accesslist network item with no type and no ip",new AccessListValidator_().
                    validate(noTypeOrIpInaccesslist).passedValidation());

        }

        @Test
        public void ShouldHaveTypeinEachNetworkItem() {

            assertFalse("Expected network item with null type in accesslist to Fail validation", new AccessListValidator_()
                    .validate(noTypeInaccesslist).passedValidation());
            assertTrue("Expected accesslist with non null type in network item to pass validation",new AccessListValidator_()
                    .validate(multiItemaccesslist).passedValidation());
            assertFalse("Expected rejection of accesslist network item with no type and no ip",new AccessListValidator_().
                    validate(noTypeOrIpInaccesslist).passedValidation());
        }

        @Test
        public void ShouldHaveValidTypeinEachNetworkItem() {
            assertFalse("Expected accesslist networkitem with out a valid Type to be rejected", new AccessListValidator_()
                    .validate(noTypeInaccesslist).passedValidation());
            assertTrue("Expected accesslist networkitem with valid Type to be accepted", new AccessListValidator_()
                    .validate(multiItemaccesslist).passedValidation());
        }

        public void ShouldHaveValidIpVersioninEachNetworkItem() {
                        assertFalse("Expected accesslist networkitem with out a valid ipVersion to be rejected", new AccessListValidator_()
                    .validate(noIpVersionInaccesslist).passedValidation());
            assertTrue("Expected accesslist networkitem with valid IpVersion  to be accepted", new AccessListValidator_()
                    .validate(multiItemaccesslist).passedValidation());
        }

        @Test
        public void shouldNotBeEmpty() {
            assertFalse("Expected emptyaccesslist to fail validation", new AccessListValidator_().validate(emptyaccesslist).passedValidation());
            assertTrue("Expected singleItemaccesslist to pass Validation", new AccessListValidator_().validate(singleItemaccesslist).passedValidation());
            assertTrue("Expected multiItemaccesslist to fail Validation", new AccessListValidator_().validate(multiItemaccesslist).passedValidation());
        }

        @Test
        public void shouldRejectbadHostipv4Items() {
            assertFalse("Expected accesslist Networkitem with bad IP to fail validation", new AccessListValidator_().validate(invalidHostInaccesslist).passedValidation());
            assertTrue("Expected accesslist Networkitems with all good IPs to pass validation",new AccessListValidator_().validate(multiItemaccesslist).passedValidation());
        }

        @Test
        public void shouldRejectbadipv4SubnetItems() {
            assertFalse("Expected rejection of Bad Subnet in networkitems", new AccessListValidator_().validate(invalidSubnetInaccesslist).passedValidation());
            assertTrue("Expected Acceptance of Good Subnet in networkitems", new AccessListValidator_().validate(multiItemaccesslist).passedValidation());
        }

        @Test
        public void ShouldHaveIpVersioninEachNetworkItem() {
            assertFalse("Expected rejection of networkitem without ipversion",new AccessListValidator_().validate(noIpVersionInaccesslist).passedValidation());
            assertTrue("Expected Acceptance of networkitem with ipversion",new AccessListValidator_().validate(multiItemaccesslist).passedValidation());
 
        }
    }
}
*/
