package com.java.FTPServer.enums;

public enum Command {
    USER,   // Đăng nhập với tên người dùng
    PASS,   // Đăng nhập với mật khẩu
    LIST,   // Liệt kê thư mục
    RETR,   // Tải file từ server
    STOR,   // Tải file lên server
    QUIT,   // Thoát khỏi kết nối
    SYST,   // Lấy thông tin hệ thống
    TYPE,   // Đặt chế độ truyền
    NOOP,   // Ping để giữ kết nối
    AUTH,   // Xác thực bảo mật
    PASV,   // Chuyển sang chế độ passive
    PORT,   // Chuyển sang chế độ active
    MKD,    // Tạo thư mục mới
    RMD,    // Xóa thư mục
    DELE,   // Xóa file
    RNFR,   // Đổi tên file - phần đầu
    RNTO,   // Đổi tên file - phần đích
    ABOR,   // Hủy lệnh hiện tại
    PWD,    // Hiển thị thư mục hiện tại
    CWD,    // Thay đổi thư mục làm việc hiện tại
    CDUP,  // Thay đổi lên thư mục cha
    LOGIN;

    public static Command fromString(String command) {
        try {
            return Command.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
