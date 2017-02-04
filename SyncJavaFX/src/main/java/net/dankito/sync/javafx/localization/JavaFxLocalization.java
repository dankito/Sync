package net.dankito.sync.javafx.localization;

import net.dankito.sync.javafx.FXUtils;
import net.dankito.sync.localization.Localization;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.inject.Named;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


@Named
public class JavaFxLocalization {

  private final static ObjectProperty<Locale> locale = new SimpleObjectProperty<>(Locale.getDefault());

  public static ObjectProperty<Locale> localeProperty() {
    return locale ;
  }

  public static Locale getLocale() {
    return locale.get();
  }

  public static void setLocale(Locale locale) {
    FXUtils.runOnUiThread(() -> localeProperty().set(locale));
  }


  protected Localization localization;

  protected ResourceBundle resources;


  public JavaFxLocalization(Localization localization) {
    this.localization = localization;
    this.resources = new AvoidFxmlLoaderTranslatesResourceKeysResourceBundle(localization.getStringsResourceBundle());
  }


  public void bindLabeledText(Labeled labeled, final String key, final Object... formatArguments) {
    labeled.textProperty().bind(Bindings.createStringBinding(
        () -> localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public void bindMenuItemText(MenuItem menuItem, final String key, final Object... formatArguments) {
    menuItem.textProperty().bind(Bindings.createStringBinding(
        () -> localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public void bindTableColumnBaseText(TableColumnBase column, final String key, final Object... formatArguments) {
    column.textProperty().bind(Bindings.createStringBinding(
        () -> localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public void bindTabText(Tab tab, final String key, final Object... formatArguments) {
    tab.textProperty().bind(Bindings.createStringBinding(
        () -> localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public void bindTableColumnText(TableColumnBase tableColumn, final String key, final Object... formatArguments) {
    tableColumn.textProperty().bind(Bindings.createStringBinding(
        () -> localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public void bindControlToolTip(Control control, String key, Object... formatArguments) {
    control.setTooltip(createBoundTooltip(key, formatArguments));
  }

  // usage: myButton.setTooltip(createBoundTooltip("mybutton"));
  public Tooltip createBoundTooltip(final String key, final Object[] formatArguments) {
    Tooltip tooltip = new Tooltip();
    tooltip.textProperty().bind(Bindings.createStringBinding(
        () -> localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
    return tooltip ;
  }

  public void bindTextInputControlPromptText(TextInputControl control, final String key, final Object... formatArguments) {
    control.promptTextProperty().bind(Bindings.createStringBinding(
        () -> localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public void bindStageTitle(Stage stage, final String key, final Object... formatArguments) {
    stage.titleProperty().bind(Bindings.createStringBinding(
        () -> localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }


  public void resolveResourceKeys(Node node) {
    resolveNodeResourceKeys(node);
  }

  protected void resolveNodeResourceKeys(Node node) {
    if(node instanceof Labeled)
      resolveLabeledResourceKeys((Labeled)node);
    else if(node instanceof TextInputControl)
      resolveTextInputControlResourceKeys((TextInputControl) node);
    else if(node instanceof TableView) {
      for(TableColumn column : (ObservableList<TableColumn>)((TableView) node).getColumns())
        resolveTableColumnBaseResourceKeys(column);
    }
    else if(node instanceof TreeTableView) {
      for(TreeTableColumn column : (ObservableList<TreeTableColumn>)((TreeTableView)node).getColumns())
        resolveTableColumnBaseResourceKeys(column);
    }
    else if(node instanceof ScrollPane) {
      if(((ScrollPane)node).getContent() != null)
        resolveNodeResourceKeys(((ScrollPane)node).getContent());
    }
    else if(node instanceof SplitPane) {
      for(Node child : ((SplitPane)node).getItems())
        resolveNodeResourceKeys(child);
    }
    else if(node instanceof TabPane) {
      for(Tab tab : ((TabPane)node).getTabs())
        resolveTabResourceKeys(tab);
    }
    else if(node instanceof MenuBar) {
      for(Menu menu : ((MenuBar)node).getMenus())
        resolveMenuItemResourceKeys(menu);
    }
    else if(node instanceof BorderPane) {
      resolveBorderPaneResourceKeys((BorderPane) node);
    }
    else if(node instanceof Parent)
      resolveChildrenResourceKeys((Parent) node);
  }

  protected void resolveChildrenResourceKeys(Parent parent) {
    for(Node child : parent.getChildrenUnmodifiable()) {
      resolveNodeResourceKeys(child);
    }
  }

  protected void resolveLabeledResourceKeys(Labeled labeled) {
    if(hasResourceKeyPrefix(labeled.getText()))
      bindLabeledText(labeled, extractResourceKey(labeled.getText()));

    if(labeled.getGraphic() != null)
      resolveNodeResourceKeys(labeled.getGraphic());

    if(labeled instanceof TitledPane)
      resolveNodeResourceKeys(((TitledPane) labeled).getContent());
  }

  protected void resolveTextInputControlResourceKeys(TextInputControl textInputControl) {
    if(hasResourceKeyPrefix(textInputControl.getPromptText()))
      bindTextInputControlPromptText(textInputControl, extractResourceKey(textInputControl.getPromptText()));
  }

  protected void resolveTableColumnBaseResourceKeys(TableColumnBase column) {
    if(hasResourceKeyPrefix(column.getText()))
      bindTableColumnBaseText(column, extractResourceKey(column.getText()));

    if(column.getGraphic() != null)
      resolveNodeResourceKeys(column.getGraphic());
  }

  protected void resolveMenuItemResourceKeys(MenuItem item) {
    if(hasResourceKeyPrefix(item.getText()))
      bindMenuItemText(item, extractResourceKey(item.getText()));

    if(item instanceof Menu) {
      Menu menu = (Menu)item;
      for(MenuItem subItem : menu.getItems())
        resolveMenuItemResourceKeys(subItem);
    }
  }

  protected void resolveTabResourceKeys(Tab tab) {
    if(hasResourceKeyPrefix(tab.getText()))
      bindTabText(tab, extractResourceKey(tab.getText()));

    if (tab.getGraphic() != null)
      resolveNodeResourceKeys(tab.getGraphic());
    resolveNodeResourceKeys(tab.getContent());
  }

  protected void resolveBorderPaneResourceKeys(BorderPane node) {
    BorderPane borderPane = node;

    if(borderPane.getTop() != null)
      resolveNodeResourceKeys(borderPane.getTop());
    if(borderPane.getLeft() != null)
      resolveNodeResourceKeys(borderPane.getLeft());
    if(borderPane.getBottom() != null)
      resolveNodeResourceKeys(borderPane.getBottom());
    if(borderPane.getRight() != null)
      resolveNodeResourceKeys(borderPane.getRight());
    if(borderPane.getCenter() != null)
      resolveNodeResourceKeys(borderPane.getCenter());
  }


  protected boolean hasResourceKeyPrefix(String text) {
    return text != null && text.startsWith(FXMLLoader.RESOURCE_KEY_PREFIX);
  }

  protected String extractResourceKey(String internationalizationKey) {
    if(internationalizationKey.startsWith(FXMLLoader.RESOURCE_KEY_PREFIX))
      return internationalizationKey.substring(FXMLLoader.RESOURCE_KEY_PREFIX.length());

    return internationalizationKey;
  }


  public ResourceBundle getStringsResourceBundle() {
    return resources;
  }

}
