package com.gongbj.editor;

import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;

public class NodeEditor extends UIComponentGroup {

    private int backgroundColor = 0xff2b2b2b;
    private int gridColor = 0xff333333;

    private float scrollX = 0;
    private float scrollY = 0;
    private float scale = 1;

    private int gridGap = 20;

    public ArrayList<Node> nodes = new ArrayList<>();
    Node.NodeStyle nodeStyle = new Node.NodeStyle();

    public NodeEditor(Context mainContext, int sizew, int sizeh) {
        super(mainContext, sizew, sizeh, 0, 0, true);

        Node node = new Node(NodeEditor.this, nodeStyle, 10, 10);
        node.title = "Hello World";
        node.addConnection("item1thisisItem11", 0xff638C31, Node.ConnectionType.Input);
        node.addConnection("item2", 0xffEB7A54, Node.ConnectionType.Input);
        node.addConnection("item1", 0xff3B7D60, Node.ConnectionType.Output);
        node.addConnection("item2", 0xff476137, Node.ConnectionType.Output);

        nodes.add(node);
        addUIComponent(node);

        Node node2 = new Node(NodeEditor.this, nodeStyle, 10, 16);
        node2.title = "Hello World";
        node2.addConnection("item1thisisItem11", 0xff638C31, Node.ConnectionType.Input);
        node2.addConnection("item2", 0xffEB7A54, Node.ConnectionType.Input);
        node2.addConnection("item1", 0xff3B7D60, Node.ConnectionType.Output);
        node2.addConnection("item2", 0xff476137, Node.ConnectionType.Output);

        nodes.add(node2);
        addUIComponent(node2);

    }


    public float getScale() {
        return scale;
    }

    public int[] transformPosition(int x, int y) {
        return new int[]{
                (int) ((x + scrollX) * scale),
                (int) ((y + scrollY) * scale)
        };
    }


    @Override
    protected void draw(PGraphics graphics) {
        pGraphics.background(backgroundColor);
        drawGrid(graphics);

        drawNodeConnections(graphics);

        for (int i = uiComponents.size() - 1; i >= 0; i--) {
            UIComponent component = uiComponents.get(i);
            int newPos[] = transformPosition(component.getPosx(), component.getPosy());

            graphics.pushMatrix();
            graphics.translate(newPos[0], newPos[1]);
            graphics.scale(scale);
            component.draw(graphics);
            graphics.popMatrix();
        }

        pGraphics.fill(0xfffffff, 150);
        pGraphics.noStroke();
        pGraphics.circle(getMouseX(), getMouseY(), 40);
    }

    private void drawGrid(PGraphics graphics) {
        float newGap = gridGap * scale;

        if (newGap < 15)
            return;

        float newScrollX = scrollX * scale % newGap;
        float newScrollY = scrollY * scale % newGap;

        int countX = (int) ((getSizeW() - newScrollX) / newGap) + 1;
        int countY = (int) ((getSizeH() - newScrollY) / newGap) + 1;

        graphics.fill(gridColor);
        graphics.noStroke();
        for (int x = 0; x < countX; x++) {
            for (int y = 0; y < countY; y++) {
                graphics.circle(x * newGap + newScrollX, y * newGap + newScrollY, 5);
            }
        }
    }

    private void drawNodeConnections(PGraphics graphics) {
        for (Node node : nodes) {
            for (int i = 0; i < node.countOutputConnection(); i++) {
                Node.NodeConnection nodeConnection = node.getOutputConnection(i);
                if (nodeConnection.connectedNode != null) {
                    int p1[] = new int[]{node.getConnectionCirclePositionX(nodeConnection), node.getConnectionCirclePositionY(nodeConnection)};
                    int p2[] = new int[]{nodeConnection.connectedNode.parentNodeItem.parentNode.getConnectionCirclePositionX(nodeConnection.connectedNode), nodeConnection.connectedNode.parentNodeItem.parentNode.getConnectionCirclePositionY(nodeConnection.connectedNode)};
                    float bezierL = Math.max(0, 100 - (p2[0] - p1[0])) * 1;

                    p1 = transformPosition(p1[0], p1[1]);
                    p2 = transformPosition(p2[0], p2[1]);

                    if (nodeConnection.parentNodeItem.parentNode.mouseEntered || nodeConnection.connectedNode.parentNodeItem.parentNode.mouseEntered) {
                        graphics.stroke(nodeStyle.connectionSemiHighlightColor);
                    } else {
                        graphics.stroke(0xffffffff);
                    }
                    if (nodeConnection.parentNodeItem.parentNode.focused || nodeConnection.connectedNode.parentNodeItem.parentNode.focused) {
                        graphics.stroke(nodeStyle.connectionHighlightColor);
                    }

                    graphics.strokeWeight(nodeStyle.connectionCircleRadius * scale * 0.6f);
                    graphics.noFill();
                    graphics.bezier(p1[0], p1[1], p1[0] + bezierL * scale, p1[1],
                            p2[0] - bezierL * scale, p2[1], p2[0], p2[1]);
                }
            }
        }
    }

    @Override
    public boolean mouseWheel(MouseEvent event) {
        if (!mouseEntered) {
            return false;
        }
        if(super.mouseWheel(event)){
            return true;
        }

        float e = event.getCount();
        float preScale = scale;
        scale *= 1 - e * 0.05;
        scrollX = getSizeW() / 2f / scale -
                (getSizeW() / 2f / preScale - scrollX);
        scrollY = getSizeH() / 2f / scale -
                (getSizeH() / 2f / preScale - scrollY);

        return true;
    }

    private boolean backgroundPressed = false;
    private int prex, prey;
    private float preScrollX, preScrollY;

    @Override
    public boolean mousePressed() {
        if (super.mousePressed()) {
            return true;
        }
        backgroundPressed = true;
        prex = getMouseX();
        prey = getMouseY();
        preScrollX = scrollX;
        preScrollY = scrollY;
        return true;
    }

    @Override
    public void mouseMoved() {
        super.mouseMoved();
    }

    @Override
    public void mouseDragged() {
        super.mouseDragged();
        if (backgroundPressed) {
            float dx = getMouseX() - prex;
            float dy = getMouseY() - prey;

            scrollX = preScrollX + dx / scale;
            scrollY = preScrollY + dy / scale;
        }
    }

    @Override
    public void mouseReleased() {
        super.mouseReleased();
        backgroundPressed = false;
    }


    @Override
    public Context getInnerContext() {
        if (innerContext == null) {
            innerContext = new EditorContext();
            innerContext.mainApp = context.mainApp;
            innerContext.posx = getPosx();
            innerContext.posy = getPosy();
            innerContext.parent = context;
            return innerContext;
        }
        return innerContext;
    }

    private class EditorContext extends Context {
        @Override
        public int getMouseX() {
            if (parent == null) {
                return (int) (mainApp.mouseX / scale - scrollX);
            }
            return (int) ((parent.getMouseX() - posx) / scale - scrollX);
        }

        @Override
        public int getMouseY() {
            if (parent == null) {
                return (int) (mainApp.mouseY / scale - scrollY);
            }
            return (int) ((parent.getMouseY() - posy) / scale - scrollY);
        }
    }
}
