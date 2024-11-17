package com.java.FTPServer.handle.impl;

import com.java.FTPServer.anotation.FolderOwnerShip;
import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.handle.DirectoryHandle;
import com.java.FTPServer.system.ConstFTP;
import com.java.FTPServer.system.UserSession;
import com.java.FTPServer.system.UserSessionContext;
import com.java.enums.AccessType;
import com.java.model.Folder;
import com.java.model.User;
import com.java.service.FolderService;
import com.java.service.UserService;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;

@Component
public class DirectoryHandleImpl implements DirectoryHandle {
    private final FolderService folderService;
    private final UserService userService;
    public DirectoryHandleImpl(FolderService folderService, UserService userService) {
        this.folderService = folderService;
        this.userService = userService;
    }
    private void saveDirectory(Folder folder){
        folderService.save(folder);
    }
    private void deleteDirectory(Folder folder){
        folderService.deleteById(folder.getItemId());
    }
    @Override
    public void createDirectory( PrintWriter out,String directoryName, String currDirectory) {
        try {
            File directory = new File(currDirectory + "/" + directoryName);
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    Optional<Folder> parentFolder = folderService.findFolderIdByPath(currDirectory);
                    User user=userService.findByUsername(UserSessionContext.getUserSession().getUsername() !=null ?
                            UserSessionContext.getUserSession().getUsername() : null);
                    if(parentFolder.isPresent()){
                        Folder folder=new Folder();
                        folder.setParentFolder(parentFolder.get());
                        folder.setIsPublic(true);
                        folder.setFolderName(directoryName);
                        folder.setOwner(user);
                        saveDirectory(folder);
                    }
                    out.println(ResponseCode.OPERATION_OK.getResponse("Directory created: " + directoryName));
                } else {
                    out.println(ResponseCode.FILE_CONFLICT.getResponse("Create directory operation failed"));
                }
            } else {
                out.println(ResponseCode.FILE_CONFLICT.getResponse("Create directory operation failed"));
            }
        } catch (Exception e) {
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Create directory operation failed"));
        }
    }

    @Override
    public void removeDirectory(PrintWriter out,String directoryName, String currDirectory) {
        try {
            File directory = new File(currDirectory + "/" + directoryName);
            if (directory.exists() && directory.isDirectory()) {
                if (Objects.requireNonNull(directory.list()).length == 0) {
                    Optional<Folder> parentFolder = folderService.findFolderIdByPath(currDirectory);
                    if(parentFolder.isPresent()){
                        Optional<Folder> folder=folderService.findFolderByFolderNameAndParentFolder(directoryName,
                                parentFolder.get());
                        if(folder.isPresent()){
                            deleteDirectory(folder.get());
                        }
                    }
                    directory.delete();
                    out.println(ResponseCode.OPERATION_OK.getResponse("Directory removed: " + directoryName));
                } else {
                    out.println(ResponseCode.FILE_CONFLICT.getResponse("Remove directory operation failed"));
                }
            } else {
                out.println(ResponseCode.FILE_CONFLICT.getResponse("Remove directory operation failed"));
            }
        } catch (Exception e) {
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Remove directory operation failed"));
        }
    }

    @Override
    public void changeWorkingDirectory(PrintWriter out, String directoryName, UserSession userSession) {
        try {
            String currentDirectory = userSession.getCurrDirectory();
            String newWorkingDirectory;

            if (directoryName.equals("/")) {
                // Trường hợp: Quay về thư mục root
                newWorkingDirectory = userSession.getRootDirectory();
            } else if (directoryName.equals("..")) {
                // Trường hợp: Quay về thư mục cha
                File currentDirFile = new File(currentDirectory);
                newWorkingDirectory = currentDirFile.getParent();
                if (newWorkingDirectory == null || !newWorkingDirectory.startsWith(userSession.getRootDirectory())) {
                    out.println(ResponseCode.FILE_CONFLICT.getResponse("Cannot navigate above root directory"));
                    return;
                }
            } else if (directoryName.equals(".")) {
                // Trường hợp: Giữ nguyên thư mục hiện tại
                newWorkingDirectory = currentDirectory;
            } else if (directoryName.startsWith("\\")) {
                // Trường hợp: Đường dẫn tuyệt đối
                newWorkingDirectory = directoryName;
            } else {
                // Trường hợp: Thêm thư mục con vào đường dẫn hiện tại
                newWorkingDirectory = currentDirectory + "\\" + directoryName;
            }

            // Kiểm tra xem thư mục mới có tồn tại không và có phải thư mục không
            File newDirectory = new File(newWorkingDirectory);
            if (newDirectory.exists() && newDirectory.isDirectory()) {
                userSession.setCurrDirectory(newDirectory.getCanonicalPath()); // Đảm bảo đường dẫn chuẩn hóa
                out.println(ResponseCode.OPERATION_OK.getResponse("Changed working directory to: " + newDirectory.getCanonicalPath()));
            } else {
                out.println(ResponseCode.FILE_CONFLICT.getResponse("Change directory operation failed: Directory does not exist or is invalid"));
            }
        } catch (Exception e) {
            out.println(ResponseCode.FILE_CONFLICT.getResponse("Change directory operation failed due to an error: " + e.getMessage()));
        }
    }


    @Override
    public void printWorkingDirectory(PrintWriter out, String currentDirectory) {
        File currentDirectoryViewForUser = new File(currentDirectory + "\\");
        String currentDirectoryViewForUserStr = currentDirectoryViewForUser.toString()
                .substring(ConstFTP.ROOT_DIR_FOR_USER.length());
        String response = currentDirectoryViewForUserStr + "\\" + " is a current directory";
        out.println(ResponseCode.OPERATION_OK.getResponse(response));
    }
}
