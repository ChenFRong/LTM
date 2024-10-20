package run;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import service.Client;
import service.ClientManager;
import service.GameRoom;
import view.ServerView;

public class ServerRun {

    public static volatile ClientManager clientManager; // Quản lý các client
    public static boolean isShutDown = false; // Biến kiểm tra trạng thái server
    public static ServerSocket ss; // Socket server
    public static ConcurrentHashMap<String, GameRoom> gameRooms = new ConcurrentHashMap<>();
    public static ConcurrentLinkedQueue<Client> quickMatchQueue = new ConcurrentLinkedQueue<>();

    public ServerRun() {
        try {
            int port = 2200; // Cổng server
            ss = new ServerSocket(port); // Khởi tạo server socket
            System.out.println("Created Server at port " + port + ".");

            // Khởi tạo client manager
            clientManager = new ClientManager();

            // Tạo thread pool
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    10, // corePoolSize
                    100, // maximumPoolSize
                    10, // thread timeout
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(8) // queueCapacity
            );

            // Vòng lặp chính của server - lắng nghe kết nối từ client
            while (!isShutDown) {
                try {
                    // Nhận kết nối từ client
                    Socket s = ss.accept();
                    System.out.println("+ New Client connected: " + s);

                    // Tạo đối tượng Client mới cho mỗi kết nối
                    Client client = new Client(s);
                    new Thread(client).start();
                } catch (IOException ex) {
                    Logger.getLogger(ServerRun.class.getName()).log(Level.SEVERE, null, ex);
                    isShutDown = true; // Đóng server nếu có lỗi
                }
            }

            System.out.println("Shutting down executor...");
            executor.shutdownNow(); // Ngắt kết nối các luồng
        } catch (IOException ex) {
            Logger.getLogger(ServerRun.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // Đảm bảo socket server được đóng khi kết thúc
            try {
                if (ss != null && !ss.isClosed()) {
                    ss.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerRun.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        ServerView serverView = new ServerView(); // Khởi tạo giao diện server
        serverView.setVisible(true);
        serverView.setLocationRelativeTo(null);
        
        new ServerRun(); // Khởi động server
    }
}
