package mm.teashop.domain;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="shop_tables") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShopTable { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,unique=true) private String name; @Enumerated(EnumType.STRING) private Enums.Area area; private int seats; private boolean active; }
