package it.finmatica.atti.trascodifiche

import groovy.sql.Sql
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.jsign.api.PKCS7Reader
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.apache.tika.sax.BodyContentHandler

import javax.sql.DataSource

class TrascodificaService {

    DataSource dataSource_gdm
    DataSource dataSource

    boolean importaFileAllegato () {
        //imposto il numero di file che verranno copiati in una transazione
        int fileCopiatiInTransazione = 100

        int i
        for (i = 0; i < fileCopiatiInTransazione; i++) {

            // leggo il primo file allegato con id superiore all'id dell'ultimo file allegato e che non appartiene a quelli già inseriti
            Sql sqlAtti = new Sql(dataSource);

            String queryFileAllegato = """ select id_file_allegato id
											from file_allegati
											where id_file_allegato not in (select id_file_allegato from trasco_allegati_testo)
											order by id_file_allegato ASC
										"""

            def rowFileAllegato = sqlAtti.firstRow(queryFileAllegato);

            // se non ho trovato nulla esco dal ciclo
            if (rowFileAllegato == null) {
                break
            };

            long idFileAllegato = rowFileAllegato.id
            FileAllegato fAllegato = FileAllegato.get(idFileAllegato)

            // leggo l'InputStream o da GDM o da file
            InputStream is
            if (fAllegato.idFileEsterno != null) {
                // se il documento è presente su gdm vado a prendere il blob su OGGETTI_FILE su Gdm
                String query = """ 	select testoOCR testo
								   	from oggetti_file
									where id_oggetto_file = :idFileEsterno	"""
                Sql sql = new Sql(dataSource_gdm);
                def row = sql.firstRow(query, [idFileEsterno: fAllegato.idFileEsterno]);
                if (row == null) {
                    log.debug(
                            "Problemi nel file allegato con id :" + fAllegato.id + " non è stato possibile estrarre il testo in quanto non presente su gdm il documento esterno")
                    sqlAtti.execute('insert into trasco_allegati_testo (id_file_allegato) values (?)', [fAllegato.id])
                    continue
                }

                def blob = row[0]
                is = blob.getBinaryStream()


            } else if (fAllegato.allegato != null) {
                // il file di testo è presente nella tabella FileAllegato
                is = new ByteArrayInputStream(fAllegato.allegato)

            } else {
                log.debug("Problemi nel file allegato con id :" + fAllegato.id + " non è stato possibile estrarre il testo in quanto non presente")
                sqlAtti.execute('insert into trasco_allegati_testo (id_file_allegato) values (?)', [fAllegato.id])
                continue
                // la continue passa al record successivo
            }

            // una volta letto il file verifico se è un p7m e nel caso lo sbusto
            if (fAllegato.isFirmato()) {
                PKCS7Reader reader = new PKCS7Reader(is)
                is = reader.getOriginalContent();
            }

            // con tika estraggo il testo
            try {
                Metadata metadata = new Metadata();
                metadata.set(Metadata.RESOURCE_NAME_KEY, fAllegato.nome);
                ParseContext context = new ParseContext();
                ContentHandler handler = new BodyContentHandler(-1);
                Parser parser = new AutoDetectParser() //new OOXMLParser() //new AutoDetectParser();
                parser.parse(is, handler, metadata, new ParseContext());
                String testoLetto = handler.toString()
                fAllegato.testo = testoLetto
            } catch (Exception e) {
                // Se va in crash può essere che in ingresso a tika ci va una immagine per cui non devo gestire l'errore

                log.debug("Nel file con id :" + fAllegato.id + " non è stato possibile estrarre il testo")

            }

            fAllegato.save()

            // salvo il record appena fatto in TRASCO_ALLEGATI_TESTO
            sqlAtti.execute('insert into trasco_allegati_testo (id_file_allegato) values (?)', [fAllegato.id])
        }

        if (i == fileCopiatiInTransazione) {
            // se cioè ho copiato un intero blocco completo allora possono essercene altri e restituisco true
            return true
        } else {
            return false
        }
    }
}
