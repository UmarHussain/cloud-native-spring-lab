CREATE TABLE orders (
                        id            UUID PRIMARY KEY,
                        status        VARCHAR(32) NOT NULL,
                        created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
                             id        BIGSERIAL PRIMARY KEY,
                             order_id  UUID NOT NULL,
                             sku       VARCHAR(64) NOT NULL,
                             qty       INT NOT NULL,
                             CONSTRAINT fk_order FOREIGN KEY(order_id) REFERENCES orders(id)
);
