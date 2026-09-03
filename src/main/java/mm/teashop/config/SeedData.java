package mm.teashop.config;

import lombok.RequiredArgsConstructor; 
import mm.teashop.domain.*; 
import mm.teashop.repo.*; 
import org.springframework.boot.CommandLineRunner; 
import org.springframework.context.annotation.*; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal; 
import java.util.*;

@Configuration 
@RequiredArgsConstructor 
public class SeedData {

    private final ProductRepository products; 
    private final ShopTableRepository tables; 
    private final IngredientRepository ingredients; 
    private final RecipeItemRepository recipes; 
    private final WaiterRepository waiters;
    private final OwnerRepository owners;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.owner.username:owner}")
    private String ownerUsername;

    @Value("${app.owner.password:owner123}")
    private String ownerPassword;

    @Bean 
    CommandLineRunner seed() {
        return args -> {
            // Seed Shop Tables
            if (tables.count() == 0) {
                for (int i = 1; i <= 12; i++) {
                    tables.save(ShopTable.builder()
                        .name("ဝိုင်း " + i)
                        .area(i <= 6 ? Enums.Area.INDOOR : i <= 9 ? Enums.Area.OUTDOOR : Enums.Area.PAVEMENT)
                        .seats(4)
                        .active(true)
                        .build());
                }
            } 

            // Seed Ingredients & Menu Products
            if (products.count() == 0) {
                var tea = ingredients.save(Ingredient.builder()
                    .name("လက်ဖက်ခြောက်").unit("g").stockQty(new BigDecimal("5000")).safetyQty(new BigDecimal("500"))
                    .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusMonths(6))
                    .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("12")).build());
                
                var condensed = ingredients.save(Ingredient.builder()
                    .name("နို့ဆီ").unit("g").stockQty(new BigDecimal("8000")).safetyQty(new BigDecimal("800"))
                    .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusMonths(9))
                    .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("8")).build());
                
                var evaporated = ingredients.save(Ingredient.builder()
                    .name("နို့စိမ်း").unit("ml").stockQty(new BigDecimal("6000")).safetyQty(new BigDecimal("600"))
                    .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusMonths(5))
                    .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("6")).build());

                var rice = ingredients.save(Ingredient.builder()
                    .name("ဆန်").unit("g").stockQty(new BigDecimal("20000")).safetyQty(new BigDecimal("3000"))
                    .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusMonths(6))
                    .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("3")).build());

                var cookingOil = ingredients.save(Ingredient.builder()
                    .name("ချက်ဆီ").unit("ml").stockQty(new BigDecimal("10000")).safetyQty(new BigDecimal("1000"))
                    .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusMonths(12))
                    .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("5")).build());

                var egg = ingredients.save(Ingredient.builder()
                    .name("ကြက်ဥ").unit("pc").stockQty(new BigDecimal("200")).safetyQty(new BigDecimal("30"))
                    .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusDays(14))
                    .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("400")).build());

                var onion = ingredients.save(Ingredient.builder()
                    .name("ကြက်သွန်နီ").unit("g").stockQty(new BigDecimal("5000")).safetyQty(new BigDecimal("500"))
                    .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusMonths(1))
                    .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("2")).build());

                var flour = ingredients.save(Ingredient.builder()
                    .name("ဂျုံမှုန့်").unit("g").stockQty(new BigDecimal("10000")).safetyQty(new BigDecimal("1000"))
                    .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusMonths(6))
                    .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("3")).build());

                var peas = ingredients.save(Ingredient.builder()
                    .name("ပဲ").unit("g").stockQty(new BigDecimal("8000")).safetyQty(new BigDecimal("800"))
                    .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusMonths(6))
                    .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("4")).build());

                var chicken = ingredients.save(Ingredient.builder()
                    .name("ကြက်သား").unit("g").stockQty(new BigDecimal("5000")).safetyQty(new BigDecimal("500"))
                    .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusDays(7))
                    .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("15")).build());
                var pork = ingredients.save(Ingredient.builder()
                        .name("ဝက်သား").unit("g").stockQty(new BigDecimal("5000")).safetyQty(new BigDecimal("500"))
                        .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusDays(7))
                        .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("15")).build());
                var beef = ingredients.save(Ingredient.builder()
                        .name("အမဲသား").unit("g").stockQty(new BigDecimal("5000")).safetyQty(new BigDecimal("500"))
                        .lastPurchaseDate(java.time.LocalDate.now()).expiryDate(java.time.LocalDate.now().plusDays(7))
                        .supplier("ဒေသတွင်း ရောင်းချသူ").costPerUnit(new BigDecimal("15")).build());

                // 1. Traditional Tea Styles (BAR)
                List<String[]> teaItems = List.of(
                    new String[]{"Cho Seint", "ချိုစိမ့်", "1500"},
                    new String[]{"Kyauk Padaung", "ကျောက်ပန်းတောင်း", "1600"},
                    new String[]{"Paw Hsan Mway", "ပေါ့ဆန်းမွှေး", "1500"},
                    new String[]{"Ma Nhat", "မနပ်", "1500"},
                    new String[]{"Cho Pyit", "ချိုပျစ်", "1600"},
                    new String[]{"Cho Pawt", "ချိုပေါ့", "1500"},
                    new String[]{"Pawt Seint", "ပေါ့စိမ့်", "1500"},
                    new String[]{"Kyu Kyat", "ကျကျနန / ကျူးကျပ်", "1600"}
                );

                for (String[] x : teaItems) {
                    var p = products.save(Product.builder()
                        .name(x[0]).nameMm(x[1])
                        .category(Enums.Category.DRINK).station(Enums.Station.BAR)
                        .price(new BigDecimal(x[2])).active(true).build());
                    
                    recipes.saveAll(List.of(
                        RecipeItem.builder().product(p).ingredient(tea).quantity(new BigDecimal("12")).build(),
                        RecipeItem.builder().product(p).ingredient(condensed).quantity(new BigDecimal("30")).build(),
                        RecipeItem.builder().product(p).ingredient(evaporated).quantity(new BigDecimal("20")).build()
                    ));
                }

                // 2. Cold Beverages & Other Drinks (BAR)
                List<String[]> coldDrinks = List.of(
                    new String[]{"Iced Black Tea", "ရေခဲရသောက် (လက်ဖက်ရည်ရိုးရိုး)", "1200"},
                    new String[]{"Iced Milk Tea", "လက်ဖက်ရည်အေး", "1800"},
                    new String[]{"Lime Juice", "သံပုရာရည်", "1500"},
                    new String[]{"Coffee (Hot)", "ကော်ဖီပူ", "1500"},
                    new String[]{"Coffee (Iced)", "ကော်ဖီအေး", "1800"}
                );

                for (String[] x : coldDrinks) {
                    products.save(Product.builder()
                        .name(x[0]).nameMm(x[1])
                        .category(Enums.Category.DRINK).station(Enums.Station.BAR)
                        .price(new BigDecimal(x[2])).active(true).build());
                }

                // 3. Snacks & Pastries (KITCHEN)
                List<String[]> snacks = List.of(
                    new String[]{"Samosa", "စမိုဆာ", "600"},
                    new String[]{"Baya Kyaw", "ဘယာကြော်", "600"},
                    new String[]{"Nan Batha", "နံပြား", "1000"},
                    new String[]{"Puri (2 Pcs)", "ပူရီ (၂ ခု)", "2000"},
                    new String[]{"Steamed Bun (Pork)", "ပေါက်စီ (ဝက်)", "1800"}
                );

                for (String[] x : snacks) {
                    products.save(Product.builder()
                        .name(x[0]).nameMm(x[1])
                        .category(Enums.Category.FOOD).station(Enums.Station.KITCHEN)
                        .price(new BigDecimal(x[2])).active(true).build());
                }

                // E Kyar Kway: deduct flour and cooking oil.
                var eKyarKway = products.save(Product.builder()
                    .name("E Kyar Kway").nameMm("အီကြာကွေး")
                    .category(Enums.Category.FOOD).station(Enums.Station.KITCHEN)
                    .price(new BigDecimal("800")).active(true).build());

                recipes.saveAll(List.of(
                    RecipeItem.builder().product(eKyarKway).ingredient(flour).quantity(new BigDecimal("80")).build(),
                    RecipeItem.builder().product(eKyarKway).ingredient(cookingOil).quantity(new BigDecimal("20")).build()
                ));

                // Steamed Peas: deduct peas, onion and a small amount of oil.
                var steamedPeas = products.save(Product.builder()
                    .name("Steamed Peas (Baya)").nameMm("ပဲပြုတ်")
                    .category(Enums.Category.FOOD).station(Enums.Station.KITCHEN)
                    .price(new BigDecimal("800")).active(true).build());

                recipes.saveAll(List.of(
                    RecipeItem.builder().product(steamedPeas).ingredient(peas).quantity(new BigDecimal("150")).build(),
                    RecipeItem.builder().product(steamedPeas).ingredient(onion).quantity(new BigDecimal("10")).build(),
                    RecipeItem.builder().product(steamedPeas).ingredient(cookingOil).quantity(new BigDecimal("5")).build()
                ));

                // Chicken Steamed Bun: deduct flour, chicken and onion.
                var chickenBun = products.save(Product.builder()
                    .name("Steamed Bun (Chicken)").nameMm("ပေါက်စီ (ကြက်)")
                    .category(Enums.Category.FOOD).station(Enums.Station.KITCHEN)
                    .price(new BigDecimal("1800")).active(true).build());

                recipes.saveAll(List.of(
                    RecipeItem.builder().product(chickenBun).ingredient(flour).quantity(new BigDecimal("80")).build(),
                    RecipeItem.builder().product(chickenBun).ingredient(chicken).quantity(new BigDecimal("50")).build(),
                    RecipeItem.builder().product(chickenBun).ingredient(onion).quantity(new BigDecimal("10")).build()
                ));

                // 4. Noodles & Heavy Meals (KITCHEN)
                List<String[]> mainMeals = List.of(
                    new String[]{"Mohinga", "မုန့်ဟင်းခါး", "2500"},
                    new String[]{"Mohinga Special", "မုန့်ဟင်းခါး အထူး", "3500"},
                    new String[]{"Ohm No Khow Suey", "အုန်းနို့ခေါက်ဆွဲ", "3000"},
                    new String[]{"Shan Noodles (Soup)", "ရှမ်းခေါက်ဆွဲ (အရည်)", "3000"},
                    new String[]{"Shan Noodles (Dry)", "ရှမ်းခေါက်ဆွဲ (အသုတ်)", "3000"},
                    new String[]{"Noodle Salad", "ခေါက်ဆွဲသုတ်", "2500"},
                    new String[]{"Nan Gyi Thoke", "နန်းကြီးသုတ်", "3000"}
                );

                for (String[] x : mainMeals) {
                    products.save(Product.builder()
                        .name(x[0]).nameMm(x[1])
                        .category(Enums.Category.FOOD).station(Enums.Station.KITCHEN)
                        .price(new BigDecimal(x[2])).active(true).build());
                }

                // Plain Fried Rice: deduct rice, cooking oil and onion.
                var friedRice = products.save(Product.builder()
                    .name("Fried Rice (Plain)").nameMm("ထမင်းကြော်")
                    .category(Enums.Category.FOOD).station(Enums.Station.KITCHEN)
                    .price(new BigDecimal("3500")).active(true).build());

                recipes.saveAll(List.of(
                    RecipeItem.builder().product(friedRice).ingredient(rice).quantity(new BigDecimal("250")).build(),
                    RecipeItem.builder().product(friedRice).ingredient(cookingOil).quantity(new BigDecimal("20")).build(),
                    RecipeItem.builder().product(friedRice).ingredient(onion).quantity(new BigDecimal("30")).build()
                ));

                // Egg Fried Rice: deduct the same ingredients plus one egg.
                var eggFriedRice = products.save(Product.builder()
                    .name("Fried Rice with Egg").nameMm("ကြက်ဥထမင်းကြော်")
                    .category(Enums.Category.FOOD).station(Enums.Station.KITCHEN)
                    .price(new BigDecimal("4200")).active(true).build());

                recipes.saveAll(List.of(
                    RecipeItem.builder().product(eggFriedRice).ingredient(rice).quantity(new BigDecimal("250")).build(),
                    RecipeItem.builder().product(eggFriedRice).ingredient(cookingOil).quantity(new BigDecimal("20")).build(),
                    RecipeItem.builder().product(eggFriedRice).ingredient(onion).quantity(new BigDecimal("30")).build(),
                    RecipeItem.builder().product(eggFriedRice).ingredient(egg).quantity(BigDecimal.ONE).build()
                ));
            } 

            // Seed four waiters: two indoor, one outdoor and one pavement.
            if (waiters.count() == 0) {
                var allTables = tables.findAll();
                var indoorTables = allTables.stream()
                    .filter(table -> table.getArea() == Enums.Area.INDOOR)
                    .sorted(Comparator.comparing(ShopTable::getId)).toList();
                var outdoorTables = allTables.stream()
                    .filter(table -> table.getArea() == Enums.Area.OUTDOOR)
                    .sorted(Comparator.comparing(ShopTable::getId)).toList();
                var pavementTables = allTables.stream()
                    .filter(table -> table.getArea() == Enums.Area.PAVEMENT)
                    .sorted(Comparator.comparing(ShopTable::getId)).toList();

                int indoorMiddle = indoorTables.size() / 2;

                waiters.saveAll(List.of( 
                    Waiter.builder()
                        .name("မျိုးမြင့်").code("W001").pin("1111").phone("09790820546").active(true)
                        .assignedTables(new HashSet<>(tables.findAll())).build(),
                    	Waiter.builder()
                        .name("မင်းမင်း").code("W021").pin("1121").phone("0967080546").active(true)
                        .assignedTables(new HashSet<>(indoorTables.subList(0, indoorMiddle))).build(), 
                    Waiter.builder()
                        .name("အောင်အောင်").code("W002").pin("2222").phone("09789789420").active(true)
                        .assignedTables(new HashSet<>(indoorTables.subList(indoorMiddle, indoorTables.size()))).build(),
                    Waiter.builder()
                        .name("သီဟ").code("W003").pin("3333").phone("09680978203").active(true)
                        .assignedTables(new HashSet<>(outdoorTables)).build(),
                    Waiter.builder()
                        .name("ကိုကို").code("W004").pin("4444").phone("09776456230").active(true)
                        .assignedTables(new HashSet<>(pavementTables)).build()
                ));
            }

            // Seed the first Owner account. Change the password after first login.
            if (owners.count() == 0) {
                owners.save(Owner.builder()
                    .username(ownerUsername)
                    .password(passwordEncoder.encode(ownerPassword))
                    .enabled(true)
                    .build());
            }
        };
    }
}
