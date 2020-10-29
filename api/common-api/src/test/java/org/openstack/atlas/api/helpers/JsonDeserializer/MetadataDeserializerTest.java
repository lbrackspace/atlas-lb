package org.openstack.atlas.api.helpers.JsonDeserializer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.helpers.JsonObjectMapper;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;

import java.io.IOException;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class MetadataDeserializerTest {

    public static class whenDeserializeMetadata{

        @Mock
        JsonParser jsonParser;

        @Mock
        DeserializationContext deserializationContext;

        MetadataDeserializer metadataDeserializer;

        JsonObjectMapper mapper;
        JsonNode jsonNode;
        String jNode;

        @Before
        public void setUp() throws IOException {
            MockitoAnnotations.initMocks(this);
            mapper = new JsonObjectMapper();


        }

        @Test
        public void whenRequestHasRootNameMeta() throws Exception {
            jNode = "{\"meta\":{\"key\":\"colo1r\",\"value\":\"blue\"}}";
            jsonNode = mapper.readValue(jNode, JsonNode.class);
            metadataDeserializer = new MetadataDeserializer(Metadata.class, Meta.class, "getMetas");
            when(jsonParser.readValueAsTree()).thenReturn(jsonNode);

            Object object = metadataDeserializer.deserialize(jsonParser, deserializationContext);
            Assert.assertEquals(object.getClass(), Metadata.class);
        }

        @Test(expected = JsonMappingException.class)
        public void shouldThrowExceptionForEmptyStringRequest() throws Exception {
            jNode = "{\"\":{\"key\":\"colo1r\",\"value\":\"blue\"}}";
            jsonNode = mapper.readValue(jNode, JsonNode.class);
            metadataDeserializer = new MetadataDeserializer(Metadata.class, Meta.class, "getMetas");
            when(jsonParser.readValueAsTree()).thenReturn(jsonNode);

            metadataDeserializer.deserialize(jsonParser, deserializationContext);
        }

        @Test(expected = JsonMappingException.class)
        public void shouldThrowExceptionForInvalidRootTagRequest() throws Exception {
            jNode = "{\"badRequest\":{\"key\":\"colo1r\",\"value\":\"blue\"}}";
            jsonNode = mapper.readValue(jNode, JsonNode.class);
            metadataDeserializer = new MetadataDeserializer(Metadata.class, Meta.class, "getMetas");
            when(jsonParser.readValueAsTree()).thenReturn(jsonNode);

            metadataDeserializer.deserialize(jsonParser, deserializationContext);
        }


        @Test(expected = JsonMappingException.class)
        public void shouldThrowExceptionForBadRequest() throws Exception {
            jNode = "{\"BadRequest\":[{\"key\":\"color6\",\"value\":\"red\"}]}";
            jsonNode = mapper.readValue(jNode, JsonNode.class);
            metadataDeserializer = new MetadataDeserializer(Metadata.class, Meta.class, "getMetas");
            when(jsonParser.readValueAsTree()).thenReturn(jsonNode);

            metadataDeserializer.deserialize(jsonParser, deserializationContext);
        }


        @Test
        public void shouldCreateMetadataObjectForRootNameMetaData() throws Exception {
            jNode = "{\"metadata\":[{\"key\":\"color6\",\"value\":\"red\"}]}";
            jsonNode = mapper.readValue(jNode, JsonNode.class);
            metadataDeserializer = new MetadataDeserializer(Metadata.class, Meta.class, "getMetas");
            when(jsonParser.readValueAsTree()).thenReturn(jsonNode);

            Object object = metadataDeserializer.deserialize(jsonParser, deserializationContext);
            Assert.assertEquals(object.getClass(), Metadata.class);
        }



    }





}
