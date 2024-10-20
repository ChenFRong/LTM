package view;

import javax.swing.*;
import java.awt.*;
import javax.swing.SwingUtilities;

public class GameRoom extends javax.swing.JFrame {
    private String playerName;
    private String roomCode;
    private javax.swing.JLabel roomCodeLabel;
    private javax.swing.JLabel playerNameLabel;
    private javax.swing.JLabel opponentNameLabel;
    private javax.swing.JLabel statusLabel;

    public GameRoom(String playerName, String roomCode) {
        initComponents();
        this.playerName = playerName;
        this.roomCode = roomCode;
        roomCodeLabel.setText("Mã phòng: " + roomCode);
        playerNameLabel.setText("Bạn: " + playerName);
        setVisible(true);
    }

    public void setOpponentName(String newOpponentName, String playerOrder) {
        System.out.println("GameRoom: Setting opponent name to " + newOpponentName + ", playerOrder: " + playerOrder);
        SwingUtilities.invokeLater(() -> {
            // Thay đổi logic ở đây
            if (playerOrder.equals("FIRST")) {
                playerNameLabel.setText("Bạn (Người chơi 1): " + playerName);
                opponentNameLabel.setText("Đối thủ (Người chơi 2): " + newOpponentName);
            } else {
                playerNameLabel.setText("Bạn (Người chơi 2): " + playerName);
                opponentNameLabel.setText("Đối thủ (Người chơi 1): " + newOpponentName);
            }
            
            statusLabel.setText("Đối thủ đã tham gia. Chuẩn bị bắt đầu trò chơi!");
            System.out.println("Updated labels - Player: " + playerNameLabel.getText() + ", Opponent: " + opponentNameLabel.getText());
        });
    }

    public void startGame() {
        statusLabel.setText("Trò chơi đã bắt đầu! Chúc may mắn!");
        // Thêm các thành phần UI cho trò chơi ở đây
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Phòng chơi game");

        roomCodeLabel = new javax.swing.JLabel();
        playerNameLabel = new javax.swing.JLabel();
        opponentNameLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(roomCodeLabel)
                    .addComponent(playerNameLabel)
                    .addComponent(opponentNameLabel)
                    .addComponent(statusLabel))
                .addContainerGap(300, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(roomCodeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(playerNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(opponentNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statusLabel)
                .addContainerGap(200, Short.MAX_VALUE))
        );

        pack();
    }
}
