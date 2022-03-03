package atti

import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.dto.documenti.*
import it.finmatica.atti.zk.components.FileAllegatoInfo
import it.finmatica.atti.zk.components.FileAllegatoTreeNode
import it.finmatica.atti.zk.components.FileAllegatoTreeNodeCollection
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.DefaultTreeModel
import org.zkoss.zul.TreeModel
import org.zkoss.zul.Treeitem
import org.zkoss.zul.Window

class PopupMarcaAllegatiViewModel {

    Window self

    def documento
    boolean marcati = false

    TreeModel model
    def selectedItems = new ArrayList();
    private int numeroFileAllegati = 0

    @NotifyChange("documento")
    @Init
    void init (
            @ContextParam(ContextType.COMPONENT) Window w,
            @ExecutionArgParam("documento") def documento, @ExecutionArgParam("marcati") boolean marcati) {
        this.self = w
        this.documento = documento
        this.marcati = marcati

        FileAllegatoTreeNode<FileAllegatoInfo> root = new FileAllegatoTreeNode<FileAllegatoInfo>(new FileAllegatoInfo(documento.id, documento.domainObject.TIPO_OGGETTO, "Atto"), null)
        addTesto(documento.testo, root)
        addAllegati(documento, root)
        addVistiECerficati(root)
        model = new DefaultTreeModel<FileAllegatoInfo>(root)
        model.setMultiple(true)
        numeroFileAllegati = selectedItems.size()
    }

    @Command
    void onChiudi () {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Command
    void onSalva () {
        Events.postEvent(Events.ON_CLOSE, self, [allegati         : selectedItems
                                                 , smarcaDocumento: (selectedItems.size() == numeroFileAllegati)])
    }

    private addAllegati (def documento, FileAllegatoTreeNode nodo) {
        def allegati = Allegato.createCriteria().list {
            if (documento instanceof DeterminaDTO) {
                eq("determina", documento.domainObject)
            } else if (documento instanceof DeliberaDTO) {
                eq("delibera", documento.domainObject)
            } else if (documento instanceof PropostaDeliberaDTO) {
                eq("propostaDelibera", documento.domainObject)
            } else if (documento instanceof VistoParereDTO) {
                eq("vistoParere", documento.domainObject)
            } else {
                isNull("id")
            }
            order("sequenza", "asc")
            order("titolo", "asc")
        }.toDTO(['fileAllegati'])

        for (AllegatoDTO allegato : allegati) {
            def list = new FileAllegatoTreeNodeCollection<FileAllegatoInfo>()
            for (def file : allegato.fileAllegati) {
                if (file.firmato && file.valido && marcati ? file.statoMarcatura == StatoMarcatura.MARCATO : file.statoMarcatura == StatoMarcatura.DA_MARCARE) {
                    FileAllegatoInfo fileInfo = new FileAllegatoInfo(file.id, "file", file.nome)
                    selectedItems.
                            add([idFileAllegato: file.id, idDocumento: allegato.id, tipoDocumento: Allegato.TIPO_OGGETTO])
                    list.add(new FileAllegatoTreeNode<FileAllegatoInfo>(fileInfo))
                }
            }
            if (list.size() > 0) {
                nodo.add(new FileAllegatoTreeNode<FileAllegatoInfo>(new FileAllegatoInfo(allegato.id, Allegato.TIPO_OGGETTO, allegato.titolo), list))
            }
        }
    }

    @Command
    void onSelect (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        selectedItems.clear()
        for (Treeitem item : event.getSelectedItems()) {
            selectedItems.add([idFileAllegato : item.value.getData().id
                               , idDocumento  : (item.getParentItem()?.value?.getData()?.id) ?: documento.id
                               , tipoDocumento: (item.getParentItem()?.value?.getData()?.tipologia) ?: documento.domainObject.TIPO_OGGETTO])
        }
    }

    private addTesto (def testo, FileAllegatoTreeNode nodo) {
        FileAllegato fileAllegato = testo.domainObject
        if (fileAllegato?.firmato && marcati ? fileAllegato.statoMarcatura == StatoMarcatura.MARCATO : fileAllegato.statoMarcatura == StatoMarcatura.DA_MARCARE) {
            FileAllegatoInfo testoInfo = new FileAllegatoInfo(fileAllegato.id, "file", fileAllegato.nome)
            selectedItems.add([idFileAllegato: testo.id, idDocumento: nodo.getData().id, tipoDocumento: nodo.
                    getData().tipologia])
            nodo.add(new FileAllegatoTreeNode<FileAllegatoInfo>(testoInfo, null))
        }
    }

    private addVistiECerficati (FileAllegatoTreeNode nodo) {
        if (documento instanceof DeterminaDTO || documento instanceof DeliberaDTO || documento instanceof PropostaDeliberaDTO) {
            List<VistoParere> visti = VistoParere.createCriteria().list {
                if (documento instanceof DeterminaDTO) {
                    eq("determina", documento.domainObject)
                } else if (documento instanceof DeliberaDTO) {
                    or {
                        eq("propostaDelibera", documento.proposta.domainObject)
                        eq("delibera", documento.domainObject)
                    }
                } else if (documento instanceof PropostaDeliberaDTO) {
                    eq("propostaDelibera", documento.domainObject)
                }

                if (marcati) {
                    eq("statoMarcatura", StatoMarcatura.MARCATO)
                } else {
                    eq("statoMarcatura", StatoMarcatura.DA_MARCARE)
                }

                eq("valido", true)

                tipologia {
                    order("sequenzaStampaUnica", "asc")
                }
            }.toDTO()
            for (def visto : visti) {
                FileAllegatoTreeNode nodoVisto = new FileAllegatoTreeNode<FileAllegatoInfo>(new FileAllegatoInfo(visto.id, VistoParere.TIPO_OGGETTO, visto.tipologia.titolo), new FileAllegatoTreeNodeCollection<FileAllegatoInfo>())
                addTesto(visto.testo, nodoVisto)
                addAllegati(visto, nodoVisto)
                if (nodoVisto.childCount > 0) {
                    nodo.add(nodoVisto)
                }
            }
        }
        if (documento instanceof DeterminaDTO || documento instanceof DeliberaDTO) {
            List<CertificatoDTO> certificati = Certificato.createCriteria().list {
                if (documento instanceof DeterminaDTO) {
                    eq("determina", documento.domainObject)
                } else if (documento instanceof DeliberaDTO) {
                    eq("delibera", documento.domainObject)
                }

                if (marcati) {
                    eq("statoMarcatura", StatoMarcatura.MARCATO)
                }
                else {
                    eq("statoMarcatura", StatoMarcatura.DA_MARCARE)
                }

                eq("valido", true)
                tipologia {
                    order("titolo", "asc")
                }

            }.toDTO()

            for (CertificatoDTO c : certificati) {
                if (c.valido && c.testo != null) {
                    FileAllegatoTreeNode nodoCertificato = new FileAllegatoTreeNode<FileAllegatoInfo>(new FileAllegatoInfo(c.id, Certificato.TIPO_OGGETTO, c.tipologia.titolo), new FileAllegatoTreeNodeCollection<FileAllegatoInfo>())
                    addTesto(c.testo, nodoCertificato)
                    if (nodoCertificato.childCount > 0) {
                        nodo.add(nodoCertificato)
                    }
                }
            }
        }
    }
}
