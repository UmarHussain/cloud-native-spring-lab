package com.example.order.svc;

import com.example.order.api.OrderController.CreateOrderItem;
import com.example.order.domain.Order;
import com.example.order.domain.OrderStatus;
import com.example.order.repo.OrderRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OrderAppService {

  private final OrderRepository orderRepo;
  private final RestClient inventoryClient;

  public record OrderView(UUID id, String status) {}

  public OrderAppService(OrderRepository orderRepo,
      @Qualifier("inventoryClient") RestClient inventoryClient) {
    this.orderRepo = orderRepo;
    this.inventoryClient = inventoryClient;
  }

  @Transactional
  public OrderView createOrder(String idempotencyKey, List<CreateOrderItem> items) {
    // Skeleton: skip real idempotency storage for now; you’ll add it in Phase 2
    UUID orderId = UUID.randomUUID();
    Order order = new Order(orderId, OrderStatus.NEW, Instant.now());
    orderRepo.save(order);

    // Call inventory-service (REST) for reservation (Phase 1)
    var reserveReq = new InventoryReserveRequest(orderId, items);
    var reserveRes = inventoryClient.post()
        .uri("/api/inventory/reservations")
        .body(reserveReq)
        .retrieve()
        .body(InventoryReserveResponse.class);

    if (reserveRes != null && reserveRes.reserved()) {
      order.setStatus(OrderStatus.STOCK_RESERVED);
    } else {
      order.setStatus(OrderStatus.STOCK_REJECTED);
    }
    // JPA dirty-checking will update
    return new OrderView(order.getId(), order.getStatus().name());
  }

  public OrderView getOrder(UUID id) {
    var o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
    return new OrderView(o.getId(), o.getStatus().name());
  }

  // DTOs to match inventory-service API
  public record InventoryReserveRequest(UUID orderId, List<CreateOrderItem> items) {}
  public record InventoryReserveResponse(boolean reserved, String reason) {}
}

