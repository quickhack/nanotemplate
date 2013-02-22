package com.oldratlee.templet.internal.node;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public abstract class ContainerNode implements Node {
    Node subNode;

    public ContainerNode(Node subNode) {
        this.subNode = subNode;
    }
}