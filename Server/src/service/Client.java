package service;

import UserController.UserController;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import run.ServerRun;

public class Client implements Runnable {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private static final ThreadLocal<String> threadLocalUser = new ThreadLocal<>();

    private String loginUser;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
        this.loginUser = null; // Khởi tạo là null
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
                }
                
                // Thêm log sau mỗi lần xử lý yêu cầu
                System.out.println("After processing " + type + ", loginUser is: " + getLoginUser());
            } catch (IOException ex) {
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
            
            // Giả sử login trả về dạng "success;1000;5" với 1000 là điểm số, 5 là số trận thắng
            String[] resultData = result.split(";");
            int score = Integer.parseInt(resultData[2]);
            int wins = Integer.parseInt(resultData[3]);
            
            // Thêm client vào danh sách quản lý client của server
            ServerRun.clientManager.addClient(username, this);
            
            // Trả về thông tin đăng nhập theo định dạng: LOGIN;success;username;score;wins
            sendData("LOGIN;success;" + username + ";" + score + ";" + wins);
            logAction("LOGIN_SUCCESS");
            
            // Thêm log để kiểm tra
            System.out.println("Login successful. loginUser set to: " + getLoginUser());
        } else {
            // Nếu đăng nhập thất bại, trả về thông báo lỗi
            sendData("LOGIN;" + result); // result sẽ là "failed;Lý do thất bại"
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
        String result = ServerRun.clientManager.getListUseOnline();
        sendData("GET_LIST_ONLINE;" + result);
    }

    private void handleCreateRoom() {
        String currentUser = getLoginUser();
        System.out.println("Checking loginUser in handleCreateRoom: " + currentUser);
        if (currentUser == null) {
            System.out.println("Cannot create room. User not logged in.");
            sendData("CREATE_ROOM_FAILED;User not logged in");
            return;
        }
        checkLoginUser("handleCreateRoom");
        String roomCode = generateUniqueRoomCode();
        GameRoom newRoom = new GameRoom(roomCode);
        newRoom.addPlayer(this);
        ServerRun.gameRooms.put(roomCode, newRoom);
        sendData("ROOM_CREATED;" + roomCode);
        logAction("ROOM_CREATED");
    }

    // ... existing code ...

    private void handleJoinRoom(String received) {
        String roomCode = received.split(";")[1];
        GameRoom room = ServerRun.gameRooms.get(roomCode);
        
        if (room == null) {
            sendData("JOIN_FAILED;Room not found");
            logAction("JOIN_ROOM_FAILED");
            return;
        }
        
        if (!room.isFull()) {
            boolean joined = room.addPlayer(this);
            if (joined) {
                sendData("ROOM_JOINED;" + roomCode);
                logAction("ROOM_JOINED");
                
                if (room.getPlayers().size() == 2) {
                    room.notifyPlayersAboutOpponents();
                }
            } else {
                sendData("JOIN_FAILED;Unable to join the room");
                logAction("JOIN_ROOM_FAILED");
            }
        } else {
            sendData("JOIN_FAILED;Room is full");
            logAction("JOIN_ROOM_FAILED");
        }
    }

// ... existing code ...

    private String generateUniqueRoomCode() {
        String roomCode;
        do {
            roomCode = String.format("%04d", new Random().nextInt(10000));
        } while (ServerRun.gameRooms.containsKey(roomCode));
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
            if (ServerRun.quickMatchQueue.size() == 1) {
                // Người chơi đầu tiên, đợi đối thủ
                sendData("WAITING_FOR_OPPONENT");
            } else if (ServerRun.quickMatchQueue.size() == 2) {
                // Đã đủ 2 người chơi, tạo phòng
                checkForMatch();
            }
        }
    }

    private void checkForMatch() {
        synchronized (ServerRun.quickMatchQueue) {
            if (ServerRun.quickMatchQueue.size() == 2) {
                Client player1 = ServerRun.quickMatchQueue.poll();
                Client player2 = ServerRun.quickMatchQueue.poll();
                if (player1 != null && player2 != null) {
                    String roomCode = generateUniqueRoomCode();
                    GameRoom newRoom = new GameRoom(roomCode);
                    newRoom.addPlayer(player1);
                    newRoom.addPlayer(player2);
                    ServerRun.gameRooms.put(roomCode, newRoom);
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
    }
}
