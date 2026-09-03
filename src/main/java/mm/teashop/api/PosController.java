package mm.teashop.api;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import mm.teashop.api.ApiDtos.*; import mm.teashop.domain.*; import mm.teashop.repo.*; import mm.teashop.service.PosService; import org.springframework.web.bind.annotation.*; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api") @RequiredArgsConstructor @CrossOrigin public class PosController {
 private final ProductRepository products; private final ShopTableRepository tables; private final IngredientRepository ingredients; private final SaleOrderRepository orders; private final WaiterRepository waiters; private final PosService pos;
 @GetMapping("/catalog") Map<String,Object> catalog(){return Map.of("products",products.findAll(),"tables",tables.findAll());}
 @GetMapping("/waiters") List<Waiter> activeWaiters(){return waiters.findAll().stream().filter(Waiter::isActive).toList();}
 @PostMapping("/orders") SaleOrder create(@Valid @RequestBody OrderRequest r){return pos.create(r);}
 @GetMapping("/orders") List<SaleOrder> orders(){return orders.findAllDetailed();}
 @GetMapping("/stations/{status}") List<SaleOrder> station(@PathVariable Enums.OrderStatus status){return orders.findByStatusOrderByCreatedAtAsc(status);}
 @PatchMapping("/orders/{id}/status") SaleOrder status(@PathVariable Long id,@RequestParam Enums.OrderStatus value){return pos.updateStatus(id,value);}
 @PostMapping("/orders/{id}/payments") SaleOrder pay(@PathVariable Long id,@Valid @RequestBody PaymentRequest r){return pos.pay(id,r);}
 @GetMapping("/inventory/low-stock") List<Ingredient> low(){return ingredients.findByStockQtyLessThanEqualSafetyQty();}
 @GetMapping("/reports/today") Map<String,Object> today(){var data=orders.between(LocalDate.now().atStartOfDay(),LocalDate.now().plusDays(1).atStartOfDay()); var paid=data.stream().filter(o->o.getStatus()==Enums.OrderStatus.PAID).toList(); var sales=paid.stream().map(SaleOrder::getTotal).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add); var payments=paid.stream().flatMap(o->o.getPayments().stream()).collect(java.util.stream.Collectors.groupingBy(p->p.getMethod().name(),java.util.stream.Collectors.reducing(java.math.BigDecimal.ZERO,Payment::getAmount,java.math.BigDecimal::add))); return Map.of("orders",paid.size(),"sales",sales,"payments",payments);}
}
