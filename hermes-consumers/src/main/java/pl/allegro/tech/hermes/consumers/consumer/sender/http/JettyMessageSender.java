package pl.allegro.tech.hermes.consumers.consumer.sender.http;

import com.google.common.util.concurrent.SettableFuture;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response.Listener.Adapter;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import pl.allegro.tech.hermes.common.http.MessageMetadataHeaders;
import pl.allegro.tech.hermes.common.util.MessageId;
import pl.allegro.tech.hermes.consumers.consumer.receiver.Message;
import pl.allegro.tech.hermes.consumers.consumer.sender.ConcurrentRequestsLimitingMessageSender;
import pl.allegro.tech.hermes.consumers.consumer.sender.MessageSendingResult;
import pl.allegro.tech.hermes.consumers.consumer.sender.resolver.EndpointAddressResolutionException;
import pl.allegro.tech.hermes.consumers.consumer.sender.resolver.ResolvableEndpointAddress;

import javax.ws.rs.core.MediaType;
import java.util.concurrent.TimeUnit;

public class JettyMessageSender extends ConcurrentRequestsLimitingMessageSender {

    private final HttpClient client;
    private final ResolvableEndpointAddress endpoint;
    private final long timeout;

    public JettyMessageSender(HttpClient client, ResolvableEndpointAddress endpoint, int timeout) {
        this.client = client;
        this.endpoint = endpoint;
        this.timeout = timeout;
    }

    @Override
    protected void sendMessage(Message message, final SettableFuture<MessageSendingResult> resultFuture) {
        try {
            client.newRequest(endpoint.resolveFor(message))
                    .method(HttpMethod.POST)
                    .header(HttpHeader.KEEP_ALIVE.toString(), "true")
                    .header(MessageMetadataHeaders.MESSAGE_ID.getName(),
                            MessageId.forTopicAndOffset(message.getTopic(), message.getOffset()))
                    .header(HttpHeader.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON)
                    .timeout(timeout, TimeUnit.MILLISECONDS)
                    .content(new BytesContentProvider(message.getData())).send(new Adapter() {

                        @Override
                        public void onComplete(Result result) {
                            resultFuture.set(new MessageSendingResult(result));
                        }
                    });
        } catch (EndpointAddressResolutionException exception) {
            resultFuture.set(MessageSendingResult.failedResult(exception));
        }
    }

    @Override
    public void stop() {
    }
}
