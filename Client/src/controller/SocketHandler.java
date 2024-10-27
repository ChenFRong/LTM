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
import view.HomeView;

public class SocketHandler {
    private Socket s;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String loginUser = null; // Lưu tài khoản đăng nhập hiện tại
    private double score = 0.0;
    private int wins = 0; // Thêm biến để lưu số lần thắng
    private String roomIdPresent = null; // Thêm biến này
    

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
                    case "GET_ALL_USERS":
                        onReceiveGetAllUsers(received);
                        break;
                    case "GET_ALL_PRODUCTS":
                        onReceiveGetAllProducts(received);
                        break;
                    case "INSERT_PRODUCT":
                        onProductAdded(received);
                        break;
                    case "UPDATE_PRODUCT":
                        onProductUpdated(received);
                        break;
                    case "DELETE_PRODUCT":
                        onProductDeleted(received);
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
    
    public void getAllUsers() {
        sendData("GET_ALL_USERS");
    }
    
    public void getAllProducts() {
        sendData("GET_ALL_PRODUCTS");
    }
    
     public void addProduct(String name, String description, double price, String image_path) {
        String data = "INSERT_PRODUCT" + ";" + name + ";" + price + ";" + description + ";" + image_path;
        sendData(data);
    }

    public void updateProduct(int id, String name,  String description, double price, String image_path) {
        String data = "UPDATE_PRODUCT" + ";" + id + ";" + name + ";" + description + ";" + price + ";" + image_path;
        sendData(data);
    }

    public void deleteProduct(int id) {
        String data = "DELETE_PRODUCT" + ";" + id;
        sendData(data);
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
            this.score = Double.parseDouble(splitted[3]);
            this.wins = Integer.parseInt(splitted[4]);
            ClientRun.closeScene(ClientRun.SceneName.LOGIN);
            ClientRun.openScene(ClientRun.SceneName.HOMEVIEW);
        } else {
            String failedMsg = splitted[2];
            JOptionPane.showMessageDialog(ClientRun.loginView, failedMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void onReceiveGetAllUsers(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        System.out.println("test" + splitted[splitted.length - 1]);
    if ("success".equals(status)) {
        // Làm sạch bảng trước khi thêm dữ liệu mới
        ClientRun.rankView.clearRankTable();
        int rank = 1;
        for (int i = 2; i < splitted.length; i += 7) {
            // Kiểm tra xem có đủ phần tử không
            if (i + 7 <= splitted.length) {
                System.out.println("username" + splitted[i]); 
                String username = splitted[i];
                String score = splitted[i + 1];
                String wins = splitted[i + 2];
                String draws = splitted[i + 3];
                String losses = splitted[i + 4];
                String avgCompetitor = splitted[i + 5];
                String avgTime = splitted[i + 6];

               
                ClientRun.rankView.setListUsers(rank, username, score, wins, draws, losses, avgCompetitor, avgTime);
                rank++;
            } else {
                System.out.println("Không đủ dữ liệu cho chỉ số: " + i);
            }
        }

        // Hiển thị RankView
        ClientRun.rankView.setVisible(true);
//        ClientRun.openScene(ClientRun.SceneName.RANKVIEW);
        } else {
            System.out.println("Lỗi: " + splitted[2]); // Nếu không thành công, in ra thông báo lỗi
        }
    }
    
    public void onReceiveGetAllProducts(String received) {
    String[] splitted = received.split(";");
    String status = splitted[1];
        System.out.println("test" + received);
    if ("success".equals(status)) {
        // Clear existing products in the table
        ClientRun.productView.clearProductTable();
        
        for (int i = 2; i < splitted.length; i += 5) {
            if (i + 5 <= splitted.length) {
                String id = splitted[i];
                String name = splitted[i + 1];
                String price = splitted[i + 3];
                String description = splitted[i + 2];
                String imagePath = splitted[i + 4];

                // Call method to add product to the ProductManagementView
                ClientRun.productView.setListProducts(id, name, price, description, imagePath);
            }
        }
                ClientRun.productView.setVisible(true);

    } else {
        System.out.println("Error: " + splitted[2]); // Handle error
    }
}

    private void onProductAdded(String received) {
        // Xử lý thông báo sản phẩm đã được thêm
        String[] splitted = received.split(";");
        String status = splitted[1];

        if (status.equals("failed")) {
            // hiển thị lỗi
            String failedMsg = splitted[2];
            JOptionPane.showMessageDialog(ClientRun.productView, failedMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);

        } else if (status.equals("success")) {
            JOptionPane.showMessageDialog(ClientRun.productView, "Sản phẩm đã được thêm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onProductUpdated(String received) {
        // Xử lý thông báo sản phẩm đã được cập nhật
        String[] splitted = received.split(";");
        String status = splitted[1];

        if (status.equals("failed")) {
            // hiển thị lỗi
            String failedMsg = splitted[2];
            JOptionPane.showMessageDialog(ClientRun.productView, failedMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);

        } else if (status.equals("success")) {
            JOptionPane.showMessageDialog(ClientRun.productView, "Sản phẩm đã được cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onProductDeleted(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];

        if (status.equals("failed")) {
            // hiển thị lỗi
            String failedMsg = splitted[2];
            JOptionPane.showMessageDialog(ClientRun.productView, failedMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);

        } else if (status.equals("success")) {
            JOptionPane.showMessageDialog(ClientRun.productView, "Sản phẩm đã được xóa thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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

    public double getScore() {
        return score;
    }

    public int getWins() {
        return wins;
    }

    private void onLogoutSuccess() {
        System.out.println("Logout successful");
        // C�� thể thêm xử lý bổ sung nếu cần
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
                    gameRoom.setRound(round); // Cập nhật số lượt chơi
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
    public void sendPlayAgainRequest(String status) {
        try {
            String message = "ASK_PLAY_AGAIN;" + status + ";" + loginUser;
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi gửi yêu cầu chơi lại: " + e.getMessage());
        }
    }


    private void onPlayAgainTimeout(String received) {
        GameRoom gameRoom = ClientRun.findGameRoom(roomIdPresent);
        if (gameRoom != null) {
            gameRoom.onPlayAgainTimeout();
        }
    }
}
