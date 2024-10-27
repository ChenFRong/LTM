/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import run.ClientRun;

public class RegisterView extends JFrame {
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JLabel statusLabel;

    public RegisterView() {
        // Thiết lập tiêu đề cho cửa sổ
        setTitle("Hãy chọn giá đúng");
        setLayout(new BorderLayout());

        // Tạo panel chứa các thành phần chính
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 240, 240)); // Màu nền nhẹ

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tạo các thành phần giao diện
        JLabel titleLabel = new JLabel("Đăng ký tài khoản", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 204)); // Màu xanh dương
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        JLabel userLabel = new JLabel("Username:");
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(userLabel, gbc);

        userTextField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(userTextField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(confirmPasswordField, gbc);

        registerButton = new JButton("Đăng ký");
        registerButton.setBackground(new Color(0, 153, 76)); // Màu xanh lá cây
        registerButton.setForeground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(registerButton, gbc);

        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setForeground(Color.RED);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(statusLabel, gbc);

        // Thêm panel vào cửa sổ
        add(panel, BorderLayout.CENTER);

        // Thiết lập hành động khi nhấn nút Register
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userTextField.getText();
                String password = String.valueOf(passwordField.getPassword());
                String confirmPassword = String.valueOf(confirmPasswordField.getPassword());

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    statusLabel.setText("Please fill in all fields.");
                } else if (!password.equals(confirmPassword)) {
                    statusLabel.setText("Passwords do not match.");
                } else {
                    ClientRun.socketHandler.register(username, password);
                    // Thêm logic để lưu thông tin người dùng nếu cần
                     // Mở lại trang đăng nhập
                   
                }
            }
        });

        // Cài đặt các thuộc tính cho cửa sổ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // Đặt cửa sổ ở giữa màn hình
        setVisible(true);
    }

    public static void main(String[] args) {
        new RegisterView(); // Khởi tạo giao diện đăng ký
    }

   
}
