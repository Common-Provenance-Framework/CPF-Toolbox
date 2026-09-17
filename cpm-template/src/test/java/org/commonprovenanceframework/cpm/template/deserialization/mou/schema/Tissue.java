package org.commonprovenanceframework.cpm.template.deserialization.mou.schema;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javax.xml.datatype.XMLGregorianCalendar;

public class Tissue {
    @JacksonXmlProperty(isAttribute = true)
    private int number;
    @JacksonXmlProperty(isAttribute = true)
    private String sampleId;
    @JacksonXmlProperty(isAttribute = true)
    private int year;

    private int samplesNo;
    private int availableSamplesNo;
    private int materialType;
    private String pTNM;
    private String morphology;
    private String diagnosis;
    private XMLGregorianCalendar cutTime;
    private XMLGregorianCalendar freezeTime;
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

    public int getSamplesNo() {
        return samplesNo;
    }

    public void setSamplesNo(int samplesNo) {
        this.samplesNo = samplesNo;
    }

    public int getAvailableSamplesNo() {
        return availableSamplesNo;
    }

    public void setAvailableSamplesNo(int availableSamplesNo) {
        this.availableSamplesNo = availableSamplesNo;
    }

    public int getMaterialType() {
        return materialType;
    }

    public void setMaterialType(int materialType) {
        this.materialType = materialType;
    }

    public String getpTNM() {
        return pTNM;
    }

    public void setpTNM(String pTNM) {
        this.pTNM = pTNM;
    }

    public String getMorphology() {
        return morphology;
    }

    public void setMorphology(String morphology) {
        this.morphology = morphology;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public XMLGregorianCalendar getCutTime() {
        return cutTime;
    }

    public void setCutTime(XMLGregorianCalendar cutTime) {
        this.cutTime = cutTime;
    }

    public XMLGregorianCalendar getFreezeTime() {
        return freezeTime;
    }

    public void setFreezeTime(XMLGregorianCalendar freezeTime) {
        this.freezeTime = freezeTime;
    }

    public String getRetrieved() {
        return retrieved;
    }

    public void setRetrieved(String retrieved) {
        this.retrieved = retrieved;
    }
}
