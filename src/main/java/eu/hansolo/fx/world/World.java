/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.world;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by hansolo on 20.11.16.
 */
public abstract class World extends Region {
    private static final StyleablePropertyFactory<World> FACTORY          = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
    private static final double                          PREFERRED_WIDTH  = 1009;
    private static final double                          PREFERRED_HEIGHT = 665;
    private static final double                          MINIMUM_WIDTH    = 100;
    private static final double                          MINIMUM_HEIGHT   = 66;
    private static final double                          MAXIMUM_WIDTH    = 2018;
    private static final double                          MAXIMUM_HEIGHT   = 1330;
    private static       double                          MAP_OFFSET_X     = -PREFERRED_WIDTH * 0.0285;
    private static       double                          MAP_OFFSET_Y     = PREFERRED_HEIGHT * 0.195;
    private static final double                          ASPECT_RATIO     = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private static final CssMetaData<World, Color>       BACKGROUND_COLOR = FACTORY.createColorCssMetaData("-background-color", s -> s.backgroundColor, Color.web("#3f3f4f"), false);
    private        final StyleableProperty<Color>        backgroundColor;
    private static final CssMetaData<World, Color>       FILL_COLOR       = FACTORY.createColorCssMetaData("-fill-color", s -> s.fillColor, Color.web("#d9d9dc"), false);
    private        final StyleableProperty<Color>        fillColor;
    private static final CssMetaData<World, Color>       STROKE_COLOR     = FACTORY.createColorCssMetaData("-stroke-color", s -> s.strokeColor, Color.BLACK, false);
    private        final StyleableProperty<Color>        strokeColor;
    private static final CssMetaData<World, Color>       HOVER_COLOR      = FACTORY.createColorCssMetaData("-hover-color", s -> s.hoverColor, Color.web("#456acf"), false);
    private        final StyleableProperty<Color>        hoverColor;
    private static final CssMetaData<World, Color>       PRESSED_COLOR    = FACTORY.createColorCssMetaData("-pressed-color", s -> s.pressedColor, Color.web("#789dff"), false);
    private        final StyleableProperty<Color>        pressedColor;
    private static final CssMetaData<World, Color>       SELECTED_COLOR   = FACTORY.createColorCssMetaData("-selected-color", s-> s.selectedColor, Color.web("#9dff78"), false);
    private        final StyleableProperty<Color>        selectedColor;
    private static final CssMetaData<World, Color>       LOCATION_COLOR   = FACTORY.createColorCssMetaData("-location-color", s -> s.locationColor, Color.web("#ff0000"), false);
    private        final StyleableProperty<Color>        locationColor;
    private              BooleanProperty                 selectionEnabled;
    private              ObjectProperty<Country>         selectedCountry;
    private              BooleanProperty                 zoomEnabled;
    private              DoubleProperty                  scaleFactor;
    private              double                          zoomSceneX;
    private              double                          zoomSceneY;
    private              double                          width;
    private              double                          height;
    protected            Ikon                            locationIconCode;
    protected            Pane                            pane;
    protected            Group                           group;
    protected            Map<String, List<CountryPath>>  countryPaths;
    protected            ObservableMap<Location, Shape>  locations;
    // internal event handlers
    protected            EventHandler<MouseEvent>        _mouseEnterHandler;
    protected            EventHandler<MouseEvent>        _mousePressHandler;
    protected            EventHandler<MouseEvent>        _mouseReleaseHandler;
    protected            EventHandler<MouseEvent>        _mouseExitHandler;
    private              EventHandler<ScrollEvent>       _scrollEventHandler;
    // exposed event handlers
    private              EventHandler<MouseEvent>        mouseEnterHandler;
    private              EventHandler<MouseEvent>        mousePressHandler;
    private              EventHandler<MouseEvent>        mouseReleaseHandler;
    private              EventHandler<MouseEvent>        mouseExitHandler;


    // ******************** Constructors **************************************
    public World() {
        backgroundColor      = new StyleableObjectProperty<Color>(BACKGROUND_COLOR.getInitialValue(World.this)) {
            @Override protected void invalidated() { setBackground(new Background(new BackgroundFill(get(), CornerRadii.EMPTY, Insets.EMPTY))); }
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "backgroundColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return BACKGROUND_COLOR; }
        };
        fillColor            = new StyleableObjectProperty<Color>(FILL_COLOR.getInitialValue(World.this)) {
            @Override protected void invalidated() { setFillAndStroke(); }
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "fillColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return FILL_COLOR; }
        };
        strokeColor          = new StyleableObjectProperty<Color>(STROKE_COLOR.getInitialValue(World.this)) {
            @Override protected void invalidated() { setFillAndStroke(); }
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "strokeColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return STROKE_COLOR; }
        };
        hoverColor           = new StyleableObjectProperty<Color>(HOVER_COLOR.getInitialValue(World.this)) {
            @Override protected void invalidated() { }
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "hoverColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return HOVER_COLOR; }
        };
        pressedColor         = new StyleableObjectProperty<Color>(PRESSED_COLOR.getInitialValue(this)) {
            @Override protected void invalidated() { }
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "pressedColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return PRESSED_COLOR; }
        };
        selectedColor        = new StyleableObjectProperty<Color>(SELECTED_COLOR.getInitialValue(this)) {
            @Override protected void invalidated() { }
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "selectedColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return SELECTED_COLOR; }
        };
        locationColor        = new StyleableObjectProperty<Color>(LOCATION_COLOR.getInitialValue(this)) {
            @Override protected void invalidated() {
                locations.forEach((location, shape) -> shape.setFill(null == location.getColor() ? get() : location.getColor()));
            }
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "locationColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return LOCATION_COLOR; }
        };
        selectionEnabled     = new BooleanPropertyBase(false) {
            @Override protected void invalidated() {}
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "selectionEnabled"; }
        };
        selectedCountry      = new ObjectPropertyBase<Country>() {
            @Override protected void invalidated() {}
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "selectedCountry"; }
        };
        zoomEnabled          = new BooleanPropertyBase(false) {
            @Override protected void invalidated() {
                if (null == getScene()) return;
                if (get()) {
                    getScene().addEventFilter(ScrollEvent.ANY, _scrollEventHandler);
                } else {
                    getScene().removeEventFilter(ScrollEvent.ANY, _scrollEventHandler);
                }
            }
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "zoomEnabled"; }
        };
        scaleFactor          = new DoublePropertyBase(1.0) {
            @Override protected void invalidated() {
                if (isZoomEnabled()) {
                    setScaleX(scaleFactor.get());
                    setScaleY(scaleFactor.get());
                }
            }
            @Override public Object getBean() { return World.this; }
            @Override public String getName() { return "scaleFactor"; }
        };
        countryPaths         = new HashMap<>();
        locations            = FXCollections.observableHashMap();

        locationIconCode     = MaterialDesign.MDI_CHECKBOX_BLANK_CIRCLE;
        pane                 = new Pane();
        group                = new Group();

        _mouseEnterHandler   = evt -> handleMouseEvent(evt, mouseEnterHandler);
        _mousePressHandler   = evt -> handleMouseEvent(evt, mousePressHandler);
        _mouseReleaseHandler = evt -> handleMouseEvent(evt, mouseReleaseHandler);
        _mouseExitHandler    = evt -> handleMouseEvent(evt, mouseExitHandler);
        _scrollEventHandler  = evt -> {
            double delta    = 1.5;
            double scale    = getScaleFactor();
            double oldScale = scale;
            scale           = evt.getDeltaY() < 0 ? scale / delta : scale * delta;
            scale           = clamp( 1, 10, scale);
            double factor   = (scale / oldScale) - 1;
            if (Double.compare(1.0, getScaleFactor()) == 0) {
                zoomSceneX = evt.getSceneX();
                zoomSceneY = evt.getSceneY();
                resetZoom();
            }
            double deltaX = (zoomSceneX - (getBoundsInParent().getWidth() / 2 + getBoundsInParent().getMinX()));
            double deltaY = (zoomSceneY - (getBoundsInParent().getHeight() / 2 + getBoundsInParent().getMinY()));
            setScaleFactor(scale);
            setPivot(deltaX * factor, deltaY * factor);

            evt.consume();
        };

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    protected abstract void initGraphics();

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        locations.addListener(new MapChangeListener<Location, Shape>() {
            @Override public void onChanged(final Change<? extends Location, ? extends Shape> CHANGE) {
                if (CHANGE.wasAdded()) {
                    sceneProperty().addListener(o -> {
                        addShapeToScene(CHANGE.getValueAdded());
                        if (isZoomEnabled()) { getScene().addEventFilter( ScrollEvent.ANY, _scrollEventHandler); }
                    });
                    addShapeToScene(CHANGE.getValueAdded());
                } else if(CHANGE.wasRemoved()) {
                    Platform.runLater(() -> pane.getChildren().remove(CHANGE.getValueRemoved()));
                }
            }
        });
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT)  { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH)  { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT)  { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH)  { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public Map<String, List<CountryPath>> getCountryPaths() { return countryPaths; }

    public void setMouseEnterHandler(final EventHandler<MouseEvent> HANDLER) { mouseEnterHandler = HANDLER; }
    public void setMousePressHandler(final EventHandler<MouseEvent> HANDLER) { mousePressHandler = HANDLER; }
    public void setMouseReleaseHandler(final EventHandler<MouseEvent> HANDLER) { mouseReleaseHandler = HANDLER;  }
    public void setMouseExitHandler(final EventHandler<MouseEvent> HANDLER) { mouseExitHandler = HANDLER; }

    public Color getBackgroundColor() { return backgroundColor.getValue(); }
    public void setBackgroundColor(final Color COLOR) { backgroundColor.setValue(COLOR); }
    public ObjectProperty<Color> backgroundColorProperty() { return (ObjectProperty<Color>) backgroundColor; }

    public Color getFillColor() { return fillColor.getValue(); }
    public void setFillColor(final Color COLOR) { fillColor.setValue(COLOR); }
    public ObjectProperty<Color> fillColorProperty() { return (ObjectProperty<Color>) fillColor; }

    public Color getStrokeColor() { return strokeColor.getValue(); }
    public void setStrokeColor(final Color COLOR) { strokeColor.setValue(COLOR); }
    public ObjectProperty<Color> strokeColorProperty() { return (ObjectProperty<Color>) strokeColor; }

    public Color getHoverColor() { return hoverColor.getValue(); }
    public void setHoverColor(final Color COLOR) { hoverColor.setValue(COLOR); }
    public ObjectProperty<Color> hoverColorProperty() { return (ObjectProperty<Color>) hoverColor; }

    public Color getPressedColor() { return pressedColor.getValue(); }
    public void setPressedColor(final Color COLOR) { pressedColor.setValue(COLOR); }
    public ObjectProperty<Color> pressedColorProperty() { return (ObjectProperty<Color>) pressedColor; }

    public Color getSelectedColor() { return selectedColor.getValue(); }
    public void setSelectedColor(final Color COLOR) { selectedColor.setValue(COLOR); }
    public ObjectProperty<Color> selectedColorProperty() { return (ObjectProperty<Color>) selectedColor; }

    public Color getLocationColor() { return locationColor.getValue(); }
    public void setLocationColor(final Color COLOR) { locationColor.setValue(COLOR); }
    public ObjectProperty<Color> locationColorProperty() { return (ObjectProperty<Color>) locationColor; }

    public boolean isSelectionEnabled() { return selectionEnabled.get(); }
    public void setSelectionEnabled(final boolean ENABLED) { selectionEnabled.set(ENABLED); }
    public BooleanProperty selectionEnabledProperty() { return selectionEnabled; }

    public Country getSelectedCountry() { return selectedCountry.get(); }
    public void setSelectedCountry(final Country COUNTRY) { selectedCountry.set(COUNTRY); }
    public ObjectProperty<Country> selectedCountryProperty() { return selectedCountry; }

    public boolean isZoomEnabled() { return zoomEnabled.get(); }
    public void setZoomEnabled(final boolean ENABLED) { zoomEnabled.set(ENABLED); }
    public BooleanProperty zoomEnabledProperty() { return zoomEnabled; }

    public double getScaleFactor() { return scaleFactor.get(); }
    public void setScaleFactor(final double FACTOR) { scaleFactor.set(FACTOR); }
    public DoubleProperty scaleFactorProperty() { return scaleFactor; }

    public void resetZoom() {
        setScaleFactor(1.0);
        setTranslateX(0);
        setTranslateY(0);
    }

    public Ikon getLocationIconCode() { return locationIconCode; }
    public void setLocationIconCode(final Ikon ICON_CODE) { locationIconCode = ICON_CODE; }

    public void addLocation(final Location LOCATION) {
        double x = (LOCATION.getLongitude() + 180) * (PREFERRED_WIDTH / 360) + MAP_OFFSET_X;
        double y = (PREFERRED_HEIGHT / 2) - (PREFERRED_WIDTH * (Math.log(Math.tan((Math.PI / 4) + (Math.toRadians(LOCATION.getLatitude()) / 2)))) / (2 * Math.PI)) + MAP_OFFSET_Y;

        FontIcon locationIcon = new FontIcon(null == LOCATION.getIconCode() ? locationIconCode : LOCATION.getIconCode());
        //locationIcon.setFont(Font.font(LOCATION.getIconSize()));
        locationIcon.setIconSize(LOCATION.getIconSize());
        locationIcon.setTextOrigin(VPos.CENTER);
        locationIcon.setIconColor(null == LOCATION.getColor() ? getLocationColor() : LOCATION.getColor());
        locationIcon.setX(x - LOCATION.getIconSize() * 0.5);
        locationIcon.setY(y);

        StringBuilder tooltipBuilder = new StringBuilder();
        if (!LOCATION.getName().isEmpty()) tooltipBuilder.append(LOCATION.getName());
        if (!LOCATION.getInfo().isEmpty()) tooltipBuilder.append("\n").append(LOCATION.getInfo());
        String tooltipText = tooltipBuilder.toString();
        if (!tooltipText.isEmpty()) {
            Tooltip tooltip = new Tooltip(tooltipText);
            Tooltip.install(locationIcon, new Tooltip(tooltipText));
        }

        locations.put(LOCATION, locationIcon);
    }
    public void removeLocation(final Location LOCATION) {
        locations.remove(LOCATION);
    }

    public void addLocations(final Location... LOCATIONS) {
        for (Location location : LOCATIONS) { addLocation(location); }
    }
    public void clearLocations() { locations.clear(); }

    public void showLocations(final boolean SHOW) {
        for (Shape shape : locations.values()) {
            shape.setManaged(SHOW);
            shape.setVisible(SHOW);
        }
    }

    protected void setPivot(final double X, final double Y) {
        setTranslateX(getTranslateX() - X);
        setTranslateY(getTranslateY() - Y);
    }

    protected abstract void handleMouseEvent(final MouseEvent EVENT, final EventHandler<MouseEvent> HANDLER);

    protected abstract void setFillAndStroke();

    private void addShapeToScene(final Shape SHAPE) {
        if (null == getScene()) return;
        Platform.runLater(() -> pane.getChildren().add(SHAPE));
    }

    private double clamp(final double MIN, final double MAX, final double VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }


    // ******************** Style related *************************************
    @Override public String getUserAgentStylesheet() {
        return World.class.getResource("world.css").toExternalForm();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() { return FACTORY.getCssMetaData(); }

    @Override public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() { return FACTORY.getCssMetaData(); }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setCache(true);
            pane.setCacheHint(CacheHint.SCALE);

            pane.setScaleX(width / PREFERRED_WIDTH);
            pane.setScaleY(height / PREFERRED_HEIGHT);

            group.resize(width, height);
            group.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            pane.setCache(false);
        }
    }
}
