CREATE TABLE stock (
                       sku            VARCHAR(64) PRIMARY KEY,
                       available_qty  INT NOT NULL
);

CREATE TABLE reservations (
                              id        BIGSERIAL PRIMARY KEY,
                              order_id  UUID NOT NULL,
                              sku       VARCHAR(64) NOT NULL,
                              qty       INT NOT NULL,
                              status    VARCHAR(32) NOT NULL
);

INSERT INTO stock(sku, available_qty) VALUES
                                          ('SKU-1', 10),
                                          ('SKU-2', 5);
