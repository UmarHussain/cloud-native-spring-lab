package com.example.order.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
  @Id
  private UUID id;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Order() {}

  public Order(UUID id, OrderStatus status, Instant createdAt) {
    this.id = id;
    this.status = status;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public OrderStatus getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }

  public void setStatus(OrderStatus status) { this.status = status; }
}

