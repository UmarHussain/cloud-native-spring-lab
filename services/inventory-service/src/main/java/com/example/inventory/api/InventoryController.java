package com.example.inventory.api;

import com.example.inventory.svc.InventoryAppService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

  private final InventoryAppService app;

  public InventoryController(InventoryAppService app) {
    this.app = app;
  }

  public record ReserveItem(@NotBlank String sku, @Min(1) int qty) {}
  public record ReserveRequest(UUID orderId, @NotEmpty List<ReserveItem> items) {}
  public record ReserveResponse(boolean reserved, String reason) {}

  @PostMapping("/reservations")
  public ResponseEntity<ReserveResponse> reserve(@Valid @RequestBody ReserveRequest req) {
    var items = req.items().stream()
        .map(i -> new InventoryAppService.Item(i.sku(), i.qty()))
        .toList();

    var res = app.reserve(req.orderId(), items);
    return ResponseEntity.ok(new ReserveResponse(res.reserved(), res.reason()));
  }
}
