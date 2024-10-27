package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import run.ClientRun;

public class ProductView extends JFrame {

    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, priceField, descriptionField, imagePathField;
    private JButton addButton, editButton, deleteButton, refreshButton, closeButton;

    public ProductView() {
        initComponents();
        populateProductTable(); // Populate the product table upon initialization
    }

    private void initComponents() {
        setTitle("Quản lý sản phẩm");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Setup table model with column names
        String[] columnNames = {"ID", "Tên sản phẩm", "Mô tả", "Giá", "Đường dẫn ảnh"};
        tableModel = new DefaultTableModel(columnNames, 0);
        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // Setup input panel for product details
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        nameField = new JTextField(20);
        priceField = new JTextField(10);
        descriptionField = new JTextField(20);
        imagePathField = new JTextField(20);

        inputPanel.add(new JLabel("Tên sản phẩm:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Mô tả:"));
        inputPanel.add(descriptionField);
        inputPanel.add(new JLabel("Giá:"));
        inputPanel.add(priceField);
        inputPanel.add(new JLabel("Đường dẫn ảnh:"));
        inputPanel.add(imagePathField);

        // Setup action buttons
        addButton = new JButton("Thêm");
        editButton = new JButton("Sửa");
        deleteButton = new JButton("Xóa");
        refreshButton = new JButton("Tải lại");
        closeButton = new JButton("Close");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        inputPanel.add(buttonPanel);
        add(inputPanel, BorderLayout.SOUTH);

        // Add action listeners for buttons
        addButton.addActionListener(e -> addProduct());
        editButton.addActionListener(e -> editProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        refreshButton.addActionListener(e -> populateProductTable());
        closeButton.addActionListener((ActionEvent e) -> dispose());

        pack();
        setLocationRelativeTo(null);
    }

    private void populateProductTable() {
        clearProductTable();
        


    // Create a new instance of ProductView and set it visible
    
        
        // Populate the table with product data
        // Assuming you have a method in ClientRun.socketHandler to retrieve products
        // Example: List<Product> products = ClientRun.socketHandler.getProducts();
        // for each product, add it to the table
        // This is a placeholder; you need to implement the actual data retrieval
    }

    public void setListProducts(String id, String name, String price, String description, String imagePath) {
        tableModel.addRow(new Object[]{id, name, description, price, imagePath});
    }

    public void clearProductTable() {
        tableModel.setRowCount(0);
    }

    private void addProduct() {
        String name = nameField.getText();
        String price = priceField.getText();
        String description = descriptionField.getText();
        String imagePath = imagePathField.getText();

        if (!name.isEmpty() && !price.isEmpty() && !description.isEmpty() && !imagePath.isEmpty()) {
            try {
                double priceValue = Double.parseDouble(price);
                ClientRun.socketHandler.addProduct(name, description, priceValue, imagePath);
                clearFields();
                JOptionPane.showMessageDialog(this, "Sản phẩm đã được thêm thành công.");
                populateProductTable();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Giá không hợp lệ: " + price);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin sản phẩm.");
        }
    }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            String id = tableModel.getValueAt(selectedRow, 0).toString();
            String name = nameField.getText();
            String price = priceField.getText();
            String description = descriptionField.getText();
            String imagePath = imagePathField.getText();

            if (!name.isEmpty() && !price.isEmpty() && !description.isEmpty() && !imagePath.isEmpty()) {
                try {
                    double priceValue = Double.parseDouble(price);
                    System.out.println("dfdf" + id + name + price + description + imagePath);
                    ClientRun.socketHandler.updateProduct(Integer.parseInt(id), name, description, priceValue, imagePath);
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Sản phẩm đã được cập nhật.");
                    populateProductTable();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Giá không hợp lệ: " + price);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin sản phẩm.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm để sửa.");
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            String id = tableModel.getValueAt(selectedRow, 0).toString();
            ClientRun.socketHandler.deleteProduct(Integer.parseInt(id));
            tableModel.removeRow(selectedRow);
            clearFields();
            JOptionPane.showMessageDialog(this, "Sản phẩm đã được xóa.");
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm để xóa.");
        }
    }

    private void clearFields() {
        nameField.setText("");
        priceField.setText("");
        descriptionField.setText("");
        imagePathField.setText("");
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new ProductView().setVisible(true));
    }
}
