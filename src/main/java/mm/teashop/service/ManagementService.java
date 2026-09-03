package mm.teashop.service;

import lombok.RequiredArgsConstructor;
import mm.teashop.api.ApiDtos.*;
import mm.teashop.domain.*;
import mm.teashop.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ManagementService {
	private final IngredientRepository ingredients;
	private final StockMovementRepository movements;
	private final WaiterRepository waiters;
	private final ShopTableRepository tables;

	@Transactional
	public Ingredient saveIngredient(Long id, IngredientRequest r) {
		var i = id == null ? new Ingredient() : ingredients.findById(id).orElseThrow();
		i.setName(r.name());
		i.setUnit(r.unit());
		i.setStockQty(r.stockQty());
		i.setSafetyQty(r.safetyQty());
		i.setLastPurchaseDate(r.lastPurchaseDate());
		i.setExpiryDate(r.expiryDate());
		i.setSupplier(r.supplier());
		i.setCostPerUnit(r.costPerUnit());
		return ingredients.save(i);
	}

	@Transactional
	public StockMovement stock(Long id, StockRequest r) {
		var i = ingredients.findById(id).orElseThrow();
		boolean add = r.type() == Enums.StockMovementType.PURCHASE || r.type() == Enums.StockMovementType.ADJUSTMENT_IN;
		var delta = add ? r.quantity() : r.quantity().negate();
		if (i.getStockQty().add(delta).compareTo(BigDecimal.ZERO) < 0)
			throw new IllegalArgumentException("Stock cannot be negative");
		i.setStockQty(i.getStockQty().add(delta));
		if (r.purchaseDate() != null)
			i.setLastPurchaseDate(r.purchaseDate());
		if (r.expiryDate() != null)
			i.setExpiryDate(r.expiryDate());
		return movements.save(StockMovement.builder().ingredient(i).type(r.type()).quantity(delta)
				.balanceAfter(i.getStockQty()).createdAt(LocalDateTime.now()).purchaseDate(r.purchaseDate())
				.expiryDate(r.expiryDate()).reference(r.reference()).note(r.note()).build());
	}

	@Transactional
	public Waiter saveWaiter(Long id, WaiterRequest r) {
		var w = id == null ? new Waiter() : waiters.findById(id).orElseThrow();
		if (id == null && (r.pin() == null || r.pin().isBlank()))
			throw new IllegalArgumentException("PIN is required for a new waiter");
		w.setName(r.name());
		w.setCode(r.code());
		if (r.pin() != null && !r.pin().isBlank())
			w.setPin(r.pin());
		w.setPhone(r.phone());
		w.setActive(r.active());
		w.setAssignedTables(new HashSet<>(r.tableIds() == null ? List.of() : tables.findAllById(r.tableIds())));
		return waiters.save(w);
	}
}
