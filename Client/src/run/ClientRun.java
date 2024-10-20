package run;

import controller.SocketHandler;
import view.ConnectServer;
import view.LoginView;
import view.RegisterView;
import view.HomeView;
import view.PlaywithFriend;
import java.util.HashMap;
import view.GameRoom;

public class ClientRun {
    public enum SceneName {
        CONNECTSERVER,
        LOGIN,
        REGISTER,
        HOMEVIEW,
        PLAYWITHFRIEND
    }

    // scenes
    public static ConnectServer connectServer;
    public static LoginView loginView;
    public static RegisterView registerView;
    public static HomeView homeView;
    public static PlaywithFriend playWithFriendView;

    // controller 
    public static SocketHandler socketHandler;

    private static HashMap<String, GameRoom> activeGameRooms = new HashMap<>();

    public ClientRun() {
        socketHandler = new SocketHandler();
        initScene();
        openScene(SceneName.CONNECTSERVER);
    }

    public void initScene() {
        connectServer = new ConnectServer();
        loginView = new LoginView();
        registerView = new RegisterView();
        homeView = new HomeView();
        playWithFriendView = new PlaywithFriend();
    }

    public static void openScene(SceneName sceneName) {
        closeAllScene();
        if (null != sceneName) {
            System.out.println("Opening scene: " + sceneName);
            String username = socketHandler.getLoginUser();
            int score = socketHandler.getScore();
            int wins = socketHandler.getWins();
            switch (sceneName) {
                case CONNECTSERVER:
                    connectServer.setVisible(true);
                    break;
                case LOGIN:
                    loginView.setVisible(true);
                    break;
                case REGISTER:
                    registerView.setVisible(true);
                    break;
                case HOMEVIEW:
                    homeView.setVisible(true);
                    homeView.updateUserInfo(username, score, wins);
                    break;
                case PLAYWITHFRIEND:
                    playWithFriendView.setVisible(true);
                    playWithFriendView.setUsername(username);
                    break;
                default:
                    break;
            }
        }
    }

    public static void closeScene(SceneName sceneName) {
        if (null != sceneName) {
            System.out.println("Closing scene: " + sceneName); // In ra màn hình cảnh đang đóng
            switch (sceneName) {
                case CONNECTSERVER:
                    connectServer.dispose();
                    break;
                case LOGIN:
                    loginView.dispose();
                    break;
                case REGISTER:
                    registerView.dispose();
                    break;
                case HOMEVIEW:
                    homeView.dispose();
                    break;
                case PLAYWITHFRIEND:
                    playWithFriendView.dispose();
                    break;   
                default:
                    break;
            }
        }
    }

    public static void closeAllScene() {
        System.out.println("Closing all scenes"); // In ra màn hình khi đóng tất cả các cảnh
        connectServer.dispose();
        loginView.dispose();
        registerView.dispose();
        homeView.dispose();
        playWithFriendView.dispose();
    }

    public static void addGameRoom(String roomCode, GameRoom gameRoom) {
        activeGameRooms.put(roomCode, gameRoom);
        openGameRoom(roomCode);
    }

    public static GameRoom findGameRoom(String roomCode) {
        return activeGameRooms.get(roomCode);
    }

    public static void removeGameRoom(String roomCode) {
        activeGameRooms.remove(roomCode);
    }

    public static void openGameRoom(String roomCode) {
        GameRoom gameRoom = activeGameRooms.get(roomCode);
        if (gameRoom != null) {
            System.out.println("Opening GameRoom: " + roomCode);
            gameRoom.setVisible(true);
        } else {
            System.err.println("GameRoom not found for roomCode: " + roomCode);
        }
    }

    public static void main(String[] args) {
        new ClientRun();
    }
}
