package engine.scene.model;

import org.joml.Matrix4f;

import java.util.*;

public class Node {

    private final Node parent;
    private final List<Node> children;

    private final String name;
    private Matrix4f transformation;

    public Node(String name, Node parent, Matrix4f transformation) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.name = name;
        this.transformation = transformation;
    }

    public void addChild(Node node) { this.children.add(node); }

    public Node getParent() { return parent; }
    public List<Node> getChildren() { return children; }
    public String getName() { return name; }
    public Matrix4f getTransformation() { return transformation; }

}
