package test;

import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class Main extends Application {
    private static final int DICE_SIZE = 80;
    private final Random random = new Random();
    private final Pane dicePane = new Pane();
    private final List<Polygon> diceShapes = new ArrayList<>();
    private final List<Text> diceTexts = new ArrayList<>();
    private static final int NUM_DICE = 3; // Change this to set the number of dice

    @Override
    public void start(Stage stage) {
        VBox controls = new VBox();
        Button rollButton = new Button("Roll Dice");
        rollButton.setOnAction(event -> rollDice());
        controls.getChildren().add(rollButton);
        
        for (int i = 0; i < NUM_DICE; i++) {
            Polygon d20Shape = createD20Shape();
            Text numberText = new Text("20");
            numberText.setFont(Font.font(30));
            numberText.setFill(Color.WHITE);
            
            diceShapes.add(d20Shape);
            diceTexts.add(numberText);
            dicePane.getChildren().addAll(d20Shape, numberText);
        }
        
        dicePane.setPrefSize(400, 400);
        Scene scene = new Scene(new BorderPane(dicePane, controls, null, null, null), 600, 600);
        stage.setTitle("1D20 Roller");
        stage.setScene(scene);
        stage.show();
    }

    private void rollDice() {
        SequentialTransition sequence = new SequentialTransition();
        
        for (int i = 0; i < NUM_DICE; i++) {
            Polygon d20Shape = diceShapes.get(i);
            Text numberText = diceTexts.get(i);
            int finalRoll = random.nextInt(20) + 1;
            numberText.setText("?");
            
            double startX = random.nextDouble() * 400;
            double startY = random.nextDouble() * 400;
            d20Shape.setTranslateX(startX);
            d20Shape.setTranslateY(startY);
            numberText.setTranslateX(startX + 25);
            numberText.setTranslateY(startY + 25);
            
            RotateTransition rotate = new RotateTransition(Duration.seconds(1), d20Shape);
            rotate.setByAngle(720);
            
            Path path = new Path(new MoveTo(startX, startY), new LineTo(200, 200));
            PathTransition move = new PathTransition(Duration.seconds(1), path, d20Shape);
            
            Timeline timeline = new Timeline();
            for (int j = 0; j < 15; j++) {
                int rollValue = random.nextInt(20) + 1;
                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(j * 50), e -> numberText.setText(String.valueOf(rollValue))));
            }
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> numberText.setText(String.valueOf(finalRoll))));
            
            sequence.getChildren().add(new SequentialTransition(move, rotate, timeline));
        }
        
        sequence.play();
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

    public static void main(String[] args) {
        launch();
    }
}

