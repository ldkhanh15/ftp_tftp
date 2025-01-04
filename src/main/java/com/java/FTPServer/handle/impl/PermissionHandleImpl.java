package com.java.FTPServer.handle.impl;

import com.java.FTPServer.enums.ResponseCode;
import com.java.FTPServer.handle.ConnectionHandle;
import com.java.FTPServer.handle.PermissionHandle;
import com.java.controller.AccessItemController;
import com.java.controller.ItemController;
import com.java.controller.UserController;
import com.java.enums.AccessType;
import com.java.enums.Role;
import com.java.model.AccessItem;
import com.java.model.Item;
import com.java.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionHandleImpl implements PermissionHandle {
    private final ConnectionHandleImpl connectionHandle;
    private final UserController userController;
    private final AccessItemController accessItemController;
    private final ItemController itemController;

    @Override
    public void handleGetPermission(PrintWriter out, Long itemId) {
        out.println(ResponseCode.FILE_STARTING_TRANSFER.getResponse("Starting get permission"));
        PrintWriter rout = null;
        try {
            rout = new PrintWriter(connectionHandle.getDataConnection().getOutputStream(), true);
        } catch (IOException e) {
            log.error("Could not create byte streams {}", e.getMessage());
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        List<User> users = userController.getUsers();
        Item item = itemController.getById(itemId);
        List<AccessItem> accessItems = accessItemController.getAccessItemsByItem(item);
        List<String> added = new ArrayList<>();
        for (AccessItem accessItem : accessItems) {
            added.add(accessItem.getUser().getUsername());
        }
        rout.println("USER");
        for (User user : users) {
            if (!user.getRole().equals(Role.ADMIN) && !added.contains(user.getUsername()) && !user.getUsername().equals(
                    "anonymous")) {
                rout.println(user.getId() + "/" + user.getUsername());
            }
        }
        rout.println("PERMISSION");
        for (AccessItem accessItem : accessItems) {
            rout.println(accessItem.getId() + "/" + accessItem.getUser().getUsername() + "/" + accessItem.getAccessType());
        }

        if (rout != null) {
            rout.close();
        }

        connectionHandle.closeDataConnection();
        out.println("200 Success permission");
    }

    @Override
    public void handleAddPermission(PrintWriter out, String value) {
        handleSave(value);
        out.println("200 Success add permission");
    }

    @Override
    public void handleDelPermission(PrintWriter out, String value) {

        String[] values = value.split("/");
        Long itemId = Long.parseLong(values[0]);
        String username = values[1];
        accessItemController.removeAccess(username, itemId);
        out.println("200 Success delete permission");
    }

    @Override
    public void handleChangePublic(PrintWriter out, String value) {
        PrintWriter rout = null;
        try {
            rout = new PrintWriter(connectionHandle.getDataConnection().getOutputStream(), true);
        } catch (IOException e) {
            log.error("Could not create byte streams {}", e.getMessage());
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        String[] values = value.split("/");
        Long itemId = Long.parseLong(values[0]);
        String check = values[1];
        boolean isPublic = Boolean.getBoolean(check);
        itemController.changePublic(itemId, isPublic);
        if (rout != null) {
            rout.close();
        }

        connectionHandle.closeDataConnection();
        out.println("200 Success active/deactive public item");
    }

    private void handleSave(String value) {
        String[] values = value.split("/");
        Long itemId = Long.parseLong(values[0]);
        String username = values[1];
        AccessType accessType = AccessType.valueOf(values[2].toUpperCase());
        accessItemController.addAccess(username, itemId, accessType);
    }
}
