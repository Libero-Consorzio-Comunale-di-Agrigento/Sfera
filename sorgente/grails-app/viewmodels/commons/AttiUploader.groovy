package commons

import it.finmatica.atti.impostazioni.Impostazioni
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException

/**
 * Created by czappavigna on 05/12/2016.
 */
import org.zkoss.zk.au.http.AuUploader

class AttiUploader extends AuUploader {

    protected String handleError(Throwable ex) {
        if (ex instanceof SizeLimitExceededException) {
            SizeLimitExceededException e = (SizeLimitExceededException) ex;
            int exp = (int) (Math.log(e.getActualSize()) / Math.log(1024));
            return "La dimensione del file (" + String.format("%.1f", e.getActualSize() / Math.pow(1024, exp)) + " MB) supera la dimensione massima configurata (" + Impostazioni.ALLEGATO_DIMENSIONE_MASSIMA.valore + " MB)";
        }
        return super.handleError(ex);
    }
}