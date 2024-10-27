// File: UserController.java

package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Database.Config; // Import thông tin cấu hình từ file Config
import model.UserModel;
import java.util.List;
import java.util.ArrayList;


public class UserController {

    private final String CHECK_USER = "SELECT * FROM users WHERE username = ? limit 1";
    private final String INSERT_USER = "INSERT INTO users (username, password, score, win, draw, lose, avgCompetitor, avgTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private final String GET_INFO_USER = "SELECT username, score, win, draw, lose, avgCompetitor, avgTime FROM users WHERE username=?";
    private final String UPDATE_USER = "UPDATE users SET score = ?, win = ?, draw = ?, lose = ?, avgCompetitor = ?, avgTime = ? WHERE username = ?";
    private final String GET_RANKING = "SELECT * FROM users ORDER BY score DESC";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Driver not found.");
        }
        return DriverManager.getConnection(Config.DB_URL, Config.DB_USERNAME, Config.DB_PASSWORD);
    }

    // Hàm đăng nhập (giữ nguyên)
   public String login(String username, String password) {
    try (Connection con = getConnection()) {
        String query = "SELECT username, password, score, win, role FROM users WHERE username = ? AND password = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            float score = rs.getFloat("score");
            int wins = rs.getInt("win");
            String role = rs.getString("role");  // Get the role field
            return String.format("success;%s;%f;%d;%s", username, score, wins, role);
        } else {
            return "failed;Invalid username or password";
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return "failed;SQL error: " + e.getMessage();
    }
}

    // Hàm đăng ký với password và confirmPassword
    public String register(String username, String password) {
       
        try (Connection con = getConnection()) {
            // Kiểm tra xem username đã tồn tại hay chưa
            PreparedStatement checkUserStmt = con.prepareStatement(CHECK_USER);
            checkUserStmt.setString(1, username);
            ResultSet rs = checkUserStmt.executeQuery();

            if (rs.next()) {
                return "failed;Tên đăng nhập đã tồn tại!";
            } else {
                // Thêm người dùng mới với các giá trị mặc định cho các cột còn lại
                PreparedStatement insertUserStmt = con.prepareStatement(INSERT_USER);
                insertUserStmt.setString(1, username);
                insertUserStmt.setString(2, password); // Lưu mật khẩu
                insertUserStmt.setInt(3, 0); // Điểm ban đầu
                insertUserStmt.setInt(4, 0); // Số trận thắng
                insertUserStmt.setInt(5, 0); // Số trận hòa
                insertUserStmt.setInt(6, 0); // Số trận thua
                insertUserStmt.setFloat(7, 0.0f); // Điểm trung bình đối thủ
                insertUserStmt.setFloat(8, 0.0f); // Thời gian trung bình

                int rowsInserted = insertUserStmt.executeUpdate();
                
                if (rowsInserted > 0) {
                    return "success;Đăng ký thành công!";
                } else {
                    return "failed;Không thể đăng ký người dùng!";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "failed;Lỗi SQL: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "failed;Lỗi không xác định: " + e.getMessage();
        }
    }

    public String getInfoUser(String username) {
        try (Connection con = getConnection()) {
            PreparedStatement p = con.prepareStatement(GET_INFO_USER);
            p.setString(1, username);
            
            ResultSet r = p.executeQuery();
            if (r.next()) {
                return String.format("success;%s;%f;%d;%d;%d;%f;%f",
                    r.getString("username"),
                    r.getFloat("score"),
                    r.getInt("win"),
                    r.getInt("draw"),
                    r.getInt("lose"),
                    r.getFloat("avgCompetitor"),
                    r.getFloat("avgTime"));
            } else {
                return "failed;User not found";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "failed;SQL error: " + e.getMessage();
        }
    }

    public boolean updateUser(UserModel user) {
        try (Connection con = getConnection();
             PreparedStatement p = con.prepareStatement(UPDATE_USER)) {
            
            p.setFloat(1, user.getScore());
            p.setInt(2, user.getWin());
            p.setInt(3, user.getDraw());
            p.setInt(4, user.getLose());
            p.setFloat(5, user.getAvgCompetitor());
            p.setFloat(6, user.getAvgTime());
            p.setString(7, user.getUserName());

            int rowsUpdated = p.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public UserModel getUser(String username) {
        try (Connection con = getConnection()) {
            PreparedStatement p = con.prepareStatement(GET_INFO_USER);
            p.setString(1, username);
            
            ResultSet r = p.executeQuery();
            if (r.next()) {
                UserModel user = new UserModel();
                user.setUserName(r.getString("username"));
                user.setScore(r.getFloat("score"));
                user.setWin(r.getInt("win"));
                user.setDraw(r.getInt("draw"));
                user.setLose(r.getInt("lose"));
                user.setAvgCompetitor(r.getFloat("avgCompetitor"));
                user.setAvgTime(r.getFloat("avgTime"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<UserModel> getAllUsers() {
        List<UserModel> users = new ArrayList<>();

        try (Connection con = getConnection()){
            PreparedStatement p = con.prepareStatement(GET_RANKING);

            ResultSet r = p.executeQuery();
            while (r.next()) {
                UserModel user = new UserModel();
                user.setUserName(r.getString("username"));
                user.setScore(r.getFloat("score"));
                user.setWin(r.getInt("win"));
                user.setDraw(r.getInt("draw"));
                user.setLose(r.getInt("lose"));
                user.setAvgCompetitor(r.getFloat("avgCompetitor"));
                user.setAvgTime(r.getFloat("avgTime"));

                // Thêm user vào danh sách
                users.add(user);
            }
            r.close();
            p.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}
