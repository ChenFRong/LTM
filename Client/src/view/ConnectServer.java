package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import run.ClientRun;

public class ConnectServer extends JFrame {

    private JTextField txIP;
    private JTextField txPort;
    private JButton btnConnect;

    public ConnectServer() {
        // Thiết lập tiêu đề cửa sổ
        setTitle("Connect to Server");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout()); // Sử dụng GridBagLayout để dễ dàng định vị các thành phần

        // Tạo các thành phần
        JLabel jLabel1 = new JLabel("CONNECT TO SERVER");
        jLabel1.setFont(new Font("Tahoma", Font.PLAIN, 18));

        JLabel jLabel2 = new JLabel("IP:");
        jLabel2.setFont(new Font("Tahoma", Font.PLAIN, 14));

        txIP = new JTextField("127.0.0.1", 20);
        txIP.setFont(new Font("Tahoma", Font.PLAIN, 14));

        JLabel jLabel3 = new JLabel("PORT:");
        jLabel3.setFont(new Font("Tahoma", Font.PLAIN, 14));

        txPort = new JTextField("2200", 20);
        txPort.setFont(new Font("Tahoma", Font.PLAIN, 14));

        btnConnect = new JButton("CONNECT");
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        // Định vị các thành phần trên cửa sổ
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Thiết lập khoảng cách giữa các thành phần

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(jLabel1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(jLabel2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        add(txIP, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(jLabel3, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        add(txPort, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Chiếm toàn bộ chiều rộng
        add(btnConnect, gbc);

        // Thiết lập kích thước và vị trí
        pack();
        setLocationRelativeTo(null); // Đặt cửa sổ ở giữa màn hình
        setVisible(true); // Hiển thị cửa sổ
    }

    private void btnConnectActionPerformed(ActionEvent evt) {
    String ip;
    int port;

    // Xác thực đầu vào
    try {
        ip = txIP.getText();
        port = Integer.parseInt(txPort.getText());

        if (port < 0 || port > 65535) {
            JOptionPane.showMessageDialog(this, "Port phải từ 0 - 65535", "Sai port", JOptionPane.ERROR_MESSAGE);
            txPort.requestFocus();
            return;
        }

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Port phải là số nguyên", "Sai port", JOptionPane.ERROR_MESSAGE);
        txPort.requestFocus();
        return;
    }

    // Gọi hàm kết nối tới server
    String result = ClientRun.socketHandler.connect(ip, port);
    if (result.equals("success")) {
        JOptionPane.showMessageDialog(this, "Kết nối tới server " + ip + ":" + port + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        // Chuyển sang màn hình đăng nhập
        ClientRun.closeScene(ClientRun.SceneName.CONNECTSERVER);
        ClientRun.openScene(ClientRun.SceneName.LOGIN);
    } else {
        JOptionPane.showMessageDialog(this, "Kết nối không thành công: " + result.split(";")[1], "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConnectServer());
    }
}
