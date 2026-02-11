package com.example.inventory.svc;

import com.example.inventory.repo.StockRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InventoryAppService {

  private final StockRepository stockRepo;

  public InventoryAppService(StockRepository stockRepo) {
    this.stockRepo = stockRepo;
  }

  public record Item(String sku, int qty) {}
  public record ReserveResult(boolean reserved, String reason) {}

  @Transactional
  public ReserveResult reserve(UUID orderId, List<Item> items) {
    // naive reservation for skeleton (no concurrency handling yet)
    for (var i : items) {
      var stockOpt = stockRepo.findById(i.sku());

      if (stockOpt.isEmpty()) {
        return new ReserveResult(false, "Unknown SKU: " + i.sku());
      }

      if (stockOpt.get().getAvailableQty() < i.qty()) {
        return new ReserveResult(false, "Insufficient stock for " + i.sku());
      }
    }
    // deduct
    for (var i : items) {
      var stock = stockRepo.findById(i.sku()).orElseThrow();
      stock.setAvailableQty(stock.getAvailableQty() - i.qty());
    }
    return new ReserveResult(true, "OK");
  }
}
