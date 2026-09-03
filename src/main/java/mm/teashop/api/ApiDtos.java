package mm.teashop.api;
import jakarta.validation.constraints.*; import mm.teashop.domain.Enums; import java.math.BigDecimal; import java.util.List;
public final class ApiDtos { private ApiDtos(){}
 public record ItemRequest(@NotNull Long productId,@Min(1) int quantity,String modifier,String note){}
 public record OrderRequest(@NotBlank String clientRef,@NotNull Long tableId,Long waiterId,@NotEmpty List<ItemRequest> items){}
 public record PaymentPart(@NotNull Enums.PaymentMethod method,@DecimalMin("0.01") BigDecimal amount,String reference){}
 public record PaymentRequest(@NotEmpty List<PaymentPart> payments){}
 public record SplitRequest(@NotEmpty List<Long> itemIds,@NotNull Long targetTableId){}
 public record WaiterRequest(@NotBlank String name,@NotBlank String code,String pin,String phone,boolean active,List<Long> tableIds){}
 public record LoginRequest(@NotBlank String code,@NotBlank String pin){}
 public record IngredientRequest(@NotBlank String name,@NotBlank String unit,@NotNull @DecimalMin("0.0") BigDecimal stockQty,@NotNull @DecimalMin("0.0") BigDecimal safetyQty,java.time.LocalDate lastPurchaseDate,java.time.LocalDate expiryDate,String supplier,BigDecimal costPerUnit){}
 public record StockRequest(@NotNull Enums.StockMovementType type,@NotNull @DecimalMin("0.001") BigDecimal quantity,java.time.LocalDate purchaseDate,java.time.LocalDate expiryDate,String reference,String note){}
}
