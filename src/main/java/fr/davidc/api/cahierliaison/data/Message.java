package fr.davidc.api.cahierliaison.data;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bean représentant le Message du cahier de liaison.
 * @author vidda
 *
 */
public class Message {

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private final int id;
	
	/**
	 * Texte du message.
	 */
	private String texte;
	
	/**
	 * Date de création du message.
	 */
	private Date date;
	
	/**
	 * Destinataires du message.
	 */
	private List<Destinataire> destinataires;
	
	public Message() {
		this.id = COUNTER.getAndIncrement();
	}

	public String getTexte() {
		return texte;
	}

	public void setTexte(String texte) {
		this.texte = texte;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Destinataire> getDestinataires() {
		return destinataires;
	}

	public void setDestinataires(List<Destinataire> destinataires) {
		this.destinataires = destinataires;
	}

	public int getId() {
		return id;
	}
}
