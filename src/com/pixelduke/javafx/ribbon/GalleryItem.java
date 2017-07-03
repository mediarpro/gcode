package com.pixelduke.javafx.ribbon;

import javafx.scene.Node;

public class GalleryItem
{

    private transient String name;
    private transient String category;
    private transient Node graphic;

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setCategory(final String category)
    {
        this.category = category;
    }

    public String getCategory()
    {
        return category;
    }

    public void setGraphic(final Node graphic)
    {
        this.graphic = graphic;
    }

    public Node getGraphic()
    {
        return graphic;
    }

}
