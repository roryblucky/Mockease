create table api_service
(
    api_service_id varchar(64)   not null primary key,
    name           varchar(200)  not null,
    category_id    varchar(64)   not null,
    description    varchar(4000),
    prefix         varchar(1000) not null,
    version        varchar(64)   not null,
    create_at      timestamptz   not null,
    update_at      timestamptz   not null

);

create table api_path_stub
(
    api_path_stub_id      varchar(64)   not null primary key,
    api_service_id        varchar(64)   not null,
    name                  varchar(200)  not null,
    description           varchar(4000),
    operation_id          varchar(64)   not null,
    path                  varchar(1000) not null,
    method                varchar(64)   not null,
    request_headers       text          not null,
    validation_enabled    boolean       not null,
    request_schema        text,
    request_dynamic_body  boolean       not null,
    request_body          text,
    response_http_status  varchar(64)   not null,
    response_headers      text,
    response_dynamic_body boolean       not null,
    response_body         text          not null,
    api_webhook_id        varchar(64),
    api_proxy_id          varchar(64),
    create_at             timestamptz   not null,
    update_at             timestamptz   not null
);

create table api_webhook
(
    api_webhook_id       varchar(64)   not null primary key,
    url                  varchar(1000) not null,
    method               varchar(64)   not null,
    headers              text,
    request_dynamic_body boolean       not null,
    body                 text          not null,
    create_at            timestamptz   not null,
    update_at            timestamptz   not null
);

create table api_proxy
(
    api_proxy_id varchar(64)   not null primary key,
    url          varchar(1000) not null,
    create_at    timestamptz   not null,
    update_at    timestamptz   not null
);

create table api_category
(
    api_category_id varchar(64)  not null primary key,
    name            varchar(200) not null,
    description     varchar(4000),
    create_at       timestamptz  not null,
    update_at       timestamptz  not null
);



