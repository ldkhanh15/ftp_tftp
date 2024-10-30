package com.java.GUI.view;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
@Getter
@Setter
public class DirectionTreeView extends JFrame {

    private JTree directoryTree = new JTree();

    @PostConstruct
    void init() {
        setTitle("Directory Tree View");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new JScrollPane(directoryTree), BorderLayout.CENTER);
    }
}
