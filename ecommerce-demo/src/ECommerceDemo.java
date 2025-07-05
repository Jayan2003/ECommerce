
import java.time.LocalDate;
import java.util.*;

interface Shippable {
    String getName();
    double getWeight();  // weight of ONE unit, in kg
}

abstract class Product {
    protected final String name;
    protected final double price;
    protected int stock;

    protected Product(String name, double price, int stock) {
        if (price < 0 || stock < 0) throw new IllegalArgumentException();
        this.name  = name;
        this.price = price;
        this.stock = stock;
    }

    public String  getName()  { return name; }
    public double  getPrice() { return price; }
    public int     getStock() { return stock; }
    public void    reduceStock(int qty) { stock -= qty; }

    public boolean isExpired()   { return false; }        
    public boolean isShippable() { return this instanceof Shippable; }
    public double  getWeight()   { return 0; }            
}



final class ExpiringProduct extends Product implements Shippable {
    private final LocalDate expiry;
    private final double weightKg;

    ExpiringProduct(String n, double p, int s, LocalDate expiry, double w) {
        super(n, p, s);
        this.expiry   = expiry;
        this.weightKg = w;
    }
    @Override public boolean isExpired() { return LocalDate.now().isAfter(expiry); }
    @Override public double  getWeight() { return weightKg; }
}

final class DurableProduct extends Product implements Shippable {
    private final double weightKg;  

    DurableProduct(String n, double p, int s, double w) {
        super(n, p, s);
        this.weightKg = w;
    }
    @Override public double getWeight() { return weightKg; }
}

final class DigitalProduct extends Product {
    DigitalProduct(String n, double p, int s) { super(n, p, s); }
}



final class Customer {
    private final String name;
    private double balance;

    Customer(String name, double balance) {
        this.name    = name;
        this.balance = balance;
    }
    public double getBalance() { return balance; }
    public void   withdraw(double amt) { balance -= amt; }
    public String getName()    { return name; }
}


final class Cart {
    private final Map<Product,Integer> items = new LinkedHashMap<>();


    public void add(Product p, int qty) {
        int existing = items.getOrDefault(p, 0);
        if (qty <= 0 || existing + qty > p.getStock())
            throw new IllegalArgumentException("Invalid quantity for " + p.getName());
        if (p.isExpired())
            throw new IllegalStateException(p.getName() + " is expired");
        items.put(p, existing + qty);
    }
    public Map<Product,Integer> getItems() { return items; }
    public boolean isEmpty()               { return items.isEmpty(); }
}



final class DeliveryService {
    private static final double BASE = 10;  
    private static final double PER_KG = 20;


    public double ship(Map<Product,Integer> lines) {
        double totalWeight = 0;
        boolean hasParcel = lines.keySet().stream().anyMatch(Product::isShippable);
        if (!hasParcel) return 0;

        System.out.println("** Shipment notice **");
        for (var e : lines.entrySet()) {
            Product p = e.getKey();
            int qty   = e.getValue();
            if (p.isShippable()) {
                System.out.printf("%dx %s %.0fg%n",
                        qty, p.getName(), p.getWeight() * 1000);
                totalWeight += p.getWeight() * qty;
            }
        }
        System.out.printf("Total package weight %.1fkg%n%n", totalWeight);

        return Math.round(BASE + PER_KG * totalWeight);
    }
}



final class OrderProcessor {
    private final DeliveryService delivery = new DeliveryService();

    public void checkout(Customer c, Cart cart) {
        if (cart.isEmpty()) throw new IllegalStateException("Cart is empty");

        double subtotal = 0;
        for (var e : cart.getItems().entrySet()) {
            Product p = e.getKey();
            int qty   = e.getValue();
            if (p.isExpired())
                throw new IllegalStateException(p.getName() + " expired before checkout");
            if (qty > p.getStock())
                throw new IllegalStateException("Not enough " + p.getName() + " in stock");
            subtotal += p.getPrice() * qty;
        }

        double shipping = delivery.ship(cart.getItems());
        double total    = subtotal + shipping;
        if (c.getBalance() < total)
            throw new IllegalStateException("Insufficient balance for " + c.getName());

        c.withdraw(total);
        cart.getItems().forEach((p, qty) -> p.reduceStock(qty));

        System.out.println("** Checkout receipt **");
        cart.getItems().forEach((p, qty) ->
                System.out.printf("%dx %-18s %.0f%n", qty, p.getName(), p.getPrice() * qty));
        System.out.println("----------------------");
        System.out.printf("Subtotal %.0f%n",  subtotal);
        System.out.printf("Shipping %.0f%n",  shipping);
        System.out.printf("Amount   %.0f%n",  total);
        System.out.printf("Balance left: %.0f%n%n", c.getBalance());
    }
}



public class ECommerceDemo {
    public static void main(String[] args) {

        /*  Inventory */
        Product cheese   = new ExpiringProduct("Cheese",   100, 10,
                             LocalDate.now().plusDays(3), 0.4);
        Product biscuits = new ExpiringProduct("Biscuits", 150, 5,
                             LocalDate.now().plusDays(2), 0.7);
        Product tv       = new DurableProduct("TV",    5000, 3, 8);
        Product card     = new DigitalProduct("Scratch Card", 50, 100);

  
        Customer bob = new Customer("Bob", 1000);
        Cart cart    = new Cart();
        cart.add(cheese,   2);
        cart.add(biscuits, 1);
        cart.add(card,     1);

        new OrderProcessor().checkout(bob, cart);

       
      try { new OrderProcessor().checkout(bob, new Cart()); }
        catch (Exception e) {
             System.out.println(e.getMessage());
         }

       try {
           Customer JANE = new Customer("JANE", 50);
            Cart big = new Cart(); big.add(tv, 1);
            new OrderProcessor().checkout(JANE, big);
       } catch (Exception e)
       { 
            System.out.println(e.getMessage()); 
         }

      try {
            Product milk = new ExpiringProduct("Milk", 20, 1,
                             LocalDate.now().minusDays(1), 0.5);
            Cart c = new Cart(); c.add(milk, 1);
        } catch (Exception e) { 
            System.out.println(e.getMessage()); 
        }

       try { Cart c = new Cart(); c.add(tv, 99); }
        catch (Exception e) {
             System.out.println(e.getMessage());
       }
    }
}
