package old.FTPServer.handle;

import java.io.PrintWriter;

public interface CommandHandler {
    void handlePasv(PrintWriter controlOutWriter);
    void handlePort(String args, PrintWriter controlOutWriter);
    void handleType(String mode, PrintWriter controlOutWriter);
    void handleRetr(String file, PrintWriter controlOutWriter);
    void handleStor(String file, PrintWriter controlOutWriter);
    void closeDataConnection();
}
