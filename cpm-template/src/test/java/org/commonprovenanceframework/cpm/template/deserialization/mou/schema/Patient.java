package org.commonprovenanceframework.cpm.template.deserialization.mou.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "patient")
public class Patient {
    @JacksonXmlProperty(isAttribute = true)
    private String biobank;
    @JacksonXmlProperty(isAttribute = true)
    private boolean consent;
    @JacksonXmlProperty(isAttribute = true)
    private int id;
    @JacksonXmlProperty(isAttribute = true)
    private String month;
    @JacksonXmlProperty(isAttribute = true)
    private String sex;
    @JacksonXmlProperty(isAttribute = true)
    private int year;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "LTS")
    private LTS lts;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "STS")
    private STS sts;

    public String getBiobank() {
        return biobank;
    }

    public void setBiobank(String biobank) {
        this.biobank = biobank;
    }

    public boolean isConsent() {
        return consent;
    }

    public void setConsent(boolean consent) {
        this.consent = consent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public LTS getLts() {
        return lts;
    }

    public void setLts(LTS lts) {
        this.lts = lts;
    }

    public STS getSts() {
        return sts;
    }

    public void setSts(STS sts) {
        this.sts = sts;
    }
}

