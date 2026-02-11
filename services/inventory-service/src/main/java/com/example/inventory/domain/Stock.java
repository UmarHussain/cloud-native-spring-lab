package com.example.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="stock")
public class Stock {
  @Id
  private String sku;

  private int availableQty;

  protected Stock() {}

  public String getSku() { return sku; }
  public int getAvailableQty() { return availableQty; }
  public void setAvailableQty(int availableQty) { this.availableQty = availableQty; }
}

