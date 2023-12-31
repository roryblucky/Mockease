package com.rory.apimock.dto;

public interface Constants {

    String HTTPS_SCHEME = "https";

    String IDENTIFIER = "identifier";

    String DYNAMIC_RESPONSE = "dynamicResponse";
    String API_MOCK_ENDPOINT_PREFIX = "/mock";
    String API_MOCK_ENDPOINT_PREFIX_WILDCARD = "/mock/*";
    //Mock endpoints event bus address
    String API_PATH_STUB_CREATE_ADDRESS = "api.path.stub.created";
    String API_PATH_STUB_UPDATE_ADDRESS = "api.path.stub.update";
    String API_PATH_STUB_DELETE_ADDRESS = "api.path.stub.delete";
    String API_SERVICE_UPDATE_ADDRESS = "api.service.update";
    String API_SERVICE_DELETE_ADDRESS = "api.service.delete";


    String API_MOCK_WEBHOOK_ADDRESS = "api.mock.webhook";

}
