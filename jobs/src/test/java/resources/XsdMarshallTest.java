package resources;

import com.rackspace.docs.usage.core.V1Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.pojo.LBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.UsageV1Pojo;
import org.openstack.atlas.atom.util.UUIDUtil;
import org.openstack.atlas.atom.util.UsageMarshaller;
import org.w3._2005.atom.UsageContent;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

@Ignore
@RunWith(Enclosed.class)
public class XsdMarshallTest {
    public static class WhenMarshallingGeneratedXML {

        @Before
        public void standUp() {
        }

        @Test
        public void shouldMarshallToStringProperly() {
            //Builds fake data and tests the xsd/xml validation
            try {
                EntryPojo entry = new EntryPojo();

                String uString = "DFW" + "234" + "2346";
                UUID uuid = UUIDUtil.genUUID(uString);

                //core
                UsageV1Pojo usageV1 = new UsageV1Pojo();
                usageV1.setDataCenter("DFW");
                usageV1.setResourceName("LoadBalancer");
                usageV1.setVersion("1");
                usageV1.setTenantId("1");
                usageV1.setServiceCode("lbaas");
                usageV1.setResourceId("22mfmfnmf");
                usageV1.setUsageId(uuid.toString());
                Calendar cal = Calendar.getInstance();
                usageV1.setStartTime(processCalendar(cal.getTimeInMillis()));
                usageV1.setEndTime(processCalendar(cal.getTimeInMillis()));

                //product specific
                LBaaSUsagePojo lu = new LBaaSUsagePojo();
                lu.setBandWidthIn(4);
                lu.setAvgConcurrentConnections(30000);
                usageV1.getAny().add(lu);

                //Atom specific
                entry.setTitle("LBAAS");
                entry.setAuthor("LBAAS");

                JAXBContext jc = JAXBContext.newInstance(V1Element.class);
                Schema factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File("jobs/src/main/resources/META-INF/xsd/core.xsd"));

                Unmarshaller unmarshaller = jc.createUnmarshaller();
                unmarshaller.setSchema(factory);

                ByteArrayInputStream input = new ByteArrayInputStream(UsageMarshaller.marshallObject(usageV1).getBytes());
                System.out.print(unmarshaller.unmarshal(input));

                UsageContent usageContent = new UsageContent();
                usageContent.setUsage(usageV1);

                entry.setContent(usageContent);
                entry.getContent().setType("applicaiton/xml");
                System.out.print(UsageMarshaller.marshallObject(entry));

            } catch (Exception e) {
                //this will verify generated xml is valid against the usage schema...
                e.printStackTrace();
                Assert.fail();
            }
        }

        private XMLGregorianCalendar processCalendar(long timeInMillis) throws DatatypeConfigurationException {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(timeInMillis);
            DatatypeFactory dtf = DatatypeFactory.newInstance();
            return dtf.newXMLGregorianCalendar(gc);
        }
    }
}
