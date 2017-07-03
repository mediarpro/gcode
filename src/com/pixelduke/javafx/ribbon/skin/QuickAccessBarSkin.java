package com.pixelduke.javafx.ribbon.skin;

import java.util.Collection;

import com.pixelduke.javafx.ribbon.QuickAccessBar;

import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class QuickAccessBarSkin extends SkinBase<QuickAccessBar>
{

    BorderPane outerContainer;
    HBox buttonContainer;
    HBox rightButtons;

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public QuickAccessBarSkin(QuickAccessBar control) {
        super(control);
        buttonContainer = new HBox();
        rightButtons = new HBox();
        outerContainer = new BorderPane();
        buttonContainer.getStyleClass().add("button-container");
        outerContainer.getStyleClass().add("outer-container");

        outerContainer.setCenter(buttonContainer);
        outerContainer.setRight(rightButtons);
        getChildren().add(outerContainer);
        updateAddedButtons(control.getButtons());
    }

    private void updateAddedButtons(Collection<? extends Button> addedSubList) {
        QuickAccessBar skinnable = getSkinnable();
        for (Button button : skinnable.getButtons())
        {
            buttonContainer.getChildren().add(button);
        }
        for (Button button : skinnable.getRightButtons())
            rightButtons.getChildren().add(button);
    }

}
