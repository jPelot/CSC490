package gui;

import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import notation_parser.DieExpression;
import notation_parser.NotationParser;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.util.*;

import dice_roller.DiceRoller;
import dice_roller.ResultSet;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;


public class Main extends Application {

    private final HashMap<String, String> aliases = new HashMap<>();
    private final List<String> quickRollAliases = new ArrayList<>();
    
    // JavaFX Objects
    private ListView<String> aliasListView;
    private FlowPane shortcutContainer;
    private Pane dicePane = new Pane();
    Label resultLabel;
    TextField diceInput;
    Button rollButton;
    Button shortcutToggle;
    TextField aliasKeyInput;
    TextField aliasValueInput;
    
    // Constants
    private static final String ALIAS_FILE = "aliases.txt";
    private static final int DICE_SIZE = 30;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
    	
    	// Load Settings from file
        loadAliases();
        //loadQuickRolls();
        
        /* Setup and Configure GUI */
        primaryStage.setTitle("LuckEngine");
        
        // Shortcut Container
        shortcutContainer = new FlowPane();
        shortcutContainer.setHgap(5);
        shortcutContainer.setVgap(5);
        
        // Dice Roll Area
        Label instructionLabel = new Label("Enter dice notation");
        instructionLabel.setFont(new Font("Arial", 14));
        diceInput = new TextField();
        diceInput.setPromptText("Dice notation or alias (e.g. 1d20, 4d8+2, fireball)");
        rollButton = new Button("Roll Dice");
        resultLabel = new Label();
        HBox rollEntry = new HBox(10, diceInput, rollButton);
        HBox.setHgrow(diceInput, Priority.ALWAYS);
        VBox diceRollArea = new VBox(10, instructionLabel, rollEntry, resultLabel);
        
        // Alias Menu
        aliasListView = new ListView<>();
        aliasKeyInput = new TextField();
        aliasKeyInput.setPromptText("Alias name");
        aliasValueInput = new TextField();
        aliasValueInput.setPromptText("Value");
        Button aliasButton = new Button("Add");
        HBox aliasInputBox = new HBox(10, aliasKeyInput, aliasValueInput, aliasButton);
        // Alias Options
        Button deleteButton = new Button("Delete");
        Button editButton = new Button("Edit");
        shortcutToggle = new Button("Add Shortcut");
        HBox aliasOptions = new HBox(10, deleteButton, editButton, shortcutToggle);
        VBox aliasMenu = new VBox(10, new Label("Aliases"), aliasListView, aliasInputBox, aliasOptions);
         
        // Menu
        VBox menu = new VBox(10, shortcutContainer, diceRollArea, aliasMenu);
        menu.setPadding(new Insets(15));
        
        // Dice Pane
        dicePane = new Pane();
        dicePane.setPrefWidth(400);
        dicePane.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));
        clipChildren(dicePane);
        
        // GUI Root and Scene
        HBox root = new HBox(menu, dicePane);
        root.getStyleClass().add("root");
        HBox.setHgrow(menu, Priority.ALWAYS);
        Scene scene = new Scene(root, 800, 600);
        
        // Load style sheet and stage scene
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Pre-populate lists and shortcut buttons
        updateAliasListView();
        updateAliasButtons();
        
        // Register Callback Functions
        rollButton.setOnAction(_ -> rollButtonAction_cb());
        aliasListView.setOnMousePressed(_ -> aliasListMousePressed_cb());
        aliasButton.setOnAction(_ -> createAliasButton_cb());
        deleteButton.setOnAction(_ -> deleteButtonAction_cb());
        //addQuickRollButton.setOnAction(_ -> addQuickRollButton_cb());
        editButton.setOnAction(_ -> editButton_cb());
        shortcutToggle.setOnAction(_ -> shortcutToggle_cb());
    }
    
    private void animation(ResultSet result) {
    	ParallelTransition sequence = new ParallelTransition();
    	ArrayList<Polygon> diceShapes = new ArrayList<>();
        ArrayList<Text> diceTexts = new ArrayList<>();
        Random random = new Random();
        
        dicePane.getChildren().clear();
        
        for (Integer num : result.results()) {
        	diceShapes.add(createD20Shape());
            diceTexts.add(createDiceText(num.toString()));  
        }
        
        for (int i = 0; i < diceShapes.size(); i++) {
        	Integer finalValue = result.results().get(i);
            Polygon d20Shape = diceShapes.get(i);
            Text numberText = diceTexts.get(i);
            
            dicePane.getChildren().addAll(d20Shape, numberText);
            
            double startX = random.nextDouble() * 400;
            double startY = random.nextDouble() * 400;
            d20Shape.setTranslateX(startX);
            d20Shape.setTranslateY(startY);
            numberText.setTranslateX(startX);
            numberText.setTranslateY(startY);
            
            RotateTransition rotate = new RotateTransition(Duration.seconds(2), d20Shape);
            rotate.setByAngle(720);
            
            Pos dest = gridPosition(i, 75, 400, 500);
            Path path = new Path(new MoveTo(startX, startY), new LineTo(dest.x, dest.y));
            PathTransition die_move = new PathTransition(Duration.seconds(1), path, d20Shape);
            PathTransition text_move = new PathTransition(Duration.seconds(1), path, numberText);
            
            Timeline timeline = generateRandomSequenceTimeline(numberText, finalValue, 2);
            
            sequence.getChildren().add(new ParallelTransition(die_move, text_move, rotate, timeline));
        }
        
        sequence.play();
    }
    
    
    private void updateAliasButtons() {
        shortcutContainer.getChildren().clear();
        for (String alias : quickRollAliases) {
            Button aliasButton = new Button(alias);
            aliasButton.setOnAction(_ -> {
            	diceInput.setText(aliases.get(alias));        	
            	rollButton.fire();
            });
            shortcutContainer.getChildren().add(aliasButton);
        }
    }

    private void updateAliasListView() {
        aliasListView.getItems().clear();
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            aliasListView.getItems().add(entry.getKey());
        }
    }
    
    
    /* File Loading/Saving */
    
    private void saveAliases() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ALIAS_FILE))) {
            for (Map.Entry<String, String> aliasEntry : aliases.entrySet()) {
            	if (quickRollAliases.contains(aliasEntry.getKey())) {
            		writer.write("*");
            	}
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
                if (aliasParts.length != 2) 
                	continue;
                String name = aliasParts[0];
                if (name.charAt(0) == '*') {
                	name = name.substring(1);
                	quickRollAliases.add(name);
                }
                aliases.put(name, aliasParts[1]);
                
            }
        } catch (IOException e) {
            // File may not exist yet, which is fine.
        }
    }
    
    
    /* GRAPHICS HELPER FUNCTIONS */
    
    private void clipChildren(Region region) {
        final Rectangle clipPane = new Rectangle();
        region.setClip(clipPane);
        // In case we want to make a re-sizable pane we need to update our clipPane dimensions
        region.layoutBoundsProperty().addListener((_, _, newValue) -> {
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
    
    private Text createDiceText(String text) {
    	Text numberText = new Text(text);
        numberText.setFont(Font.font(25));
        numberText.setFill(Color.BLACK);
        numberText.setTextAlignment(TextAlignment.CENTER);
        return numberText;
    }
    
    private class Pos {
    	public int x;
    	public int y;
    	Pos(int x, int y) {
    		this.x = x;
    		this.y = y;
    	}
    }
    
    Pos gridPosition(int dieNum, int spacing, int width, int height) {
    	int numCol = (width / spacing) - 1;
    	
    	int gridX = dieNum % (numCol);
    	int gridY = dieNum / (numCol);
    	
    	int x = gridX * spacing + spacing;
    	int y = gridY * spacing + spacing;
    	
    	Pos pos = new Pos(x, y);
    	return pos;
    }
    
    Timeline generateRandomSequenceTimeline(Text node, int finalValue, int duration) {
    	Random random = new Random();
    	Timeline timeline = new Timeline();
    	int num_frames = 15 * duration;
    	int frame_delta_millis = (duration*1000) / num_frames;
    	
    	for (int j = num_frames; j >= 0; j--) {
    		int rollValue;
    		if (j == 0) rollValue = finalValue;
    		else        rollValue = random.nextInt(20) + 1;
    		
    		EventHandler<ActionEvent> event = (_) -> {node.setText(String.valueOf(rollValue));};
    		timeline.getKeyFrames().add(new KeyFrame(Duration.millis((num_frames-j) * frame_delta_millis), event));
    	}
    	return timeline;
    }
    
    
    /* CALLBACK FUNCTIONS */
    
    private void rollButtonAction_cb() {
    	DieExpression exp = NotationParser.parse(diceInput.getText().trim(), aliases);
    	ResultSet result = DiceRoller.roll(exp);
    	resultLabel.setText(result.toString());
    	animation(result);
    }
    
    private void aliasListMousePressed_cb() {
    	String item = aliasListView.getFocusModel().focusedItemProperty().getValue();
    	if (item != null) {
    		diceInput.setText(aliases.get(item));
    	}
    	
    	if (quickRollAliases.contains(item)) {
    		shortcutToggle.setText("Remove Shortcut");
    	} else {
    		shortcutToggle.setText("Add Shortcut");
    	}
    }
    
    private void createAliasButton_cb() {
    	if (aliasKeyInput.getText() == null || aliasValueInput.getText() == null) {
    		return;
    	}
        String key = aliasKeyInput.getText().trim();
        String value = aliasValueInput.getText().trim();
        if (!key.isEmpty() && !value.isEmpty()) {
            aliases.put(key, value);
            saveAliases();
            updateAliasListView();
        }
        aliasKeyInput.setText("");
        aliasValueInput.setText(aliases.get(""));
    }

    private void deleteButtonAction_cb() {
        String selected = aliasListView.getSelectionModel().getSelectedItem();
        if (selected == null)
        	return;
        String key = selected.split(" -> ")[0];
        aliases.remove(key);
        saveAliases();
        updateAliasListView();
    }

    private void editButton_cb() {
        String selected = aliasListView.getSelectionModel().getSelectedItem();
        if (selected == null)
        	return;
        
        aliasKeyInput.setText(selected);
        aliasValueInput.setText(aliases.get(selected));
        
        quickRollAliases.remove(selected);
        aliases.remove(selected);
    
        saveAliases();
        updateAliasButtons();
        updateAliasListView();
    }
    
    private void shortcutToggle_cb() {
    	String selected = aliasListView.getSelectionModel().getSelectedItem();
        if (selected == null)
        	return;
        if (quickRollAliases.contains(selected)) {
        	quickRollAliases.remove(selected);
        } else {
        	quickRollAliases.add(selected);
        }
        
        if (quickRollAliases.contains(selected)) {
    		shortcutToggle.setText("Remove Shortcut");
    	} else {
    		shortcutToggle.setText("Add Shortcut");
    	}
        
        saveAliases();
        updateAliasButtons();
    }
}
