package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import run.ClientRun;
import helper.CountDownTimer;
import helper.CustumDateTimeFormatter;

import java.util.concurrent.Callable;

public class GameRoom extends javax.swing.JFrame {
    private String playerName;
    private String roomCode;
    private boolean isHost;
     private String productName;
        private String productImage;
       private double totalScore = 0;
    private String opponentName;
    private CountDownTimer matchTimer;
    private CountDownTimer waitingClientTimer;
    private boolean answer = false;
    private int round = 0;

    // Thêm khai báo biến roomId
    

    // Components
    private JLabel roomIdLabel;
    private JLabel playerNameLabel;
    private JLabel opponentNameLabel;
    private JLabel statusLabel;
    private JLabel productLabel;
    private JLabel imageLabel;
    private JLabel timerLabel;
    private JTextField guessInput;
    private JButton startButton;
    private JButton submitButton;
    private JLabel waitingLabel;
    private JPanel playAgainPanel;
    private JLabel waitingTimerLabel;
    private JButton yesButton;
    private JButton noButton;
    private JLabel resultLabel;


    public GameRoom(String playerName, String roomCode, boolean isHost) {
        this.playerName = playerName;
        this.roomCode = roomCode;
        this.isHost = isHost;
        initComponents();
        updateUIForPlayerRole();
        setVisible(true);
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Phòng chơi game");

        roomIdLabel = new JLabel("Số phòng: " + roomCode);
        playerNameLabel = new JLabel("Người chơi: " + playerName);
        opponentNameLabel = new JLabel("Đối thủ: ");
        statusLabel = new JLabel("Lượt chơi: " + round);
        productLabel = new JLabel("Sản phẩm: [name]");
        imageLabel = new JLabel("Hình ảnh");
        imageLabel.setPreferredSize(new Dimension(200, 200));
        timerLabel = new JLabel("Thời gian: 30s");
        guessInput = new JTextField();
        guessInput.setPreferredSize(new Dimension(300, 30)); // Điều chỉnh chiều rộng (300) và chiều cao (30) theo nhu cầu
        startButton = new JButton("Bắt đầu");
        submitButton = new JButton("Xác nhận");
        waitingLabel = new JLabel("Đợi chủ phòng bắt đầu game...");

        playAgainPanel = new JPanel();
        playAgainPanel.setBorder(BorderFactory.createTitledBorder("Question?"));
        waitingTimerLabel = new JLabel("00:00");
        yesButton = new JButton("Tiếp tục");
        noButton = new JButton("Dừng lại");
        resultLabel = new JLabel("Bạn có muốn chơi lại không?");

        // Set fonts
        Font labelFont = new Font("Roboto", Font.PLAIN, 18);
        roomIdLabel.setFont(labelFont);
        playerNameLabel.setFont(labelFont);
        opponentNameLabel.setFont(labelFont);
        statusLabel.setFont(labelFont);
        productLabel.setFont(labelFont);
        timerLabel.setFont(labelFont);
        guessInput.setFont(labelFont);
        waitingLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
        resultLabel.setFont(new Font("Roboto", Font.BOLD, 14));
        waitingTimerLabel.setFont(new Font("Tahoma", Font.BOLD, 14));

        // Add action listeners
        startButton.addActionListener(this::startButtonActionPerformed);
        submitButton.addActionListener(this::submitButtonActionPerformed);
        yesButton.addActionListener(this::yesButtonActionPerformed);
        noButton.addActionListener(this::noButtonActionPerformed);

        // Layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; add(roomIdLabel, gbc);
        gbc.gridy++; add(playerNameLabel, gbc);
        gbc.gridy++; add(opponentNameLabel, gbc);
        gbc.gridy++; add(statusLabel, gbc);
        gbc.gridy++; add(productLabel, gbc);
        gbc.gridy++; add(guessInput, gbc);
        gbc.gridy++; add(timerLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 6;
        add(imageLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.gridheight = 1;
        add(startButton, gbc);
        gbc.gridy++; add(submitButton, gbc);
        gbc.gridy++; add(waitingLabel, gbc);

        gbc.gridy++; add(playAgainPanel, gbc);

        // PlayAgainPanel layout
        playAgainPanel.setLayout(new FlowLayout());
        playAgainPanel.add(resultLabel);
        playAgainPanel.add(waitingTimerLabel);
        playAgainPanel.add(yesButton);
        playAgainPanel.add(noButton);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.add(guessInput);
        inputPanel.add(Box.createHorizontalGlue());
        add(inputPanel, gbc);

        pack();
        setLocationRelativeTo(null);
    }

    public void updateUIForPlayerRole() {
        if (isHost) {
            playerNameLabel.setText("Bạn (Người chơi 1): " + playerName);
            startButton.setVisible(true);
            waitingLabel.setVisible(false);
        } else {
            playerNameLabel.setText("Bạn (Người chơi 2): " + playerName);
            startButton.setVisible(false);
            waitingLabel.setVisible(true);
        }
        setRound(round);
        guessInput.setMinimumSize(new Dimension(300, 30));
        submitButton.setVisible(false);
        timerLabel.setVisible(false);
        playAgainPanel.setVisible(false);
    }

    public void setOpponentName(String newOpponentName, String playerOrder) {
        this.opponentName = newOpponentName;
        SwingUtilities.invokeLater(() -> {
            opponentNameLabel.setText("Đối thủ: " + newOpponentName);
            setRound(0);
            updateUIForPlayerRole();
            
        });
    }

    public void startGame(int matchTimeLimit) {
       
        answer = false;
        startButton.setVisible(false);
        waitingLabel.setVisible(false);
        submitButton.setVisible(true);
        timerLabel.setVisible(true);

        matchTimer = new CountDownTimer(matchTimeLimit);
        matchTimer.setTimerCallBack(
            null,
            (Callable) () -> {
                int currentTick = matchTimer.getCurrentTick();
                timerLabel.setText("Thời gian: " + CustumDateTimeFormatter.secondsToMinutes(currentTick));
                if (currentTick == 0) {
                    afterSubmit();
                }
                return null;
            },
            1
        );
    }

    public void afterSubmit() {
        submitButton.setVisible(false);
        waitingLabel.setVisible(true);
        waitingLabel.setText("Đang chờ kết quả từ server...");
        timerLabel.setVisible(false);
    }

    public void setWaitingRoom() {
        submitButton.setVisible(false);
        timerLabel.setVisible(false);
        startButton.setVisible(false);
        waitingLabel.setText("đợi trận đấu...");
        waitingReplyClient();
    }

    public void showAskPlayAgain(String msg) {
        SwingUtilities.invokeLater(() -> {
            playAgainPanel.setVisible(true);
            resultLabel.setText(msg);
            guessInput.setVisible(false);
            waitingTimerLabel.setText("00:30"); // Đặt thời gian ban đầu
            waitingReplyClient(); // Bắt đầu đếm ngược
        });
    }

    public void hideAskPlayAgain() {
        playAgainPanel.setVisible(false);
        guessInput.setVisible(true); // Hiển thị lại ô nhập giá trị khi ẩn nhãn
    }

    private void waitingReplyClient() {
        waitingClientTimer = new CountDownTimer(30);
        waitingClientTimer.setTimerCallBack(
            null,
            (Callable) () -> {
                waitingTimerLabel.setText("" + CustumDateTimeFormatter.secondsToMinutes(waitingClientTimer.getCurrentTick()));
                if (waitingTimerLabel.getText().equals("00:00") && !answer) {
                    hideAskPlayAgain();
                    // Không cần gọi declinePlayAgain() ở đây nữa
                }
                return null;
            },
            1
        );
    }

    private void startButtonActionPerformed(ActionEvent evt) {
        ClientRun.socketHandler.startGame(opponentName);
    }

    private void submitButtonActionPerformed(ActionEvent evt) {
        String guess = guessInput.getText();
        if (guess.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập giá dự đoán!");
        } else {
            ClientRun.socketHandler.submitResult(opponentName);
        }
    }

    private void yesButtonActionPerformed(ActionEvent evt) {
        ClientRun.socketHandler.acceptPlayAgain();
        answer = true;
        hideAskPlayAgain();
    }

    private void noButtonActionPerformed(ActionEvent evt) {
    ClientRun.socketHandler.declinePlayAgain();
    answer = true;
    if (waitingClientTimer != null) {
        waitingClientTimer.cancel(); // Hủy bỏ bộ đếm thời gian
    }
    hideAskPlayAgain();
}

    public void setStartGame(int matchTimeLimit) {
        SwingUtilities.invokeLater(() -> {
            startGame(matchTimeLimit);
        });
    }

    public void setRound(int round) {
        this.round = round;
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Lượt chơi: " + round);
        });
    }

  

    public void setProductInfo(String name, String imagePath) {
        this.productName = name;
        this.productImage = imagePath;
        productLabel.setText("Sản phẩm: " + name);
        // Cập nhật hình ảnh
        try {
            String resourcePath = "/resources/images/products/" + imagePath;
            java.net.URL imageURL = getClass().getResource(resourcePath);
            if (imageURL != null) {
                ImageIcon originalIcon = new ImageIcon(imageURL);
                Image img = originalIcon.getImage();
                Image scaledImg = img.getScaledInstance(imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImg);
                imageLabel.setIcon(icon);
            } else {
                System.out.println("Không tìm thấy hình ảnh: " + resourcePath);
                imageLabel.setIcon(null);
                imageLabel.setText("Không tìm thấy hình ảnh");
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageLabel.setIcon(null);
            imageLabel.setText("Lỗi khi tải hình ảnh");
        }
    }


    public void startNextRound(int timeLimit) {
        SwingUtilities.invokeLater(() -> {
//            round++;
//            setRound(round);
            guessInput.setEnabled(true);
            submitButton.setEnabled(true);
            guessInput.setText("");
            setStartGame(timeLimit);
            
        });
    }

//    private void startTimer(int timeLimit) {
//        if (matchTimer != null) {
//            matchTimer.stop();
//        }
//        matchTimer = new CountDownTimer(timeLimit);
//        matchTimer.setTimerCallBack(
//            null,
//            () -> {
//                int currentTick = matchTimer.getCurrentTick();
//                SwingUtilities.invokeLater(() -> {
//                    timerLabel.setText("Thời gian: " + CustumDateTimeFormatter.secondsToMinutes(currentTick));
//                });
//                if (currentTick == 0) {
//                    SwingUtilities.invokeLater(this::afterSubmit);
//                }
//                return null;
//            },
//            1
//        );
//        matchTimer.start();
//    }

    public void showRoundResult(int round,String winner, double actualPrice, 
                                double guessClient1, double guessClient2,
                                double roundScoreClient1, double roundScoreClient2,
                                double totalScoreClient1, double totalScoreClient2,
                                String nameClient1, String nameClient2) {
//        SwingUtilities.invokeLater(() -> {
//            String message = String.format("Kết quả lượt chơi %d:\nNgười thắng: %s\nGiá thực: %.2f\n%s đoán: %.2f\n%s đoán: %.2f\n\nĐiểm lượt này:\n%s: %.2f\n%s: %.2f\n\nTổng điểm:\n%s: %.2f\n%s: %.2f",
//                    round, winner, actualPrice,
//                    guessClient1,  guessClient2,
//                    roundScoreClient1, roundScoreClient2,
//                    totalScoreClient1, totalScoreClient2,
//                    
//                      
//            JOptionPane.showMessageDialog(this, message, "Kết quả lượt chơi", JOptionPane.INFORMATION_MESSAGE);
//        });
          SwingUtilities.invokeLater(() -> {
            String message;
            message =String.format("Kết quả lượt chơi:%d\n",round);
            boolean isClient1 = playerName.equals(ClientRun.socketHandler.getLoginUser());
            if (winner.equals(playerName)) {
                message += "Bạn thắng lượt này!";
            } else if (winner.equals("DRAW")) {
                message += "Hòa!";
            } else {
                message += "Bạn thua lượt này.";
            }
            message += String.format("\nGiá thực: %,.0f", actualPrice);
            message += String.format("\n\n%s:\nDự đoán: %,.0f\nĐiểm lượt này: %.1f\nTổng điểm: %.1f",
                                    nameClient1, guessClient1, roundScoreClient1, totalScoreClient1);
            message += String.format("\n\n%s:\nDự đoán: %,.0f\nĐiểm lượt này: %.1f\nTổng điểm: %.1f",
                                    nameClient2, guessClient2, roundScoreClient2, totalScoreClient2);
            // Hiển thị JOptionPane
            final JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
            final JDialog dialog = optionPane.createDialog(this, "Kết quả lượt chơi");
            
            // Tạo và bắt đầu timer để đóng dialog sau 3 giây
            Timer timer = new Timer(3000, e -> dialog.dispose());
            timer.setRepeats(false);
            timer.start();

            // Hiển thị dialog
            dialog.setVisible(true);
        });
    }
    public void showGameOver(String winner, double scoreClient1, double scoreClient2, String nameClient1, String nameClient2) {
        SwingUtilities.invokeLater(() -> {
            String message = String.format("Kết thúc trò chơi!\nNgười thắng: %s\n\nTổng điểm:\n%s: %.2f\n%s: %.2f",
                    winner, nameClient1, scoreClient1, nameClient2, scoreClient2);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, message, "Kết thúc trò chơi", JOptionPane.INFORMATION_MESSAGE);
            showAskPlayAgain("Bạn có muốn chơi lại không?");          
        });
    }

    public String getGuessInput() {
        return guessInput.getText();
    }

    public void pauseTime() {
        if (matchTimer != null) {
            matchTimer.pause();
        }
    }

    public int getRemainingTime() {
        return matchTimer != null ? matchTimer.getCurrentTick() : 0;
    }

    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message);
        });
    }

    public void closeRoom() {
        ClientRun.removeGameRoom(roomCode);
        this.dispose();
    }

    // ... other existing methods ...

    public void setRoomId(String roomId) {
        this.roomCode = roomId;
        SwingUtilities.invokeLater(() -> {
            roomIdLabel.setText("Số phòng: " + roomId);
        });
    }
    
    public void setStateHostRoom () {
        answer = false;
        clearguessInput();
        startButton.setVisible(true);
        waitingLabel.setVisible(false);
    }
    
    public void setStateUserInvited () {
        
        answer = false;
        clearguessInput();
        startButton.setVisible(false);
        waitingLabel.setVisible(true);
    }

    public void onPlayAgainTimeout() {
        SwingUtilities.invokeLater(() -> {
            hideAskPlayAgain();
            JOptionPane.showMessageDialog(this, "Hết thời gian chờ phản hồi chơi lại.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
    }

     private void clearguessInput() {
         guessInput.setText("");
     }
}
