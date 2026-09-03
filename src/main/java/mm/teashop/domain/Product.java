package mm.teashop.domain;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,unique=true) private String name; private String nameMm; @Enumerated(EnumType.STRING) private Enums.Category category; @Enumerated(EnumType.STRING) private Enums.Station station; @Column(nullable=false,precision=12,scale=2) private BigDecimal price; private boolean active; }
