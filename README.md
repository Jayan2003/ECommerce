# 🛒 E-Commerce System 

This project is a console-based e-commerce system designed as part of the . It simulates real-world functionality like product definition, cart management, checkout, and shipping, while handling expiration, stock limits, and digital vs physical goods.

---

## 🚀 Features

- **Product Types**
  - Supports products that can **expire** (e.g. cheese, biscuits).
  - Supports **shippable** products (e.g. TVs, perishables).
  - Supports **digital items** (e.g. mobile scratch cards) which don't require shipping.

- **Cart Functionality**
  - Customers can add products to the cart.
  - Cannot add more than available quantity.
  - Expired products or invalid quantities throw errors.

- **Checkout Process**
  - Validates product availability and expiry.
  - Calculates **subtotal**, **shipping fees**, and **total amount**.
  - Deducts customer balance and product stock after purchase.
  - Throws errors if:
    - Cart is empty.
    - Balance is insufficient.
    - Item is expired or out of stock.

- **Shipping Service**
  - Only collects shippable items.
  - Prints a shipment notice with item names and weights.
  - Calculates shipping fee using:
    ```
    shipping_fee = 10 + (20 × total_weight)
    ```

- **Console Output**
  - Detailed checkout receipt.
  - Shipment breakdown.
  - Remaining balance.

---

## 🧱 Class Structure

| Class | Responsibility |
|-------|----------------|
| `Product` (abstract) | Base class for all products (name, price, stock). |
| `ExpiringProduct` | Products that expire and are shipped (e.g. cheese). |
| `DurableProduct` | Non-expiring, shippable products (e.g. TV). |
| `DigitalProduct` | Non-shippable, digital items (e.g. scratch cards). |
| `Customer` | Holds customer name and balance. |
| `Cart` | Adds and tracks products + quantities. |
| `DeliveryService` | Calculates shipping fees and prints shipping notice. |
| `OrderProcessor` | Manages checkout logic, validations, and printing. |
| `ECommerceDemo` | Main class with demo usage and test cases. |

---

## ✅ Sample Usage

```java
Product cheese = new ExpiringProduct("Cheese", 100, 10, LocalDate.now().plusDays(3), 0.4);
Product biscuits = new ExpiringProduct("Biscuits", 150, 5, LocalDate.now().plusDays(2), 0.7);
Product tv = new DurableProduct("TV", 5000, 3, 8);
Product card = new DigitalProduct("Scratch Card", 50, 100);

Customer bob = new Customer("Bob", 1000);
Cart cart = new Cart();
cart.add(cheese, 2);
cart.add(biscuits, 1);
cart.add(card, 1);

new OrderProcessor().checkout(bob, cart);
