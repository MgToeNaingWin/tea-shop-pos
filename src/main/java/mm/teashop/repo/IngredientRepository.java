package mm.teashop.repo;

import mm.teashop.domain.Ingredient;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDate;
import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
	@Query("select i from Ingredient i where i.stockQty <= i.safetyQty")
	List<Ingredient> findByStockQtyLessThanEqualSafetyQty();

	List<Ingredient> findByExpiryDateBetweenOrderByExpiryDate(LocalDate from, LocalDate to);
}
