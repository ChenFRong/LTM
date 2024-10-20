package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import run.ClientRun;

public class PlaywithFriend extends JFrame {
    private JButton createRoomButton;
    private JButton findRoomButton;
    private JTextField roomCodeInput;
    private JButton joinRoomButton;
    private JLabel statusLabel;
    private JLabel welcomeLabel;
    private JButton backButton;

    public PlaywithFriend() {
        setTitle("Hãy chọn giá đúng");
        setLayout(new BorderLayout());

        // Tạo panel cho nút Back
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButton = new JButton("Quay lại");
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        // Panel chính
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        welcomeLabel = new JLabel("Welcome, ");
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(welcomeLabel, gbc);

        

        createRoomButton = new JButton("Tạo phòng");
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(createRoomButton, gbc);

        findRoomButton = new JButton("Tìm phòng");
        gbc.gridy = 2;
        mainPanel.add(findRoomButton, gbc);

        roomCodeInput = new JTextField(10);
        roomCodeInput.setVisible(false);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(roomCodeInput, gbc);

        joinRoomButton = new JButton("Vào phòng");
        joinRoomButton.setVisible(false);
        gbc.gridx = 1;
        mainPanel.add(joinRoomButton, gbc);

        statusLabel = new JLabel("", JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(statusLabel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        createRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ClientRun.socketHandler.createRoom();
            }
        });

        findRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                roomCodeInput.setVisible(true);
                joinRoomButton.setVisible(true);
                statusLabel.setText("Nhập mã phòng và nhấn 'Vào phòng'");
            }
        });

        joinRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomCode = roomCodeInput.getText();
                if (!roomCode.isEmpty()) {
                    ClientRun.socketHandler.joinRoom(roomCode);
                } else {
                    JOptionPane.showMessageDialog(PlaywithFriend.this, "Vui lòng nhập mã phòng", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Đóng cửa sổ hiện tại
                ClientRun.socketHandler.backToHome(); // Sử dụng socketHandler để quay lại Home
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void showRoomCode(String roomCode) {
        statusLabel.setText("Phòng đã được tạo. Mã phòng: " + roomCode);
    }

    public void setUsername(String username) {
        welcomeLabel.setText("Welcome, " + username);
    }
}
