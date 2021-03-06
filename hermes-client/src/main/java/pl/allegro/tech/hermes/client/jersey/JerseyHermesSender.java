package pl.allegro.tech.hermes.client.jersey;

import pl.allegro.tech.hermes.client.HermesMessage;
import pl.allegro.tech.hermes.client.HermesResponse;
import pl.allegro.tech.hermes.client.HermesSender;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static javax.ws.rs.client.Entity.text;

public class JerseyHermesSender implements HermesSender {
    private final Client client;

    public JerseyHermesSender(Client client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<HermesResponse> send(URI uri, HermesMessage message) {
        CompletableFuture<HermesResponse> future = new CompletableFuture<>();
        client.target(uri).request().async().post(text(message.getBody()), new InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                future.complete(new JerseyHermesResponse(response));
            }

            @Override
            public void failed(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }
}
