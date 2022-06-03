package com.gongbj.editor;

import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;

public class UIComponentGroup extends UIComponent {

    protected ArrayList<UIComponent> uiComponents = new ArrayList<>();
    Context innerContext = null;
    private UIComponent focusedUIComponent = null;

    public UIComponentGroup(Context mainContext, int sizew, int sizeh, int posx, int posy, boolean individualGraphic) {
        super(mainContext, sizew, sizeh, posx, posy, individualGraphic);
    }

    public UIComponentGroup(Context mainContext, int sizew, int sizeh, int posx, int posy) {
        super(mainContext, sizew, sizeh, posx, posy, true);
    }

    public Context getInnerContext() {
        if (innerContext == null) {
            innerContext = Context.createContext(context, getPosx(), getPosy());
            return innerContext;
        }
        return innerContext;
    }

    @Override
    public void translate(int posx, int posy) {
        super.translate(posx, posy);
        if (innerContext != null) {
            innerContext.posx = posx;
            innerContext.posy = posy;
        }
    }

    public void addUIComponent(UIComponent uiComponent) {
        if (uiComponent.context != innerContext) {
            System.out.println("Warn : UIComponentGroup -> addUIComponent, inputed uiComponent's context is not same to this group's context.");
        }
        uiComponents.add(uiComponent);
    }

    public void removeUIComponent(UIComponent eventListener) {
        uiComponents.remove(eventListener);
    }

    @Override
    protected void draw(PGraphics graphics) {
        for (int i = uiComponents.size() - 1; i >= 0; i--) {
            uiComponents.get(i).drawComponent(graphics);
        }
    }

    public void cancelFocusAll() {
        focusedUIComponent = null;
        for (UIComponent uiComponent : uiComponents) {
            if (uiComponent.focused == true) {
                uiComponent.cancelFocus();
            }
        }
    }

    private void cancelFocusAll(UIComponent exclude) {
        if (focusedUIComponent != exclude)
            focusedUIComponent = null;
        for (UIComponent uiComponent : uiComponents) {
            if (uiComponent == exclude) continue;
            if (uiComponent.focused == true) {
                uiComponent.cancelFocus();
            }
        }
    }

    //mouse Event
    @Override
    public boolean mousePressed() {
        focusedUIComponent = null;
        for (UIComponent x : uiComponents) {
            if (x.isMouseInComponent() && x.shouldFocus()) {
                if (x.mousePressed()) {
                    cancelFocusAll(x);
                    if (!x.focused) {
                        x.focus();
                    }
                    focusedUIComponent = x;
                    return true;
                }
            }
        }
        cancelFocusAll(focusedUIComponent);
        return false;
    }

    public void toTop(UIComponent uiComponent) {
        uiComponents.remove(uiComponent);
        uiComponents.add(0, uiComponent);
    }

    public void toBottom(UIComponent uiComponent) {
        uiComponents.remove(uiComponent);
        uiComponents.add(uiComponent);
    }

    @Override
    public void mouseReleased() {
        if (focusedUIComponent != null) {
//            focusedUIComponent.focused = false;
            focusedUIComponent.mouseReleased();
//            cancelFocusAll();
        }
    }

    @Override
    public void mouseDragged() {
        if (focusedUIComponent != null) {
            focusedUIComponent.mouseDragged();
        }
    }

    @Override
    public void mouseMoved() {
        boolean first = true;
        for (UIComponent x : uiComponents) {
            if (first && x.isMouseInComponent()) {
                if (x.shouldFocus()) {
                    first = false;
                    if (x.mouseEntered == false) {
                        x.mouseEntered = true;
                        x.mouseEntered();
                    } else {
                        x.mouseMoved();
                    }
                    continue;
                }
            }
            if (x.mouseEntered == true) {
                x.mouseEntered = false;
                x.mouseExited();
            }
        }
    }

    @Override
    public void mouseEntered() {
    }

    @Override
    public void mouseExited() {
        for (UIComponent x : uiComponents) {
            if (x.mouseEntered == true) {
                x.mouseEntered = false;
                x.mouseExited();
            }
        }
    }

    @Override
    public boolean mouseWheel(MouseEvent event) {
        boolean result = false;
        for (UIComponent x : uiComponents) {
            if (x.mouseWheel(event)) {
                if (x.mouseEntered) {
                    result = true;
                }
            }
        }
        return result;
    }

    public void tossFocus(UIComponent newUIComponent) {
        if (focusedUIComponent != null) {
            if(focusedUIComponent.focused){
                focusedUIComponent.cancelFocus();
            }
        }
        focusedUIComponent = newUIComponent;
        focusedUIComponent.focus();
    }

    @Override
    public void focused() {
        super.focused();
    }

    @Override
    public void focusCanceled() {
        super.focusCanceled();
        cancelFocusAll();
    }

    //key event
    @Override
    public void keyPressed() {
        for (UIComponent x : uiComponents)
            x.keyPressed();
    }

    @Override
    public void keyReleased() {
        for (UIComponent x : uiComponents)
            x.keyReleased();
    }

    @Override
    public void keyTyped() {
        for (UIComponent x : uiComponents)
            x.keyTyped();
    }
}
