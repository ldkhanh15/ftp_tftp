package com.java.GUI.handle;

import com.java.GUI.view.DirectionTreeView;
import com.java.GUI.custom.DirectoryTreeCustom;
import com.java.controller.ItemController;
import com.java.dto.NodeDTO;
import com.java.enums.ItemType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectionTreeHandle {

    private final DirectionTreeView view;
    private final ItemController itemController;

    @PostConstruct
    void initJTree() {
        long rootId = itemController.getRootId();
        if (rootId == -1) {
            //...
        }
        NodeDTO rootDTO = NodeDTO.builder()
                .id(rootId)
                .name("root")
                .itemType(ItemType.FOLDER)
                .build();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootDTO);

        List<NodeDTO> nodes = itemController.getChildrenByParentId(rootDTO.getId());

        nodes.forEach(node -> {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
            root.add(treeNode);
        });

        DirectoryTreeCustom model = new DirectoryTreeCustom(root);
        view.getDirectoryTree().setModel(model);
        addTreeExpansionListener();
    }


    private void addTreeExpansionListener() {
        view.getDirectoryTree().addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

                NodeDTO nodeDTO = (NodeDTO) node.getUserObject();

                if (node.getChildCount() == 0 && nodeDTO.getItemType() == ItemType.FOLDER) {
                    loadChildren(node, nodeDTO);
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {

            }
        });
    }

    private void loadChildren(DefaultMutableTreeNode parentNode, NodeDTO parentDTO) {
        long parentId = parentDTO.getId();

        List<NodeDTO> childrenList = itemController.getChildrenByParentId(parentId);

        childrenList.forEach(child -> {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
            parentNode.add(childNode);
        });

        ((DirectoryTreeCustom) view.getDirectoryTree().getModel()).reload(parentNode);
    }
}
