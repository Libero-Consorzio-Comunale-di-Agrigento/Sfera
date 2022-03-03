package it.finmatica.atti.commons;

import org.apache.commons.io.FileUtils;

import java.io.*;

/**
 * FileInputStream wrapper che elimina il file sulla chiusura dello stream.
 * È una classe utile quando si vuole fare il download di un file creato temporaneamente.
 * Viene usata ad esempio in GestioneTestiAction.downloadZipAllegati
 *
 * Created by esasdelli on 06/02/2017.
 */
public class DeleteOnCloseFileInputStream extends FilterInputStream {

    private File file;

    public DeleteOnCloseFileInputStream (File file) throws FileNotFoundException {
        this (new FileInputStream(file));
        this.file = file;
    }

    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     */
    private DeleteOnCloseFileInputStream(InputStream in) {
        super(in);
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteQuietly(file);
        super.close();
    }
}
