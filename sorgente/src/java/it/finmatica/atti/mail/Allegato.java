package it.finmatica.atti.mail;

import java.io.ByteArrayInputStream;

public class Allegato {
	private String 					nome;
	private ByteArrayInputStream 	testo;

	public Allegato (String nome, ByteArrayInputStream testo) {
		this.nome = nome;
		this.testo = testo;
	}

	public String getNome() {
		return nome;
	}

	public ByteArrayInputStream getTesto() {
		return testo;
	}
}
