package questdesigner;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.TransformationList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import org.hildan.fxgson.FxGson;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class QuestDesignerController {
    @FXML
    private Scene scene;
    @FXML
    private TextArea answerTextArea;
    @FXML
    private TextArea choiceTextArea;
    @FXML
    private CustomTreeView treeView;
    private final Gson gson = FxGson.addFxSupport(new GsonBuilder()).setExclusionStrategies(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(Exclude.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }).registerTypeAdapter(TextItem.class, new TypeAdapter<TextItem>() {
        TypeAdapter<List<TextItem>> subItemsAdapter;

        @Override
        public void write(JsonWriter out, TextItem value) throws IOException {
            out.beginObject();
            String response = value.getResponse();
            if (response != null) {
                out.name("response").value(response);
            }
            initSubItemsAdapter();
            List<TextItem> subItems = value.subItemsProperty();
            out.name("subItems");
            subItemsAdapter.write(out, subItems);
            String text = value.getText();
            if (text != null) {
                out.name("text").value(text);
            }
            out.endObject();
        }

        @Override
        public TextItem read(JsonReader in) throws IOException {
            in.beginObject();
            boolean hasResponse = false, hasSubItems = false, hasText = false;
            TextItem textItem = new TextItem();
            while (in.hasNext()) {
                String name;
                switch (name = in.nextName()) {
                    case "response":
                        if (hasResponse) {
                            throw new IllegalStateException("response was already set");
                        }
                        hasResponse = true;
                        textItem.setResponse(in.nextString());
                        break;

                    case "subItems":
                        if (hasSubItems) {
                            throw new IllegalStateException("subItems was already set");
                        }
                        hasSubItems = true;
                        initSubItemsAdapter();
                        textItem.setSubItems(FXCollections.observableList(Objects.requireNonNull(subItemsAdapter.read(in), "subItems may not be null")));
                        break;
                    case "text":
                        if (hasText) {
                            throw new IllegalStateException("text was already set");
                        }
                        hasText = true;
                        textItem.setText(in.nextString());
                        break;
                    case "treeItem":
                        in.skipValue();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected TextItem property: " + name);
                }
            }
            in.endObject();
            return textItem;
        }

        private void initSubItemsAdapter() {
            if (subItemsAdapter == null) {
                subItemsAdapter = gson.getAdapter(new TypeToken<List<TextItem>>() {
                });
            }
        }
    }.nullSafe()).create();
    @FXML
    public final ObjectPropertyBase<TextItem> root = new ObjectPropertyBase<TextItem>() {
        @Override
        public Object getBean() {
            return QuestDesignerController.this;
        }

        @NotNull
        @Contract(pure = true)
        @Override
        public String getName() {
            return "root";
        }

        @Override
        protected void invalidated() {
            treeView.setRootValue(getValue());
        }
    };
    private ObjectBinding<TextItem> textItem;

    @FXML
    public void newQuestTree() {
        root.set(new TextItem("New Answer"));
    }


    private final FileChooser fileChooser = new FileChooser();
    private final FileChooser.ExtensionFilter jsonExtensionFilter = new FileChooser.ExtensionFilter("JSON", "*.json");
    private final FileChooser.ExtensionFilter javaSerializationExtensionFilter = new FileChooser.ExtensionFilter("Java Serialization", "*.dat");
    private final FileChooser.ExtensionFilter xmlExtensionFilter = new FileChooser.ExtensionFilter("XML", "*.xml");
    private final FileChooser.ExtensionFilter binaryExtensionFilter = new FileChooser.ExtensionFilter("Custom binary format", "*.bin");

    {
        fileChooser.getExtensionFilters().addAll(jsonExtensionFilter, javaSerializationExtensionFilter, xmlExtensionFilter, binaryExtensionFilter);
    }

    @FXML
    private void openQuestTree() {
        @Nullable final File file = fileChooser.showOpenDialog(scene.getWindow());
        if (file != null) {
            try (final InputStream inputStream = Files.newInputStream(file.toPath())) {
                if (fileChooser.getSelectedExtensionFilter() == jsonExtensionFilter) {
                    try (final InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                        root.set(gson.fromJson(reader, TextItem.class));
                    }
                } else if (fileChooser.getSelectedExtensionFilter() == javaSerializationExtensionFilter) {
                    try (final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                        root.set((TextItem) objectInputStream.readObject());
                    }
                } else if (fileChooser.getSelectedExtensionFilter() == xmlExtensionFilter) {
                    try (final InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                        root.set(JAXB.unmarshal(reader, TextItem.class));
                    }
                }
            } catch (IOException | JsonParseException | ClassNotFoundException | ClassCastException | DataBindingException e) {
                e.printStackTrace();
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                new Alert(Alert.AlertType.ERROR, writer.toString(), ButtonType.CLOSE).show();
            }
        }
    }

    @FXML
    public void saveQuestTree() {
        @Nullable final File file = fileChooser.showSaveDialog(scene.getWindow());
        if (file != null) {
            try (final OutputStream outputStream = Files.newOutputStream(file.toPath())) {
                if (fileChooser.getSelectedExtensionFilter() == jsonExtensionFilter) {
                    try (final OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                        gson.toJson(root.get(), TextItem.class, writer);
                    }
                } else if (fileChooser.getSelectedExtensionFilter() == javaSerializationExtensionFilter) {
                    try (final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
                        objectOutputStream.writeObject(root.get());
                    }
                } else if (fileChooser.getSelectedExtensionFilter() == xmlExtensionFilter) {
                    try (final OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                        JAXB.marshal(root.get(), writer);
                    }
                } else if (fileChooser.getSelectedExtensionFilter() == binaryExtensionFilter) {
                    try (final DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
                        BinaryFormatWriter.write(dataOutputStream, root.get());
                    }
                }
            } catch (IOException | JsonParseException | DataBindingException e) {
                e.printStackTrace();
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                new Alert(Alert.AlertType.ERROR, writer.toString(), ButtonType.CLOSE).show();
            }
        }
    }

    @FXML
    private void exportQuestTree() throws IOException {
        final FXMLLoader loader = new FXMLLoader(QuestDesignerController.class.getResource("ExportDialog.fxml"));
        /*loader.setControllerFactory(clazz -> {
            try {
                return new ExportDialogController();
            } catch (IOException e) {
                return Utils.sneakyThrow(e); // will propagate through FXMLLoader.load
            }
        });*/ // unneeded, just does what the default factory does
        final Dialog<ExportJavaSettings> dialog = loader.load();
        dialog.showAndWait()
        .ifPresent(exportJavaSettings -> {
            try {
                ExportJava.write(exportJavaSettings.getPath(), root.get(), exportJavaSettings.getPackageName(), exportJavaSettings.getClassName());
            } catch (IOException e) {
                e.printStackTrace();
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                new Alert(Alert.AlertType.ERROR, writer.toString(), ButtonType.CLOSE).show();
            }
        });
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    @NotNull
    static TreeItem<TextItem> createTreeItem(@Nullable TextItem item) {
        TreeItem<TextItem> treeItem = item != null ? item.treeItem.get() : null;
        return treeItem != null ? treeItem : new TreeItem<TextItem>(item) {
            @SuppressWarnings({"FieldCanBeLocal", "unused"})
            // This is NOT unused, if the GC finds this object then the binding breaks.
            private TransformationList<TreeItem<TextItem>, TextItem> pin;

            {
                setValue(item);
                final SimpleListProperty<TextItem> subItems = new SimpleListProperty<>();
                subItems.bind(Bindings.select(valueProperty(), "subItems"));
                Bindings.bindContent(getChildren(), pin = new TransformationList<TreeItem<TextItem>, TextItem>(subItems) {
                    @Override
                    public TreeItem<TextItem> get(int index) {
                        final TextItem item = getSource().get(index);
                        return createTreeItem(item);
                    }

                    @Override
                    public int getSourceIndex(int index) {
                        return index;
                    }

                    @Override
                    public int size() {
                        return getSource().size();
                    }

                    @Override
                    protected void sourceChanged(ListChangeListener.Change<? extends TextItem> c) {
                        fireChange(new ListChangeListener.Change<TreeItem<TextItem>>(this) {

                            @Override
                            public boolean wasAdded() {
                                return c.wasAdded();
                            }

                            @Override
                            public boolean wasRemoved() {
                                return c.wasRemoved();
                            }

                            @Override
                            public boolean wasReplaced() {
                                return c.wasReplaced();
                            }

                            @Override
                            public boolean wasUpdated() {
                                return c.wasUpdated();
                            }

                            @Override
                            public boolean wasPermutated() {
                                return c.wasPermutated();
                            }

                            @Override
                            public int getPermutation(int i) {
                                return c.getPermutation(i);
                            }

                            @Override
                            protected int[] getPermutation() {
                                throw new AssertionError("Unreachable code");
                            }

                            @Override
                            public List<TreeItem<TextItem>> getRemoved() {
                                ArrayList<TreeItem<TextItem>> res = new ArrayList<>(c.getRemovedSize());
                                for (TextItem e : c.getRemoved()) {
                                    TreeItem<TextItem> treeItem = e.treeItem.get();
                                    res.add(treeItem != null ? treeItem : createTreeItem(e));
                                }
                                return res;
                            }

                            @Override
                            public int getFrom() {
                                return c.getFrom();
                            }

                            @Override
                            public int getTo() {
                                return c.getTo();
                            }

                            @Override
                            public boolean next() {
                                return c.next();
                            }

                            @Override
                            public void reset() {
                                c.reset();
                            }
                        });
                    }
                });
            }
        };
    }

    public void initialize() {
        newQuestTree();
        textItem = Utils.createMapperObjectBinding(treeView.getSelectionModel().selectedItemProperty(), treeItem -> Optional.ofNullable(treeItem).map(TreeItem::getValue).orElse(null));
        textItem.addListener((observable1, oldValue1, newValue1) -> {
            if (oldValue1 != null) {
                choiceTextArea.textProperty().unbindBidirectional(oldValue1.responseProperty());
                answerTextArea.textProperty().unbindBidirectional(oldValue1.textProperty());
            }
            if (newValue1 != null) {
                choiceTextArea.textProperty().bindBidirectional(newValue1.responseProperty());
                answerTextArea.textProperty().bindBidirectional(newValue1.textProperty());
            } else {
                choiceTextArea.setText("");
                answerTextArea.setText("");
            }
        });

        final BooleanBinding disable = treeView.getSelectionModel().selectedItemProperty().isNull();
        choiceTextArea.disableProperty().bind(disable);
        answerTextArea.disableProperty().bind(disable);
    }
}
