package service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import service.Client;
import controller.UserController;

public class ClientManager {
    // Danh sách người dùng trực tuyến
    private ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<>();
    private UserController userController;

    public ClientManager() {
        this.userController = new UserController();
    }

    // Thêm client vào danh sách
    public void addClient(String username, Client client) {
        clients.put(username, client);
        broadcastOnlineListUpdate();
    }

    // Xóa client khỏi danh sách
    public void removeClient(String username) {
        clients.remove(username);
        broadcastOnlineListUpdate();
    }

    // Lấy danh sách người dùng trực tuyến
    // public String getListUseOnline() {
    //     List<String> onlineUsers = new ArrayList<>(clients.keySet());
    //     return String.join(",", onlineUsers);
    // }

    // Kiểm tra xem người dùng có đang trực tuyến không
    public boolean isUserOnline(String username) {
        return clients.containsKey(username);
    }

    // Lấy client theo username
    public Client getClient(String username) {
        return clients.get(username);
    }

    public String getListUserOnline() {
        StringBuilder sb = new StringBuilder();
        for (Client client : clients.values()) {
            String username = client.getLoginUser();
            // Lấy thông tin điểm số và số trận thắng từ cơ sở dữ liệu
            double score = userController.getUserScore(username);
            int wins = userController.getUserWins(username);
            sb.append(username).append(",")
              .append(score).append(",")
              .append(wins).append(";");
        }
        String result = sb.toString();
        System.out.println("Generated online list: " + result);
        return result.isEmpty() ? "EMPTY" : result;
    }

    public void broadcastOnlineList() {
        String onlineList = getListUserOnline();
        for (Client client : clients.values()) {
            client.sendData("UPDATE_ONLINE_LIST;" + onlineList);
        }
    }

    public Client getClientByUsername(String username) {
        return clients.get(username);
    }

    public void broadcastOnlineListUpdate() {
        String onlineList = getListUserOnline();
        for (Client client : getAllClients()) {
            client.sendData("UPDATE_ONLINE_LIST;" + onlineList);
        }
    }

    private List<Client> getAllClients() {
        return new ArrayList<>(clients.values());
    }
}
