package com.gongbj.editor;

import processing.core.PGraphics;


public abstract class UIComponent extends EventListener {

    protected PGraphics pGraphics = null;
    protected Context context;

    private int posx, posy;
    private int sizew, sizeh;
    private int margin = 0;

    private boolean individualGraphic;

    public UIComponent(Context mainContext, int sizew, int sizeh, int posx, int posy, boolean individualGraphic) {
        this.context = mainContext;
        this.sizew = sizew;
        this.sizeh = sizeh;
        this.posx = posx;
        this.posy = posy;
        this.individualGraphic = individualGraphic;
        if (individualGraphic) {
            pGraphics = context.mainApp.createGraphics(sizew, sizeh);
        }
    }

    public UIComponent(Context mainContext, int sizew, int sizeh, int posx, int posy) {
        this(mainContext, sizew, sizeh, posx, posy, true);
    }

    public void setPosx(int posx) {
        translate(posx, this.posy);
    }

    public void setPosy(int posy) {
        translate(this.posx, posy);
    }

    public void setSizeW(int sizew) {
        resize(sizew, this.sizeh);
    }

    public void setSizeH(int sizeh) {
        resize(this.sizew, sizeh);
    }

    public int getPosx() {
        return posx;
    }

    public int getPosy() {
        return posy;
    }

    public int getSizeH() {
        return sizeh;
    }

    public int getSizeW() {
        return sizew;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public void resize(int sizew, int sizeh) {
        this.sizew = sizew;
        this.sizeh = sizeh;
        if (individualGraphic) {
            pGraphics.dispose();
            pGraphics = context.mainApp.createGraphics(sizew, sizeh);
        }
    }

    public void translate(int posx, int posy) {
        this.posx = posx;
        this.posy = posy;
    }

    public int getMouseX() {
        return context.getMouseX() - posx;
    }

    public int getMouseY() {
        return context.getMouseY() - posy;
    }

    protected abstract void draw(PGraphics graphics);

    protected boolean shouldFocus() {
        return true;
    }

//    public boolean isPointInComponent(int x, int y) {
//        return (x >= getPosx() - getMargin() && x <= getPosx() + getSizeW() + getMargin())
//                && (y >= getPosy()  - getMargin() && y <= getPosy() + getSizeH() + getMargin());
//    }

    public float distanceBetweenMouse() {
        return (float)(Math.pow(getMouseX() - getSizeW() / 2, 2) + Math.pow(getMouseY() - getSizeH() / 2, 2));
    }

    public boolean isMouseInComponent(boolean withoutPadding) {
        int mx = getMouseX();
        int my = getMouseY();

        if (withoutPadding) {
            return (mx >= 0 && mx <= getSizeW()) && (my >= 0 && my <= getSizeH());
        } else {
            return (mx >= -getMargin() && mx <= getSizeW() + getMargin()) && (my >= -getMargin() && my <= getSizeH() + getMargin());
        }
    }

    public boolean isMouseInComponent() {
        return isMouseInComponent(false);
    }

    public void drawComponent() {
        if (individualGraphic) {
            pGraphics.beginDraw();
            draw(pGraphics);
            pGraphics.endDraw();

            context.mainApp.image(pGraphics, posx, posy);
        } else {
            throw new RuntimeException("When individualGraphic is false, drawComponent() cannot be called.");
        }
    }

    public void drawComponent(PGraphics graphics) {
        if (individualGraphic) {
            pGraphics.beginDraw();
            draw(pGraphics);
            pGraphics.endDraw();

            graphics.image(pGraphics, posx, posy);
        } else {
            graphics.pushMatrix();
            graphics.translate(getPosx(), getPosy());
            draw(graphics);
            graphics.popMatrix();
        }
    }

    public void focus(){
        focused = true;
        focused();
    }

    public void cancelFocus(){
        focused = false;
        focusCanceled();
    }

    public void dispose() {
        if (pGraphics != null) {
            pGraphics.dispose();
            pGraphics = null;
        }
    }

}
