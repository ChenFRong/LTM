package service;

import controller.UserController;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import run.ServerRun;
import model.ProductModel;

public class Client implements Runnable {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String loginUser;
    private Room joinedRoom;
    private Client cCompetitor;
    private double score;
    private int wins;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
        this.loginUser = null;
    }

    @Override
    public void run() {
        String received;
        while (!ServerRun.isShutDown) {
            try {
                received = dis.readUTF();
                System.out.println("Received: " + received);
                String type = received.split(";")[0];

                switch (type) {
                    case "LOGIN":
                        onReceiveLogin(received);
                        logAction("LOGIN");
                        break;
                    case "REGISTER":
                        handleRegister(received);
                        logAction("REGISTER");
                        break;
                    case "GET_LIST_ONLINE":
                        handleGetListOnline();
                        logAction("GET_LIST_ONLINE");
                        break;
                    case "CREATE_ROOM":
                        handleCreateRoom();
                        logAction("CREATE_ROOM");
                        break;
                    case "JOIN_ROOM":
                        handleJoinRoom(received);
                        logAction("JOIN_ROOM");
                        break;
                    case "QUICK_MATCH":
                        handleQuickMatch();
                        logAction("QUICK_MATCH");
                        break;
                    case "LOGOUT":
                        handleLogout();
                        break;
                    case "START_GAME":
                        onReceiveStartGame(received);
                        break;
                    case "NEXT_ROUND":
                        onReceiveNextRound(received);
                        break;
                    case "SUBMIT_RESULT":
                        onReceiveSubmitResult(received);
                        break;
                    case "ASK_PLAY_AGAIN":
                        onReceiveAskPlayAgain(received);
                        break;
                }
                
                System.out.println("After processing " + type + ", loginUser is: " + getLoginUser());
            } catch (IOException | SQLException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
        closeResources();
    }

    private void closeResources() {
        try {
            this.socket.close();
            this.dis.close();
            this.dos.close();
            System.out.println("- Client disconnected: " + socket);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void onReceiveLogin(String received) {
        String[] splitted = received.split(";");
        String username = splitted[1];
        String password = splitted[2];

        // Lấy kết quả đăng nhập từ UserController
        String result = new UserController().login(username, password);

        if (result.split(";")[0].equals("success")) {
            setLoginUser(username);
            System.out.println("Login successful. loginUser set to: " + getLoginUser());
            
            // Giả sử login trả về dạng "success;1000.0;5" với 1000.0 là điểm số, 5 là số trận thắng
            String[] resultData = result.split(";");
            float score = Float.parseFloat(resultData[2]);
            int wins = Integer.parseInt(resultData[3]);
            
            // Thêm client vào danh sách quản lý client của server
            ServerRun.clientManager.addClient(username, this);
            
            // Trả về thông tin đăng nhập theo định dạng: LOGIN;success;username;score;wins
            sendData("LOGIN;success;" + username + ";" + score + ";" + wins);
            logAction("LOGIN_SUCCESS");
            
            // Thêm log để kiểm tra
            System.out.println("Login successful. loginUser set to: " + getLoginUser());
            //broadcastOnlineList();
            ServerRun.clientManager.broadcastOnlineList();
        } else {
            // Nếu đăng nhập thất bại, trả về thông báo lỗi
            sendData("LOGIN;" + result); // result sẽ là "failed;L do thất bại"
            logAction("LOGIN_FAILED");
        }
        
        // Cập nhật danh sách người chơi online sau khi đăng nhập
        handleGetListOnline();
        
        // Thêm log để kiểm tra sau khi xử lý đăng nhập
        System.out.println("After login processing, loginUser is: " + getLoginUser());
    }

    private void handleRegister(String received) {
        String[] splitted = received.split(";");
        String username = splitted[1];
        String password = splitted[2];
        

        String result = new UserController().register(username, password); // Gọi phương thức register với mật khẩu xác nhận
        sendData("REGISTER;" + result);
        logAction("REGISTER_" + (result.startsWith("success") ? "SUCCESS" : "FAILED"));
    }

    private void handleGetListOnline() {
        String result = ServerRun.clientManager.getListUserOnline();
        sendData("GET_LIST_ONLINE;success;" + result);
        System.out.println("Sent online list: " + result); // Thêm log này
    }

    private void handleCreateRoom() {
        String currentUser = getLoginUser();
        if (currentUser == null) {
            sendData("CREATE_ROOM_FAILED;User not logged in");
            return;
        }
        String roomCode = generateUniqueRoomCode();
        Room newRoom = ServerRun.roomManager.createRoom(roomCode);
        if (newRoom.addClient(this)) {
            joinedRoom = newRoom;
            sendData("ROOM_CREATED;" + roomCode);
            logAction("ROOM_CREATED");
        } else {
            sendData("CREATE_ROOM_FAILED;Unable to join the room");
        }
    }

    private void handleJoinRoom(String received) {
        String roomCode = received.split(";")[1];
        Room room = ServerRun.roomManager.getRoom(roomCode);
        
        if (room == null) {
            sendData("JOIN_FAILED;Room not found");
            logAction("JOIN_ROOM_FAILED");
            return;
        }
        
        if (ServerRun.roomManager.addPlayerToRoom(roomCode, this)) {
            joinedRoom = room;
            sendData("ROOM_JOINED;" + roomCode);
            logAction("ROOM_JOINED");
            
            if (room.getSizeClient() == 2) {
                ServerRun.roomManager.notifyPlayersAboutOpponents(roomCode);
            }
        } else {
            sendData("JOIN_FAILED;Room is full or unable to join");
            logAction("JOIN_ROOM_FAILED");
        }
    }

    private String generateUniqueRoomCode() {
        String roomCode;
        do {
            roomCode = String.format("%04d", new Random().nextInt(10000));
        } while (ServerRun.roomManager.getRoom(roomCode) != null);
        return roomCode;
    }

    String sendData(String data) {
        try {
            if (dos != null) {
                dos.writeUTF(data);
                dos.flush();
                return "success";
            } else {
                return "failed;DataOutputStream is null";
            }
        } catch (IOException e) {
            System.err.println("Send data failed: " + e.getMessage());
            return "failed;" + e.getMessage();
        }
    }  

    public synchronized String getLoginUser() {
        return this.loginUser;
    }

    private void logAction(String action) {
        System.out.println("Action performed: " + action + " | User: " + getLoginUser());
    }

    private void checkLoginUser(String methodName) {
        System.out.println("Checking loginUser in " + methodName + ": " + getLoginUser());
    }

    public synchronized void setLoginUser(String username) {
        this.loginUser = username;
        System.out.println("Set loginUser for client to: " + username);
    }

    public void printClientInfo() {
        System.out.println("Client Info - LoginUser: " + this.getLoginUser());
    }

    private void handleQuickMatch() {
        synchronized (ServerRun.quickMatchQueue) {
            ServerRun.quickMatchQueue.offer(this);
            System.out.println("User " + getLoginUser() + " added to quick match queue");
            sendData("WAITING_FOR_OPPONENT");
            checkForMatch();
        }
    }

    private void checkForMatch() {
        synchronized (ServerRun.quickMatchQueue) {
            if (ServerRun.quickMatchQueue.size() >= 2) {
                Client player1 = ServerRun.quickMatchQueue.poll();
                Client player2 = ServerRun.quickMatchQueue.poll();
                if (player1 != null && player2 != null) {
                    String roomCode = generateUniqueRoomCode();
                    Room newRoom = ServerRun.roomManager.createRoom(roomCode);
                    newRoom.addClient(player1);
                    newRoom.addClient(player2);
                    player1.setJoinedRoom(newRoom);
                    player2.setJoinedRoom(newRoom);
                    player1.sendData("QUICK_MATCH_FOUND;" + roomCode + ";" + player2.getLoginUser() + ";FIRST");
                    player2.sendData("QUICK_MATCH_FOUND;" + roomCode + ";" + player1.getLoginUser() + ";SECOND");
                    System.out.println("Quick match found: " + player1.getLoginUser() + " (Player 1) vs " + player2.getLoginUser() + " (Player 2) in room " + roomCode);
                }
            }
        }
    }

    private void handleLogout() {
        ServerRun.clientManager.removeClient(this.getLoginUser());
        this.loginUser = null;
        sendData("LOGOUT_SUCCESS");
        System.out.println("User logged out: " + this.getLoginUser());
        ServerRun.clientManager.broadcastOnlineList();
    }

    private void onReceiveStartGame(String received) {
        String[] splitted = received.split(";");
        System.out.println(received);
        String user1 = splitted[1];
        String user2 = splitted[2];
        String roomId = splitted[3];
        
        Room room = ServerRun.roomManager.find(roomId);
        if (room != null) {
            ProductModel product = ServerRun.productManager.getRandomProduct();
            String productInfo = "START_GAME;success;" 
                    + roomId + ";" 
                    + product.getName() + ";" 
                    + product.getImagePath();
            room.broadcast(productInfo);
            room.startGame();
        }
    }

    private void onReceiveNextRound(String received) {
        String[] splitted = received.split(";");
        String roomId = splitted[2];
        String productName = splitted[3];
        String imagePath = splitted[4];
        int round = Integer.parseInt(splitted[5]);

        Room room = ServerRun.roomManager.find(roomId);
        if (room != null) {
            String msg = "NEXT_ROUND;success;" + roomId + ";" + productName + ";" + imagePath + ";" + round;
            room.broadcast(msg);
        }
    }

    private void onReceiveSubmitResult(String received) throws SQLException {
        String[] splitted = received.split(";");
        String user = splitted[1];
        String competitor = splitted[2];
        String roomId = splitted[3];
        double priceGuess = Double.parseDouble(splitted[4]);
        
        Room room = ServerRun.roomManager.find(roomId);
        if (room != null) {
            if (user.equals(room.getClient1().getLoginUser())) {
                room.setPriceGuessClient1(priceGuess);
            } else if (user.equals(room.getClient2().getLoginUser())) {
                room.setPriceGuessClient2(priceGuess);
            }

            if (room.getPriceGuessClient1() > 0 && room.getPriceGuessClient2() > 0) {
                room.handleRoundEnd();
            }
        }
    }

    private void onReceiveAskPlayAgain(String received) throws SQLException {
        String[] splitted = received.split(";");
        String reply = splitted[1];
        String user1 = splitted[2];
        
        if (joinedRoom == null) {
            System.out.println("Error: Received ASK_PLAY_AGAIN but joinedRoom is null");
            return;
        }
        
        if (user1.equals(joinedRoom.getClient1().getLoginUser())) {
            joinedRoom.setPlayAgainC1(reply);
        } else if (user1.equals(joinedRoom.getClient2().getLoginUser())) {
            joinedRoom.setPlayAgainC2(reply);
        }
        
        String result = joinedRoom.handlePlayAgain();
        if (result == null || "WAITING".equals(result)) {
            // Chờ đợi phản hồi từ người chơi khác
            return;
        }
        
        if ("YES".equals(result)) {
            joinedRoom.broadcast("ASK_PLAY_AGAIN;YES;" + joinedRoom.getClient1().loginUser + ";" + joinedRoom.getClient2().loginUser);
            Room room = ServerRun.roomManager.find(joinedRoom.getId());
            if (room != null) {
             joinedRoom.resetRoom();
             joinedRoom.startGame();
            }
        } else {
            joinedRoom.broadcast("ASK_PLAY_AGAIN;NO;");
            Room room = ServerRun.roomManager.find(joinedRoom.getId());
            if (room != null) {
                joinedRoom.deleteRoom();
                ServerRun.roomManager.remove(room);
            }
            this.joinedRoom = null;
            this.cCompetitor = null;
        }
    }

    // Thêm các getter và setter cần thiết
    public Room getJoinedRoom() {
        return joinedRoom;
    }

    public void setJoinedRoom(Room joinedRoom) {
        this.joinedRoom = joinedRoom;
    }

    public Client getcCompetitor() {
        return cCompetitor;
    }

    public void setcCompetitor(Client cCompetitor) {
        this.cCompetitor = cCompetitor;
    }

    // private void broadcastOnlineList() {
    //     String onlineList = ServerRun.clientManager.getListUserOnline();
    //     for (Client client : ServerRun.clientManager.getAllClients()) {
    //         client.sendData("UPDATE_ONLINE_LIST;" + onlineList);
    //     }
    // }

    // Thêm getters cho score và wins
    public double getScore() {
        return score;
    }

    public int getWins() {
        return wins;
    }
}
