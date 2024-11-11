package old.FTPServer;

import old.FTPServer.enums.Command;
import old.FTPServer.enums.ResponseCode;
import old.FTPServer.handle.CommandHandlerImpl;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
public class Router {
    private final CommandHandlerImpl commandHandler;
    private PrintWriter controlOutWriter;
    public Router() {
        this.commandHandler = new CommandHandlerImpl();
    }
    public void executeCommand(String c,PrintWriter controlOutWriter,int dataPort) {
        this.controlOutWriter = controlOutWriter;
        commandHandler.setDataPort(dataPort);
        String[] commands;
        commands = c.split(" ");
        Command commandType = Command.fromString(commands[0]);
        if (commandType == null) {
            sendMsgToClient(ResponseCode.NOT_IMPLEMENTED.getResponse());
            return;
        }
        printOutput("Command: " + commands[0]);
        if(commands.length>1){
            printOutput("Args: "+commands[1]);
        }
        printOutput("=======end=======");
        switch (commandType) {
            case USER:
                controlOutWriter.println(ResponseCode.NEED_PASSWORD.getResponse());
                break;
            case PASS:
                controlOutWriter.println(ResponseCode.USER_LOGGED_IN.getResponse());
                break;
            case STOR:
                commandHandler.handleStor(commands[1],controlOutWriter);
                break;
            case TYPE:
                commandHandler.handleType(commands[1],controlOutWriter);
                break;
            case RETR:
                commandHandler.handleRetr(commands[1],controlOutWriter);
                break;
            case EPRT, PORT:
                commandHandler.handlePort(commands[1],controlOutWriter);
                break;
            case PASV:
                commandHandler.handlePasv(controlOutWriter);
                break;
            default:
                sendMsgToClient("Unknown command");
                break;
        }
    }
    private void sendMsgToClient(String msg) {
        controlOutWriter.println(msg);
    }
    private void printOutput(String msg) {
        System.out.println(msg);
    }
}