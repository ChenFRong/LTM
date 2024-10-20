package view;

import controller.SocketHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import run.ClientRun;

public class LoginView extends JFrame {
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    // HashMap lưu thông tin username và password
    private HashMap<String, String> userDatabase = new HashMap<>();

    public LoginView() {
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
        JLabel titleLabel = new JLabel("Welcome to My Game", JLabel.CENTER);
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

        // Tạo JPanel chứa các nút login và register
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Đặt các nút ở giữa và cách nhau 10px
        loginButton = new JButton("Đăng nhập");
        loginButton.setBackground(new Color(0, 153, 76)); // Màu xanh lá cây
        loginButton.setForeground(Color.WHITE);
        buttonPanel.add(loginButton);

        registerButton = new JButton("Đăng ký");
        registerButton.setBackground(new Color(0, 102, 204)); // Màu xanh dương
        registerButton.setForeground(Color.WHITE);
        buttonPanel.add(registerButton);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(statusLabel, gbc);

        // Thêm panel vào cửa sổ
        add(panel, BorderLayout.CENTER);

        // Thiết lập hành động khi nhấn nút Login
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userTextField.getText();
                String password = String.valueOf(passwordField.getPassword());

                // Sử dụng SocketHandler từ ClientRun
                if (ClientRun.socketHandler.isConnected()) {
                    ClientRun.socketHandler.login(username, password);
                } else {
                    String connectionStatus = ClientRun.socketHandler.connect("127.0.0.1", 2200);
                    if (connectionStatus.equals("success")) {
                        ClientRun.socketHandler.login(username, password);
                    } else {
                        statusLabel.setText("Connection to server failed.");
                        statusLabel.setForeground(Color.RED);
                    }
                }
            }
        });

        // Thiết lập hành động khi nhấn nút Register
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Mở trang đăng ký
                new RegisterView();
                dispose(); // Đóng cửa sổ đăng nhập nếu cần
            }
        });

        // Cài đặt các thuộc tính cho cửa sổ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // Đặt cửa sổ ở giữa màn hình
        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginView(); // Khởi tạo giao diện đăng nhập
    }
}
