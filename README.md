# Myanmar Tea Shop POS

Spring Boot 3 + Java 17 + MySQL web POS MVP for Myanmar tea shops.

## Included

- Indoor/outdoor/pavement tables and fast product buttons
- Tea modifiers/notes and automatic BAR/KITCHEN routing
- Cash, KBZPay, WavePay, AYA Pay and CB Pay recording; mixed payment API
- Recipe-based ingredient deduction and low-stock alerts
- Daily sales/payment reconciliation API and dashboard
- Browser offline app shell plus queued offline order sync with idempotent `clientRef`
- Responsive tablet/desktop POS interface
- Full ingredient CRUD with supplier, cost, safety stock, purchase and expiry dates
- Stock purchase, adjustment, waste and automatic recipe-usage movement history
- Waiter accounts (code/PIN), active status, table assignments and waiter-linked orders
- Date-range sales reports with per-waiter sales totals
- Expiry-within-30-days and low-stock dashboards

## Run with XAMPP MySQL

1. Start **MySQL** from XAMPP Control Panel.
2. Open `http://localhost/phpmyadmin`, choose **Import**, and import `database/create_database.sql`.
3. Install Java 17 and Maven 3.9+.
4. Open a terminal in this project folder and run:

   ```bash
   mvn spring-boot:run
   ```

5. Open `http://localhost:8080`.

Default XAMPP settings are `root` with no password. If yours differs:

```bash
DB_USER=root DB_PASSWORD=your_password mvn spring-boot:run
```

On Windows PowerShell:

```powershell
$env:DB_USER="root"; $env:DB_PASSWORD="your_password"; mvn spring-boot:run
```

Tables and demo products/recipes are seeded automatically on first launch.

The demo waiter account is `W001` with PIN `1234`. Existing databases are upgraded automatically by Hibernate when the full version starts.

## Production notes

This is a working MVP. Wallet QR/payment confirmation needs merchant API credentials from each provider. Printer integration should use a local ESC/POS print agent. For multiple devices during a total power outage, keep the server and Wi-Fi router on a UPS; browser offline mode queues orders on each device until the local server returns.
# Owner / Admin access

Waiter management and date-range reports require an Owner login. The first run creates an account from environment variables, with development defaults:

```text
Username: owner
Password: owner123
```

For normal use, set a private password before the first run:

```bash
export OWNER_USERNAME=owner
export OWNER_PASSWORD='change-this-password'
mvn spring-boot:run
```

The password is stored in MySQL as a BCrypt hash. If an Owner row already exists, changing the environment variable does not overwrite that account.
