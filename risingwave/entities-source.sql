CREATE SOURCE IF NOT EXISTS entities (
    id        BIGINT,
    type      VARCHAR,
    payload   VARCHAR,
    ts        VARCHAR
) WITH (
    connector                   = 'kafka',
    topic                       = 'entities',
    properties.bootstrap.server = 'kafka.risingwavepoc-kafka.svc.cluster.local:9092',
    scan.startup.mode           = 'earliest'
) FORMAT PLAIN ENCODE PROTOBUF (
    message         = 'Entity',
    schema.location = 'http://proto-server.risingwavepoc.svc.cluster.local:80/entity.pb'
);
