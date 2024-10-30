package com.java.GUI.custom;

import com.java.dto.NodeDTO;
import com.java.enums.ItemType;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DirectoryTreeCustom extends DefaultTreeModel {

    public DirectoryTreeCustom(DefaultMutableTreeNode root) {
        super(root);
    }

    @Override
    public boolean isLeaf(Object node) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
        NodeDTO nodeDTO = (NodeDTO) treeNode.getUserObject();
        return nodeDTO.getItemType() != ItemType.FOLDER;
    }
}
