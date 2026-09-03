package mm.teashop.domain;
import com.fasterxml.jackson.annotation.JsonIgnore; import jakarta.persistence.*; import lombok.*; import java.util.*;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Waiter { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private String name; @Column(nullable=false,unique=true) private String code; @JsonIgnore @Column(nullable=false) private String pin; private String phone; private boolean active; @Builder.Default @ManyToMany private Set<ShopTable> assignedTables=new HashSet<>(); }
