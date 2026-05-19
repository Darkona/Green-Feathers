package com.darkona.feathers.client.gui;


public record GuiIcon(int x, int y, int width, int height) {

    public static GuiIcon featherIcon(int x, int y) {
        return new GuiIcon((x * 9), (y * 9), 9, 9);
    }

}
