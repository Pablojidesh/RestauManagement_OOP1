# The Royal Spice - Restaurant Management System
### Core Java Desktop Application (Java Swing + MySQL plain JDBC)

A complete, production-grade desktop Restaurant Management System designed as a college **Object-Oriented Programming (OOP)** course project. Built entirely with **Core Java (`javax.swing`)** and **plain JDBC** (no ORM, no Hibernate, no JPA, no web servers, no REST APIs).

---

## 1. Object-Oriented Programming (OOP) Architecture

The application strictly implements and demonstrates core OOP paradigms:

1. **Encapsulation**:
   - All domain entities (`MenuItem`, `Category`, `RestaurantTable`, `Customer`, `Order`, `OrderItem`, `Employee`, `InventoryItem`, `Bill`, `Payment`) are encapsulated POJOs with private fields and validated getters/setters.
   - Defensive validation is enforced twice: first in Swing input forms, and secondly inside DAO persistence methods. Corrupted database rows are skipped rather than rendered.

2. **Inheritance & Abstraction**:
   - The billing module defines an abstract base class `com.restaurant.model.Payment` with protected fields (`orderId`, `amount`, `paymentDate`, `paymentMethod`, `confirmationMessage`) and an abstract method `public abstract String processPayment()`.
   - Three concrete subclasses extend `Payment`:
     - `CashPayment`: verifies tendered amount, calculates change, records cashier drawer confirmation.
     - `CardPayment`: masks card PAN (`**** **** **** 1234`), captures cardholder/network, generates terminal authorization code.
     - `UPIPayment`: validates Virtual Payment Address format (`user@bank`), generates NPCI banking transaction reference.

3. **Dynamic Polymorphism (Core Assessment Focus)**:
   - During checkout, the UI instantiates the appropriate subclass (`CashPayment`, `CardPayment`, or `UPIPayment`) and passes it into `RestaurantService.checkoutOrder(..., Payment payment)`.
   - The method invocation `payment.processPayment()` is dispatched **polymorphically at runtime via the `Payment` reference**, resolving dynamically to the overridden subclass implementation and yielding genuinely different confirmation outputs.

4. **Multi-Layer Separation of Concerns**:
   ```
   [ Swing UI Layer ]         (LoginFrame, AdminDashboardFrame, KitchenDashboardFrame, WaiterDashboardFrame)
            |
            v
   [ Service Layer ]          (RestaurantService business logic facade - no direct SQL)
            |
            v
   [ Data Access Layer ]      (CategoryDAO, MenuDAO, TableDAO, CustomerDAO, OrderDAO, BillDAO, EmployeeDAO, InventoryDAO)
            |
            v
   [ Database Layer ]         (DBConnection via plain JDBC to MySQL utf8mb4)
   ```

---

## 2. Directory Structure

```
RestauManagement/
├── db.properties                                # MySQL database configuration
├── lib/
│   └── mysql-connector-j-8.3.0.jar              # MySQL Connector/J JDBC Driver JAR
├── sql/
│   └── schema.sql                               # MySQL schema creation and seed script (utf8mb4)
├── src/
│   └── com/
│       └── restaurant/
│           ├── Main.java                        # Desktop application entry point
│           ├── config/
│           │   └── DBConnection.java            # JDBC connection manager (UTF-8, utf8mb4)
│           ├── dao/
│           │   ├── BillDAO.java                 # Invoices and revenue queries
│           │   ├── CategoryDAO.java             # Menu categories
│           │   ├── CustomerDAO.java             # Customer registration
│           │   ├── EmployeeDAO.java             # Staff credentials and salaries
│           │   ├── InventoryDAO.java            # Pantry raw materials and stock
│           │   ├── MenuDAO.java                 # Dishes and live keyword search
│           │   ├── OrderDAO.java                # Dining orders and line items
│           │   └── TableDAO.java                # Restaurant tables
│           ├── model/
│           │   ├── Bill.java                    # Invoice entity
│           │   ├── CardPayment.java             # Concrete card payment subclass
│           │   ├── CashPayment.java             # Concrete cash payment subclass
│           │   ├── Category.java                # Category POJO
│           │   ├── Customer.java                # Customer POJO
│           │   ├── Employee.java                # Staff member POJO
│           │   ├── InventoryItem.java           # Pantry stock POJO
│           │   ├── MenuItem.java                # Dish POJO
│           │   ├── Order.java                   # Order POJO
│           │   ├── OrderItem.java               # Order line item POJO
│           │   ├── Payment.java                 # Abstract payment base class
│           │   ├── ProfitLossReport.java        # Financial audit model
│           │   ├── Receipt.java                 # Formatted checkout receipt model
│           │   ├── RestaurantTable.java         # Dining table POJO
│           │   └── UPIPayment.java              # Concrete UPI payment subclass
│           ├── service/
│           │   └── RestaurantService.java       # Central business logic and security facade
│           └── ui/
│               ├── AdminDashboardFrame.java     # Admin Control Suite (P&L, Menu, Inventory, Staff)
│               ├── KitchenDashboardFrame.java   # Kitchen Display System (Status transitions, Salary)
│               ├── LoginFrame.java              # Staff authentication portal
│               ├── ReceiptDialog.java           # Formatted receipt modal
│               ├── WaiterDashboardFrame.java    # Dining hall POS (Cart, Polymorphic checkout)
│               └── components/
│                   └── UIUtils.java             # Colors, typography, plain-text currency format (Rs.xxx.xx)
├── test/
│   └── com/
│       └── restaurant/
│           └── SystemVerificationTest.java      # Automated end-to-end test suite (44/44 tests)
└── README.md
```

---

## 3. Database Setup & Configuration

### Prerequisites
- **Java SE Development Kit (JDK)**: JDK 17, 21, or 25.
- **MySQL Server**: 8.0 or newer running on `localhost:3306`.

### Step 1: Execute `sql/schema.sql`
Run the schema script to create the `restaurant_db` database and populate the pre-seeded records:

**On Windows (PowerShell):**
```powershell
Get-Content -Raw "sql/schema.sql" | mysql -u root -p --default-character-set=utf8mb4
```

**On Windows (Command Prompt / CMD):**
```cmd
mysql -u root -p --default-character-set=utf8mb4 < sql\schema.sql
```

**On Linux / macOS:**
```bash
mysql -u root -p --default-character-set=utf8mb4 < sql/schema.sql
```

### Step 2: Configure `db.properties`
Open `db.properties` in the project root and confirm your MySQL credentials:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/restaurant_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
db.username=root
db.password=pablo
```
*(If your MySQL root password is blank or different, change `db.password` accordingly).*

### Step 3: MySQL Driver JAR Location
The MySQL Connector/J driver is bundled at:
`lib/mysql-connector-j-8.3.0.jar`

---

## 4. Compilation & Execution Commands

### Exact `javac` Compile Command

**Windows (PowerShell):**
```powershell
javac -cp "lib/*;." -d bin (Get-ChildItem -Path src -Filter *.java -Recurse).FullName
```

**Windows (CMD):**
```cmd
javac -cp "lib\mysql-connector-j-8.3.0.jar;." -d bin src\com\restaurant\config\*.java src\com\restaurant\model\*.java src\com\restaurant\dao\*.java src\com\restaurant\service\*.java src\com\restaurant\ui\components\*.java src\com\restaurant\ui\*.java src\com\restaurant\*.java
```

**Linux / macOS:**
```bash
javac -cp "lib/*:." -d bin $(find src -name "*.java")
```

---

### Exact `java` Run Command (Desktop Application)

**Windows (PowerShell or CMD):**
```powershell
java -cp "lib/*;bin" com.restaurant.Main
```

**Linux / macOS:**
```bash
java -cp "lib/*:bin" com.restaurant.Main
```

---

### Running the Automated Test Suite

To execute the 44-point end-to-end automated verification suite:

```powershell
javac -cp "lib/*;bin" -d bin (Get-ChildItem -Path test -Filter *.java -Recurse).FullName
java -cp "lib/*;bin" com.restaurant.SystemVerificationTest
```

---

## 5. Sample Logins

| Role | Username | Password | Full Name | Default Monthly Salary | Permitted Capabilities |
|---|---|---|---|---|---|
| **ADMIN** | `admin` | `admin123` | Administrator | Rs.55,000.00 | Dashboard & P&L Reporting, Categories, Menu Items, Inventory & Restocking, Staff Salary Administration, All Orders |
| **KITCHEN** | `chef1` | `chef123` | Chef Rajesh | Rs.38,000.00 | Real-time Orders Queue (`Placed` -> `Preparing` -> `Served`), View Own Salary (read-only) |
| **WAITER** | `waiter1` | `waiter123` | Waiter Suresh | Rs.24,000.00 | Customer Registration, Table Selection, Cart Assembly & Live Search, Checkout with Cash/Card/UPI, View Own Salary (read-only) |

---

## 6. Comprehensive Test Cases & Acceptance Walkthrough

### Test Case 1: Role-Based Login & Dashboard Routing
- **Steps**:
  1. Launch the app (`java -cp "lib/*;bin" com.restaurant.Main`).
  2. Enter `admin` / `admin123` -> confirms `AdminDashboardFrame` opens, login closes.
  3. Logout and enter `chef1` / `chef123` -> confirms `KitchenDashboardFrame` opens.
  4. Logout and enter `waiter1` / `waiter123` -> confirms `WaiterDashboardFrame` opens.
  5. Test invalid credentials `admin` / `wrongpass` -> confirms error prompt appears without closing login.

### Test Case 2: Category & Menu Management with Live Search
- **Steps**:
  1. Log in as `admin`.
  2. Navigate to **Categories** tab. Enter "Biryani & Rice" and click **Add Category**.
  3. Navigate to **Menu Items** tab -> verify the Category dropdown immediately includes "Biryani & Rice".
  4. Fill Dish Name: "Hyderabadi Dum Biryani", Price: "320.00", Description: "Basmati rice dum cooked with spices". Click **Add Dish**.
  5. Verify the dish appears in the table with no blank or corrupted rows.
  6. Test the live search bar: type `"paneer"` -> confirms dishes like "Paneer Tikka" and "Paneer Butter Masala" appear in real time.
  7. Test defensive validation: attempt to add a dish with price `0` or blank name -> confirms validation modal rejects insert.

### Test Case 3: Customer Registration & Table Order Placement
- **Steps**:
  1. Log in as `waiter1`.
  2. Under **Register Customer**, enter Name: "Rahul Verma", Contact: "9876543299". Click **Register Guest**.
  3. Switch to **Place Order & Checkout** tab.
  4. Verify the Dining Table dropdown lists ALL tables (Table 1 through Table 6).
  5. Select Table 2 and customer "Rahul Verma".
  6. Search for "Paneer", select "Paneer Tikka", quantity 2 -> Click **Add to Cart**.
  7. Add "Butter Naan", quantity 4 -> Click **Add to Cart**.
  8. Confirm the cart table displays quantities, unit prices, and correct subtotal.

### Test Case 4: Polymorphic Payment Processing (Cash, Card, UPI)
- **Calculation Rule**:
  $$\text{Grand Total} = (\text{Subtotal} - \text{Discount}) + \text{Tax on Discounted Subtotal}$$
- **Checkout 1 (Cash)**:
  - Enter Discount: `10.0` %, Tax: `5.0` %.
  - Select "Cash Payment", enter Tendered Amount higher than grand total.
  - Click **Complete Checkout & Print Receipt**.
  - Confirms receipt shows:
    `CASH PAYMENT PROCESSED: Amount Due, Cash Tendered, Change Returned`.
- **Checkout 2 (Card)**:
  - Place a new order for Table 3.
  - Select "Card Payment", enter Card Number `4532-1111-2222-3333`, Cardholder Name "Rahul Verma".
  - Click **Complete Checkout**.
  - Confirms receipt shows:
    `CARD PAYMENT AUTHORIZED: **** **** **** 3333, Cardholder, Terminal Auth: AUTH-XXXXXX`.
- **Checkout 3 (UPI)**:
  - Place an order for Table 4.
  - Select "UPI Payment", enter UPI VPA `rahul@okhdfcbank`.
  - Click **Complete Checkout**.
  - Confirms receipt shows:
    `UPI PAYMENT COMPLETED: rahul@okhdfcbank, Bank Ref Number: UPI-REF-XXXXXX`.

### Test Case 5: Kitchen Order Progression
- **Steps**:
  1. Log in as `chef1`.
  2. Under **Active Orders Queue**, inspect the active orders list.
  3. Click on an order -> verifies the dishes list and quantities display in the bottom pane.
  4. Click **Set Status: Preparing** -> confirms order status transitions to `Preparing`.
  5. Click **Set Status: Served** -> confirms status transitions to `Served`.

### Test Case 6: Inventory Low-Stock Proactive Alert & Restock
- **Steps**:
  1. Log in as `admin`.
  2. Notice the prominent red **CRITICAL STOCK ALERT** banner at the top of the Admin window alerting that "Fresh Paneer" is low stock (pre-seeded at 2.50 kg <= 5.00 kg threshold).
  3. Open **Inventory** tab -> verify "Fresh Paneer" has a red "LOW STOCK" label in the status column.
  4. In the **Restock Inventory Stock** form, select "Fresh Paneer", enter quantity `10.0`, click **Confirm Restock**.
  5. Table updates quantity to 12.50 kg; status changes to green "NORMAL", and the alert banner disappears automatically.

### Test Case 7: Salary Privacy & Fresh Database Retrieval
- **Steps**:
  1. Log in as `waiter1` -> click **My Salary & Profile** tab -> confirms monthly salary displays Rs.24,000.00. Waiter has NO access to Admin screens.
  2. Log out and log in as `admin`.
  3. Under **Salary Management** tab, select Waiter Suresh, enter New Salary `28,000.00`, click **Update Salary**.
  4. Log out and log back in as `waiter1`.
  5. Under **My Salary & Profile**, click **Check Fresh Salary from DB** -> confirms salary immediately updates to Rs.28,000.00 fetched fresh via their employee ID.

### Test Case 8: Profit & Loss Statement Consistency
- **Steps**:
  1. Log in as `admin`.
  2. Open **Dashboard / P&L** tab.
  3. Verify:
     - Total Revenue = sum of completed bills.
     - Total Salary Expense = sum of all staff salaries.
     - Total Inventory Cost = sum of pantry holdings (Qty x Cost/Unit).
     - Net P&L = Revenue - Salaries - Inventory Cost (labeled "Profit" or "Loss").
     - Total Orders Count matches the total count in **All Orders** tab.

---

## 7. Currency Formatting Compliance
All currency values throughout the entire application (labels, text fields, table cells, receipt outputs) are rendered exclusively in plain ASCII text format:
`Rs.123.45`
*(The Unicode rupee symbol ₹ is avoided by design to prevent cross-platform encoding artifacts and font mojibake).*
