package com.java.configuration;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class Ownership {

//    private final UserService userService;
//    private final FolderService folderService;
//    private final AccessService accessService;
//    private final FileService fileService;
//    private final ItemService itemService;
//
//    @Value("${role.admin}")
//    private String roleAdmin;
//    public OwnershipAspect(
//            UserService userService,
//            FolderService folderService,
//            AccessService accessService,
//            FileService fileService,
//            ItemService itemService
//    ) {
//        this.userService = userService;
//        this.folderService = folderService;
//        this.accessService = accessService;
//        this.fileService = fileService;
//        this.itemService = itemService;
//    }
//
//    @Before("@annotation(folderOwnerShip) && args(folderId,..)")
//    @Order(2)
//    public void checkFolderOwnership(FolderOwnerShip folderOwnerShip, Long folderId) throws InValidException {
//        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
//        User user = userService.findByEmail(email);
//
//        Folder folder = folderService.findById(folderId);
//        if (folder == null) {
//            throw new InValidException(
//                    "Folder does not exist"
//            );
//        }
//        AccessEnum accessType = folderOwnerShip.action();
//        if (folder.getUser().getId() != user.getId() && !user.getRole().getName().equals(roleAdmin)) {
//
//            if (accessType == AccessEnum.VIEW) {
//                if (!folder.getIsPublic()) {
//                    List<AccessItem> accessItem = accessService.findByItemAndUser(folder, user);
//                    if (accessItem == null || accessItem.isEmpty()) {
//                        throw new NotOwnerException("You cannot access this folder");
//                    }
//                }
//            } else {
//                AccessItem accessItem = accessService.findByItemAndUserAndAccessType(folder, user, accessType);
//                if (accessItem == null) {
//                    throw new NotOwnerException("You cannot access this folder");
//                }
//            }
//
//        }
//
//    }
//
//    @Before("@annotation(fileOwnerShip) && args(folderId,fileId,..)")
//    @Order(3)
//    public void checkFileOwnership(FileOwnerShip fileOwnerShip, Long folderId, Long fileId) throws InValidException {
//        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
//        User user = userService.findByEmail(email);
//
//        File file = fileService.findById(fileId);
//        if (file == null) {
//            throw new InValidException(
//                    "File does not exist"
//            );
//        }
//        AccessEnum accessType = fileOwnerShip.action();
//        if (file.getUser().getId() != user.getId() && !user.getRole().getName().equals(roleAdmin)) {
//            if (accessType == AccessEnum.VIEW) {
//                if (!file.getIsPublic()) {
//                    List<AccessItem> accessItem = accessService.findByItemAndUser(file, user);
//                    if (accessItem == null || accessItem.isEmpty()) {
//                        throw new NotOwnerException("You cannot access this file");
//                    }
//                }
//            } else {
//                AccessItem accessItem = accessService.findByItemAndUserAndAccessType(file, user, accessType);
//                if (accessItem == null) {
//                    throw new NotOwnerException("You cannot access this file");
//                }
//            }
//        }
//
//    }
//
//    @Before("@annotation(itemOwnerShip) && args(req,..)")
//    @Order(1)
//    public void checkItemOwnership(ItemOwnerShip itemOwnerShip, ReqAccessDTO req) throws InValidException {
//        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
//        User user = userService.findByEmail(email);
//
//        Item item = itemService.findById(req.getItem().getId());
//        if (item == null) {
//            throw new InValidException(
//                    "Item does not exist"
//            );
//        }
//        if (item.getUser() != null && item.getUser().getId() != user.getId()) {
//            if (!roleAdmin.equals(user.getRole().getName())) {
//                throw new NotOwnerException(
//                        "You cannot access to this item"
//                );
//            }
//        }
//
//    }

}

