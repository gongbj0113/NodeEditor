package com.gongbj.editor;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;

public class Node extends UIComponentGroup {

    private NodeEditor parentNodeEditor;

    public String title = "title";
    public NodeStyle nodeStyle;

    private ArrayList<NodeItem> inputs = new ArrayList<>();
    private ArrayList<NodeItem> outputs = new ArrayList<>();

    public Node(NodeEditor parentNodeEditor, NodeStyle nodeStyle, int posx, int posy) {
        super(parentNodeEditor.getInnerContext(), 0, 0, posx, posy, false);
        this.parentNodeEditor = parentNodeEditor;
        this.nodeStyle = nodeStyle;
        calculateLayout();
    }

    public void calculateLayout() {
        int h = (int) (nodeStyle.titleSize + nodeStyle.titlePadding * 2 + nodeStyle.titleBottomMargin +
                (nodeStyle.itemGap + nodeStyle.itemHeight) * outputs.size() +
                nodeStyle.inputOutputGap +
                (nodeStyle.itemGap + nodeStyle.itemHeight) * inputs.size() +
                nodeStyle.itemsBottomMargin);

        float maxWidth = textWidth(title, nodeStyle.titleSize);
        for (NodeItem nc : inputs) {
            float w = textWidth(nc.nodeConnection.name, nodeStyle.itemNameSize);
            if (w > maxWidth) maxWidth = w;
        }

        maxWidth += nodeStyle.maxWidthPadding;
        maxWidth = Math.max(maxWidth, nodeStyle.minNodeWidth);
        int w = (int) maxWidth;
        resize(w, h);
        for (NodeItem ni : inputs) {
            ni.calculateLayout();
        }
        for (NodeItem ni : outputs) {
            ni.calculateLayout();
        }
        setMargin((int) nodeStyle.connectionCircleRadius);
    }

    @Override
    protected boolean shouldFocus() {
        if (isMouseInComponent(true)) return true;
        for (NodeItem nc : inputs) {
            if (nc.connectionCircle.isMouseInComponent()) return true;
        }
        for (NodeItem nc : outputs) {
            if (nc.connectionCircle.isMouseInComponent()) return true;
        }
        return false;
    }

    //Connections
    private NodeConnection createNodeConnection(String name, int color, ConnectionType connectionType) {
        NodeConnection nc = new NodeConnection();
        nc.name = name;
        nc.color = color;
        nc.type = connectionType;
        nc.parentNodeItem = null;
        return nc;
    }

    public NodeConnection addConnection(String name, int color, ConnectionType connectionType) {
        NodeConnection nc = createNodeConnection(name, color, connectionType);
        if (connectionType == ConnectionType.Input) {
            NodeItem ni = new NodeItem(Node.this, nc, inputs.size());
            nc.parentNodeItem = ni;
            inputs.add(ni);
            addUIComponent(ni);
        } else {
            NodeItem ni = new NodeItem(Node.this, nc, outputs.size());
            nc.parentNodeItem = ni;
            outputs.add(ni);
            addUIComponent(ni);
        }

        calculateLayout();
        return nc;
    }

    public void removeConnection(NodeConnection nodeConnection) {
        if (nodeConnection.type == ConnectionType.Input) {
            boolean del = false;
            for (NodeItem ni : inputs) {
                if (ni.nodeConnection == nodeConnection) {
                    del = true;
                    inputs.remove(ni);
                    ni.removeUIComponent();
                    continue;
                }
                if (del) {
                    ni.index--;
                }
            }
        } else {
            boolean del = false;
            for (NodeItem ni : outputs) {
                if (ni.nodeConnection == nodeConnection) {
                    del = true;
                    outputs.remove(ni);
                    ni.removeUIComponent();
                    continue;
                }
                if (del) {
                    ni.index--;
                }
            }
        }

        calculateLayout();
    }

    public int countInputConnection() {
        return inputs.size();
    }

    public int countOutputConnection() {
        return outputs.size();
    }

    public NodeConnection getInputConnection(int i) {
        return inputs.get(i).nodeConnection;
    }

    public NodeConnection getOutputConnection(int i) {
        return outputs.get(i).nodeConnection;
    }

    private NodeItem findNodeItem(NodeConnection nodeConnection) {
        if (nodeConnection.type == ConnectionType.Input) {
            for (NodeItem ni : inputs) {
                if (ni.nodeConnection == nodeConnection) {
                    return ni;
                }
            }
        } else {
            for (NodeItem ni : outputs) {
                if (ni.nodeConnection == nodeConnection) {
                    return ni;
                }
            }
        }

        return null;
    }

    public int getConnectionCirclePositionX(NodeConnection nodeConnection) {
        NodeItem ni = nodeConnection.parentNodeItem;
        return (int) (getPosx() + ni.connectionCircle.getPosx() + ni.connectionCircle.getSizeW() / 2);
    }

    public int getConnectionCirclePositionY(NodeConnection nodeConnection) {
        NodeItem ni = nodeConnection.parentNodeItem;
        return (int) (getPosy() + ni.connectionCircle.getPosy() + ni.connectionCircle.getSizeH() / 2);
    }

    private float textWidth(String str, int fontSize) {
        parentNodeEditor.context.mainApp.textSize(fontSize);
        return parentNodeEditor.context.mainApp.textWidth(str);
    }

    private float textWidth(char chr, int fontSize) {
        parentNodeEditor.context.mainApp.textSize(fontSize);
        return parentNodeEditor.context.mainApp.textWidth(chr);
    }

    @Override
    protected void draw(PGraphics graphics) {
        //main rect
        graphics.fill(nodeStyle.backgroundColor);
        graphics.noStroke();
        graphics.rect(0, (nodeStyle.titleSize + nodeStyle.titlePadding * 2), getSizeW(), getSizeH() - (nodeStyle.titleSize + nodeStyle.titlePadding * 2),
                0, 0, nodeStyle.radius, nodeStyle.radius);

        //title background
        graphics.fill(nodeStyle.titleBackgroundColor);
        graphics.rect(0, 0, getSizeW(), (nodeStyle.titleSize + nodeStyle.titlePadding * 2), nodeStyle.radius, nodeStyle.radius, 0, 0);

        //mouse Enter effect
        if (mouseEntered) {
            graphics.fill(0x11ffffff);
            graphics.noStroke();
            graphics.rect(0, 0, getSizeW(), getSizeH(), nodeStyle.radius);
        }

        //title
        graphics.textSize(nodeStyle.titleSize);
        graphics.fill(nodeStyle.titleColor);
        graphics.textAlign(PConstants.LEFT, PConstants.CENTER);
        graphics.text(title, nodeStyle.titleLeadingPadding, nodeStyle.titlePadding + nodeStyle.titleSize / 2f);

        //border
        graphics.noFill();
        graphics.stroke(nodeStyle.borderColor);
        graphics.strokeWeight(1 / parentNodeEditor.getScale());
        graphics.rect(0, 0, getSizeW(), getSizeH(), nodeStyle.radius);

        //draw NodeItems
        super.draw(graphics);

        if (isDragging) {
            graphics.fill(0x44ffffff);
            graphics.noStroke();
            graphics.rect(0, 0, getSizeW(), getSizeH(), nodeStyle.radius);
        }
    }

    //Mouse Events
    private boolean isDragging = false;
    int preMx, preMy;
    int prePx, prePy;

    @Override
    public boolean mousePressed() {
        parentNodeEditor.toTop(Node.this);
        if (super.mousePressed()) {
            return true;
        }
        isDragging = true;
        preMx = context.getMouseX();
        preMy = context.getMouseY();
        prePx = getPosx();
        prePy = getPosy();
        return true;
    }

    @Override
    public void mouseDragged() {
        super.mouseDragged();
        if (isDragging) {
            int dx = context.getMouseX() - preMx;
            int dy = context.getMouseY() - preMy;
            translate(prePx + dx, prePy + dy);
        }
    }

    @Override
    public void mouseReleased() {
        super.mouseReleased();
        isDragging = false;
    }

    public class NodeItem extends UIComponent {
        private NodeConnection nodeConnection;
        public ConnectionCircle connectionCircle;
        public Node parentNode;
        private int index;

        private static final NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

        public NodeItem(Node parentNode, NodeConnection nodeConnection, int index) {
            super(getInnerContext(), 0, 0, 0, 0, false);
            nf.setMaximumFractionDigits(3);
            this.parentNode = parentNode;
            this.nodeConnection = nodeConnection;
            this.index = index;

            if (nodeConnection.connectionAvailable) {
                connectionCircle = new ConnectionCircle(NodeItem.this, index);
                Node.this.addUIComponent(connectionCircle);
            } else {
                connectionCircle = null;
            }
            calculateLayout();
        }

        public ConnectionCircle getConnectionCircle() {
            return connectionCircle;
        }

        public NodeConnection getNodeConnection() {
            return nodeConnection;
        }


        private void calculateLayout() {
            resize(parentNode.getSizeW(), (int) (parentNode.nodeStyle.itemHeight));
            if (nodeConnection.type == ConnectionType.Output) {
                translate(0,
                        (int) (parentNode.nodeStyle.titlePadding * 2 + parentNode.nodeStyle.titleSize + parentNode.nodeStyle.titleBottomMargin +
                                (parentNode.nodeStyle.itemGap + parentNode.nodeStyle.itemHeight) * index + parentNode.nodeStyle.itemGap)
                );
            } else {
                translate(0,
                        (int) (parentNode.nodeStyle.titlePadding * 2 + parentNode.nodeStyle.titleSize + parentNode.nodeStyle.titleBottomMargin +
                                (parentNode.nodeStyle.itemGap + parentNode.nodeStyle.itemHeight) * parentNode.countOutputConnection() + parentNode.nodeStyle.inputOutputGap +
                                (parentNode.nodeStyle.itemGap + parentNode.nodeStyle.itemHeight) * index + parentNode.nodeStyle.itemGap)
                );
            }

            if (connectionCircle != null) connectionCircle.calculateLayout();
        }

        private void drawText(PGraphics graphics) {
            graphics.textSize(parentNode.nodeStyle.itemNameSize);
            graphics.fill(parentNode.nodeStyle.itemNameColor);
            if (nodeConnection.type == ConnectionType.Output) {
                graphics.textAlign(PConstants.RIGHT, PConstants.BOTTOM);
                graphics.text(nodeConnection.name, getSizeW() - parentNode.nodeStyle.itemNameLeadingPadding,
                        getSizeH() - (getSizeH() - parentNode.nodeStyle.itemNameSize) / 2 + getSizeH() / 6
                );
            } else {
                graphics.textAlign(PConstants.LEFT, PConstants.BOTTOM);
                graphics.text(nodeConnection.name, parentNode.nodeStyle.itemNameLeadingPadding,
                        getSizeH() - (getSizeH() - parentNode.nodeStyle.itemNameSize) / 2 + getSizeH() / 6
                );
            }
        }

        public boolean isValueSlider() {
            return nodeConnection.connectedNode == null && nodeConnection.type == ConnectionType.Input;
        }

        private void drawValueEditor(PGraphics graphics) {
            graphics.fill(parentNode.nodeStyle.valueEditorBackgroundColor);
            graphics.noStroke();
            graphics.rect(parentNode.nodeStyle.itemNameLeadingPadding / 2, 0, getSizeW() - parentNode.nodeStyle.itemNameLeadingPadding, getSizeH(), 2.5f);
            if (mouseEntered) {
                graphics.fill(0x11ffffff);
                graphics.noStroke();
                graphics.rect(parentNode.nodeStyle.itemNameLeadingPadding / 2, 0, getSizeW() - parentNode.nodeStyle.itemNameLeadingPadding, getSizeH(), 2.5f);
            }
            //draw Value
            graphics.textSize(parentNode.nodeStyle.itemNameSize);
            graphics.fill(parentNode.nodeStyle.itemNameColor);
            graphics.textAlign(PConstants.LEFT, PConstants.BOTTOM);
            float yy = getSizeH() - (getSizeH() - parentNode.nodeStyle.itemNameSize) / 2 + getSizeH() / 6;
            float xx = parentNode.nodeStyle.itemNameLeadingPadding;
            float cursorX = -1;
            float selectionStartX = -1;
            float selectionEndX = -1;

            for (int i = 0; i < editorString.size(); i++) {
                graphics.text(editorString.get(i), xx, yy);
                if (cursorPosition == i) {
                    cursorX = xx;
                }
                if (selectionStart == i) {
                    selectionStartX = xx;
                }
                if (selectionEnd == i) {
                    selectionEndX = xx;
                }
                xx += parentNode.textWidth(editorString.get(i), parentNode.nodeStyle.itemNameSize);
            }
            if (cursorX == -1) cursorX = xx;
            if (selectionStartX == -1) selectionStartX = xx;
            if (selectionEndX == -1) selectionEndX = xx;

            if (!textSelected) {
                if ((System.currentTimeMillis() / 500) % 2 == 0) {
                    graphics.stroke(parentNode.nodeStyle.itemNameColor);
                    graphics.strokeWeight(1);
                    graphics.line(cursorX, getSizeH() / 7f, cursorX, getSizeH() / 7f * 6);
                }
            } else {
                graphics.noStroke();
                graphics.fill(parentNode.nodeStyle.valueEditorSelectionColor);
                graphics.rect(Math.min(selectionStartX, selectionEndX), 0, Math.abs(selectionEndX - selectionStartX), getSizeH());
            }
        }

        private void drawValueSlider(PGraphics graphics) {
            graphics.fill(parentNode.nodeStyle.valueSliderBackgroundColor);
            graphics.noStroke();
            graphics.rect(parentNode.nodeStyle.itemNameLeadingPadding / 2, 0, getSizeW() - parentNode.nodeStyle.itemNameLeadingPadding, getSizeH(), 2.5f);
            if (mouseEntered) {
                graphics.fill(0x11ffffff);
                graphics.noStroke();
                graphics.rect(parentNode.nodeStyle.itemNameLeadingPadding / 2, 0, getSizeW() - parentNode.nodeStyle.itemNameLeadingPadding, getSizeH(), 2.5f);
            }

            graphics.fill(parentNode.nodeStyle.valueSliderColor);
            graphics.noStroke();
            graphics.rect(parentNode.nodeStyle.itemNameLeadingPadding / 2, 0, (getSizeW() - parentNode.nodeStyle.itemNameLeadingPadding) * nodeConnection.value, getSizeH(), 2.5f);

            drawText(graphics);

            //draw Value
            graphics.textSize(parentNode.nodeStyle.itemNameSize);
            graphics.fill(parentNode.nodeStyle.itemNameColor);
            graphics.textAlign(PConstants.RIGHT, PConstants.BOTTOM);
            graphics.text(nf.format(nodeConnection.value), getSizeW() - parentNode.nodeStyle.itemNameLeadingPadding,
                    getSizeH() - (getSizeH() - parentNode.nodeStyle.itemNameSize) / 2 + getSizeH() / 6
            );
        }

        @Override
        protected void draw(PGraphics graphics) {
            if (isValueEditor) {
                // Value Editor
                drawValueEditor(graphics);
            } else if (isValueSlider()) {
                // Value Slider
                drawValueSlider(graphics);
            } else {
                //Default
                if (mouseEntered) {
                    graphics.fill(0x11ffffff);
                    graphics.noStroke();
                    graphics.rect(parentNode.nodeStyle.itemNameLeadingPadding / 2, 0, getSizeW() - parentNode.nodeStyle.itemNameLeadingPadding, getSizeH(), 2.5f);
                }
                drawText(graphics);
            }
        }


        Instant preTime;
        private boolean dragging = false;
        private int preMx;
        private float preValue;

        private boolean isValueEditor = false;
        private ArrayList<Character> editorString = new ArrayList<>();
        private int cursorPosition = 0;
        private boolean textSelected = false;
        private int selectionStart = 0;
        private int selectionEnd = 0;

        private void startValueEditor() {
            String text = String.valueOf(nodeConnection.value);
            editorString.clear();
            for (int i = 0; i < text.length(); i++) {
                editorString.add(text.charAt(i));
            }
            isValueEditor = true;
            textSelected = true;
            cursorPosition = 0;
            selectionStart = 0;
            selectionEnd = editorString.size();
        }

        private void endValueEditor() {
            StringBuilder builder = new StringBuilder(editorString.size());
            for (Character ch : editorString) {
                builder.append(ch);
            }
            String str = builder.toString();
            try {
                float d = Float.parseFloat(str);
                nodeConnection.value = Math.max(0f, Math.min(1f, d));
            } catch (NumberFormatException nfe) {
                //fail.
            }
            isValueEditor = false;
            textSelected = false;
        }

        private int getCursorPosition() {
            float xx = parentNode.nodeStyle.itemNameLeadingPadding;
            float prexx = parentNode.nodeStyle.itemNameLeadingPadding;

            for (int i = 0; i < editorString.size(); i++) {
                float w = parentNode.textWidth(editorString.get(i), parentNode.nodeStyle.itemNameSize);
                if (i == 0 && getMouseX() < xx + w / 2) {
                    return i;
                } else if (prexx <= getMouseX() && getMouseX() < xx + w / 2) {
                    return i;
                }
                if (i == editorString.size() - 1) {
                    return editorString.size();
                }
                prexx = xx + w / 2;
                xx = xx + w;
            }

            return 0;
        }

        private int deleteSelectedText() {
            int m = Math.min(selectionStart, selectionEnd);
            int M = Math.max(selectionStart, selectionEnd);
            for (int i = m; i < M; i++) {
                editorString.remove(m);
            }
            return m;
        }

        @Override
        public boolean mousePressed() {
            if (isValueEditor) {
                cursorPosition = getCursorPosition();
                if (textSelected) {
                    textSelected = false;
                }
                return true;
            } else if (isValueSlider()) {
                dragging = true;
                preTime = Instant.now();
                preMx = getMouseX();
                preValue = nodeConnection.value;
                return true;
            }
            return false;


        }

        @Override
        public void mouseDragged() {
            if (isValueEditor) {
                if (textSelected == false) {
                    textSelected = true;
                    selectionStart = getCursorPosition();
                } else {
                    int s = getCursorPosition();
                    selectionEnd = s;
                }
                return;
            }

            if (dragging) {
                nodeConnection.value = preValue + (getMouseX() - preMx) / (float) getSizeW();
                nodeConnection.value = Math.max(0f, Math.min(1f, nodeConnection.value));
            }
        }

        @Override
        public boolean mouseWheel(MouseEvent event) {
            if (isValueSlider() && mouseEntered && !isValueEditor) {
                System.out.println(event.getCount());
                nodeConnection.value = Math.max(0, Math.min(1, nodeConnection.value - (event.getCount() * 0.001f)));
                return true;
            }
            return false;
        }

        @Override
        public void mouseReleased() {
            if (!isValueEditor && dragging) {
                Instant instant = Instant.now();
                float dis = Math.abs((getMouseX() - preMx) / (float) getSizeW());
                if ((dis == 0) ||
                        ((Duration.between(preTime, instant).toMillis() < 500)
                                && (Math.abs((getMouseX() - preMx) / (float) getSizeW()) < 0.001))
                ) {
                    startValueEditor();
                }
            }
            dragging = false;

        }

        @Override
        public void keyPressed() {
            if(isValueEditor) {
                if (context.mainApp.keyCode == PApplet.RIGHT) {
                    if (textSelected) {
                        textSelected = false;
                        cursorPosition = Math.max(selectionEnd, selectionStart) - 1;
                    }
                    cursorPosition++;
                    cursorPosition = Math.min(editorString.size(), Math.max(0, cursorPosition));
                    return;
                } else if (context.mainApp.keyCode == PApplet.LEFT) {
                    if (textSelected) {
                        textSelected = false;
                        cursorPosition = Math.min(selectionStart, selectionEnd) + 1;
                    }
                    cursorPosition--;
                    cursorPosition = Math.min(editorString.size(), Math.max(0, cursorPosition));
                    return;
                } else if (context.mainApp.keyCode == PApplet.BACKSPACE) {
                    if (textSelected) {
                        cursorPosition = deleteSelectedText();
                        textSelected = false;
                    } else {
                        cursorPosition--;
                        if (cursorPosition < 0) {
                            cursorPosition = 0;
                        } else {
                            editorString.remove(cursorPosition);
                        }
                    }
                } else if (context.mainApp.keyCode == PApplet.ENTER){
                    endValueEditor();
                }

                if (context.mainApp.key == '.' || (context.mainApp.key >= '0' && context.mainApp.key <= '9')) {
                    if (textSelected) {
                        cursorPosition = deleteSelectedText();
                        textSelected = false;
                    }
                    editorString.add(cursorPosition, context.mainApp.key);
                    cursorPosition++;
                }
            }
        }

        @Override
        public void focusCanceled() {
            super.focusCanceled();
            if(isValueEditor) {
                endValueEditor();
            }
        }

        private void removeUIComponent() {
            parentNode.removeUIComponent(this);
            parentNode.removeUIComponent(connectionCircle);
        }


        public class ConnectionCircle extends UIComponent {

            private NodeItem parentNodeItem;
            private int index;
            private boolean focusIndicate = false;
            private float maxDetectionDistance = 30;

            public ConnectionCircle(NodeItem parentNodeItem, int index) {
                super(getInnerContext(), 0, 0, 0, 0, false);
                this.parentNodeItem = parentNodeItem;
                this.index = index;
                calculateLayout();
            }

            public void calculateLayout() {
                resize((int) (parentNodeItem.parentNode.nodeStyle.connectionCircleRadius * 2), (int) (parentNodeItem.parentNode.nodeStyle.connectionCircleRadius * 2));
                if (nodeConnection.type == ConnectionType.Output) {
                    translate((int) (Node.this.getSizeW() - parentNodeItem.parentNode.nodeStyle.connectionCircleRadius),
                            (int) (parentNodeItem.parentNode.nodeStyle.titlePadding * 2 + parentNodeItem.parentNode.nodeStyle.titleSize + parentNodeItem.parentNode.nodeStyle.titleBottomMargin +
                                    (parentNodeItem.parentNode.nodeStyle.itemGap + parentNodeItem.parentNode.nodeStyle.itemHeight) * index + parentNodeItem.parentNode.nodeStyle.itemGap +
                                    (parentNodeItem.parentNode.nodeStyle.itemHeight - parentNodeItem.parentNode.nodeStyle.connectionCircleRadius * 2) / 2)
                    );
                } else {
                    translate(-(int) (parentNodeItem.parentNode.nodeStyle.connectionCircleRadius),
                            (int) (parentNodeItem.parentNode.nodeStyle.titlePadding * 2 + parentNodeItem.parentNode.nodeStyle.titleSize + parentNodeItem.parentNode.nodeStyle.titleBottomMargin +
                                    (parentNodeItem.parentNode.nodeStyle.itemGap + parentNodeItem.parentNode.nodeStyle.itemHeight) * parentNodeItem.parentNode.countOutputConnection() + parentNodeItem.parentNode.nodeStyle.inputOutputGap +
                                    (parentNodeItem.parentNode.nodeStyle.itemGap + parentNodeItem.parentNode.nodeStyle.itemHeight) * index + parentNodeItem.parentNode.nodeStyle.itemGap +
                                    (parentNodeItem.parentNode.nodeStyle.itemHeight - parentNodeItem.parentNode.nodeStyle.connectionCircleRadius * 2) / 2)
                    );
                }
            }

            private boolean dragging = false;

            @Override
            public boolean mousePressed() {
                if (nodeConnection.connectedNode != null) {
                    nodeConnection.connectedNode.connectedNode = null;
                    parentNodeItem.parentNode.parentNodeEditor.tossFocus(nodeConnection.connectedNode.parentNodeItem.parentNode);
                    nodeConnection.connectedNode.parentNodeItem.parentNode.tossFocus(nodeConnection.connectedNode.parentNodeItem.connectionCircle);
                    nodeConnection.connectedNode.parentNodeItem.connectionCircle.dragging = true;
                    nodeConnection.connectedNode = null;
                } else {
                    dragging = true;
                }
                return true;
            }


            private NodeItem findNearConnectionCircle() {
                NodeItem finded = null;
                if (nodeConnection.type == ConnectionType.Input) {
                    for (Node node : parentNodeItem.parentNode.parentNodeEditor.nodes) {
                        if (node == Node.this) continue;
                        for (NodeItem ni : node.outputs) {
                            if (ni.connectionCircle.distanceBetweenMouse() < maxDetectionDistance) {
                                ni.connectionCircle.focusIndicate = true;
                                finded = ni;
                            } else {
                                ni.connectionCircle.focusIndicate = false;
                            }
                        }
                    }
                } else {
                    for (Node node : parentNodeItem.parentNode.parentNodeEditor.nodes) {
                        if (node == Node.this) continue;
                        for (NodeItem ni : node.inputs) {
                            if (ni.connectionCircle.distanceBetweenMouse() < maxDetectionDistance) {
                                ni.connectionCircle.focusIndicate = true;
                                finded = ni;
                            } else {
                                ni.connectionCircle.focusIndicate = false;
                            }
                        }
                    }
                }

                return finded;
            }

            @Override
            public void mouseDragged() {
                if (dragging) {
                    findNearConnectionCircle();
                }
            }

            @Override
            public void mouseReleased() {
                if (dragging) {
                    dragging = false;
                    NodeItem ni = findNearConnectionCircle();
                    if (ni != null) {
                        if (ni.nodeConnection.connectedNode != null) {
                            ni.nodeConnection.connectedNode.connectedNode = null;
                        }
                        ni.nodeConnection.connectedNode = nodeConnection;
                        nodeConnection.connectedNode = ni.nodeConnection;
                        ni.connectionCircle.focusIndicate = false;
                    }
                }
            }

            @Override
            protected void draw(PGraphics graphics) {
                graphics.stroke(parentNodeItem.parentNode.nodeStyle.borderColor);
                graphics.strokeWeight(1 / parentNodeItem.parentNode.parentNodeEditor.getScale());
                graphics.fill(nodeConnection.color);
                graphics.circle(getSizeW() / 2, getSizeH() / 2, parentNodeItem.parentNode.nodeStyle.connectionCircleRadius);
                if (mouseEntered) {
                    graphics.noStroke();
                    graphics.fill(0x33ffffff);
                    graphics.circle(getSizeW() / 2, getSizeH() / 2, parentNodeItem.parentNode.nodeStyle.connectionCircleRadius * 3);
                }

                if (dragging) {
                    graphics.stroke(nodeConnection.color);
                    graphics.strokeWeight(parentNodeItem.parentNode.nodeStyle.connectionCircleRadius);
                    graphics.line(getSizeW() / 2, getSizeH() / 2, getMouseX(), getMouseY());
                }

                if (focusIndicate) {
                    graphics.noStroke();
                    graphics.fill(nodeStyle.connectionHighlightColor);
                    graphics.circle(getSizeW() / 2, getSizeH() / 2, parentNodeItem.parentNode.nodeStyle.connectionCircleRadius * 3);
                }
            }
        }

    }


    public class NodeConnection {
        public ConnectionType type = ConnectionType.Input;
        public boolean connectionAvailable = true;
        public int color = 0xffff0000;
        public String name = "item";
        public NodeConnection connectedNode = null; // If type is Input, previousNodeConnection. If type is Output, nextNodeConnection.
        public NodeItem parentNodeItem = null;

        public float value = 0.5f;
    }

    public enum ConnectionType {
        Input, Output
    }

    public static class NodeStyle {
        //Color
        public int titleBackgroundColor = 0xf6D63954;
        public int backgroundColor = 0xE63F3F3F;
        public int titleColor = 0xffffffff;
        public int itemNameColor = 0xffffffff;
        public int borderColor = 0xffBBBBBB;
        public int connectionHighlightColor = 0xFFFFDDA2;
        public int connectionSemiHighlightColor = 0xFFFEFFE0;
        public int valueSliderBackgroundColor = 0xb0515151;
        public int valueEditorBackgroundColor = 0xb0616161;
        public int valueEditorSelectionColor = 0xb05DADFF;
        public int valueSliderColor = 0x88cd8df3;

        //Layout
        int titleSize = 7;
        float titlePadding = 3;
        float titleLeadingPadding = 6;
        float titleBottomMargin = 5;

        int itemNameSize = 6;
        float itemNameLeadingPadding = 6;
        float itemHeight = 10;
        float itemGap = 3;
        float itemsBottomMargin = 3;

        float inputOutputGap = 4;
        float maxWidthPadding = 50;
        float minNodeWidth = 40;

        float radius = 6;

        float connectionCircleRadius = 3;
    }

}
