package com.gongbj.editor;

import processing.event.MouseEvent;

public abstract class EventListener {
    public boolean mouseEntered = false;
    public boolean focused = false;

    //mouse Event
    public boolean mousePressed() {
        return false;
    }

    public void mouseReleased() {
    }

    public void mouseDragged() {
    }

    public void mouseMoved() {
    }

    public void mouseEntered() {
    }

    public void mouseExited() {
    }

    public boolean mouseWheel(MouseEvent event) {
        return false;
    }

    //key event
    public void keyPressed() {
    }

    public void keyReleased() {
    }

    public void keyTyped() {
    }

    public void focused(){
        System.out.println("focused");
    }

    public void focusCanceled(){
        System.out.println("focusCanceled");
    }

}
