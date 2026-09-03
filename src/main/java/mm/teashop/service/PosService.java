package mm.teashop.service;

import lombok.RequiredArgsConstructor;
import mm.teashop.api.ApiDtos.OrderRequest;
import mm.teashop.api.ApiDtos.PaymentPart;
import mm.teashop.api.ApiDtos.PaymentRequest;
import mm.teashop.domain.Enums;
import mm.teashop.domain.OrderItem;
import mm.teashop.domain.Payment;
import mm.teashop.domain.Product;
import mm.teashop.domain.SaleOrder;
import mm.teashop.domain.StockMovement;
import mm.teashop.repo.ProductRepository;
import mm.teashop.repo.RecipeItemRepository;
import mm.teashop.repo.SaleOrderRepository;
import mm.teashop.repo.ShopTableRepository;
import mm.teashop.repo.StockMovementRepository;
import mm.teashop.repo.WaiterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PosService {

    private final ProductRepository products;
    private final ShopTableRepository tables;
    private final SaleOrderRepository orders;
    private final RecipeItemRepository recipes;
    private final WaiterRepository waiters;
    private final StockMovementRepository movements;

    @Transactional
    public SaleOrder create(OrderRequest request) {
        var existingOrder = orders.findByClientRef(request.clientRef());
        if (existingOrder.isPresent()) {
            return existingOrder.get();
        }

        var table = tables.findById(request.tableId())
            .orElseThrow(() -> new IllegalArgumentException("ဝိုင်းကို ရှာမတွေ့ပါ"));

        var waiter = request.waiterId() == null
            ? null
            : waiters.findById(request.waiterId())
                .orElseThrow(() -> new IllegalArgumentException("စားပွဲထိုးကို ရှာမတွေ့ပါ"));

        if (waiter != null && !waiter.isActive()) {
            throw new IllegalArgumentException("စားပွဲထိုးအကောင့်ကို ပိတ်ထားပါသည်");
        }

        if (waiter != null
            && !waiter.getAssignedTables().isEmpty()
            && !waiter.getAssignedTables().contains(table)) {
            throw new IllegalArgumentException("ဤဝိုင်းကို ရွေးချယ်ထားသော စားပွဲထိုးအား တာဝန်မပေးထားပါ");
        }

        var order = SaleOrder.builder()
            .clientRef(request.clientRef())
            .shopTable(table)
            .waiter(waiter)
            .status(Enums.OrderStatus.SENT)
            .createdAt(LocalDateTime.now())
            .total(BigDecimal.ZERO)
            .build();

        BigDecimal total = BigDecimal.ZERO;

        for (var requestedItem : request.items()) {
            var product = products.findById(requestedItem.productId())
                .orElseThrow(() -> new IllegalArgumentException("မီနူးပစ္စည်းကို ရှာမတွေ့ပါ"));

            var item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(requestedItem.quantity())
                .modifier(requestedItem.modifier())
                .note(requestedItem.note())
                .station(product.getStation())
                .unitPrice(product.getPrice())
                .build();

            order.getItems().add(item);

            total = total.add(
                product.getPrice().multiply(BigDecimal.valueOf(requestedItem.quantity()))
            );

            deduct(product, requestedItem.quantity());
        }

        order.setTotal(total);
        return orders.save(order);
    }

    private void deduct(Product product, int quantity) {
        String productDisplayName = product.getNameMm() != null
            && !product.getNameMm().isBlank()
                ? product.getNameMm()
                : product.getName();

        for (var recipe : recipes.findByProduct(product)) {
            var ingredient = recipe.getIngredient();
            var usedQuantity = recipe.getQuantity()
                .multiply(BigDecimal.valueOf(quantity));

            if (ingredient.getStockQty().compareTo(usedQuantity) < 0) {
                throw new IllegalArgumentException(
                    "ကုန်ကြမ်းလက်ကျန် မလုံလောက်ပါ: " + ingredient.getName()
                );
            }

            ingredient.setStockQty(
                ingredient.getStockQty().subtract(usedQuantity)
            );

            movements.save(
                StockMovement.builder()
                    .ingredient(ingredient)
                    .type(Enums.StockMovementType.RECIPE_USAGE)
                    .quantity(usedQuantity.negate())
                    .balanceAfter(ingredient.getStockQty())
                    .createdAt(LocalDateTime.now())
                    .reference(productDisplayName)
                    .note("POS အရောင်း")
                    .build()
            );
        }
    }

    @Transactional
    public SaleOrder pay(Long id, PaymentRequest request) {
        var order = orders.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("အော်ဒါကို ရှာမတွေ့ပါ"));

        BigDecimal priorPayment = order.getPayments().stream()
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal addedPayment = request.payments().stream()
            .map(PaymentPart::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (priorPayment.add(addedPayment).compareTo(order.getTotal()) > 0) {
            throw new IllegalArgumentException("ပေးချေသည့်ငွေသည် ကျသင့်ငွေထက် များနေပါသည်");
        }

        for (var paymentPart : request.payments()) {
            order.getPayments().add(
                Payment.builder()
                    .order(order)
                    .method(paymentPart.method())
                    .amount(paymentPart.amount())
                    .reference(paymentPart.reference())
                    .paidAt(LocalDateTime.now())
                    .build()
            );
        }

        if (priorPayment.add(addedPayment).compareTo(order.getTotal()) == 0) {
            order.setStatus(Enums.OrderStatus.PAID);
        }

        return orders.save(order);
    }

    @Transactional
    public SaleOrder updateStatus(Long id, Enums.OrderStatus status) {
        var order = orders.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("အော်ဒါကို ရှာမတွေ့ပါ"));
        order.setStatus(status);
        return order;
    }
}
