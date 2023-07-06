package com.rory.apimock.dto;

import com.rory.apimock.dto.web.APIPathDefinition;
import com.rory.apimock.dto.web.APIService;
import com.rory.apimock.dto.web.ResponseInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class APIStub implements Serializable {


    public APIStub(String apiServiceId, String operationId) {
        this.apiServiceId = apiServiceId;
        this.operationId = operationId;
    }

    public APIStub(APIService apiService, APIPathDefinition pathDefinition) {
        //service
        this.apiServiceId = apiService.getId();
        this.version = apiService.getVersion();
        this.basePath =  apiService.getBasePath();
        //Request
        this.operationId = pathDefinition.getOperationId();
        this.path = pathDefinition.getRequest().getPath();
        this.method = pathDefinition.getRequest().getMethod();
        this.validationEnabled = pathDefinition.getRequest().isValidationEnabled();
        this.requestSchema = pathDefinition.getRequest().getSchema();
        this.requestHeaders = convert(pathDefinition.getRequest().getHeaders());
        this.requestBody = pathDefinition.getRequest().getBody();
        this.requestDynamicBodyEnabled = pathDefinition.getRequest().isDynamicBodyEnabled();
        //Response
        final ResponseInfo response = pathDefinition.getResponse();
        this.responseHttpStatus = response.getHttpStatus();
        this.responseHeaders = convert(response.getHeaders());
        this.responseDynamicBodyEnabled = response.isDynamicBodyEnabled();
        this.responseBody = response.getBody();
        this.webhookEnabled = response.isWebhookEnabled();
        this.proxyEnabled = response.isProxyEnabled();
        if (response.getWebhook() != null) {
            this.webhookUrl = response.getWebhook().getUrl();
            this.webhookMethod = response.getWebhook().getMethod();
            this.webhookDynamicBodyEnabled = response.getWebhook().isDynamicBodyEnabled();
            this.webhookHeaders = convert(response.getWebhook().getHeaders());
            this.webhookBody = response.getWebhook().getBody();
        }
        if (response.getProxy() != null) {
            this.proxyUrl = response.getProxy().getUrl();
        }
    }

    private String apiServiceId;
    private String version;
    private String basePath;
    private String operationId;

    private String path;

    private String method;

    private boolean validationEnabled;

    private String requestSchema;

    private Map<String, String> requestHeaders;

    private String requestBody;

    private boolean requestDynamicBodyEnabled;


    private Integer responseHttpStatus;


    private Map<String, String> responseHeaders;

    private boolean responseDynamicBodyEnabled;


    private String responseBody;

    private boolean webhookEnabled;
    private boolean proxyEnabled;

    private String webhookUrl;

    private String webhookMethod;

    private boolean webhookDynamicBodyEnabled;

    private Map<String, String> webhookHeaders;

    private String webhookBody;

    private String proxyUrl;

    private Map<String, String> convert(Map<String, Object> map) {
        return map.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue()));
    }


    public String getIdentifier() {
        return this.apiServiceId + this.operationId;
    }

    public String getWholeUrl() {
        return "/" + this.version + this.basePath + this.path;
    }

}
