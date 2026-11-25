package stockmaster;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    private static final String DATA_FILE = "stockmaster-data.ser";
    private final List<Product> products = new ArrayList<>();
    private final List<Sale> sales = new ArrayList<>();
    private String currentUser = "";
    private String currentRole = "";

    private JLabel totalProductsLabel, totalUnitsLabel, lowStockLabel, revenueLabel;
    private DefaultTableModel productModel, salesModel;
    private JTable productTable, salesTable;
    private JTextField nameField, categoryField, priceField, quantityField, lowField, searchField;
    private JTextField saleProductField, saleQtyField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().showLogin());
    }

    private void showLogin() {
        JTextField username = new JTextField("admin", 15);
        JPasswordField password = new JPasswordField("admin123", 15);
        Object[] fields = {"Username:", username, "Password:", password,
            "Demo accounts: admin / admin123   |   employee / emp123"};
        int result = JOptionPane.showConfirmDialog(null, fields,
                "StockMaster Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String u = username.getText().trim();
        String p = new String(password.getPassword());
        if (("admin".equalsIgnoreCase(u) && "admin123".equals(p))
                || ("employee".equalsIgnoreCase(u) && "emp123".equals(p))) {
            currentUser = u;
            currentRole = u.equalsIgnoreCase("admin") ? "Admin" : "Employee";
            loadData();
            buildWindow();
        } else {
            JOptionPane.showMessageDialog(null, "Incorrect username or password.");
            showLogin();
        }
    }

    private void buildWindow() {
        setTitle("StockMaster - Inventory Management System");
        setSize(1050, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                saveData();
                dispose();
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 251));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(28, 57, 94));
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        JLabel title = new JLabel("STOCKMASTER");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        JLabel user = new JLabel(currentUser + "  |  " + currentRole);
        user.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        header.add(user, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboardPanel());
        tabs.addTab("Products", productsPanel());
        tabs.addTab("Record Sale", salePanel());
        tabs.addTab("Sales History", salesPanel());

        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
        refreshAll();
        setVisible(true);
    }

    private JPanel dashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        panel.setBackground(new Color(245, 247, 251));
        JLabel heading = new JLabel("Business Overview");
        heading.setFont(new Font("Arial", Font.BOLD, 23));

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 14));
        cards.setOpaque(false);
        totalProductsLabel = card(cards, "PRODUCTS");
        totalUnitsLabel = card(cards, "UNITS IN STOCK");
        lowStockLabel = card(cards, "LOW STOCK ITEMS");
        revenueLabel = card(cards, "TODAY'S REVENUE");

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);

        JTextArea note = new JTextArea(
            "Welcome to StockMaster.\n\n"
            + "Use Products to add and maintain your inventory.\n"
            + "Use Record Sale to process customer purchases.\n"
            + "Low-stock products are those at or below their reorder level.\n\n"
            + "Your data is saved locally in stockmaster-data.ser.");
        note.setEditable(false);
        note.setFont(new Font("Arial", Font.PLAIN, 15));
        note.setBackground(Color.WHITE);
        note.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        panel.add(top, BorderLayout.NORTH);
        panel.add(note, BorderLayout.CENTER);
        return panel;
    }

    private JLabel card(JPanel parent, String caption) {
        JPanel box = new JPanel(new BorderLayout(5, 10));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 238)),
            BorderFactory.createEmptyBorder(18, 14, 18, 14)));
        JLabel name = new JLabel(caption);
        name.setFont(new Font("Arial", Font.BOLD, 11));
        name.setForeground(new Color(90, 103, 120));
        JLabel value = new JLabel("0");
        value.setFont(new Font("Arial", Font.BOLD, 25));
        value.setForeground(new Color(28, 57, 94));
        box.add(name, BorderLayout.NORTH);
        box.add(value, BorderLayout.CENTER);
        parent.add(box);
        return value;
    }

    private JPanel productsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridLayout(2, 6, 8, 7));
        nameField = new JTextField();
        categoryField = new JTextField();
        priceField = new JTextField();
        quantityField = new JTextField();
        lowField = new JTextField("5");
        searchField = new JTextField();

        form.add(new JLabel("Product Name"));
        form.add(new JLabel("Category"));
        form.add(new JLabel("Unit Price (R)"));
        form.add(new JLabel("Quantity"));
        form.add(new JLabel("Low Stock Level"));
        form.add(new JLabel("Search"));
        form.add(nameField);
        form.add(categoryField);
        form.add(priceField);
        form.add(quantityField);
        form.add(lowField);
        form.add(searchField);

        productModel = new DefaultTableModel(
            new String[]{"ID", "Product", "Category", "Price (R)", "Quantity", "Low Level"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        productTable = new JTable(productModel);
        productTable.setRowHeight(24);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() >= 0) {
                int r = productTable.convertRowIndexToModel(productTable.getSelectedRow());
                Product p = findProduct(productModel.getValueAt(r, 0).toString());
                if (p != null) {
                    nameField.setText(p.name);
                    categoryField.setText(p.category);
                    priceField.setText(String.valueOf(p.price));
                    quantityField.setText(String.valueOf(p.quantity));
                    lowField.setText(String.valueOf(p.lowLevel));
                }
            }
        });

        JButton add = new JButton("Add Product");
        JButton update = new JButton("Update Selected");
        JButton delete = new JButton("Delete Selected");
        JButton clear = new JButton("Clear");
        add.addActionListener(e -> addProduct());
        update.addActionListener(e -> updateProduct());
        delete.addActionListener(e -> deleteProduct());
        clear.addActionListener(e -> clearProductFields());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add); buttons.add(update); buttons.add(delete); buttons.add(clear);

        JPanel top = new JPanel(new BorderLayout(5, 8));
        top.add(form, BorderLayout.NORTH);
        top.add(buttons, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String s = searchField.getText().trim().replaceAll("[\\\\\\[\\]{}()*+?.\\\\$^|]", "\\\\$0");
                if (productTable.getRowSorter() == null)
                    productTable.setRowSorter(new javax.swing.table.TableRowSorter<>(productModel));
                ((javax.swing.table.TableRowSorter<?>) productTable.getRowSorter())
                    .setRowFilter(s.isEmpty() ? null : javax.swing.RowFilter.regexFilter("(?i)" + s));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        return panel;
    }

    private JPanel salePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Product ID:"), c);
        c.gridx = 1;
        saleProductField = new JTextField(20);
        panel.add(saleProductField, c);
        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("Quantity sold:"), c);
        c.gridx = 1;
        saleQtyField = new JTextField(20);
        panel.add(saleQtyField, c);
        c.gridx = 1; c.gridy = 2;
        JButton sell = new JButton("Complete Sale");
        sell.addActionListener(e -> recordSale());
        panel.add(sell, c);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        panel.add(new JLabel("Tip: Find the Product ID in the Products tab."), c);
        return panel;
    }

    private JPanel salesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        salesModel = new DefaultTableModel(
            new String[]{"Sale ID", "Date", "Product ID", "Product", "Quantity", "Total (R)", "Sold By"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        salesTable = new JTable(salesModel);
        salesTable.setRowHeight(24);
        JButton refresh = new JButton("Refresh Sales");
        refresh.addActionListener(e -> refreshSales());
        panel.add(refresh, BorderLayout.NORTH);
        panel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        return panel;
    }

    private void addProduct() {
        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int qty = Integer.parseInt(quantityField.getText().trim());
            int low = Integer.parseInt(lowField.getText().trim());
            if (name.isEmpty() || category.isEmpty() || price < 0 || qty < 0 || low < 0)
                throw new IllegalArgumentException();
            String id = "P" + (products.size() + 1);
            while (findProduct(id) != null) id = "P" + (Integer.parseInt(id.substring(1)) + 1);
            products.add(new Product(id, name, category, price, qty, low));
            saveData(); refreshAll(); clearProductFields();
            JOptionPane.showMessageDialog(this, "Product added successfully. ID: " + id);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Enter a product name, category, valid price, quantity and low-stock level.");
        }
    }

    private void updateProduct() {
        int selected = productTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to update.");
            return;
        }
        try {
            int r = productTable.convertRowIndexToModel(selected);
            Product p = findProduct(productModel.getValueAt(r, 0).toString());
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int qty = Integer.parseInt(quantityField.getText().trim());
            int low = Integer.parseInt(lowField.getText().trim());
            if (p == null || name.isEmpty() || category.isEmpty()
                    || price < 0 || qty < 0 || low < 0) throw new IllegalArgumentException();
            p.name = name; p.category = category; p.price = price;
            p.quantity = qty; p.lowLevel = low;
            saveData(); refreshAll(); clearProductFields();
            JOptionPane.showMessageDialog(this, "Product updated successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid product details.");
        }
    }

    private void deleteProduct() {
        int selected = productTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to delete.");
            return;
        }
        int r = productTable.convertRowIndexToModel(selected);
        String id = productModel.getValueAt(r, 0).toString();
        int answer = JOptionPane.showConfirmDialog(this,
            "Delete this product?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            products.removeIf(p -> p.id.equals(id));
            saveData(); refreshAll(); clearProductFields();
        }
    }

    private void clearProductFields() {
        nameField.setText(""); categoryField.setText(""); priceField.setText("");
        quantityField.setText(""); lowField.setText("5"); productTable.clearSelection();
    }

    private void recordSale() {
        try {
            String id = saleProductField.getText().trim();
            int qty = Integer.parseInt(saleQtyField.getText().trim());
            Product p = findProduct(id);
            if (p == null || qty <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a valid Product ID and quantity.");
                return;
            }
            if (p.quantity < qty) {
                JOptionPane.showMessageDialog(this, "Insufficient stock. Available: " + p.quantity);
                return;
            }
            double total = p.price * qty;
            p.quantity -= qty;
            String saleId = "S" + (sales.size() + 1);
            sales.add(new Sale(saleId, LocalDate.now().toString(), p.id,
                    p.name, qty, total, currentUser));
            saveData(); refreshAll();
            saleProductField.setText(""); saleQtyField.setText("");
            JOptionPane.showMessageDialog(this,
                "Sale recorded!\nTotal: R" + String.format("%.2f", total));
            if (p.quantity <= p.lowLevel)
                JOptionPane.showMessageDialog(this, "LOW STOCK ALERT: " + p.name
                    + "\nRemaining quantity: " + p.quantity);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid whole-number quantity.");
        }
    }

    private Product findProduct(String id) {
        for (Product p : products) if (p.id.equalsIgnoreCase(id)) return p;
        return null;
    }

    private void refreshAll() {
        refreshProducts();
        refreshSales();
        int units = 0, low = 0;
        for (Product p : products) {
            units += p.quantity;
            if (p.quantity <= p.lowLevel) low++;
        }
        totalProductsLabel.setText(String.valueOf(products.size()));
        totalUnitsLabel.setText(String.valueOf(units));
        lowStockLabel.setText(String.valueOf(low));
        revenueLabel.setText("R" + String.format("%.2f", todayRevenue()));
    }

    private void refreshProducts() {
        productModel.setRowCount(0);
        for (Product p : products)
            productModel.addRow(new Object[]{p.id, p.name, p.category,
                String.format("%.2f", p.price), p.quantity, p.lowLevel});
    }

    private void refreshSales() {
        if (salesModel == null) return;
        salesModel.setRowCount(0);
        for (Sale s : sales)
            salesModel.addRow(new Object[]{s.id, s.date, s.productId,
                s.productName, s.quantity, String.format("%.2f", s.total), s.soldBy});
    }

    private double todayRevenue() {
        double total = 0;
        String today = LocalDate.now().toString();
        for (Sale s : sales) if (today.equals(s.date)) total += s.total;
        return total;
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            products.clear(); products.addAll((List<Product>) in.readObject());
            sales.clear(); sales.addAll((List<Sale>) in.readObject());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Could not read saved data. A new data file will be created.\n" + ex.getMessage());
            products.clear(); sales.clear();
        }
    }

    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(products);
            out.writeObject(sales);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage());
        }
    }

    private static class Product implements Serializable {
        private static final long serialVersionUID = 1L;
        String id, name, category;
        double price;
        int quantity, lowLevel;
        Product(String id, String name, String category, double price, int quantity, int lowLevel) {
            this.id=id; this.name=name; this.category=category; this.price=price;
            this.quantity=quantity; this.lowLevel=lowLevel;
        }
    }

    private static class Sale implements Serializable {
        private static final long serialVersionUID = 1L;
        String id, date, productId, productName, soldBy;
        int quantity;
        double total;
        Sale(String id, String date, String productId, String productName,
             int quantity, double total, String soldBy) {
            this.id=id; this.date=date; this.productId=productId;
            this.productName=productName; this.quantity=quantity;
            this.total=total; this.soldBy=soldBy;
        }
    }
}
