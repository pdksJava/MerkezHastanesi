package com.pdks.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for sendERPIzinler complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendERPIzinler">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="izin" type="{http://webService.pdks.com/}izinERP" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendERPIzinler", propOrder = { "izin" })
public class SendERPIzinler {

	protected List<IzinERP> izin;

	/**
	 * Gets the value of the izin property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the izin property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getIzin().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link IzinERP }
	 * 
	 * 
	 */
	public List<IzinERP> getIzin() {
		if (izin == null) {
			izin = new ArrayList<IzinERP>();
		}
		return this.izin;
	}

}
