package mm.teashop.domain;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecipeItem { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(optional=false) private Product product; @ManyToOne(optional=false) private Ingredient ingredient; @Column(precision=12,scale=3) private BigDecimal quantity; }
