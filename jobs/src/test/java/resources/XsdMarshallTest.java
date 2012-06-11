package resources;

import com.rackspace.docs.core.event.DC;
import com.rackspace.docs.core.event.EventType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.pojo.LBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.UsageV1Pojo;
import org.openstack.atlas.atom.util.UUIDUtil;
import org.openstack.atlas.atom.util.UsageMarshaller;
import org.w3._2005.atom.Title;
import org.w3._2005.atom.Type;
import org.w3._2005.atom.UsageCategory;
import org.w3._2005.atom.UsageContent;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
                uuid = UUIDUtil.genUUID(uString);
            } catch (NoSuchAlgorithmException e) {
                Assert.fail(e.getLocalizedMessage());
            }
        }

        @Test
        public void shouldMarshallAndValidateLBaaSUsage() {
            //Builds fake data and tests the xsd/xml validation
            try {
                EntryPojo entry = new EntryPojo();

                //core
                UsageV1Pojo usageV1 = new UsageV1Pojo();
                usageV1.setDataCenter(DC.DFW_1);
                usageV1.setResourceName("LoadBalancer");
                usageV1.setVersion("1");
                usageV1.setTenantId("1");
                usageV1.setResourceId("22mfmfnmf");
                usageV1.setType(EventType.CREATE);
                usageV1.setId(uuid.toString());
                usageV1.setStartTime(processCalendar(cal.getTimeInMillis()));
                usageV1.setEndTime(processCalendar(cal.getTimeInMillis()));

                //product specific
                LBaaSUsagePojo lu = new LBaaSUsagePojo();
                lu.setBandWidthIn(4);
                lu.setAvgConcurrentConnections(30000);
                usageV1.getAny().add(lu);

//                AccountLBaaSUsagePojo ausage = new AccountLBaaSUsagePojo();
//                ausage.setId(2);
//                ausage.setNumLoadbalancers(33);
//                ausage.setNumPublicVips(3);
//                ausage.setNumServicenetVips(4);
//                usageV1.getAny().add(ausage);


                //Atom specific
                Title title = new Title();
                title.setType(Type.TEXT);
                title.setValue("LBAAS");
                entry.setTitle(title);

                UsageCategory category = new UsageCategory();
                category.setLabel("label");
                category.setTerm("term");
                category.setScheme("PLAIN");

                entry.getCategory().add(category);

                entry.setUpdated(processCalendar(cal.getTimeInMillis()));
                entry.setId("11");

                UsageContent usageContent = new UsageContent();
                usageContent.setEvent(usageV1);
                entry.setContent(usageContent);
                entry.getContent().setType(MediaType.APPLICATION_XML);


                Unmarshaller unmarshaller = createUnmarshaller();

                ByteArrayInputStream input = new ByteArrayInputStream(UsageMarshaller.marshallObject(entry).getBytes());
                unmarshaller.unmarshal(input);
                System.out.print(UsageMarshaller.marshallObject(entry));

            } catch (Exception e) {
                //If Failure occurs here its most likely because of validation, this should pass if object is valid... see stack trace to verify...
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        private Unmarshaller createUnmarshaller() throws JAXBException, SAXException {
            //Test purpose only, not needed for any code functionality..

            //Unmarshall and verify against schema
            JAXBContext jc = JAXBContext.newInstance(EntryPojo.class);
            Schema factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File("jobs/src/main/resources/META-INF/xsd/entry.xsd"));
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setSchema(factory);
            return unmarshaller;
        }


        private XMLGregorianCalendar processCalendar(long timeInMillis) throws DatatypeConfigurationException {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(timeInMillis);
            DatatypeFactory dtf = DatatypeFactory.newInstance();
            return dtf.newXMLGregorianCalendar(gc);
        }
    }
}
