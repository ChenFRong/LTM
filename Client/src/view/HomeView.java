/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import run.ClientRun;
import model.UserInfo;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;

public class HomeView extends JFrame {
    private JButton createRoomButton;
    private JButton quickJoinButton;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JLabel winsLabel;
    private JLabel playerNameLabel; // Thêm biến để quản lý label tên người chơi
    private String playerName; // Tên người chơi
    private int playerWins; // Số trận thắng
    private int playerScore; // Xếp hạng
    private JButton logoutButton;
    private JButton onlineListButton;
    private JDialog onlineListDialog;
    private JList<String> onlineUsersList;
    private DefaultListModel<String> onlineUsersModel;
    private JButton chatButton;
    private JButton rankButton;
    private JButton productManagementButton; // Declare the new button
    
     private String userRole; // Member variable to hold user role
    // Other variables and constructor

    public void setUserRole(String role) {
        this.userRole = role;
        updateUIForRole(); // Update the UI based on the role
    }

    private void updateUIForRole() {
        if ("admin".equals(this.userRole)) {
            productManagementButton.setVisible(true);
        } else {
            productManagementButton.setVisible(false);
        }
    }


    // Danh sách lưu trữ các phòng chơi
    private List<GameRoom> gameRooms = new ArrayList<>();

    public HomeView() {
        setTitle("Hãy chọn giá đúng");
        setLayout(new BorderLayout());

        // Tạo panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Tạo panel cho nút logout
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Nút danh sách online ở góc trái
        onlineListButton = new JButton("Danh sách online");
        onlineListButton.addActionListener(e -> showOnlineListDialog());
        topPanel.add(onlineListButton, BorderLayout.WEST);

        // Nút đăng xuất ở góc phải
        logoutButton = new JButton("Đăng xuất");
        logoutButton.addActionListener(e -> handleLogout());
        topPanel.add(logoutButton, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Tạo panel chứa các thông tin và nút
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tên người chơi sau khi đăng nhập
        playerNameLabel = new JLabel("Người chơi: ");
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(playerNameLabel, gbc);

        // Thông tin người chơi: Điểm số và tổng số trận thắng
        scoreLabel = new JLabel("Tổng điểm: ");
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(scoreLabel, gbc);

        winsLabel = new JLabel("Tổng số trận thắng: ");
        gbc.gridx = 1;
        contentPanel.add(winsLabel, gbc);
        
        

        // Tạo nút "Chơi với bạn"
        createRoomButton = new JButton("Chơi với bạn");
        createRoomButton.setBackground(Color.BLUE);
        createRoomButton.setForeground(Color.WHITE);
        createRoomButton.setPreferredSize(new Dimension(150, 50));
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(createRoomButton, gbc);

        // Tạo nút "Ghép nhanh"
        quickJoinButton = new JButton("Ghép nhanh");
        quickJoinButton.setBackground(Color.GREEN);
        quickJoinButton.setForeground(Color.WHITE);
        quickJoinButton.setPreferredSize(new Dimension(150, 50));
        gbc.gridx = 1;
        contentPanel.add(quickJoinButton, gbc);

        statusLabel = new JLabel("", JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        contentPanel.add(statusLabel, gbc);

        // Tạo panel cho nút danh sách online
        JPanel topLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        onlineListButton = new JButton("Danh sách online");
        topLeftPanel.add(onlineListButton);
        
        rankButton = new JButton("Rank");
        topLeftPanel.add(rankButton);
        
        productManagementButton = new JButton("Quản lý sản phẩm");
        productManagementButton.setVisible(false); // Initially hidden
        topLeftPanel.add(productManagementButton);
        
        mainPanel.add(topLeftPanel, BorderLayout.WEST);
        
        rankButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call the method to get all users' ranking data from the server
                ClientRun.socketHandler.getAllUsers();

                // Open the RankView to display the ranking information
                RankView rankView = new RankView();
//                rankView.setVisible(true);
            }
        });
        
        productManagementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call the method to get all products from the server
                ClientRun.socketHandler.getAllProducts(); // Make sure you have implemented this method in your socket handler

                // Open the ProductManagementView to display the product information
                ProductView productView = new ProductView();
            }
        });

        // Thêm ActionListener cho nút danh sách online
        onlineListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOnlineListDialog();
            }
        });

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Thêm mainPanel vào JFrame
        add(mainPanel);

        // Thêm các ActionListener
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });

        createRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ClientRun.closeScene(ClientRun.SceneName.HOMEVIEW);
                ClientRun.openScene(ClientRun.SceneName.PLAYWITHFRIEND);
            }
        });
        


        quickJoinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ClientRun.socketHandler.quickMatch();
                statusLabel.setText("Đang tìm đối thủ...");
                quickJoinButton.setEnabled(false);
            }
        });

        // Thiết lập các thuộc tính cho cửa sổ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Khởi tạo onlineUsersModel ngay trong constructor
        onlineUsersModel = new DefaultListModel<>();
        onlineUsersList = new JList<>(onlineUsersModel);

        // Khởi tạo tableModel và onlineUsersTable
        tableModel = new DefaultTableModel(new Object[]{"Tên người chơi", "Điểm số", "Số trận thắng"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Làm cho bảng không thể chỉnh sửa
            }
        };
        onlineUsersTable = new JTable(tableModel);
        onlineUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    //Thêm các phương thức mới vào đây
  public void enableQuickMatch() {
      quickJoinButton.setEnabled(true);
      
  }

//    public void disableQuickMatch() {
//        quickJoinButton.setEnabled(false);
       
//    }


    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            ClientRun.socketHandler.logout();
            ClientRun.closeScene(ClientRun.SceneName.HOMEVIEW);
            ClientRun.openScene(ClientRun.SceneName.LOGIN);
        }
    }

    

    public void updateUserInfo(String username, double score, int wins) {
        setUsername(username);
        setUserScore(score);
        setUserWins(wins);
    }

    // Lớp đại diện cho một người chơi
    class Player {
        private String name;
        private float score;
        private int wins;

        public Player(String name, float score, int wins) {
            this.name = name;
            this.score = score;
            this.wins = wins;
        }
    }

    // Lớp đại diện cho một phòng chơi
    class GameRoom {
        private List<Player> players = new ArrayList<>();
        private static final int MAX_PLAYERS = 2;

        public void addPlayer(Player player) {
            if (players.size() < MAX_PLAYERS) {
                players.add(player);
            }
        }

        public boolean isFull() {
            return players.size() == MAX_PLAYERS;
        }

        public List<Player> getPlayers() {
            return players;
        }
    }

    public static void main(String[] args) {
        HomeView homeView = new HomeView();
        homeView.setVisible(true);
        //homeView.updateUserInfo(username, score, wins);
    }

    private void setUsername(String username) {
        playerNameLabel.setText("Người chơi: " + username);
    }

    private void setUserScore(double score) {
        scoreLabel.setText("Tổng điểm: " + score);
    }

    private void setUserWins(int wins) {
        winsLabel.setText("Tổng số trận thắng: " + wins);
    }

//    public void enableCreateRoom() {
//        SwingUtilities.invokeLater(() -> {
//            createRoomButton.setEnabled(true);
//        });
//    }

    // public void disableCreateRoom() {
    //     SwingUtilities.invokeLater(() -> {
    //         createRoomButton.setEnabled(false);
    //     });
    // }

    public void satusLabel() {

        statusLabel.setVisible(false);
    }

    private void showOnlineListDialog() {
        if (onlineListDialog == null) {
            createOnlineListDialog();
        }
        ClientRun.socketHandler.getListOnline();
        onlineListDialog.setVisible(true);
    }

    private void createOnlineListDialog() {
        onlineListDialog = new JDialog(this, "Danh sách người dùng online", false);
        onlineListDialog.setSize(400, 400);
        onlineListDialog.setLocationRelativeTo(this);

        JScrollPane scrollPane = new JScrollPane(onlineUsersTable);

        JButton chatButton = new JButton("Trò chuyện");
        chatButton.addActionListener(e -> {
            int selectedRow = onlineUsersTable.getSelectedRow();
            if (selectedRow != -1) {
                String selectedUser = (String) tableModel.getValueAt(selectedRow, 0);
                startChat(selectedUser);
            } else {
                JOptionPane.showMessageDialog(onlineListDialog, "Vui lòng chọn một người dùng để trò chuyện.");
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(chatButton);

        onlineListDialog.setLayout(new BorderLayout());
        onlineListDialog.add(scrollPane, BorderLayout.CENTER);
        onlineListDialog.add(buttonPanel, BorderLayout.SOUTH);
    }

    public void updateOnlineUsersList(List<UserInfo> onlineUsers) {
        SwingUtilities.invokeLater(() -> {
            Set<String> updatedUsers = new HashSet<>();
            List<Object[]> newRows = new ArrayList<>();
            
            // Tạo danh sách mới với người dùng mới ở đầu
            for (UserInfo user : onlineUsers) {
                newRows.add(new Object[]{user.getUsername(), user.getScore(), user.getWins()});
                updatedUsers.add(user.getUsername());
            }
            
            // Thêm lại những người dùng hiện tại không có trong danh sách mới
            // for (int i = 0; i < tableModel.getRowCount(); i++) {
            //     String username = (String) tableModel.getValueAt(i, 0);
            //     if (!updatedUsers.contains(username)) {
            //         newRows.add(new Object[]{
            //             username,
            //             tableModel.getValueAt(i, 1),
            //             tableModel.getValueAt(i, 2)
            //         });
            //     }
            // }
            
            // Cập nhật bảng với danh sách mới
            tableModel.setRowCount(0);
            for (Object[] row : newRows) {
                tableModel.addRow(row);
            }
            
            if (onlineListDialog != null) {
                onlineListDialog.setTitle("Danh sách người dùng online (" + tableModel.getRowCount() + ")");
                if (onlineListDialog.isVisible()) {
                    onlineListDialog.repaint();
                    onlineUsersTable.repaint(); // Thêm dòng này
                }
            }
            
            System.out.println("Updated online users list. Count: " + tableModel.getRowCount());
        });
    }

    private void startChat(String selectedUser) {
        ClientRun.socketHandler.inviteChat(selectedUser);
        JOptionPane.showMessageDialog(this, "Đã gửi lời mời trò chuyện đến " + selectedUser);
        onlineListDialog.setVisible(false); // Đóng dialog danh sách online
    }

//    public void enableCreateRoom() {
//        SwingUtilities.invokeLater(() -> {
//            createRoomButton.setEnabled(true);
//        });
//    }

    // public void disableCreateRoom() {
    //     SwingUtilities.invokeLater(() -> {
    //         createRoomButton.setEnabled(false);
    //     });
    // }

   
}


