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
    String[] choices = {"Inventory", "Save & Exit"};
    while (true) {
      int pick = JOptionPane.showOptionDialog(null,
        "Family Farm (v1)\nChoose an option:",
        "Main Menu", 0, JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
      if (pick == 0) inventoryMenu();
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