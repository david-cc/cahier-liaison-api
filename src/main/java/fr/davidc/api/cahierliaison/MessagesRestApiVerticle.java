package fr.davidc.api.cahierliaison;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.davidc.api.cahierliaison.data.Destinataire;
import fr.davidc.api.cahierliaison.data.Message;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Verticle exposant l'API REST du cahier de liaison.
 * 
 * @author vidda
 *
 */
public class MessagesRestApiVerticle extends AbstractVerticle {

	private Map<Integer, Message> messages = new LinkedHashMap<>();
	private static final String HEADER_USERTYPE = "userType";
	private static final String HEADER_USERNAME = "userName";
	private static final String USERTYPE_PROFESSEUR = "professeur";
	private static final String USERTYPE_PARENT = "parent";

	@Override
	public void start(Future<Void> fut) {
		initData();

		Router router = Router.router(vertx);

		// Router static pour test de bon déploiement
		router.route("/monitor").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html").end("UP");
		});

		// Routage REST
		router.get("/api/messages").handler(this::getMessages);
		router.get("/api/messages/:id").handler(this::getMessage);
		router.route("/api/messages*").handler(BodyHandler.create());
		router.post("/api/messages").handler(this::createMessage);

		// Création du serveur HTTP
		vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 8081),
				result -> {
					if (result.succeeded()) {
						fut.complete();
					} else {
						fut.fail(result.cause());
					}
				});
	}

	private void getMessages(RoutingContext routingContext) {
		// Headers
		String userType = decodeHeader(routingContext.request(), HEADER_USERTYPE);
		if (userType == null) {
			routingContext.response().setStatusCode(400).end();
			return;
		}
		
		String userName = decodeHeader(routingContext.request(), HEADER_USERNAME);
		if (userName == null) {
			routingContext.response().setStatusCode(400).end();
			return;
		}

		// Messages à retourner
		Map<Integer, Message> messagesToReturn = new LinkedHashMap<>();
		if (userType.equals(USERTYPE_PROFESSEUR)) {
			messagesToReturn = messages;
		} else if (userType.equals(USERTYPE_PARENT)) {
			for (Map.Entry<Integer, Message> entry : messages.entrySet()) {
				for (Destinataire dest : entry.getValue().getDestinataires()) {
					if (dest.getNom().equals(userName)) {
						messagesToReturn.put(entry.getKey(), entry.getValue());
					}
				}
			}
		} else {
			routingContext.response().setStatusCode(400).end();
			return;
		}

		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(messagesToReturn.values()));
	}

	private void getMessage(RoutingContext routingContext) {
		// Headers
		String userType = decodeHeader(routingContext.request(), HEADER_USERTYPE);
		if (userType == null) {
			routingContext.response().setStatusCode(400).end();
			return;
		}
		
		String userName = decodeHeader(routingContext.request(), HEADER_USERNAME);
		if (userName == null) {
			routingContext.response().setStatusCode(400).end();
			return;
		}

		// Message à retourner
		Message messageRequested = messages.get(Integer.valueOf(routingContext.request().getParam("id")));
		Message messageToReturn = null;

		if (userType.equals(USERTYPE_PROFESSEUR)) {
			messageToReturn = messageRequested;
		} else if (userType.equals(USERTYPE_PARENT)) {
			for (Destinataire dest : messageRequested.getDestinataires()) {
				if (dest.getNom().equals(userName)) {
					messageToReturn = messageRequested;
				}
			}
		} else {
			routingContext.response().setStatusCode(400).end();
			return;
		}

		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(messageToReturn));
	}

	private void createMessage(RoutingContext routingContext) {
		// Headers
		String userType = decodeHeader(routingContext.request(), HEADER_USERTYPE);
		if (userType == null) {
			routingContext.response().setStatusCode(400).end();
			return;
		}
		
		String userName = decodeHeader(routingContext.request(), HEADER_USERNAME);
		if (userName == null) {
			routingContext.response().setStatusCode(400).end();
			return;
		}

		if (userType.equals(USERTYPE_PROFESSEUR)) {
			final Message message = Json.decodeValue(routingContext.getBodyAsString(), Message.class);
			messages.put(message.getId(), message);

			routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encodePrettily(message));
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}

	private String decodeHeader(HttpServerRequest request, String headerKey) {
		if (request.getHeader(headerKey) == null) {
			return null;
		}
		return new String(Base64.getDecoder().decode(request.getHeader(headerKey)));
	}
	
	private void initData() {
		// Premier message
		Message message1 = new Message();
		message1.setTexte("Corps du message " + message1.getId());
		message1.setDate(new Date());

		List<Destinataire> destList = new ArrayList<Destinataire>();
		Destinataire parent1 = new Destinataire();
		parent1.setNom("parent1");
		parent1.setConfirmation(false);
		destList.add(parent1);

		Destinataire parent2 = new Destinataire();
		parent2.setNom("parent2");
		parent2.setConfirmation(false);
		destList.add(parent2);
		message1.setDestinataires(destList);
		
		messages.put(message1.getId(), message1);
		
		// Deuxieme message
		Message message2 = new Message();
		message2.setTexte("Corps du message " + message2.getId());
		message2.setDate(new Date());
		
		List<Destinataire> destList2 = new ArrayList<Destinataire>();
		destList2.add(parent1);
		message2.setDestinataires(destList2);
		
		messages.put(message2.getId(), message2);
		
		// Troisieme message
		Message message3 = new Message();
		message3.setTexte("Corps du message " + message3.getId());
		message3.setDate(new Date());
		
		List<Destinataire> destList3 = new ArrayList<Destinataire>();
		destList3.add(parent2);
		message3.setDestinataires(destList3);
		
		messages.put(message3.getId(), message3);
	}
}
