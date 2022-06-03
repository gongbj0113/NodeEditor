package com.gongbj.editor;

import processing.core.PApplet;

public class Context {
    public PApplet mainApp;
    public Context parent = null;
    public int posx, posy;

    public int getMouseX(){
        if(parent == null){
            return mainApp.mouseX;
        }
        return parent.getMouseX() - posx;
    }

    public int getMouseY(){
        if(parent == null){
            return mainApp.mouseY;
        }
        return parent.getMouseY() - posy;
    }

    public Context(){
    }

    public static Context createContext(Context parent, int posx, int posy){
        Context con = new Context();
        con.mainApp = parent.mainApp;
        con.posx = posx;
        con.posy = posy;
        con.parent = parent;
        return con;
    }
    public static Context createContext(PApplet mainApp){
        Context con = new Context();
        con.posx = 0; con.posy = 0;
        con.mainApp = mainApp;
        return con;
    }


}
