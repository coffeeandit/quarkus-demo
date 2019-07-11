package br.com.cdsoft;

import br.com.cdsoft.domain.Fruit;
import br.com.cdsoft.kafka.MessageFruit;
import br.com.cdsoft.repository.FruitRepository;
import io.reactivex.Flowable;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Path("/fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {

    public static final int PERIOD = 5;
    private static final Logger LOGGER = Logger.getLogger(FruitResource.class.getName());
    private List<MessageFruit> messageFruits = new ArrayList<>();


    @Inject
    private FruitRepository fruitRepository;

    @GET
    public List<Fruit> list() {
        List<Fruit> fruits = fruitRepository.findAll().get();
        return fruits;

    }

    @POST
    @Transactional
    public Response add(final Fruit fruit) {
        LOGGER.info("add fruit .:" + fruit);
        Fruit persist = fruitRepository.persist(fruit);
        messageFruits.add(new MessageFruit(fruit, MessageFruit.MessageType.INSERT));
        return Response.ok(persist).status(201).build();

    }

    @GET
    @Path("{id}")
    public Fruit getSingle(@PathParam("id") Long id) {
        Fruit entity = Fruit.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        return entity;
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Fruit update(@PathParam("id") Long id, Fruit fruit) {
        if (fruit.getName() == null) {
            throw new WebApplicationException("Fruit Name was not set on request.", 422);
        }

        Fruit entity = Fruit.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }

        entity.setName(fruit.getName());
        entity.setDescription(fruit.getDescription());
        entity.persistAndFlush();
        messageFruits.add(new MessageFruit(entity, MessageFruit.MessageType.UPDATE));

        return entity;
    }


    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Fruit entity = Fruit.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        messageFruits.add(new MessageFruit(entity, MessageFruit.MessageType.DELETE));

        return Response.status(204).build();
    }

    @Outgoing("fruits")
    public Flowable<List<MessageFruit>> kafkaStream() {


        Flowable<List<MessageFruit>> listFlowable = Flowable.just(messageFruits);
        return Flowable.interval(PERIOD, TimeUnit.SECONDS).flatMap(aLong -> {
            return listFlowable;
        });


    }

    @Incoming("fruits")
    public CompletionStage<Void> printFruint(Message<List<MessageFruit>> fruit) {
        fruit.ack();
        return CompletableFuture.runAsync(() -> {
            List<MessageFruit> payload = fruit.getPayload();
            if (!payload.isEmpty()) {
                LOGGER.info("fruits.:" + payload);
            }
        }).thenAccept(aVoid -> {
            messageFruits.clear();
        });
    }

}