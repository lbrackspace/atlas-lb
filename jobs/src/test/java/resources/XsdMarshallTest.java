package resources;

import com.rackspace.docs.core.event.DC;
import com.rackspace.docs.core.event.EventType;
import com.rackspace.docs.core.event.V1Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.atom.pojo.AccountLBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.pojo.LBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.UsageV1Pojo;
import org.openstack.atlas.atom.util.UUIDUtil;
import org.openstack.atlas.atom.util.UsageMarshaller;
import org.w3._2005.atom.Title;
import org.w3._2005.atom.Type;
import org.w3._2005.atom.UsageContent;

import javax.ws.rs.core.MediaType;
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
//                UsageEntry entry = new UsageEntry();

                String uString = "DFW" + "234" + "2346";
                UUID uuid = UUIDUtil.genUUID(uString);

                //core
                UsageV1Pojo usageV1 = new UsageV1Pojo();
//                V1Element usageV1 = new V1Element();
                usageV1.setDataCenter(DC.DFW_1);
                usageV1.setResourceName("LoadBalancer");
                usageV1.setVersion("1");
                usageV1.setTenantId("1");
                usageV1.setResourceId("22mfmfnmf");
                usageV1.setType(EventType.CREATE);
                usageV1.setId(uuid.toString());
                Calendar cal = Calendar.getInstance();
                usageV1.setStartTime(processCalendar(cal.getTimeInMillis()));
                usageV1.setEndTime(processCalendar(cal.getTimeInMillis()));

                //product specific
                LBaaSUsagePojo lu = new LBaaSUsagePojo();
//                LoadBalancerUsage lu = new LoadBalancerUsage();
                lu.setBandWidthIn(4);
                lu.setAvgConcurrentConnections(30000);
                usageV1.getAny().add(lu);

                AccountLBaaSUsagePojo ausage = new AccountLBaaSUsagePojo();
                ausage.setId(2);
                ausage.setNumLoadbalancers(33);
                ausage.setNumPublicVips(3);
                ausage.setNumServicenetVips(4);
                usageV1.getAny().add(ausage);


                //Atom specific
                Title title = new Title();
                title.setType(Type.TEXT);
                title.setValue("LBAAS");
                entry.setTitle(title);

//                UsageContent usageContent = new UsageContent();
//                usageContent.setEvent(usageV1);
//                entry.setContent(usageContent);
//                entry.getContent().setType(MediaType.APPLICATION_XML);

                //Unmarshall and verify against schema
                JAXBContext jc = JAXBContext.newInstance(V1Element.class);
                Schema factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File("jobs/src/main/resources/META-INF/xsd/core.xsd"));
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                unmarshaller.setSchema(factory);
                ByteArrayInputStream input = new ByteArrayInputStream(UsageMarshaller.marshallObject(usageV1).getBytes());


                System.out.print(unmarshaller.unmarshal(input));

                UsageContent usageContent = new UsageContent();
                usageContent.setEvent(usageV1);

                entry.setContent(usageContent);
                entry.getContent().setType(MediaType.APPLICATION_XML);
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
