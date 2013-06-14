package org.rackspace.stingray.client.mock;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.mockito.Mockito;
import org.rackspace.stingray.client.exception.StingrayRestClientException;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public final class MockClientHandler implements ClientHandler {

    public static final String ROOT = "https://localhost:9070/api/tm/1.0/config/active/";

    private Map<Request, MockResponse> mocked = new HashMap<Request, MockResponse>();

    @Override
    public ClientResponse handle(ClientRequest cr)
            throws ClientHandlerException {
        String uri = cr.getURI().toString();
        String relative = uri.replace(ROOT, "");

        Request request = new Request(relative, cr.getMethod());
        MockResponse response = mocked.get(request);
        if (response == null) {
            throw new RuntimeException(String.format("Path %s for %s was not mocked", relative, cr.getMethod()));
        }
        if (response.exception != null) {
            throw new ClientHandlerException(response.exception);
        }
        if (response.clientResponse != null) {
            return response.clientResponse;
        }
        return null;
    }

    public ContinuedMocking when(String path, String method) {
        Request request = new Request(path, method);
        return new ContinuedMocking(mocked, request);
    }

    public static final class ContinuedMocking {

        private final Map<Request, MockResponse> mocked;
        private final Request request;

        private ContinuedMocking(final Map<Request, MockResponse> mocked, final Request request) {
            this.mocked = mocked;
            this.request = request;
        }

        @SuppressWarnings("unchecked")
        public void thenReturn(final Response.Status status, final Object response) {
            // TODO we might try to use the real response here
            // InBoundHeaders headers = new InBoundHeaders();
            // MessageBodyWorkers workers = Mockito.mock(MessageBodyWorkers.class);
            // ClientResponse clientResponse = new ClientResponse(status.getStatusCode(), headers, new ByteArrayInputStream(response.getBytes()), workers);

            ClientResponse clientResponse = Mockito.mock(ClientResponse.class);
            Mockito.when(clientResponse.getStatus()).thenReturn(status.getStatusCode());
            Mockito.when(clientResponse.getClientResponseStatus()).thenReturn(Status.fromStatusCode(status.getStatusCode()));
            Mockito.when(clientResponse.getEntity(Mockito.<Class>any())).thenReturn(response);

            MockResponse mockResponse = new MockResponse(clientResponse, null);
            mocked.put(request, mockResponse);
        }

        public void thenThrow(final Exception exception) {
            mocked.put(request, new MockResponse(null, exception));
        }

    }

    private static final class MockResponse {
        private final ClientResponse clientResponse;
        private final Exception exception;

        public MockResponse(final ClientResponse clientResponse, final Exception exception) {
            this.clientResponse = clientResponse;
            this.exception = exception;
        }
    }

    private static final class Request {
        private String path;
        private String method;

        public Request(final String path, final String method) {
            this.path = path;
            this.method = method;
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).
                    append(path).append(method).toHashCode();
        }
    }

}