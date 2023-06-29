package com.rory.apimock.dto;

public interface Constants {

     String MOCK_ROUTER = "mockRouter";

    //Admin API event bus address
     String API_SERVICES_CREATE_ADDRESS = "api.services.channel.create";


    //Mock endpoints event bus address
     String API_MOCK_CREATE_ADDRESS =  "api.mock.channel.create";
     String API_MOCK_UPDATE_ADDRESS =  "api.mock.channel.update";
     String API_MOCK_DELETE_ADDRESS =  "api.mock.channel.delete";
     String API_MOCK_CLEAR_ADDRESS =  "api.mock.channel.clear";

}
