package mm.teashop.domain;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ingredient { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,unique=true) private String name; private String unit; @Column(precision=14,scale=3) private BigDecimal stockQty; @Column(precision=14,scale=3) private BigDecimal safetyQty; private LocalDate lastPurchaseDate; private LocalDate expiryDate; private String supplier; @Column(precision=12,scale=2) private BigDecimal costPerUnit; }
