package controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import model.UserInfo;
import run.ClientRun;
import view.GameRoom;
import view.HomeView;
import view.ChatRoom;
//import model.UserInfo;

public class SocketHandler {
    private Socket s;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String loginUser = null; // Lưu tài khoản đăng nhập hiện tại
    private double score = 0.0;
    private int wins = 0; // Thêm biến để lưu số lần thắng
    private String roomIdPresent = null; // Thêm biến này
    
    private boolean isLoggingOut = false;

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
                    case "START_GAME":
                        onReceiveStartGame(received);
                        break;
                    case "NEXT_ROUND":
                        onReceiveNextRound(received);
                        break;
                    case "ROUND_RESULT":
                        onReceiveRoundResult(received);
                        break;
                    case "GAME_OVER":
                        onReceiveGameOver(received);
                        break;
                    case "ASK_PLAY_AGAIN":
                        onReceiveAskPlayAgain(received);
                        break;
                    case "GET_LIST_ONLINE":
                        onReceiveGetListOnline(received);
                        break;
                    case "UPDATE_ONLINE_LIST":
                        onReceiveUpdateOnlineList(received);
                        break;
                    case "CHAT_INVITATION":
                        onReceiveChatInvitation(received);
                        break;
                    case "CHAT_ACCEPTED":
                        onChatAccepted(received);
                        break;
                    case "CHAT_DECLINED":
                        onChatDeclined(received);
                        break;
                    case "CHAT_MESSAGE":
                        onReceiveChatMessage(received);
                        break;
                    case "OPEN_CHAT_ROOM":
                        onReceiveOpenChatRoom(received);
                        break;
                    case "EXIT_CHAT_ROOM":
                        onReceiveExitChatRoom(received);
                        break;
                        
                    case "SEND_LEAVE_INGAME":
                        sendLeaveInGame(received);
                        break;
                    case "RECEIVE_LEAVE_INGAME":
                        receiveLeaveInGame(received);
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
        if (!isLoggingOut) {
            isLoggingOut = true;
            sendData("LOGOUT");
            this.loginUser = null;
            this.score = 0;
            this.wins = 0;
        }
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
            this.score = Double.parseDouble(splitted[3]);
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
        this.roomIdPresent = roomCode;
        SwingUtilities.invokeLater(() -> {
             ClientRun.closeScene(ClientRun.SceneName.PLAYWITHFRIEND); // Đóng cửa sổ PlayWithFriend
            GameRoom gameRoom = new GameRoom(loginUser, roomCode, true);
            ClientRun.addGameRoom(roomCode, gameRoom);
            gameRoom.setVisible(true);
            System.out.println("GameRoom created and added: " + roomCode); // Log để kiểm tra
        });
    }

    private void onRoomJoined(String received) {
        String[] parts = received.split(";");
        String roomCode = parts[1];
        this.roomIdPresent = roomCode;
        SwingUtilities.invokeLater(() -> {
             ClientRun.closeScene(ClientRun.SceneName.PLAYWITHFRIEND); // Đóng cửa sổ PlayWithFriend
            GameRoom gameRoom = new GameRoom(loginUser, roomCode, false);
            ClientRun.addGameRoom(roomCode, gameRoom);
            gameRoom.setVisible(true);
            System.out.println("GameRoom joined and added: " + roomCode); // Log để kiểm tra
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
            gameRoom.setOpponentName(opponentName, playerOrder);
            // ... other updates if needed ...
        } else {
            System.out.println("Error: GameRoom not found for room code " + roomCode);
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
            boolean isHost = playerOrder.equals("FIRST");
            GameRoom gameRoom = new GameRoom(loginUser, roomCode, isHost);
            gameRoom.setOpponentName(opponentName, playerOrder);
            ClientRun.addGameRoom(roomCode, gameRoom);
            gameRoom.setVisible(true);
        });
        this.roomIdPresent = roomCode; // Cập nhật roomIdPresent
    }

    // private void onGameStart(String received) {
    //     String[] parts = received.split(";");
    //     String roomCode = parts[1];
    //     this.roomIdPresent = roomCode; // Cập nhật roomIdPresent

    //     SwingUtilities.invokeLater(() -> {
    //         GameRoom gameRoom = ClientRun.findGameRoom(roomCode);
    //         if (gameRoom != null) {
    //             gameRoom.startGame();
    //         }
    //     });
    // }

    private void onJoinFailed(String received) {
        String errorMessage = received.split(";")[1];
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, errorMessage, "Tham gia phòng thất bi", JOptionPane.ERROR_MESSAGE);
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

    public double getScore() {
        return score;
    }

    public int getWins() {
        return wins;
    }

    private void onLogoutSuccess() {
        System.out.println("Logout successful");
        isLoggingOut = false;
        SwingUtilities.invokeLater(() -> {
            ClientRun.closeScene(ClientRun.SceneName.HOMEVIEW);
            ClientRun.openScene(ClientRun.SceneName.LOGIN);
        });
    }

    public void backToHome() {
        // Gửi thông báo đến server (nếu cần)
        sendData("BACK_TO_HOME");

        // Đóng tt cả các cửa sổ hiện tại (nếu cần)
        ClientRun.closeAllScene();

        // Mở cửa sổ Home mới
        SwingUtilities.invokeLater(() -> {
            ClientRun.openScene(ClientRun.SceneName.HOMEVIEW);
        });
    }

    private void onReceiveStartGame(String received) {
        String[] splitted = received.split(";");
        if (splitted.length >= 6 && splitted[1].equals("success")) {
            String roomId = splitted[2];
            String productName = splitted[3];
            String imagePath = splitted[4];
            int round = Integer.parseInt(splitted[5]);

            SwingUtilities.invokeLater(() -> {
                GameRoom gameRoom = ClientRun.findGameRoom(roomId);
                if (gameRoom != null) {
                    gameRoom.setRoomId(roomIdPresent);
                    gameRoom.setProductInfo(productName, imagePath);
                    gameRoom.setStartGame(30);
                    gameRoom.setRound(round); // Cập nhật s lượt chơi
                } else {
                    System.out.println("Error: GameRoom not found for room ID " + roomId);
                }
            });
        } else {
            System.out.println("Failed to start game: " + received);
        }
    }
    

    private void onReceiveNextRound(String received) {
        String[] parts = received.split(";");
        String roomCode = parts[2];
        int round = Integer.parseInt(parts[5]);
        String productName = parts[3];
        String imagePath = parts[4];
        
        SwingUtilities.invokeLater(() -> {
            GameRoom gameRoom = ClientRun.findGameRoom(roomCode);
            if (gameRoom != null) {
                gameRoom.setRoomId(roomCode);
                gameRoom.setRound(round);
                gameRoom.setProductInfo(productName, imagePath);
                gameRoom.startNextRound(30);
            }
        });
    }

    private void onReceiveRoundResult(String received) {
        String[] parts = received.split(";");
        if(parts[1].equals("success")){
        String winner = parts[2];
        double actualPrice = Double.parseDouble(parts[3]);
        double guessClient1 = Double.parseDouble(parts[4]);
        double guessClient2 = Double.parseDouble(parts[5]);
        double roundScoreClient1 = Double.parseDouble(parts[6]);
        double roundScoreClient2 = Double.parseDouble(parts[7]);
        double totalScoreClient1 = Double.parseDouble(parts[8]);
        double totalScoreClient2 = Double.parseDouble(parts[9]);
        String nameClient1 = parts[10];
        String nameClient2 = parts[11];
        int round =Integer.parseInt(parts[12]);
       
        
        SwingUtilities.invokeLater(() -> {
            GameRoom gameRoom = ClientRun.findGameRoom(roomIdPresent);
            if (gameRoom != null) {
                gameRoom.showRoundResult(round,winner, actualPrice, guessClient1, guessClient2,
                                         roundScoreClient1, roundScoreClient2,
                                         totalScoreClient1, totalScoreClient2,
                                         nameClient1, nameClient2);
            }
        });
        }
    }

    private void onReceiveGameOver(String received) {
    String[] splitted = received.split(";");
    if (splitted.length >= 8 && splitted[1].equals("success")) {
        String winner = splitted[2];
        String nameClient1 = splitted[3];
        String nameClient2 = splitted[4];
        String roomId = splitted[5];
        double scoreClient1 = Double.parseDouble(splitted[6]);
        double scoreClient2 = Double.parseDouble(splitted[7]);

        // Lưu điểm số cho client hiện tại
        if (this.loginUser.equals(nameClient1)) {
            this.score = this.score+scoreClient1;
        } else if (this.loginUser.equals(nameClient2)) {
            this.score = this.score+scoreClient2;
        }

        // Cập nhật số trận thắng nếu client là người thắng
        if (this.loginUser.equals(winner)) {
            this.wins++;
        }

        GameRoom gameRoom = ClientRun.findGameRoom(roomIdPresent);
        SwingUtilities.invokeLater(() -> {
            if (gameRoom != null) {
                gameRoom.showGameOver(winner, scoreClient1, scoreClient2, nameClient1, nameClient2);
            } else {
                System.err.println("GameRoom not found for roomId: " + roomId);
            }
        });
    } else {
        System.err.println("Invalid GAME_OVER message received: " + received);
    }
}

//private void updateHomeViewInfo() {
//    SwingUtilities.invokeLater(() -> {
//        if (ClientRun.homeView != null) {
//            ClientRun.homeView.updateUserInfo(loginUser, score, wins);
//        }
//    });
//}
    
    public void submitResult(String competitor) {
        GameRoom gameRoom = ClientRun.findGameRoom(roomIdPresent);
        String guess = gameRoom.getGuessInput();
        
        if (guess.isEmpty()) {
            gameRoom.showMessage("Vui lòng nhập giá dự đoán!");
        } else {
            gameRoom.pauseTime();
            int remainingTime = gameRoom.getRemainingTime();
            int elapsedTime = 30 - remainingTime; // Giả sử thời gian ban đầu là 30 giây

            sendData("SUBMIT_RESULT;" + loginUser + ";" + competitor + ";" + roomIdPresent + ";" + guess + ";" + elapsedTime);
            gameRoom.afterSubmit();
        }
    }

   

    public void startGame(String opponentName) {
        sendData("START_GAME;" + loginUser + ";" + opponentName + ";" + roomIdPresent);
    }

//    public void submitGuess(String guess) {
//        GameRoom gameRoom = ClientRun.findGameRoom(roomIdPresent);
//        if (gameRoom != null) {
//            int remainingTime = gameRoom.getRemainingTime();
//            int elapsedTime = 30 - remainingTime; // Assuming initial time is 30 seconds
//            sendData("SUBMIT_GUESS;" + loginUser + ";" + roomIdPresent + ";" + guess + ";" + elapsedTime);
//            gameRoom.afterSubmit();
//        }
//    }

    public void leaveGame() {
        sendData("LEAVE_GAME;" + loginUser + ";" + roomIdPresent);
        roomIdPresent = null;
    }

    public void requestNextRound() {
        sendData("REQUEST_NEXT_ROUND;" + loginUser + ";" + roomIdPresent);
    }

    public void acceptPlayAgain() {
        sendData("ASK_PLAY_AGAIN;" +"YES"+";"+ loginUser + ";" + roomIdPresent);
    }

    public void declinePlayAgain() {
        sendData("ASK_PLAY_AGAIN;" +"NO"+";"+ loginUser + ";" + roomIdPresent);
    }
    
     public void getListOnline() {
        sendData("GET_LIST_ONLINE");
    }

    private void onJoinRoomSuccess(String received) {
        String[] parts = received.split(";");
        String roomCode = parts[1];
        boolean isHost = Boolean.parseBoolean(parts[2]);
        ClientRun.createGameRoom(roomCode, loginUser, isHost);
    }

    private void onReceiveAskPlayAgain(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        
        SwingUtilities.invokeLater(() -> {
            GameRoom gameRoom = ClientRun.findGameRoom(roomIdPresent);
            if (gameRoom != null) {
                if (status.equals("NO")) {
                    gameRoom.closeRoom();
                    ClientRun.openScene(ClientRun.SceneName.HOMEVIEW);
                    // ((HomeView) ClientRun.homeView).enableCreateRoom();
                     ((HomeView) ClientRun.homeView).enableQuickMatch();
                    roomIdPresent = null;
                } else if (status.equals("YES")) {
                    if (loginUser.equals(splitted[2])) {
                        gameRoom.setStateHostRoom();
                    } else {
                        gameRoom.setStateUserInvited();
                    }
                }
            }
        });
    }

    private void onPlayAgainTimeout(String received) {
        GameRoom gameRoom = ClientRun.findGameRoom(roomIdPresent);
        if (gameRoom != null) {
            gameRoom.onPlayAgainTimeout();
        }
    }

    private void onReceiveGetListOnline(String received) {
        System.out.println("Received online list data: " + received);
        String[] splitted = received.split(";");
        if (splitted[1].equals("success")) {
            List<UserInfo> onlineUsers = new ArrayList<>();
            if (splitted.length > 2 && !splitted[2].equals("EMPTY")) {
                for (int i = 2; i < splitted.length; i++) {
                    String[] userInfo = splitted[i].split(",");
                    if (userInfo.length == 3) {
                        String username = userInfo[0];
                        double score = Double.parseDouble(userInfo[1]);
                        int wins = Integer.parseInt(userInfo[2]);
                        if (!username.equals(loginUser)) {  // Không thêm người dùng hiện tại vào danh sách
                            onlineUsers.add(new UserInfo(username, score, wins));
                        }
                    }
                }
            }
            System.out.println("Parsed online users: " + onlineUsers.size());
            SwingUtilities.invokeLater(() -> {
                if (ClientRun.homeView != null) {
                    ((HomeView) ClientRun.homeView).updateOnlineUsersList(onlineUsers);
                }
            });
        } else {
            JOptionPane.showMessageDialog(ClientRun.homeView, "Có lỗi khi lấy danh sách người dùng online", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReceiveUpdateOnlineList(String received) {
        String[] parts = received.split(";");
        List<UserInfo> onlineUsers = new ArrayList<>();
        if (parts.length > 1 && !parts[1].equals("EMPTY")) {
            for (int i = 1; i < parts.length; i++) {
                    String[] userInfo = parts[i].split(",");
                    if (userInfo.length == 3) {
                        String username = userInfo[0];
                        double score = Double.parseDouble(userInfo[1]);
                        int wins = Integer.parseInt(userInfo[2]);
                        if (!username.equals(loginUser)) {  // Không thêm người dùng hiện tại vào danh sách
                            onlineUsers.add(new UserInfo(username, score, wins));
                        }
                    }
                }
        }
        
        SwingUtilities.invokeLater(() -> {
            if (ClientRun.homeView != null) {
                ((HomeView) ClientRun.homeView).updateOnlineUsersList(onlineUsers);
            }
        });
    }

    public void inviteChat(String invitedUser) {
        sendData("INVITE_CHAT;" + loginUser + ";" + invitedUser);
    }

    public void acceptChatInvitation(String inviter) {
        sendData("ACCEPT_CHAT;" + loginUser + ";" + inviter);
    }

    public void declineChatInvitation(String inviter) {
        sendData("DECLINE_CHAT;" + loginUser + ";" + inviter);
    }

    private void onReceiveChatInvitation(String received) {
        String[] parts = received.split(";");
        String inviter = parts[1];
        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showConfirmDialog(
                null,
                inviter + " đã mời bạn trò chuyện. Bạn có chấp nhận không?",
                "Lời mời trò chuyện",
                JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.YES_OPTION) {
                acceptChatInvitation(inviter);
            } else {
                declineChatInvitation(inviter);
            }
        });
    }

    private void onChatAccepted(String received) {
        String[] parts = received.split(";");
        String acceptedUser = parts[1];
        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showConfirmDialog(
                null,
                acceptedUser + " đã đồng ý trò chuyện cùng bạn. Bạn có muốn mở phòng chat không?",
                "Xác nhận mở phòng chat",
                JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.YES_OPTION) {
                openChatRoom(acceptedUser);
            }
        });
    }

    private void onChatDeclined(String received) {
        String[] parts = received.split(";");
        String declinedUser = parts[1];
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, declinedUser + " đã từ chối lời mời trò chuyện của bạn.");
        });
    }

    public void openChatRoom(String otherUser) {
        sendData("OPEN_CHAT_ROOM;" + loginUser + ";" + otherUser);
        SwingUtilities.invokeLater(() -> {
            ChatRoom chatRoom = ChatRoom.getInstance(loginUser, otherUser);
            chatRoom.setOtherUserLeft(false);
            chatRoom.loadSavedMessages();
            chatRoom.setVisible(true);
        });
    }

    private void onReceiveOpenChatRoom(String received) {
        String[] parts = received.split(";");
        String otherUser = parts[1];
        SwingUtilities.invokeLater(() -> {
            ChatRoom chatRoom = ChatRoom.getInstance(loginUser, otherUser);
            chatRoom.setOtherUserJoined(true);
            chatRoom.setVisible(true);
        });
    }

    public void sendChatMessage(String recipient, String message) {
        sendData("CHAT_MESSAGE;" + loginUser + ";" + recipient + ";" + message);
    }

    private void onReceiveChatMessage(String received) {
        String[] parts = received.split(";");
        String sender = parts[1];
        String message = parts[3];
        SwingUtilities.invokeLater(() -> {
            ChatRoom chatRoom = ChatRoom.getInstance(loginUser, sender);
            if (chatRoom != null) {
                if (chatRoom.isVisible()) {
                    chatRoom.addMessage(sender, message, false);
                } else {
                    // Nếu cửa sổ chat không hiển thị, chỉ lưu tin nhắn mà không hiển thị
                    chatRoom.saveMessage(sender, message);
                }
            }
        });
    }

    public void exitChatRoom(String otherUser) {
        sendData("EXIT_CHAT_ROOM;" + loginUser + ";" + otherUser);
    }

    private void onReceiveExitChatRoom(String received) {
        String[] parts = received.split(";");
        String exitedUser = parts[1];
        SwingUtilities.invokeLater(() -> {
            ChatRoom chatRoom = ChatRoom.getInstance(loginUser, exitedUser);
            if (chatRoom != null) {
                chatRoom.setOtherUserLeft(true);
                chatRoom.displayExitMessage(exitedUser);
            }
        });
    }
    
    
    

    // Phương thức gửi thông điệp rời phòng
    public void sendLeaveInGame(String opponentName) {
        if (s != null && !s.isClosed()) { // Kiểm tra kết nối
            sendData("SEND_LEAVE_INGAME;" + loginUser + ";" + opponentName + ";" + roomIdPresent);
        }
        //sendData("LEAVE_GAME;" + loginUser + ";" + opponentName + ";" + roomIdPresent);
        GameRoom gameRoom = ClientRun.findGameRoom(roomIdPresent);
        if (gameRoom != null) {
            gameRoom.closeRoom();
            ClientRun.openScene(ClientRun.SceneName.HOMEVIEW);
            ((HomeView) ClientRun.homeView).enableQuickMatch();
        }
        roomIdPresent = null;
    }

    /*// Phương thức xử lý khi nhận được thông báo rời phòng
    private void receiveLeaveInGame(String received) {
        String[] parts = received.split(";");
        if (parts.length >= 2) {
            String leavingUser = parts[1];
            SwingUtilities.invokeLater(() -> {
                GameRoom gameRoom = ClientRun.findGameRoom(roomIdPresent);
                if (gameRoom != null) {
                    // Hiển thị thông báo
                    JOptionPane.showMessageDialog(gameRoom, 
                        leavingUser + " đã rời khỏi trận đấu!", 
                        "Thông báo", 
                        JOptionPane.INFORMATION_MESSAGE);

                    // Đóng phòng game và trở về màn hình chính
                    gameRoom.closeRoom();
                    ClientRun.openScene(ClientRun.SceneName.HOMEVIEW);
                    ((HomeView) ClientRun.homeView).enableQuickMatch();
                    roomIdPresent = null;
                }
            });
        }
    }
     */
    private void receiveLeaveInGame(String received) {
        String[] parts = received.split(";");
        //double scoreClient1 = 0.0;
        //double scoreClient2 = 0.0;
        if (parts.length >= 6) { // Kiểm tra đủ thông tin: người rời đi, auto flag, tên người chơi 1, điểm 1, tên người chơi 2, điểm 2
            String leavingUser = parts[1];
            // boolean isAutoLeave = "AUTO".equals(parts[2]);
            String client1Name = parts[3];
            double scoreClient1 = Double.parseDouble(parts[4].replace(",", "."));
            String client2Name = parts[5];
            double scoreClient2 = Double.parseDouble(parts[6].replace(",", "."));

            SwingUtilities.invokeLater(() -> {
                // Tạo thông báo chi tiết
                StringBuilder message = new StringBuilder();
                message.append(leavingUser).append(" đã rời khỏi trận đấu!\n\n");
                message.append("Kết quả cuối cùng:\n");
                message.append(client1Name).append(": ").append(String.format("%.1f", scoreClient1)).append(" điểm\n");
                message.append(client2Name).append(": ").append(String.format("%.1f", scoreClient2)).append(" điểm\n");
                //message.append(client1Name).append(": ").append(String.format("%.1f", scoreClient1)).append(" điểm\n");
                //message.append(client2Name).append(": ").append(String.format("%.1f", scoreClient2)).append(" điểm\n");
                
                // Xác định người thắng (người không rời đi)
                String winner = leavingUser.equals(client1Name) ? client2Name : client1Name;
                message.append("\nNgười thắng: ").append(winner);
                System.out.println(message);
                // Hiển thị thông báo
                JOptionPane.showMessageDialog(null,
                        message.toString(),
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);

                // Đóng phòng game và trở về màn hình chính
                GameRoom gameRoom = ClientRun.findGameRoom(roomIdPresent);
                if (gameRoom != null) {
                    gameRoom.closeRoom();
                    ClientRun.openScene(ClientRun.SceneName.HOMEVIEW);
                    ((HomeView) ClientRun.homeView).enableQuickMatch();
                    roomIdPresent = null;
                }
            });
        }
    }
}
