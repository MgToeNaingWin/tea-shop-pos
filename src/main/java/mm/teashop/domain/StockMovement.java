package mm.teashop.domain;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.*;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockMovement { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(optional=false) private Ingredient ingredient; @Enumerated(EnumType.STRING) private Enums.StockMovementType type; @Column(precision=14,scale=3) private BigDecimal quantity; @Column(precision=14,scale=3) private BigDecimal balanceAfter; private LocalDateTime createdAt; private LocalDate purchaseDate; private LocalDate expiryDate; private String reference; private String note; }
