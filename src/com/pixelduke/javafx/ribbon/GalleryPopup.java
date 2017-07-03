package com.pixelduke.javafx.ribbon;

import java.util.HashMap;

import com.pixelduke.javafx.ribbon.skin.GallerySkin;

import bg.tu_sofia.gcode.Home;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

public class GalleryPopup extends Popup
{

    static final String STYLE_SHEET = "css/fxribbon.css";

    private ObservableList<GalleryItem> galleryItems;

    private VBox outerContainer;

    private ToggleGroup buttonGroup;

    private HashMap<String, FlowPane> categoryContainer;

    private GallerySkin gallerySkin;

    private Gallery gallery;

    private HashMap<GalleryItem, ToggleButton> galleryItemButton;

    private int itemIndex;

    public GalleryPopup(GallerySkin gallerySkin, Gallery gallery)
    {
        this.gallerySkin = gallerySkin;
        this.gallery = gallery;

        buttonGroup = new ToggleGroup();
        galleryItemButton = new HashMap<>();

        outerContainer = new VBox();
        outerContainer.getStylesheets().add(getClass().getResource(STYLE_SHEET).toExternalForm());

        categoryContainer = new HashMap<>();

        galleryItems = gallery.getItems();

        setAutoHide(true);
        setHideOnEscape(true);
        setAutoFix(true);

        galleryItems.addListener(new ListChangeListener<GalleryItem>() {
            @Override
            public void onChanged(Change<? extends GalleryItem> c) {
                updatePopup();
            }
        });

        getContent().add(outerContainer);

        outerContainer.getStyleClass().add("gallery-popup-container");

        updatePopup();

        gallery.selectedItemProperty().addListener(new ChangeListener<GalleryItem>() {
            @Override
            public void changed(ObservableValue<? extends GalleryItem> observable, GalleryItem oldValue, GalleryItem newValue) {
                selectedItemChanged();
            }
        });
    }

    private void selectedItemChanged() {
        ToggleButton toggleButton = galleryItemButton.get(gallery.getSelectedItem());
        toggleButton.setSelected(true);
    }

    private void updatePopup() {
        itemIndex = 0;

        for (GalleryItem item : galleryItems)
        {
            if (categoryContainer.get(item.getCategory()) == null) {
                Label categoryTitle = new Label(item.getCategory());
                categoryTitle.getStyleClass().add("category-title");

                BorderPane borderPane = new BorderPane();
                borderPane.getStyleClass().add("category-title-background");
                borderPane.setLeft(categoryTitle);

                outerContainer.getChildren().add(borderPane);
                FlowPane flowPane = new FlowPane();
                outerContainer.getChildren().add(flowPane);

                categoryContainer.put(item.getCategory(), flowPane);
                flowPane.getChildren().add(createPopupItem(item));
            } else
            {
                FlowPane flowPane = categoryContainer.get(item.getCategory());
                flowPane.getChildren().add(createPopupItem(item));
            }

            itemIndex++;
        }
    }

    public ObservableList<GalleryItem> getGalleryItems()
    {
        return galleryItems;
    }

    private Node createPopupItem(final GalleryItem item)
    {
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.TRANSPARENT);

        Image image = item.getGraphic().snapshot(snapshotParameters, null);
        ToggleButton button = new ToggleButton(item.getName());
        button.setContentDisplay(ContentDisplay.TOP);
        button.setGraphic(new ImageView(image));
        button.setId(String.valueOf(itemIndex));

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                gallery.setSelectedItem(item);
                gallerySkin.setScrollIndex(Integer.valueOf(button.getId()));
                Home.changeModule(item);
            }
        });

        buttonGroup.getToggles().add(button);

        galleryItemButton.put(item, button);

        return button;
    }

}
