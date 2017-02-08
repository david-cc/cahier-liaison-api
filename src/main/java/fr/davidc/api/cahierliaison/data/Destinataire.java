package fr.davidc.api.cahierliaison.data;

/**
 * Bean Destinataire avec information de confirmation.
 * Utilisé dans Message.
 * @author vidda
 *
 */
public class Destinataire {

	public String nom;
	
	public boolean confirmation;

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public boolean isConfirmation() {
		return confirmation;
	}

	public void setConfirmation(boolean confirmation) {
		this.confirmation = confirmation;
	}
	
	
}
