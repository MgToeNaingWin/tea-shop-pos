package mm.teashop.domain;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @JsonIgnore @ManyToOne(optional=false) private SaleOrder order; @ManyToOne(optional=false) private Product product; private int quantity; private String modifier; private String note; @Enumerated(EnumType.STRING) private Enums.Station station; @Column(precision=12,scale=2) private BigDecimal unitPrice; }
