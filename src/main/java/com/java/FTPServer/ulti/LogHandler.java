package com.java.FTPServer.ulti;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class LogHandler {
    public static void write(String folderPath, String fileName, String content) {
        write(folderPath, fileName, content, null);
    }

    public static void write(String folderPath, String fileName, String content, Throwable throwable) {
        try {
            File folder = new File(folderPath);

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, fileName);

            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            LocalDateTime now = LocalDateTime.now();
            String formattedTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Ghi dấu phân cách và thời gian
            bufferedWriter.write("-----------------------");
            bufferedWriter.newLine();
            bufferedWriter.write("Thời gian: " + formattedTime);
            bufferedWriter.newLine();

            // Ghi nội dung log
            if(content!=null){
                bufferedWriter.write(content);
            }
            bufferedWriter.newLine();

            // Ghi thông tin ngoại lệ nếu được cung cấp
            if (throwable != null) {
                bufferedWriter.write("Vị trí lỗi:");
                bufferedWriter.newLine();
                for (StackTraceElement element : throwable.getStackTrace()) {
                    bufferedWriter.write("    at " + element.toString());
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.close();
            System.out.println("Ghi log thành công: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Có lỗi khi ghi log: " + e.getMessage());
        }
    }
    public static String read(String folderPath, String fileName) {
        StringBuilder content = new StringBuilder();
        File file = new File(folderPath, fileName);

        if (!file.exists()) {
            System.err.println("File không tồn tại: " + file.getAbsolutePath());
            return "Chua co log";
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Có lỗi khi đọc file: " + e.getMessage());
        }

        return content.toString();
    }
}
