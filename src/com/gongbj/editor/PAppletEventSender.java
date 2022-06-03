package com.gongbj.editor;

import processing.core.PApplet;
import processing.event.MouseEvent;

import java.util.ArrayList;

public class PAppletEventSender extends PApplet {
    private ArrayList<EventListener> eventListeners = new ArrayList<>();
    int prew, preh;

    public void addEventListener(EventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void removeEventListener(EventListener eventListener) {
        eventListeners.remove(eventListener);
    }


    //mouse Event
    @Override
    public void mousePressed() {
        for (EventListener x : eventListeners){
            x.mousePressed();
        }
    }

    @Override
    public void mouseReleased() {
        for (EventListener x : eventListeners){
            x.mouseReleased();
        }
    }

    @Override
    public void mouseDragged() {
        for (EventListener x : eventListeners){
            x.mouseDragged();
        }
    }

    @Override
    public void mouseMoved() {
        for (EventListener x : eventListeners){
            x.mouseMoved();
        }
    }

    @Override
    public void mouseEntered() {
        for (EventListener x : eventListeners){
            x.mouseEntered();
        }
    }

    @Override
    public void mouseExited() {
        for (EventListener x : eventListeners){
            x.mouseExited();
        }
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        for (EventListener x : eventListeners)
            x.mouseWheel(event);
    }

    //key event
    @Override
    public void keyPressed() {
        for (EventListener x : eventListeners)
            x.keyPressed();
    }

    @Override
    public void keyReleased() {
        for (EventListener x : eventListeners)
            x.keyReleased();
    }

    @Override
    public void keyTyped() {
        for (EventListener x : eventListeners)
            x.keyTyped();
    }

    // This method should be called in end of child class's overrided method.
    public void settings(){
        prew = width;
        preh = height;
    }

    public void draw(){
        if(width != prew || height != preh){
            prew = width;
            preh = height;
            resized();
        }
    }

    public void resized(){

    }
}
