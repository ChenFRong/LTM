// File: UserController.java

package UserController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Database.Config; // Import thông tin cấu hình từ file Config

public class UserController {

    private final String CHECK_USER = "SELECT * FROM users WHERE username = ? limit 1";
    private final String INSERT_USER = "INSERT INTO users (username, password, score, win, draw, lose, avgCompetitor, avgTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
        // Cập nhật câu truy vấn để lấy thêm cột score và wins
        String query = "SELECT score, win FROM users WHERE username = ? AND password = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            // Lấy giá trị score và wins từ kết quả truy vấn
            int score = rs.getInt("score");
            int wins = rs.getInt("win");
            
            // Trả về chuỗi bao gồm thông tin đăng nhập, điểm số và số trận thắng
            return "success;" + username + ";" + score + ";" + wins;
        } else {
            return "failed;Invalid username or password";
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return "failed;Lỗi SQL: " + e.getMessage();
    } catch (Exception e) {
        e.printStackTrace();
        return "failed;Lỗi không xác định: " + e.getMessage();
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
}
