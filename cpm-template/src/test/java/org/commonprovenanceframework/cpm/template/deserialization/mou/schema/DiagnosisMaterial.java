package org.commonprovenanceframework.cpm.template.deserialization.mou.schema;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javax.xml.datatype.XMLGregorianCalendar;

public class DiagnosisMaterial {
    @JacksonXmlProperty(isAttribute = true)
    private int number;
    @JacksonXmlProperty(isAttribute = true)
    private String sampleId;
    @JacksonXmlProperty(isAttribute = true)
    private int year;

    private String materialType;
    private String diagnosis;
    private XMLGregorianCalendar takingDate;
    private String retrieved;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public XMLGregorianCalendar getTakingDate() {
        return takingDate;
    }

    public void setTakingDate(XMLGregorianCalendar takingDate) {
        this.takingDate = takingDate;
    }

    public String getRetrieved() {
        return retrieved;
    }

    public void setRetrieved(String retrieved) {
        this.retrieved = retrieved;
    }
}
