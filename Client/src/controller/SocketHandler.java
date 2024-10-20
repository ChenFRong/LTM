package controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import run.ClientRun;
import view.GameRoom;

public class SocketHandler {
    private Socket s;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String loginUser = null; // Lưu tài khoản đăng nhập hiện tại
    private int score = 0;
    private int wins = 0; // Thêm biến để lưu số lần thắng

    public String connect(String addr, int port) {
        try {
            InetAddress ip = InetAddress.getByName(addr);
            s = new Socket();
            s.connect(new InetSocketAddress(ip, port), 4000);
            System.out.println("Connected to " + ip + ":" + port + ", localport:" + s.getLocalPort());

            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            // Bắt đầu lắng nghe dữ liệu từ server
            new Thread(this::listen).start();

            return "success";

        } catch (IOException e) {
            return "failed;" + e.getMessage();
        }
    }

    public boolean isConnected() {
        return s != null && s.isConnected() && !s.isClosed();
    }

    private void listen() {
        boolean running = true;

        while (running) {
            try {
                String received = dis.readUTF();
                System.out.println("RECEIVED: " + received);

                String type = received.split(";")[0];

                switch (type) {
                    case "LOGIN":
                        onReceiveLogin(received);
                        break;
                    case "REGISTER":
                        onReceiveRegister(received);
                        break;
                    case "ROOM_CREATED":
                        onRoomCreated(received);
                        break;
                    case "ROOM_JOINED":
                        onRoomJoined(received);
                        break;
                    case "OPPONENT_JOINED":
                        onOpponentJoined(received);
                        break;
                    case "GAME_START":
                        onGameStart(received);
                        break;
                    case "JOIN_FAILED":
                        onJoinFailed(received);
                        break;
                    case "WAITING_FOR_OPPONENT":
                        onWaitingForOpponent();
                        break;
                    case "QUICK_MATCH_FOUND":
                        onQuickMatchFound(received);
                        break;
                    case "LOGOUT_SUCCESS":
                        onLogoutSuccess();
                        break;
                    // Bạn có thể thêm các trường hợp khác nếu cần
                }

            } catch (IOException ex) {
                Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
                running = false;
            }
        }

        // Đóng kết nối
        closeConnection();
        JOptionPane.showMessageDialog(null, "Mất kết nối tới server", "Lỗi", JOptionPane.ERROR_MESSAGE);
        ClientRun.closeAllScene();
        ClientRun.openScene(ClientRun.SceneName.CONNECTSERVER);
    }

    public void login(String username, String password) {
        String data = "LOGIN;" + username + ";" + password;
        sendData(data);
    }

    public void register(String username, String password) {
        String data = "REGISTER;" + username + ";" + password;
        sendData(data);
    }

    public void createRoom() {
        sendData("CREATE_ROOM");
    }

    public void joinRoom(String roomCode) {
        sendData("JOIN_ROOM;" + roomCode);
    }

    public void quickMatch() {
        sendData("QUICK_MATCH");
    }

    public void logout() {
        sendData("LOGOUT");
        this.loginUser = null;
        this.score = 0;
        this.wins = 0;
    }

    private void sendData(String data) {
        try {
            if (dos != null) {
                dos.writeUTF(data);
            }
        } catch (IOException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void onReceiveLogin(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];

        if (status.equals("success")) {
            this.loginUser = splitted[2];
            this.score = Integer.parseInt(splitted[3]);
            this.wins = Integer.parseInt(splitted[4]);
            ClientRun.closeScene(ClientRun.SceneName.LOGIN);
            ClientRun.openScene(ClientRun.SceneName.HOMEVIEW);
        } else {
            String failedMsg = splitted[2];
            JOptionPane.showMessageDialog(ClientRun.loginView, failedMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReceiveRegister(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];

        if (status.equals("failed")) {
            String failedMsg = splitted[2];
            JOptionPane.showMessageDialog(ClientRun.registerView, failedMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else if (status.equals("success")) {
            JOptionPane.showMessageDialog(ClientRun.registerView, "Register account successfully! Please login!");
            ClientRun.closeScene(ClientRun.SceneName.REGISTER);
            ClientRun.openScene(ClientRun.SceneName.LOGIN);
        }
    }

    private void onRoomCreated(String received) {
        String[] parts = received.split(";");
        String roomCode = parts[1];
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "Phòng đã được tạo. Mã phòng: " + roomCode);
            ClientRun.closeScene(ClientRun.SceneName.PLAYWITHFRIEND);
            GameRoom gameRoom = new GameRoom(loginUser, roomCode);
            ClientRun.addGameRoom(roomCode, gameRoom);
        });
    }

    private void onRoomJoined(String received) {
        String[] parts = received.split(";");
        String roomCode = parts[1];
        SwingUtilities.invokeLater(() -> {
            ClientRun.closeScene(ClientRun.SceneName.PLAYWITHFRIEND);
            GameRoom gameRoom = new GameRoom(loginUser, roomCode);
            ClientRun.addGameRoom(roomCode, gameRoom);
        });
    }

    // ... existing code ...

   private void onOpponentJoined(String received) {
    String[] parts = received.split(";");
    String roomCode = parts[1];
    String opponentName = parts[2];
    String playerOrder = parts[3]; // "FIRST" or "SECOND"
    System.out.println("Received opponent joined notification: " + received);
    SwingUtilities.invokeLater(() -> {
        GameRoom gameRoom = ClientRun.findGameRoom(roomCode);
        if (gameRoom != null) {
            System.out.println("Setting opponent name to: " + opponentName + ", playerOrder: " + playerOrder);
            gameRoom.setOpponentName(opponentName, playerOrder);
        } else {
            System.err.println("GameRoom not found for roomCode: " + roomCode);
        }
    });
}

    private void onWaitingForOpponent() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "Đang chờ đối thủ...", "Ghép nhanh", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void onQuickMatchFound(String received) {
        String[] parts = received.split(";");
        if (parts.length < 4) {
            System.err.println("Invalid QUICK_MATCH_FOUND data received: " + received);
            return;
        }
        String roomCode = parts[1];
        String opponentName = parts[2];
        String playerOrder = parts[3];
        
        System.out.println("Quick match found. Room code: " + roomCode + ", Opponent: " + opponentName + ", Order: " + playerOrder);
        
        SwingUtilities.invokeLater(() -> {
            ClientRun.closeScene(ClientRun.SceneName.HOMEVIEW);
            GameRoom gameRoom = new GameRoom(loginUser, roomCode);
            gameRoom.setOpponentName(opponentName, playerOrder);
            ClientRun.addGameRoom(roomCode, gameRoom);
            gameRoom.setVisible(true);
        });
    }

    private void onGameStart(String received) {
        String roomCode = received.split(";")[1];
        SwingUtilities.invokeLater(() -> {
            GameRoom gameRoom = ClientRun.findGameRoom(roomCode);
            if (gameRoom != null) {
                gameRoom.startGame();
            }
        });
    }

    private void onJoinFailed(String received) {
        String errorMessage = received.split(";")[1];
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, errorMessage, "Tham gia phòng thất bại", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void closeConnection() {
        try {
            if (s != null && !s.isClosed()) {
                s.close();
            }
            if (dis != null) {
                dis.close();
            }
            if (dos != null) {
                dos.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getLoginUser() {
        return loginUser;
    }

    public int getScore() {
        return score;
    }

    public int getWins() {
        return wins;
    }

    private void onLogoutSuccess() {
        System.out.println("Logout successful");
        // Có thể thêm xử lý bổ sung nếu cần
    }

    public void backToHome() {
        // Gửi thông báo đến server (nếu cần)
        sendData("BACK_TO_HOME");

        // Đóng tất cả các cửa sổ hiện tại (nếu cần)
        ClientRun.closeAllScene();

        // Mở cửa sổ Home mới
        SwingUtilities.invokeLater(() -> {
            ClientRun.openScene(ClientRun.SceneName.HOMEVIEW);
        });
    }
}
