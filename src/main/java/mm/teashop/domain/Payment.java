package mm.teashop.domain;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDateTime; import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @JsonIgnore @ManyToOne(optional=false) private SaleOrder order; @Enumerated(EnumType.STRING) private Enums.PaymentMethod method; @Column(precision=12,scale=2) private BigDecimal amount; private String reference; private LocalDateTime paidAt; }
