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
import io.vertx.ext.web.handler.CorsHandler;

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

		// Problème de CORS lors des tests avec le front AngularJS 
		// depuis localhost et port différent => ajout de Headers
		router.route().handler(CorsHandler.create("*")
			.allowedMethod(io.vertx.core.http.HttpMethod.GET)
			.allowedMethod(io.vertx.core.http.HttpMethod.POST)
			.allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
			.allowedMethod(io.vertx.core.http.HttpMethod.PUT)
			.allowCredentials(true)
			.allowedHeader("Access-Control-Allow-Method")
			.allowedHeader("Access-Control-Allow-Origin")
			.allowedHeader("Access-Control-Allow-Credentials")
			.allowedHeader("Access-Control-Allow-Headers")
			.allowedHeader("Content-Type"));
		
		// Routage REST
		router.get("/api/messages").handler(this::getMessages);
		router.get("/api/messages/:id").handler(this::getMessage);
		router.route("/api/messages*").handler(BodyHandler.create());
		router.post("/api/messages").handler(this::createMessage);
		router.put("/api/messages/:id").handler(this::editMessage);

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

		routingContext.response()
			.putHeader("content-type", "application/json; charset=utf-8")
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

		// Creation du message
		if (userType.equals(USERTYPE_PROFESSEUR)) {
			final Message message = Json.decodeValue(routingContext.getBodyAsString(), Message.class);
			messages.put(message.getId(), message);

			routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encodePrettily(message));
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}

	private void editMessage(RoutingContext routingContext) {
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
		
		// Edition du message
		Message receivedMessage = Json.decodeValue(routingContext.getBodyAsString(), Message.class);
		Integer msgIdToEdit = Integer.valueOf(routingContext.request().getParam("id"));
		
		messages.put(msgIdToEdit, receivedMessage);
		
		routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
				.end();
	}
	
	private String decodeHeader(HttpServerRequest request, String headerKey) {
		if (request.getHeader(headerKey) == null) {
			return null;
		}
		return new String(Base64.getDecoder().decode(request.getHeader(headerKey)));
	}
	
	private void initData() {
		// 1er message
		Message message1 = new Message();
		message1.setTexte("Corps du message " + message1.getId());
		message1.setDate(new Date());
		
		List<Destinataire> destList1 = new ArrayList<Destinataire>();
		Destinataire parent1 = new Destinataire();
		parent1.setNom("parent1");
		parent1.setConfirmation(false);
		destList1.add(parent1);
		message1.setDestinataires(destList1);
		
		messages.put(message1.getId(), message1);
		
		// 2e message
		Message message2 = new Message();
		message2.setTexte("Corps du message " + message2.getId());
		message2.setDate(new Date());
		
		List<Destinataire> destList2 = new ArrayList<Destinataire>();
		Destinataire parent2 = new Destinataire();
		parent2.setNom("parent2");
		parent2.setConfirmation(false);
		destList2.add(parent2);
		message2.setDestinataires(destList2);
		
		messages.put(message2.getId(), message2);
		
		// 3e message
		Message message3 = new Message();
		message3.setTexte("Corps du message " + message3.getId());
		message3.setDate(new Date());
		
		List<Destinataire> destList3 = new ArrayList<Destinataire>();
		Destinataire parent3 = new Destinataire();
		parent3.setNom("parent3");
		parent3.setConfirmation(false);
		destList3.add(parent3);
		message3.setDestinataires(destList3);
		
		messages.put(message3.getId(), message3);
		
		// 4e message
		Message message4 = new Message();
		message4.setTexte("Corps du message " + message4.getId());
		message4.setDate(new Date());

		List<Destinataire> destList = new ArrayList<Destinataire>();
		destList.add(parent1);
		destList.add(parent2);
		destList.add(parent3);
		message4.setDestinataires(destList);
		
		messages.put(message4.getId(), message4);
	}
}
