package gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {

    private final Random random = new Random();
    private final Map<String, String> aliases = new HashMap<>();
    private final List<String> quickRollAliases = new ArrayList<>();
    private static final String ALIAS_FILE = "aliases.txt";
    private static final String QUICK_ROLL_FILE = "quick_rolls.txt";
    private ListView<String> aliasListView;
    private VBox buttonContainer;
    private ListView<String> quickRollListView;
    
    
    private static final int DICE_SIZE = 80;
    private final Random random1 = new Random();
    //private final Pane dicePane = new Pane();
    private final List<Polygon> diceShapes = new ArrayList<>();
    private final List<Text> diceTexts = new ArrayList<>();
    private static final int NUM_DICE = 3; // Change this to set the number of dice
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        loadAliases();
        loadQuickRolls();
        primaryStage.setTitle("Dice Roller");

        Label instructionLabel = new Label("Enter dice notation or alias (e.g., 2d6, 1d20, 4d8+2, fireball):");
        instructionLabel.setFont(new Font("Arial", 14));
        TextField diceInput = new TextField();
        Button rollButton = new Button("Roll Dice");
        aliasListView = new ListView<>();
        quickRollListView = new ListView<>();
        TextField aliasKeyInput = new TextField();
        TextField aliasValueInput = new TextField();
        Button aliasButton = new Button("Add Alias");
        Button addQuickRollButton = new Button("Add to Quick Rolls");
        Label resultLabel = new Label();
        Button deleteButton = new Button("Delete Alias");
        Button removeQuickRollButton = new Button("Remove from Quick Rolls");
        buttonContainer = new VBox(5);

        updateAliasListView();
        updateQuickRollListView();
        updateAliasButtons();

        rollButton.setOnAction(e -> {
            String input = diceInput.getText().trim();
            String result = rollDice(input);
            resultLabel.setText(result);
        });

        aliasButton.setOnAction(e -> {
            String key = aliasKeyInput.getText().trim();
            String value = aliasValueInput.getText().trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                aliases.put(key, value);
                saveAliases();
                updateAliasListView();
            }
        });

        deleteButton.setOnAction(e -> {
            String selected = aliasListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String key = selected.split(" -> ")[0];
                aliases.remove(key);
                saveAliases();
                updateAliasListView();
            }
        });

        addQuickRollButton.setOnAction(e -> {
            String selected = aliasListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String key = selected.split(" -> ")[0];
                if (!quickRollAliases.contains(key)) {
                    quickRollAliases.add(key);
                    saveQuickRolls();
                    updateQuickRollListView();
                    updateAliasButtons();
                }
            }
        });

        removeQuickRollButton.setOnAction(e -> {
            String selected = quickRollListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                quickRollAliases.remove(selected);
                saveQuickRolls();
                updateQuickRollListView();
                updateAliasButtons();
            }
        });

        HBox aliasInputBox = new HBox(10, aliasKeyInput, aliasValueInput, aliasButton);
        VBox vbox = new VBox(10, instructionLabel, diceInput, rollButton, resultLabel,
                new Label("Aliases:"), aliasListView, aliasInputBox, deleteButton, addQuickRollButton,
                new Label("Quick Roll Buttons:"), quickRollListView, removeQuickRollButton, buttonContainer);
        vbox.setPadding(new Insets(15));
        //vbox.getStyleClass().add("root");
        
        Pane dicePane = new Pane();
        dicePane.setBackground(new Background(new BackgroundFill(Color.GRAY, null, null)));
        
        for (int i = 0; i < NUM_DICE; i++) {
            Polygon d20Shape = createD20Shape();
            Text numberText = new Text("20");
            numberText.setFont(Font.font(30));
            numberText.setFill(Color.WHITE);
            
            diceShapes.add(d20Shape);
            diceTexts.add(numberText);
            dicePane.getChildren().addAll(d20Shape, numberText);
        }
        clipChildren(dicePane);
        
        
        HBox hbox = new HBox(vbox, dicePane);
        hbox.getStyleClass().add("root");
        HBox.setHgrow(dicePane, Priority.ALWAYS);
        Scene scene = new Scene(hbox, 800, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void clipChildren(Region region) {
        final Rectangle clipPane = new Rectangle();
        region.setClip(clipPane);
        // In case we want to make a resizable pane we need to update
        // our clipPane dimensions
        region.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            clipPane.setWidth(newValue.getWidth());
            clipPane.setHeight(newValue.getHeight());
        });
    }
    
    private Polygon createD20Shape() {
        Polygon polygon = new Polygon(
            0, -DICE_SIZE,
            DICE_SIZE * 0.95, -DICE_SIZE * 0.31,
            DICE_SIZE * 0.59, DICE_SIZE * 0.81,
            -DICE_SIZE * 0.59, DICE_SIZE * 0.81,
            -DICE_SIZE * 0.95, -DICE_SIZE * 0.31
        );
        polygon.setFill(Color.RED);
        polygon.setStroke(Color.BLACK);
        polygon.setStrokeWidth(3);
        return polygon;
    }
    

    private String rollDice(String notation) {
        if (aliases.containsKey(notation)) {
            notation = aliases.get(notation);
        }

        Pattern pattern = Pattern.compile("(\\d*)d(\\d+)([+-]\\d+)?");
        Matcher matcher = pattern.matcher(notation);

        if (!matcher.matches()) {
            return "Invalid notation";
        }

        int numDice = matcher.group(1).isEmpty() ? 1 : Integer.parseInt(matcher.group(1));
        int numSides = Integer.parseInt(matcher.group(2));
        int modifier = (matcher.group(3) != null) ? Integer.parseInt(matcher.group(3)) : 0;

        int total = 0;
        StringBuilder rollDetails = new StringBuilder("Rolls: ");
        
        for (int i = 0; i < numDice; i++) {
            int roll = random.nextInt(numSides) + 1;
            total += roll;
            rollDetails.append(roll).append(i < numDice - 1 ? ", " : "");
        }

        total += modifier;
        if (modifier != 0) {
            rollDetails.append(" Modifier: ").append(modifier);
        }

        return rollDetails.append(" Total: ").append(total).toString();
    }

    private void saveQuickRolls() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(QUICK_ROLL_FILE))) {
            for (String alias : quickRollAliases) {
                writer.write(alias);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadQuickRolls() {
        try (BufferedReader reader = new BufferedReader(new FileReader(QUICK_ROLL_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                quickRollAliases.add(line);
            }
        } catch (IOException e) {
            // File may not exist yet, which is fine.
        }
    }

    private void updateQuickRollListView() {
        quickRollListView.getItems().setAll(quickRollAliases);
    }

    private void updateAliasButtons() {
        buttonContainer.getChildren().clear();
        for (String alias : quickRollAliases) {
            Button aliasButton = new Button(alias);
            aliasButton.setOnAction(e -> {
                String result = rollDice(alias);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, result, ButtonType.OK);
                alert.setHeaderText("Roll Result");
                alert.showAndWait();
            });
            buttonContainer.getChildren().add(aliasButton);
        }
    }
    
    private void saveAliases() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ALIAS_FILE))) {
            for (Map.Entry<String, String> aliasEntry : aliases.entrySet()) {
                writer.write(aliasEntry.getKey() + "=" + aliasEntry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAliases() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ALIAS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] aliasParts = line.split("=");
                if (aliasParts.length == 2) {
                    aliases.put(aliasParts[0], aliasParts[1]);
                }
            }
        } catch (IOException e) {
            // File may not exist yet, which is fine.
        }
    }

    private void updateAliasListView() {
        aliasListView.getItems().clear();
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            aliasListView.getItems().add(entry.getKey() + " -> " + entry.getValue());
        }
    }
}
