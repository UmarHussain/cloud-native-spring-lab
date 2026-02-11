package com.example.order.api;

import com.example.order.svc.OrderAppService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderAppService app;

  public OrderController(OrderAppService app) {
    this.app = app;
  }

  public record CreateOrderItem(@NotBlank String sku, @Min(1) int qty) {}
  public record CreateOrderRequest(@NotEmpty List<CreateOrderItem> items) {}
  public record OrderResponse(UUID id, String status) {}

  @PostMapping
  public ResponseEntity<OrderResponse> create(@RequestHeader(value="Idempotency-Key", required=false) String idemKey,
      @Valid @RequestBody CreateOrderRequest req) {
    var order = app.createOrder(idemKey, req.items());
    return ResponseEntity.ok(new OrderResponse(order.id(), order.status()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> get(@PathVariable UUID id) {
    var order = app.getOrder(id);
    return ResponseEntity.ok(new OrderResponse(order.id(), order.status()));
  }
}
