package gui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import notation_parser.DieExpression;
import notation_parser.NotationParser;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import java.io.*;
import java.util.*;

import dice_roller.DiceRoller;
import dice_roller.ResultSet;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;


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
        dicePane.setBackground(new Background(new BackgroundFill(Color.DARKGREEN, null, null)));
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
    	
    	Rectangle rect = new Rectangle(0, 0, 600, 600);
        //rect.setArcHeight(50);
        //rect.setArcWidth(50);
        rect.setFill(Color.DARKGREEN);
        dicePane.getChildren().add(rect);
    
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), rect);
        fadeOut.setFromValue(0.0);
        fadeOut.setToValue(1.0);
        //ft.setCycleCount(4);
        //ft.setAutoReverse(true);
        
        fadeOut.setOnFinished(_ -> {
        
		    dicePane.getChildren().clear();
		    
		    for (int i = 0; i < result.results().size(); i++) {
		    	// Value of die
		    	Integer finalValue = result.results().get(i);
		    	
		    	int dieSize = result.expression().dice().get(i).getValue();
		    	
		        // Generate object containing nodes that make up one die
		        DieNode die = new DieNode(dieSize, DICE_SIZE);
		        // Add the group node (all elements) to pane
		        dicePane.getChildren().addFirst(die.group);
		        // Create translation animation for group node
		        PathTransition die_move = randomDiePath(die.group, i, 2000);
		        // Create number scrolling animation for die number
		        int time = 3000 + randInt(400)-200;
		        int scrollTime = time;
		        if (dieSize < 3) scrollTime = 10;
		        Timeline textScroll = generateRandomSequenceTimeline(die.text, finalValue, scrollTime+200);
		        // Create spin animations for die and shadow, separately
		        int offset = randInt(30)-15;
		        RotateTransition die_spin = spinNode(die.die,time, offset);
		        RotateTransition shadow_spin = spinNode(die.shadow, time, offset);
		        sequence.getChildren().add(new ParallelTransition(die_move, die_spin, shadow_spin, textScroll));
		    }  
		    sequence.play();
        });
        
        fadeOut.play();
    }
    
    
    /* ANIMATION HELPER FUNCTIONS */
    
    private class DieNode {
    	public Text text;
    	public Node group;
    	public Node die;
    	public Node shadow;
    	private int radius;
    	DieNode(int sides, int radius) {
    		
    		this.radius = radius;
    		
    		// Die
    		Polygon polygon;
    		polygon = createDiePolygon(sides, radius);
    		
    		polygon.setFill(randomDiceColor());
            polygon.setStroke(Color.BLACK);
            polygon.setStrokeWidth(2);
    		// Die Text
    		this.text = new Text("?");
    		text.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
            this.text.setFont(Font.font(22));
            this.text.setFill(Color.WHITE);
            this.text.setOpacity(.75);
            text.layoutBoundsProperty().addListener((_, _, newBounds) -> boundsChange_cb(newBounds));
            // Die Group
            this.die = new Group(generateBorder(), polygon, text);
            
            // Shadow Group
            polygon = createDiePolygon(sides, radius);
            polygon.setFill(Color.BLACK);
            polygon.setOpacity(.5);
            this.shadow = new Group(generateBorder(), polygon);
            shadow.setTranslateX(-5);
            shadow.setTranslateY(5);
            
            // Die + Shadow
            this.group = new Group(shadow, this.die);
            
            this.group.setTranslateX(-radius);
            this.group.setTranslateY(-radius);
    	}
    	
    	private Rectangle generateBorder() {
    		Rectangle border = new Rectangle();
            border.setWidth(3 * this.radius);
            border.setHeight(3 * this.radius);
            border.setX(-1.5 * this.radius);
            border.setY(-1.5 * this.radius);
            border.setFill(Color.TRANSPARENT);
            return border;
    	}
    	
    	private void boundsChange_cb(Bounds newBounds) {
    		double textWidth = newBounds.getWidth();
            double textHeight = newBounds.getHeight();
            text.setX((-textWidth/2));
            text.setY(0+textHeight/4); 
    	}
    }
    
    Color randomDiceColor() {
    	Color[] colors = {Color.DARKORANGE, Color.DARKBLUE, Color.DARKRED, Color.DARKVIOLET};
    	
    	Random rand = new Random();
    	
    	double index = Math.floor((1.0 - rand.nextDouble()) * 4);
    	return colors[(int) index];
    }
    
    private Polygon createDiePolygon(int sides, int radius) {
    	Polygon polygon;
    	
    	if (sides < 3)
    		return createStarPolygon(radius);
    	
    	
    	
    	if      (sides ==  4) sides = 3;
		else if (sides ==  6) sides = 4;
		else if (sides ==  6) sides = 6;
		else if (sides == 20) sides = 6;
    	polygon = createRegularPolygon(sides, radius);
    	
    	
    	return polygon;
    }
    
    private Polygon createRegularPolygon(int sides, int radius) {
    	return createRegularPolygon(sides, radius, false);
    }
    
    private Polygon createRegularPolygon(int sides, int radius, boolean offset) {
        Polygon polygon = new Polygon();
        List<Double> points = polygon.getPoints();
       
        
        double angleStep = Math.PI*2 / sides;
        double curAngle = -Math.PI/2;
   
        if (sides % 2 == 0) {
        	curAngle -= angleStep/2;
        }
        if (offset) curAngle += angleStep/2;
        
        for (int i = 0; i < sides; i++) {
        	points.add(Math.cos(curAngle)*radius);
        	points.add(Math.sin(curAngle)*radius);
        	curAngle += angleStep;
        }
        return polygon;
    }
    
    private Polygon createStarPolygon(int radius) {
    	final int POINTS = 5;
    	Polygon polygon = new Polygon();
        List<Double> points = polygon.getPoints();
       
        double angleStep = Math.PI*2 / (POINTS*2);
        double curAngle = -Math.PI/2;
        
        int innerRad = radius / 2;
        
        for (int i = 0; i < POINTS; i++) {
        	points.add(Math.cos(curAngle)*radius);
        	points.add(Math.sin(curAngle)*radius);
        	curAngle += angleStep;
        	points.add(Math.cos(curAngle)*innerRad);
        	points.add(Math.sin(curAngle)*innerRad);
        	curAngle += angleStep;
        }
        return polygon;
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
    	
    	Random random = new Random();
    	
    	x += random.nextDouble()*20 -10;
    	y += random.nextDouble()*20 -10;
    	
    	Pos pos = new Pos(x, y);
    	return pos;
    }
    
    PathTransition randomDiePath(Node node, int i, int duration) {
    	Random random = new Random();
    	
    	double paneWidth  = dicePane.getLayoutBounds().getWidth();
    	double paneHeight = dicePane.getLayoutBounds().getHeight();
    	
    	double angle = random.nextDouble() * Math.PI*2;
    	double startX = Math.cos(angle) * (paneWidth*1.2) + (paneWidth/2);
        double startY = Math.sin(angle) * (paneHeight*1.2) + (paneHeight/2);
        
        Pos dest = gridPosition(i, 75, 400, 500);
        
        double randTimeOffset = random.nextDouble() * 600 - 300;
        
        Path path = new Path(new MoveTo(startX, startY), new LineTo(dest.x, dest.y));
        PathTransition trans = new PathTransition(Duration.millis(duration + randTimeOffset), path, node);
        
        return trans;
    }
    
    RotateTransition spinNode(Node node, int duration, int offset) {
    	RotateTransition rotate = new RotateTransition(Duration.millis(duration), node);
    	rotate.setByAngle(720* (duration/1000) + offset);
    	return rotate;
    }
    
    Timeline generateRandomSequenceTimeline(Text node, int finalValue, int duration) {
    	Random random = new Random();
    	Timeline timeline = new Timeline();
    	int num_frames = 15 * (duration/1000);
    	if (num_frames == 0) num_frames = 1;
    	int frame_delta_millis = duration / num_frames;
    	
    	for (int j = num_frames; j >= 0; j--) {
    		int rollValue;
    		if (j == 0) rollValue = finalValue;
    		else        rollValue = random.nextInt(20) + 1;
    		
    		EventHandler<ActionEvent> event = (_) -> {node.setText(String.valueOf(rollValue));};
    		timeline.getKeyFrames().add(new KeyFrame(Duration.millis((num_frames-j) * frame_delta_millis), event));
    	}
    	return timeline;
    }
    
    private void clipChildren(Region region) {
        final Rectangle clipPane = new Rectangle();
        region.setClip(clipPane);
        // In case we want to make a re-sizable pane we need to update our clipPane dimensions
        region.layoutBoundsProperty().addListener((_, _, newValue) -> {
            clipPane.setWidth(newValue.getWidth());
            clipPane.setHeight(newValue.getHeight());
        });
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
    
    
    /* MISC */
    
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
    
    private int randInt(int max) {
    	Random rand = new Random();
    	return (int) Math.floor((1.0 - rand.nextDouble()) * max);
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
        quickRollAliases.remove(key);
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
