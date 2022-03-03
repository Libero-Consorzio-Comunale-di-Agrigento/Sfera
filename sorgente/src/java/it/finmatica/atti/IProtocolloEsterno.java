package it.finmatica.atti;

import it.finmatica.atti.documenti.IFascicolabile;
import it.finmatica.atti.documenti.IProtocollabile;
import it.finmatica.docer.atti.anagrafiche.DatiRicercaDocumento;
import org.apache.poi.hpsf.ClassID;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public interface IProtocolloEsterno {

	void fascicola (IFascicolabile documento);

	void protocolla (IProtocollabile documento);

	List<Classifica> getListaClassificazioni (String filtro, String codiceUoProponente);

	Classifica getClassifica (String codice, Date dal);

	Fascicolo getFascicolo (Classifica classifica, String numero, int anno);

	List<Fascicolo> getListaFascicoli (String filtro, String codiceClassifica, Date lassificaDal, String codiceUoProponente);

	void creaFascicolo (String numero, String anno, String descrizione, String parent_progressivo, String classifica);

	List<Documento> getListaDocumenti (DatiRicercaDocumento datiRicerca);

	InputStream downloadFile(String docNum);

    void creaAllegatoProtocollo(IProtocollabile documento, String descrizione, String nomeFileAllegato, InputStream is);
	
	/**
	 * Aggiorna le classificazioni tra il protocollo e Sfera.
	 * Tale funzione serve in particolare per l'integrazione con AGSPR quando le classificazioni possono essere disallineate perché
	 * modificate da AGSPR e non su sfera.
	 */
	void sincronizzaClassificazioniEFascicoli ();

	class Documento {

		private String docNum, typeId, codEnte, codAoo, statoArchivistico;
		private String statoBusinness, docName, descrizione,dataAcquisizione;
		private String tipoComponente, classifica, progrFascicolo;
		private String annoFascicolo, numProtocollo, annoProtocollo;
		private String oggettoProtocollo, registroProtocollo;
		private String numRegistrazione, annoRegistrazione, oggettoRegistrazione;
		private String idRegistrazione, numPubblicazione, annoPubblicazione;
		private String oggettoPubblicazione, registroPubblicazione;
		private String statoConservazione, tipoConservazione;

		public String getDocNum() {
			return docNum;
		}
		public void setDocNum(String docNum) {
			this.docNum = docNum;
		}
		public String getTypeId() {
			return typeId;
		}
		public void setTypeId(String typeId) {
			this.typeId = typeId;
		}
		public String getCodEnte() {
			return codEnte;
		}
		public void setCodEnte(String codEnte) {
			this.codEnte = codEnte;
		}
		public String getCodAoo() {
			return this.codAoo;
		}
		public void setCodAoo(String codAoo) {
			this.codAoo = codAoo;
		}
		public String getStatoArchivistico() {
			return statoArchivistico;
		}
		public void setStatoArchivistico(String statoArchivistico) {
			this.statoArchivistico = statoArchivistico;
		}
		public String getStatoBusinness() {
			return statoBusinness;
		}
		public void setStatoBusinness(String statoBusinness) {
			this.statoBusinness = statoBusinness;
		}
		public String getDocName() {
			return docName;
		}
		public void setDocName(String docName) {
			this.docName = docName;
		}
		public String getDescrizione() {
			return descrizione;
		}
		public void setDescrizione(String descrizione) {
			this.descrizione = descrizione;
		}
		public String getDataAcquisizione() {
			return dataAcquisizione;
		}
		public void setDataAcquisizione(String dataAcquisizione) {
			this.dataAcquisizione = dataAcquisizione;
		}
		public String getTipoComponente() {
			return tipoComponente;
		}
		public void setTipoComponente(String tipoComponente) {
			this.tipoComponente = tipoComponente;
		}
		public String getClassifica() {
			return classifica;
		}
		public void setClassifica(String classifica) {
			this.classifica = classifica;
		}
		public String getProgrFascicolo() {
			return progrFascicolo;
		}
		public void setProgrFascicolo(String progrFascicolo) {
			this.progrFascicolo = progrFascicolo;
		}
		public String getAnnoFascicolo() {
			return annoFascicolo;
		}
		public void setAnnoFascicolo(String annoFascicolo) {
			this.annoFascicolo = annoFascicolo;
		}
		public String getNumProtocollo() {
			return numProtocollo;
		}
		public void setNumProtocollo(String numProtocollo) {
			this.numProtocollo = numProtocollo;
		}
		public String getAnnoProtocollo() {
			return annoProtocollo;
		}
		public void setAnnoProtocollo(String annoProtocollo) {
			this.annoProtocollo = annoProtocollo;
		}
		public String getOggettoProtocollo() {
			return oggettoProtocollo;
		}
		public void setOggettoProtocollo(String oggettoProtocollo) {
			this.oggettoProtocollo = oggettoProtocollo;
		}
		public String getRegistroProtocollo() {
			return registroProtocollo;
		}
		public void setRegistroProtocollo(String registroProtocollo) {
			this.registroProtocollo = registroProtocollo;
		}
		public String getNumRegistrazione() {
			return numRegistrazione;
		}
		public void setNumRegistrazione(String numRegistrazione) {
			this.numRegistrazione = numRegistrazione;
		}
		public String getAnnoRegistrazione() {
			return annoRegistrazione;
		}
		public void setAnnoRegistrazione(String annoRegistrazione) {
			this.annoRegistrazione = annoRegistrazione;
		}
		public String getOggettoRegistrazione() {
			return oggettoRegistrazione;
		}
		public void setOggettoRegistrazione(String oggettoRegistrazione) {
			this.oggettoRegistrazione = oggettoRegistrazione;
		}
		public String getIdRegistrazione() {
			return idRegistrazione;
		}
		public void setIdRegistrazione(String idRegistrazione) {
			this.idRegistrazione = idRegistrazione;
		}
		public String getNumPubblicazione() {
			return numPubblicazione;
		}
		public void setNumPubblicazione(String numPubblicazione) {
			this.numPubblicazione = numPubblicazione;
		}
		public String getAnnoPubblicazione() {
			return annoPubblicazione;
		}
		public void setAnnoPubblicazione(String annoPubblicazione) {
			this.annoPubblicazione = annoPubblicazione;
		}
		public String getOggettoPubblicazione() {
			return oggettoPubblicazione;
		}
		public void setOggettoPubblicazione(String oggettoPubblicazione) {
			this.oggettoPubblicazione = oggettoPubblicazione;
		}
		public String getRegistroPubblicazione() {
			return registroPubblicazione;
		}
		public void setRegistroPubblicazione(String registroPubblicazione) {
			this.registroPubblicazione = registroPubblicazione;
		}
		public String getStatoConservazione() {
			return statoConservazione;
		}
		public void setStatoConservazione(String statoConservazione) {
			this.statoConservazione = statoConservazione;
		}
		public String getTipoConservazione() {
			return tipoConservazione;
		}
		public void setTipoConservazione(String tipoConservazione) {
			this.tipoConservazione = tipoConservazione;
		}
	}

	class Classifica {
		private String codice;
		private String descrizione;
		private Date dal;

		public Date getDal () {
			return dal;
		}

		public void setDal (Date dal) {
			this.dal = dal;
		}

		public String getCodice () {
			return codice;
		}

		public void setCodice (String codice) {
			this.codice = codice;
		}

		public String getDescrizione () {
			return descrizione;
		}

		public void setDescrizione (String descrizione) {
			this.descrizione = descrizione;
		}
	}

	class Fascicolo {
		private Classifica classifica;
		private int anno;
		private String numero;
		private String oggetto;
		private String sub;

		public Classifica getClassifica () {
			return classifica;
		}

		public void setClassifica (Classifica classifica) {
			this.classifica = classifica;
		}

		public int getAnno () {
			return anno;
		}

		public void setAnno (int anno) {
			this.anno = anno;
		}

		public String getOggetto () {
			return oggetto;
		}

		public void setOggetto (String oggetto) {
			this.oggetto = oggetto;
		}

		public String getNumero () {
			return numero;
		}

		public void setNumero (String numero) {
			this.numero = numero;
		}

		public String getSub () {
			return sub;
		}

		public void setSub (String sub) {
			this.sub = sub;
		}

	}
}
