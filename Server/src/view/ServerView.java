package view;

import javax.swing.*;
import run.ServerRun;

public class ServerView extends JFrame {

    private JTextArea jTextArea1;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JButton shutdownButton;

    public ServerView() {
        initComponents();
    }

    private void initComponents() {
        // Tạo các thành phần giao diện
        jLabel1 = new JLabel("Server Running...");
        jTextArea1 = new JTextArea();
        jScrollPane1 = new JScrollPane(jTextArea1);
        shutdownButton = new JButton("Shutdown Server");

        // Cấu hình các thuộc tính cho các thành phần
        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18));
        
        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("Hello world!");
        
        // Đặt JTextArea vào JScrollPane để có thể cuộn
        jScrollPane1.setViewportView(jTextArea1);

        // Sử dụng layout kiểu null (absolute positioning)
        setLayout(null);
        
        // Đặt vị trí và kích thước cho các thành phần
        jLabel1.setBounds(200, 30, 200, 30);  // (x, y, width, height)
        jScrollPane1.setBounds(50, 80, 400, 200);
        shutdownButton.setBounds(200, 300, 200, 30);
        
        // Thêm các thành phần vào JFrame
        add(jLabel1);
        add(jScrollPane1);
        add(shutdownButton);
        
        // Cấu hình cho JFrame
        setTitle("Server View");
        setSize(500, 350);  // Kích thước JFrame (width, height)
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Căn giữa màn hình

        shutdownButton.addActionListener(e -> ServerRun.shutdownServer());
    }

    public static void main(String[] args) {
        // Hiển thị giao diện
        SwingUtilities.invokeLater(() -> {
            new ServerView().setVisible(true);
        });
    }
}
