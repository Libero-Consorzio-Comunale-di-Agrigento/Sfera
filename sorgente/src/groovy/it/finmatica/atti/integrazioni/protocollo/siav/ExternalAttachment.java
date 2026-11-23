package it.finmatica.atti.integrazioni.protocollo.siav;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import integrazione.siav.classes.Attachment;
import it.finmatica.atti.integrazioniws.siav.archiflow.card.TimeStampFileFormat;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExternalAttachment", namespace = "http://schemas.datacontract.org/2004/07/SIAV.Archiflow.Entities.Card.ValueObject", propOrder = {
        "__type",
        "IsInInvoiceXML",
        "IsInteroperability",
        "IsReadOnly",
        "IsSealed",
        "IsSigned",
        "IsSignedPdf",
        "IsSignedXml",
        "IsTimeStamp",
        "SignedExtension",
        "Size",
        "TimeStampFormat",
        "VolumeID",
        "WarningExtensionOff"
})
@JsonPropertyOrder({
        "__type",
        "SecurityUserId",
        "Name",
        "Note",
        "Extension",
        "Content"
})
public class ExternalAttachment   extends Attachment
{
    @XmlElement(nillable = true)
    public String __type;
    @XmlElement(
            name = "IsInInvoiceXML"
    )
    public Boolean IsInInvoiceXML;
    @XmlElement(
            name = "IsInteroperability"
    )
    public Boolean IsInteroperability;
    @XmlElement(
            name = "IsReadOnly"
    )
    public Boolean IsReadOnly;
    @XmlElement(
            name = "IsSealed"
    )
    public Boolean IsSealed;
    @XmlElement(
            name = "IsSigned"
    )
    public Boolean IsSigned;
    @XmlElement(
            name = "IsSignedPdf"
    )
    public Boolean IsSignedPdf;
    @XmlElement(
            name = "IsSignedXml"
    )
    public Boolean IsSignedXml;
    @XmlElement(
            name = "IsTimeStamp"
    )
    public Boolean IsTimeStamp;
    @XmlElement(
            name = "SignedExtension",
            nillable = true
    )
    public String SignedExtension;
    @XmlElement(
            name = "Size"
    )
    @XmlSchemaType(
            name = "unsignedInt"
    )
    public Long Size;
    @XmlElement(
            name = "TimeStampFormat"
    )
    public TimeStampFileFormat TimeStampFormat;
    @XmlElement(
            name = "VolumeID"
    )
    public Integer VolumeID;
    @XmlElement(
            name = "WarningExtensionOff"
    )
    public Boolean WarningExtensionOff;

}
