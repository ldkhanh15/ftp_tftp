package com.java.FTPClient;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket controlSocket;
    private PrintWriter controlOutWriter;
    private BufferedReader controlIn;
    private ServerSocket dataSocket;
    private Socket dataConnection;
    private String serverAddress;
    private int controlPort;
    private int dataPort;
    private String fileSeparator = "/";

    public Client(String serverAddress, int controlPort, int dataPort) {
        this.serverAddress = serverAddress;
        this.controlPort = controlPort;
        this.dataPort = dataPort;
    }

    public void connect() throws IOException {
        // Establish control connection
        controlSocket = new Socket(serverAddress, controlPort);
        controlOutWriter = new PrintWriter(controlSocket.getOutputStream(), true);
        controlIn = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

        // Display server greeting
        System.out.println("Server: " + controlIn.readLine());
    }

    public void sendCommand(String command) {
        controlOutWriter.println(command);
    }

    public String readServerResponse() throws IOException {
        return controlIn.readLine();
    }
    private void active() throws IOException {
        dataSocket = new ServerSocket(dataPort);
        // Send file to server
        int p1 = dataPort / 256;
        int p2 = dataPort % 256;
        sendCommand("PORT 127,0,0,1," + p1 + "," + p2);
        System.out.println("Server: " + readServerResponse());

        dataConnection = dataSocket.accept();
    }

    public void passive() throws IOException {
        sendCommand("PASV");
        String response = readServerResponse();
        String[] parts = response.split("\\(|\\)")[1].split(",");

        String ip = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
        int port = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);


        dataConnection = new Socket(ip, port);
        System.out.println("Connecting to IP: " + ip + " on port: " + port);
    }
    public void retrieveFile() throws IOException {
        // Get the user's home directory and set the download folder
        String downloadDir = System.getProperty("user.home") + "/downloads/";
        System.out.println(downloadDir);
        // Ensure the directory exists
        File directory = new File(downloadDir);
        if (!directory.exists()) {
            directory.mkdirs(); // Create directory if it doesn't exist
        }

        int type = 0;
        Scanner scanner = new Scanner(System.in);
        while (type != 1 && type != 2) {
            System.out.println("Chon type:");
            System.out.println("1. Active");
            System.out.println("2. Passive");
            type = scanner.nextInt();
            if (type == 1 || type == 2) {
                break;
            }
        }
        if (type == 1) {
            active();
        } else {
            passive();
        }
        sendCommand("RETR " + "actor_movies.txt");
        System.out.println("Server: " + readServerResponse());

        // Create the output file with the specified download directory
        try (InputStream dataIn = dataConnection.getInputStream();
             FileOutputStream fileOut = new FileOutputStream(downloadDir + "actor_movies.txt")) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = dataIn.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
            }
            System.out.println("File download complete.");
        }
        System.out.println("Server: " + readServerResponse());
        if (dataSocket != null) {
            dataSocket.close();
        }
        if (dataConnection != null) {
            dataConnection.close();
        }
    }
    public void storeFile() throws IOException {
        int type = 0;
        Scanner scanner = new Scanner(System.in);
        while (type != 1 && type != 2) {
            System.out.println("Chon type:");
            System.out.println("1. Active");
            System.out.println("2. Passive");
            type = scanner.nextInt();
            if (type == 1 || type == 2) {
                break;
            }
        }
        if (type == 1) {
            active();
        } else {
            passive();
        }
        sendCommand("STOR " + "21d936f5-5150-4ff2-8cc3-00b9044f4439.jpg");
        String response = readServerResponse();
        System.out.println("Server: " + response);
        if (!response.equalsIgnoreCase("File already exists")) {

            try (FileInputStream fileIn = new FileInputStream("D:\\Dowloads\\21d936f5-5150-4ff2-8cc3-00b9044f4439.jpg");
                 OutputStream dataOut = dataConnection.getOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileIn.read(buffer)) != -1) {
                    dataOut.write(buffer, 0, bytesRead);
                }
                System.out.println("File upload complete.");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
            System.out.println("Server: " + readServerResponse());
        }
        if (dataSocket != null) {
            dataSocket.close();
        }
        if (dataConnection != null) {
            dataConnection.close();
        }

    }

    public void closeConnections() throws IOException {
        if (dataSocket != null) dataSocket.close();
        if (controlSocket != null) {
            sendCommand("QUIT");
            controlSocket.close();
        }
        System.out.println("Disconnected from server.");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Client ftpClient = new Client("localhost", 21, 5000);

        try {
            ftpClient.connect();

            while (true) {
                System.out.println("Choose an option: \n1. Retrieve file\n2. Store file\n3. Quit");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        ftpClient.retrieveFile();
                        break;

                    case 2:
                        ftpClient.storeFile();
                        break;

                    case 3:
                        ftpClient.closeConnections();
                        scanner.close();
                        return;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
