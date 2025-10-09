import java.io.*;
import java.util.*;
import javax.swing.*;

public class MiniFarmManagerV1 implements Serializable {

  // --- Data stuff ---
  static class Product implements Serializable {
    int id;
    String itemId;
    String name;
    String category;
    double price;
    boolean taxable;
    int stock;

    public String toString() {
      return "[" + itemId + "] " + name + " - $" + String.format("%.2f", price) + " (" + stock + " in stock)";
    }
  }

  static class DataStore implements Serializable {
    int nextId = 1;
    List<Product> products = new ArrayList<>();
    double taxRate = 0.07;
  }

  // --- variables / file ---
  private static DataStore data;
  private static final String FILE = "data/farm.dat";

  public static void main(String[] args) {
    loadData();
    mainMenu();
    saveData();
  }

  // --- menus ---
  private static void mainMenu() {
    String[] choices = {"Inventory", "Sales", "Save & Exit"};
    while (true) {
      int pick = JOptionPane.showOptionDialog(null,
        "Family Farm (v1)\nChoose an option:",
        "Main Menu", 0, JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
      if (pick == 0) inventoryMenu();
      else if (pick ==1) salesMenu();
      else break;
    }
  }

  private static void inventoryMenu() {
    String[] options = {"Add Product", "List Products", "Back"};
    int pick = JOptionPane.showOptionDialog(null,
      "Inventory Menu", "Inventory", 0, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

    if (pick == 0) addNewProduct();
    else if (pick == 1) listAllProducts();
  }

  // --- Sales Menu ---

  private static void salesMenu() {
    String[] options = {"Quick Sale (single item)", "Back"};
    int pick = JOptionPane.showOptionDialog(null, "Sales Menu", "Sales", 0, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

      if (pick == 0) quickSale();
  }

  private static void quickSale() {
    // ask item
    String id = JOptionPane.showInputDialog("Item ID to sell:");
    if (id == null || id.trim().isEmpty()) return;

    Product p = findByItemId(id);
    if (p == null) {
      showMsg("Item Not Found.");
      return;   
    }
     // Ask Qnty
     int qty = 0;
     try {
      qty = Integer.parseInt(JOptionPane.showInputDialog("Quantity (stock " + "):"));
     } catch (Exception e) { /* leave qty=0 */ }
     if (qty <= 0) { showMsg("Invalid quantity."); return; }
     //totals
     double sub = p.price * qty;
     double tax = p.taxable ? (sub * data.taxRate) : 0.0;
     tax = Math.round(tax * 100.0) / 100.0;
     double total = Math.round((sub * tax) * 100.0) / 100.0;
     // Payment Method
     String [] pay = {"CASH", "CARD", "OTHER", "Cancel"};
     String pm = (String) JOptionPane.showInputDialog(null,"Subtotal: $" +  String.format("%.2f", sub) + "\nTax: $" + String.format("%.2f", tax) + "\nTotal: $" + String.format("%.2f", total) + "\n\nPayment method:", "Checkout", JOptionPane.PLAIN_MESSAGE, null, pay, pay[0]);
     if (pm == null || "cancel".equals(pm)) return;
     // COMMIT: REDUCE STOCK AND SAVE
     p.stock = p.stock = qty;
     showMsg("Sale complete. \n" + qty + " x " + p.name + "\nPaid: " + pm + "\nTotal: $" + String.format("%.2f", total));
     saveData();
    }
       
  

  // --- add & list ---
  private static void addNewProduct() {
    Product p = new Product();
    p.id = data.nextId++;

    p.itemId = JOptionPane.showInputDialog("Item ID:");
    if (p.itemId == null || p.itemId.trim().isEmpty()) {
      showMsg("Canceled or empty ID");
      return;
    }

    p.name = JOptionPane.showInputDialog("Name:");
    if (p.name == null || p.name.trim().isEmpty()) return;

    p.category = JOptionPane.showInputDialog("Category (Feed/Toys/etc):");
    if (p.category == null) p.category = "";

    try {
      p.price = Double.parseDouble(JOptionPane.showInputDialog("Unit Price:"));
    } catch (Exception e) {
      showMsg("Invalid price");
      return;
    }

    int taxAsk = JOptionPane.showConfirmDialog(null, "Taxable?", "Tax", JOptionPane.YES_NO_OPTION);
    p.taxable = (taxAsk == JOptionPane.YES_OPTION);

    try {
      p.stock = Integer.parseInt(JOptionPane.showInputDialog("Starting Stock:"));
    } catch (Exception e) {
      p.stock = 0;
    }

    data.products.add(p);
    showMsg("Added: " + p);
    saveData();
  }

  private static void listAllProducts() {
    if (data.products.isEmpty()) {
      showMsg("No products yet.");
      return;
    }
    StringBuilder out = new StringBuilder("Products:\n\n");
    for (Product p : data.products) out.append("- ").append(p).append("\n");
    JTextArea text = new JTextArea(out.toString(), 15, 40);
    text.setEditable(false);
    JOptionPane.showMessageDialog(null, new JScrollPane(text), "Product List", JOptionPane.PLAIN_MESSAGE);
  }

  // --- helpers ---
  private static void showMsg(String msg) {
    JOptionPane.showMessageDialog(null, msg);
  }

  private static Product findByItemId(String id) {
    if (id == null) return null;
    String key = id.trim();
    for (Product p : data.products) {
      if (p.itemId != null && p.itemId.equalsIgnoreCase(key)) return p;
    }
    return null;
  }

  private static void loadData() {
    File f = new File(FILE);
    if (!f.exists()) { data = new DataStore(); return; }
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
      data = (DataStore) in.readObject();
      in.close();
    } catch (Exception e) {
      data = new DataStore();
    }
  }

  private static void saveData() {
    try {
      File f = new File(FILE);
      if (f.getParentFile() != null) f.getParentFile().mkdirs();
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
      out.writeObject(data);
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}