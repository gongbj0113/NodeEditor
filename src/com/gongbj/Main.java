package com.gongbj;

import com.gongbj.editor.*;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Main extends PAppletEventSender {
    UIComponentGroup mainLayout;
    NodeEditor nodeEditor;
    NodeEditor nodeEditor2;
    PGraphics graphics;

    @Override
    public void settings() {
        size(500, 500);
        super.settings();
    }

    @Override
    public void setup() {
        getSurface().setResizable(true);
        graphics = createGraphics(width, height);

        mainLayout = new UIComponentGroup(Context.createContext(this), width, height, 0, 0, true);
        addEventListener(mainLayout);
        {
            nodeEditor = new NodeEditor(mainLayout.getInnerContext(), width, height);
            nodeEditor2 = new NodeEditor(mainLayout.getInnerContext(), width / 2, height);
            nodeEditor.translate(0, 0);
            //nodeEditor2.translate(width / 2, 0);
        }
        mainLayout.addUIComponent(nodeEditor);
        //mainLayout.addUIComponent(nodeEditor2);

    }

    @Override
    public void resized() {
        mainLayout.resize(width, height);
        nodeEditor.resize(width, height);
        //nodeEditor2.resize(width, height);
        //nodeEditor2.translate(width / 2, 0);
        graphics.dispose();
        graphics = createGraphics(width, height);
    }

    public void draw() {
        image(graphics, 0, 0);
        super.draw();
        graphics.beginDraw();

        graphics.background(255, 255, 100);
        mainLayout.drawComponent(graphics);

        graphics.stroke(255,255,255);
        graphics.strokeWeight(1);
        //graphics.line(width/2, 0, width/2, height);

        graphics.endDraw();
        image(graphics, 0, 0);
    }

    public static void main(String[] args) {
        Main mainSketch = new Main();
        PApplet.runSketch(new String[]{"Main"}, mainSketch);
    }


}
