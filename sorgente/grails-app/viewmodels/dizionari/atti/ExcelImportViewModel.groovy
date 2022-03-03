package dizionari.atti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.atti.dto.dizionari.TipoBudgetDTO
import it.finmatica.atti.dto.dizionari.TipoBudgetDTOService
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.log4j.Logger
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.ExcelImportService

import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.util.media.Media
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.UploadEvent
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

import javax.servlet.ServletContext

class ExcelImportViewModel {

    private static final Logger log = Logger.getLogger(ExcelImportViewModel.class)

	def self
	def grailsApplication
	
	// services
    TipoBudgetDTOService tipoBudgetDTOService
    SpringSecurityService springSecurityService
    ServletContext servletContext

	List<Map> budgets

	def excel

    @Init
    init (@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
	}

	@Command uploadAllegato(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
		excel = new ExcelAllegato()

		Media media = event.getMedia()

		excel.allegato  	= media.getStreamData()
		excel.nome   		= media.getName()
		excel.contentType 	= media.getContentType()
		BindUtils.postNotifyChange(null, null, this, "excel")
	}

	@Command downloadTemplate() {
		File template = new File(servletContext.getRealPath("WEB-INF") +"/configurazioneStandard/modelliTesto/xlsx/IMPORT_TIPIBUDGET.xlsx")
		Filedownload.save(template, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	}
	
	//E' solo una prova per elaborazioni in locale
	@Command onImportaBudget() {
		if (!excel || !excel?.nome)
			Messagebox.show("Caricare un file compilato come da template.", "Attenzione", Messagebox.OK, Messagebox.EXCLAMATION)
		else {
			BudgetExcelImporter importer = new BudgetExcelImporter(excel.allegato)
			budgets = importer.getBudget()
            verificaDati();
			BindUtils.postNotifyChange(null, null, this, "budgets")
		}
	}


	//E' solo una prova per elaborazioni in locale
	@Command onSalvaBudget() {
		if (!budgets || !(budgets.size() > 0))
			Messagebox.show("Lista vuota.", "Attenzione", Messagebox.OK, Messagebox.EXCLAMATION)
		else {
			Messagebox.show("L'operazione non può essere annullata.", "Confermi l'import?", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							boolean ok = true
                            TipoBudget.withTransaction {
								for (def bdg in budgets) {
                                    // verifica dei parametri
                                    if (bdg.errore == "") {
                                        try {
                                            BigDecimal importoIniziale = new BigDecimal(bdg.importo.toBigDecimal())
                                            So4UnitaPubbDTO unitaPubbDTO = So4UnitaPubb.findByDescrizioneAndAlIsNullAndAmministrazione(bdg.ufficio, springSecurityService.principal.amministrazione)?.toDTO()
                                            Ad4UtenteDTO utenteDTO = Ad4Utente.findByNominativoSoggetto(bdg.responsabile).toDTO()

                                            def dto = new TipoBudgetDTO([tipo             : bdg.tipo
                                                                         , titolo         : bdg.titolo
                                                                         , anno           : Integer.parseInt(isIntegerParseInt(bdg.anno.toString()))
                                                                         , contoEconomico : bdg.contoEconomico
                                                                         , attivo         : bdg.attivo?.toBoolean()
                                                                         , importoIniziale: importoIniziale
                                                                         , unitaSo4       : unitaPubbDTO
                                                                         , utenteAd4      : utenteDTO
                                                                         , valido         : true
                                            ])
                                            tipoBudgetDTOService.salva(dto)
                                        } catch (Exception ex){
                                            log.error("Errore nell'import dell'excel", ex);
                                            ok = false;
                                        }
                                    }
								}
							}
							if (ok) {
								onChiudi()
								Messagebox.show("Import terminato con successo.", "Operazione completata", Messagebox.OK, Messagebox.INFORMATION)
							}
						}
					}
				}
			)
		}
	}
	
	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onCancel () {
		budgets 	= null
		excel		= null
		BindUtils.postNotifyChange(null, null, this, "budgets")
		BindUtils.postNotifyChange(null, null, this, "excel")
	}
	

	private String isIntegerParseInt(String str){
		String newValue = ""
		try{
			newValue = new BigDecimal(str.toBigDecimal())
			return newValue
		}catch(Exception exc){
			return str
		}
	}

    private void verificaDati(){
        for (def bdg in budgets) {
            // verifica dei parametri
            bdg.errore = ""
            So4UnitaPubb unitaPubb
            Ad4Utente utente
            Integer anno = 0

            try {
                BigDecimal importoIniziale = new BigDecimal(bdg.importo?.toBigDecimal())
            }
            catch (Exception ex){
                bdg.errore += "Importo errato\n"
            }
            try {
                anno = Integer.parseInt(isIntegerParseInt(bdg.anno.toString()))
            } catch (Exception ex){
                bdg.errore += "Anno errato\n"
            }

            try {
                unitaPubb = So4UnitaPubb.findByDescrizioneAndAlIsNullAndAmministrazione(bdg.ufficio, springSecurityService.principal.amministrazione)
                if (bdg.ufficio== null || unitaPubb == null){
                    bdg.errore += "Ufficio non trovato\n"
                }
            }
            catch (Exception ex){
                bdg.errore += "Ufficio non trovato\n"
            }
            try {
                utente = Ad4Utente.findByNominativoSoggetto(bdg.responsabile)
                if (bdg.responsabile == null || utente == null){
                    bdg.errore += "Responsabile non trovato\n"
                }
            }
            catch (Exception ex){
                bdg.errore += "Responsabile non trovato\n"
            }

            if (bdg.errore == ""){
                if (TipoBudget.countByAnnoAndAttivoAndUnitaSo4(anno, true, unitaPubb) > 0){
                    bdg.errore += "Budget attivo già presente per anno/Ufficio"
                }
            }

        }
    }

}

class ExcelAllegato {
    InputStream allegato;
    String contentType;
    String nome;
}

class BudgetExcelImporter extends AbstractExcelImporter {

    static Map CONFIG_BUDGET_COLUMN_MAP = [
        sheet:'Budget',
        startRow: 1,
        columnMap:  [
            'a':'tipo'
            , 'b':'titolo'
            , 'c':'anno'
            , 'd':'attivo'
            , 'e':'contoEconomico'
            , 'f':'ufficio'
            , 'g':'responsabile'
            , 'h':'importo'
        ]
    ]


    //can also configure injection in resources.groovy
    def getExcelImportService() {
        ExcelImportService.getService()
    }

    public BudgetExcelImporter(InputStream inputStream) {
        super.read(inputStream)
    }

    List<Map> getBudget() {
        excelImportService.columns(workbook, CONFIG_BUDGET_COLUMN_MAP)
    }
}