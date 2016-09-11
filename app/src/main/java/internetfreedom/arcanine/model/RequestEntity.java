package internetfreedom.arcanine.model;

import java.util.List;

/**
 * Author: @karthikb351
 * Project: arcanine
 */
public class RequestEntity {
    String code;
    String body;
    List<String> headerKeys;
    List<String> headerValues;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getHeaderKeys() {
        return headerKeys;
    }

    public void setHeaderKeys(List<String> headerKeys) {
        this.headerKeys = headerKeys;
    }

    public List<String> getHeaderValues() {
        return headerValues;
    }

    public void setHeaderValues(List<String> headerValues) {
        this.headerValues = headerValues;
    }
}
