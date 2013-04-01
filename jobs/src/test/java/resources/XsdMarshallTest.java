package resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.atomhopper.util.UUIDUtil;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.UUID;


@RunWith(Enclosed.class)
public class XsdMarshallTest {
    public static class WhenMarshallingGeneratedXML {
        private UUID uuid;
        private Calendar cal;

        @Before
        public void standUp() {
            String uString = "DFW" + "234" + "2346";
            try {
                cal = Calendar.getInstance();
                uuid = UUIDUtil.genUUIDMD5Hash(uString);
            } catch (NoSuchAlgorithmException e) {
                Assert.fail(e.getLocalizedMessage());
            }
        }

        @Test
        public void shouldSomething() {
        }

//        @Test
//        public void shouldSuccessfullyMarshallEntry() {
//            //Builds fake data and tests the xsd/xml validation
//            try {
//                EntryPojo entry = new EntryPojo();
//
//                //core
//                UsageV1Pojo usageV1 = buildUsageCore(DC.DFW_1, "loadBalancer", "1", "23456", "1234",
//                       EventType.USAGE,  AHUSLUtil.processCalendar(cal), AHUSLUtil.processCalendar(cal), false);
//
//                //product specific
//                usageV1.getAny().add(buildProductEntry("1"));
//
//                //Atom specific
//                entry.setTitle(buildTitle());
//                entry.getCategory().add(buildCategory());
//                buildContent(usageV1, entry);
//
//                unmarshallEntry(entry);
//            } catch (Exception e) {
//                //If Failure occurs here its most likely because of validation, this should pass if object is valid... see stack trace to verify...
//                e.printStackTrace();
//                Assert.fail(e.getMessage());
//            }
//        }
//
//        @Test
//        public void shouldValidateDeleteEventTimeEntry() {
//            //Builds fake data and tests the xsd/xml validation
//            try {
//                EntryPojo entry = new EntryPojo();
//
//                //core
//                UsageV1Pojo usageV1 = buildUsageCore(DC.DFW_1, "loadBalancer", "1", "23456", "1234",
//                       EventType.DELETE,  AHUSLUtil.processCalendar(cal), AHUSLUtil.processCalendar(cal), true);
//
//                //product specific
//                usageV1.getAny().add(buildProductEntry("1"));
//
//                //Atom specific
//                entry.setTitle(buildTitle());
//                entry.getCategory().add(buildCategory());
//                buildContent(usageV1, entry);
//
//                unmarshallEntry(entry);
//            } catch (Exception e) {
//                //If Failure occurs here its most likely because of validation, this should pass if object is valid... see stack trace to verify...
//                e.printStackTrace();
//                Assert.fail(e.getMessage());
//            }
//        }
//
//        @Test(expected = UnmarshalException.class)
//        public void shouldFailWhenVersionDoesNotMatchSchema() throws DatatypeConfigurationException, JAXBException, SAXException {
//            //Builds fake data and tests the xsd/xml validation
//            EntryPojo entry = new EntryPojo();
//
//            //core
//            UsageV1Pojo usageV1 = buildUsageCore(DC.DFW_1, "loadBalancer", "37", "23456", "1234",
//                    EventType.USAGE, AHUSLUtil.processCalendar(cal), AHUSLUtil.processCalendar(cal), false);
//
//            //product specific
//            usageV1.getAny().add(buildProductEntry("1"));
//
//            //Atom specific
//            entry.setTitle(buildTitle());
//            entry.getCategory().add(buildCategory());
//            buildContent(usageV1, entry);
//
//            unmarshallEntry(entry);
//        }
//
//        @Test(expected = UnmarshalException.class)
//        public void shouldFailWhenProductVersionDoesNotMatchSchema() throws DatatypeConfigurationException, JAXBException, SAXException {
//            //Builds fake data and tests the xsd/xml validation
//            EntryPojo entry = new EntryPojo();
//
//            //core
//            UsageV1Pojo usageV1 = buildUsageCore(DC.DFW_1, "loadBalancer", "1", "23456", "1234",
//                    EventType.USAGE, AHUSLUtil.processCalendar(cal), AHUSLUtil.processCalendar(cal), false);
//
//            //product specific
//            usageV1.getAny().add(buildProductEntry("345345"));
//
//            //Atom specific
//            entry.setTitle(buildTitle());
//            entry.getCategory().add(buildCategory());
//            buildContent(usageV1, entry);
//
//            unmarshallEntry(entry);
//        }
//
//        @Ignore //This validation is on server side...
//        @Test
//        public void shouldFailWhenNotEventTimeAndStartEndTimeEqual() {
//            //Builds fake data and tests the xsd/xml validation
//            try {
//                EntryPojo entry = new EntryPojo();
//
//                Calendar now = AHUSLUtil.getNow();
//
//                //core
//                UsageV1Pojo usageV1 = buildUsageCore(DC.DFW_1, "loadBalancer", "1", "23456", "1234",
//                       EventType.USAGE,  AHUSLUtil.processCalendar(now), AHUSLUtil.processCalendar(now), false);
//
//                //product specific
//                usageV1.getAny().add(buildProductEntry("1"));
//
//                //Atom specific
//                entry.setTitle(buildTitle());
//                entry.getCategory().add(buildCategory());
//                buildContent(usageV1, entry);
//
//                unmarshallEntry(entry);
//            } catch (Exception e) {
//                //If Failure occurs here its most likely because of validation, this should pass if object is valid... see stack trace to verify...
//                e.printStackTrace();
//                Assert.fail(e.getMessage());
//            }
//        }
//
//        private UsageV1Pojo buildUsageCore(DC datacenter, String resourceName, String version, String tenantId, String resourceId, EventType eventType, XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, boolean isEvent) throws DatatypeConfigurationException {
//            UsageV1Pojo usage = new UsageV1Pojo();
//            usage.setDataCenter(datacenter);
//            usage.setResourceName(resourceName);
//            usage.setVersion(version);
//            usage.setTenantId(tenantId);
//            usage.setResourceId(resourceId);
//
//            usage.setType(eventType);
//            usage.setId(uuid.toString());
////            System.out.println("Cal before :: " + startTime);
////            System.out.println("Cal after :: " + AHUSLUtil.processCalendar(cal));
//
//            if (!isEvent) {
//            usage.setStartTime(startTime);
//            usage.setEndTime(endTime);
//            } else {
//                usage.setEventTime(startTime);
//            }
//            return usage;
//        }
//
//        private LBaaSUsagePojo buildProductEntry(String version) {
//            LBaaSUsagePojo lu = new LBaaSUsagePojo();
//            lu.setVersion(version);
//            lu.setBandWidthIn(4);
//            lu.setBandWidthInSsl(4);
//            lu.setBandWidthOut(4);
//            lu.setBandWidthOutSsl(4);
//            lu.setAvgConcurrentConnections(30000);
//            lu.setResourceType(ResourceTypes.LOADBALANCER);
//            lu.setServiceCode("CloudLoadBalancers");
//            lu.setVipType(VipTypeEnum.PUBLIC);
//            lu.setSslMode(SslModeEnum.MIXED);
//            lu.setStatus(StatusEnum.ACTIVE);
//            return lu;
//        }
//
//        private Unmarshaller createUnmarshaller() throws JAXBException, SAXException {
//            //Test purpose only, not needed for any code functionality..
//
//            //Unmarshall and verify against schema
//            JAXBContext jc = JAXBContext.newInstance(UsageEntry.class);
//            //For Tests...
////            Schema factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File("jobs/src/main/resources/META-INF/xsd/entry.xsd"));
//            //For jenkins...
//            Schema factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File("src/main/resources/META-INF/xsd/entry.xsd"));
//            Unmarshaller unmarshaller = jc.createUnmarshaller();
//            unmarshaller.setSchema(factory);
//            return unmarshaller;
//        }
//
//        private void unmarshallEntry(EntryPojo entry) throws JAXBException, SAXException {
//            Unmarshaller unmarshaller = createUnmarshaller();
//
//            ByteArrayInputStream input = new ByteArrayInputStream(UsageMarshaller.marshallObject(entry).getBytes());
//            unmarshaller.unmarshal(input);
//            System.out.print(UsageMarshaller.marshallObject(entry));
//        }
//
//        private Title buildTitle() {
//            Title title = new Title();
//            title.setType(Type.TEXT);
//            title.setValue("LBAAS");
//            return title;
//        }
//
//        private UsageCategory buildCategory() {
//            UsageCategory category = new UsageCategory();
//            category.setLabel("loadBalancerUsage");
//            category.setTerm("term");
//            category.setScheme("PLAIN");
//            return category;
//        }
//
//        private UsageContent buildContent(UsageV1Pojo usageV1, EntryPojo entry) {
//            UsageContent usageContent = new UsageContent();
//            usageContent.setEvent(usageV1);
//            entry.setContent(usageContent);
//            entry.getContent().setType(MediaType.APPLICATION_XML);
//            return usageContent;
//        }
//    }
    }
}
