package resources;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.atom.pojo.EntryPojo;
import org.openstack.atlas.atom.pojo.LBaaSUsagePojo;
import org.openstack.atlas.atom.pojo.UsageV1Pojo;
import org.openstack.atlas.jobs.ObjectFactory;
import org.openstack.atlas.service.domain.entities.Usage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

@RunWith(Enclosed.class)
public class XsdMarshallTest {
    public static class WhenMarshallingGeneratedXML {
        private Double originalAverage = 50.0;
        private Usage usageRecord;


        @Before
        public void standUp() {
            usageRecord = new Usage();
            usageRecord.setPushed(false);
            usageRecord.setAccountId(5432);
            usageRecord.setEntryVersion(1);
            usageRecord.setNumberOfPolls(22);
        }

        @Test
        public void shouldMarshallToStringProperly() {
            ObjectFactory objectFactory = new ObjectFactory();

            EntryPojo entry = new EntryPojo();

            //core
            UsageV1Pojo usageV1 = new UsageV1Pojo();
            usageV1.setDataCenter("DFW");
            usageV1.setResourceName("LoadBalancer");

            //product specific
            LBaaSUsagePojo lu = new LBaaSUsagePojo();
            lu.setMemory(usageRecord.getNumberOfPolls());
            lu.setVersion("1");
            usageV1.getAny().add(lu);

            //Atom specific
            entry.setTitle("LBAAS");
            entry.setAuthor("LBAAS");

            //Build the above toString in order to set in entry content
            JAXBContext jc = null;
            try {
                jc = JAXBContext.newInstance(EntryPojo.class);

                Marshaller marshaller = jc.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
                    @Override
                    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
                        out.write(ch, start, length);
                    }
                });

                StringWriter st = new StringWriter();
                marshaller.marshal(usageV1, st);
                String xml = st.toString();
                entry.setContent(xml);

                StringWriter st1 = new StringWriter();
                marshaller.marshal(entry, st1);
                String entrystring = st1.toString();
                System.out.print(entrystring);


            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                Assert.fail();
            }
        }
    }
}
