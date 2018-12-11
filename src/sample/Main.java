package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import sample.Basic.Cube;
import sample.model.Move;
import sample.model.Moves;
import sample.model.Rubik;
import sample.model.Utils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Main extends Application {

    private final BorderPane pane = new BorderPane();
    private Rubik rubik;

    private LocalTime time = LocalTime.now();
    private Timeline timer;
    private final StringProperty clock = new SimpleStringProperty("00:00:00");
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private Button btnHover;

    private Moves moves = new Moves();

    @Override
    public void start(Stage stage) {
        /*
        Import Rubik's Cube
        */
        rubik = new Rubik();

        /*
        Toolbars with buttons
        */
        ToolBar tbTop = new ToolBar(new Button("U"), new Button("Ui"), new Button("F"),
                new Button("Fi"), new Separator(), new Button("Y"),
                new Button("Yi"), new Button("Z"), new Button("Zi"));
        Button bReset = new Button("Restart");
        bReset.setOnAction(e -> {
            if (moves.getNumMoves() > 0) {
                moves.getMoves().clear();
                rubik.doReset();
            }

        });
        Button bSc = new Button("Scramble");
        bSc.setOnAction(e -> {
            {
                rubik.doReset();
                doScramble();
            }
        });

        /**
         * Amalia
         */
        Button basicSearchButton = new Button("Basic");
        basicSearchButton.setOnAction(e -> {
            //TODO basic search
            //TODO test
            rubik.doSequence("X");

//            String str = "D D F R Li B Ui";
            String str = "F F D L R Li U Bi";
            rubik.runSequence(str);

            Cube c = new Cube();
//            str = "F F D L R Li U Bi";
            c.scramble(str);

            String sunflower = c.makeSunflower();
            System.out.println(1);
            System.out.println(sunflower);
            rubik.runSequence(sunflower);

            String whiteCross = c.makeWhiteCross();
            System.out.println(2);
            System.out.println(whiteCross);
            rubik.runSequence(whiteCross);

            String whiteCorners = c.finishWhiteLayer();
            System.out.println(3);
            System.out.println(whiteCorners);
            rubik.runSequence(whiteCorners);

            String edges = c.insertAllEdges();
            System.out.println(4);
            System.out.println(edges);
            rubik.runSequence(edges);

            String yellowCross = c.makeYellowCross();
            System.out.println(5);
            System.out.println(yellowCross);
            rubik.runSequence(yellowCross);

            String ool = c.orientLastLayer();
            System.out.println(6);
            System.out.println(ool);
            rubik.runSequence(ool);

            String pll = c.permuteLastLayer();
            System.out.println(7);
            System.out.println(pll);
            rubik.runSequence(pll);

//            rubik.doSequence("Y Yi");
        });

        /**
         * Seda
         */
        Button kociembaSearchButton = new Button("Kociemba");
        kociembaSearchButton.setOnAction(e -> {
            //TODO Kociemba search method
            List<Integer> flattenRotations = rubik.getRotations().getCube();
            //TODO test
            Utils.getMovements();
//            String str = "D D Ri D D R R L Di L L R B B R D D U B B U Fi D F Li Ui Fi L Di L U U Di";
//            rubik.doSequence(str);
        });


        /**
         * Tigran
         */
        Button idaSearchButton = new Button("IDA*");
        idaSearchButton.setOnAction(e -> {
            //TODO IDA* search
            //TODO test
            String str = "D D Ri D D R R L Di L L R B B R D D U B B U Fi D F Li Ui Fi L Di L U U Di";
            rubik.doSequence(str);
        });


        ChangeListener<Number> clockLis =
                (ov, l, l1) -> clock.set(LocalTime.ofNanoOfDay(l1.longValue()).format(fmt));


        rubik.isOnReplaying().addListener((ov, b, b1) -> {
            if (b && !b1) {
                rubik.getTimestamp().removeListener(clockLis);
                if (!rubik.isSolved().get()) {
                    timer.play();
                }
            }
        });


        tbTop.getItems().addAll(new Separator(), bReset, bSc,
                new Separator(), basicSearchButton,
                new Separator(), kociembaSearchButton,
                new Separator(), idaSearchButton);
        pane.setTop(tbTop);

        ToolBar tbBottom = new ToolBar(new Button("B"), new Button("Bi"), new Button("D"),
                new Button("Di"), new Button("E"), new Button("Ei"));
        Label lMov = new Label();
        rubik.getCount().addListener((ov, v, v1) -> {
            lMov.setText("Movements: " + (v1.intValue() + 1));
        });
        rubik.getLastRotation().addListener((ov, v, v1) -> {
            if (!rubik.isOnReplaying().get() && !v1.isEmpty()) {
                moves.addMove(new Move(v1, LocalTime.now().minusNanos(time.toNanoOfDay()).toNanoOfDay()));
            }
        });
        Label lTime = new Label();
        lTime.textProperty().bind(clock);
        tbBottom.getItems().addAll(new Separator(), lMov, new Separator(), lTime);
        pane.setBottom(tbBottom);

        ToolBar tbRight = new ToolBar(new Button("R"), new Button("Ri"), new Separator(),
                new Button("X"), new Button("Xi"));
        tbRight.setOrientation(Orientation.VERTICAL);
        pane.setRight(tbRight);
        ToolBar tbLeft = new ToolBar(new Button("L"), new Button("Li"), new Button("M"),
                new Button("Mi"), new Button("S"), new Button("Si"));
        tbLeft.setOrientation(Orientation.VERTICAL);
        pane.setLeft(tbLeft);

        pane.setCenter(rubik.getSubScene());

        pane.getChildren().stream()
                .filter(withToolbars())
                .forEach(tb -> {
                    ((ToolBar) tb).getItems().stream()
                            .filter(withMoveButtons())
                            .forEach(n -> {
                                Button b = (Button) n;
                                b.setOnAction(e -> rotateFace(b.getText()));
                                b.hoverProperty().addListener((ov, b0, b1) -> updateArrow(b.getText(), b1));
                            });
                });

        rubik.isOnRotation().addListener((b0, b1, b2) -> {
            if (b2) {
                // store the button hovered
                pane.getChildren().stream().filter(withToolbars())
                        .forEach(tb -> {
                            ((ToolBar) tb).getItems().stream().filter(withMoveButtons().and(isButtonHovered()))
                                    .findFirst().ifPresent(n -> btnHover = (Button) n);
                        });
            } else {
                if (rubik.getPreviewFace().get().isEmpty()) {
                    btnHover = null;
                } else {
                    // after rotation
                    if (btnHover != null && !btnHover.isHover()) {
                        updateArrow(btnHover.getText(), false);
                    }
                }
            }
        });

        // disable rest of buttons to avoid new hover events
        rubik.isOnPreview().addListener((b0, b1, b2) -> {
            final String face = rubik.getPreviewFace().get();
            pane.getChildren().stream().filter(withToolbars())
                    .forEach(tb -> {
                        ((ToolBar) tb).getItems().stream().filter(withMoveButtons())
                                .forEach((b) -> {
                                    b.setDisable(!(!b2 || face.isEmpty() || face.equals("V") || face.equals(((Button) b).getText())));
                                });
                    });
        });


        timer = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            clock.set(LocalTime.now().minusNanos(time.toNanoOfDay()).format(fmt));
        }), new KeyFrame(Duration.seconds(1)));
        timer.setCycleCount(Animation.INDEFINITE);
        rubik.isSolved().addListener((ov, b, b1) -> {
            if (b1) {
                timer.stop();
                moves.setTimePlay(LocalTime.now().minusNanos(time.toNanoOfDay()).toNanoOfDay());
                System.out.println(moves);
            }
        });

        final Scene scene = new Scene(pane, 1000, 600, true);
        scene.addEventHandler(MouseEvent.ANY, rubik.eventHandler);
        scene.cursorProperty().bind(rubik.getCursor());
        scene.setFill(Color.ALICEBLUE);
        stage.setTitle("Rubik's Cube - JavaFX3D");
        stage.setScene(scene);
        stage.show();
    }


//    public List<String> solveWithKociemba(char[][] facelets) {
//        rubik.getState();
//    }

    // called on button click
    private void rotateFace(final String btRot) {
        pane.getChildren().stream()
                .filter(withToolbars())
                .forEach(tb -> {
                    ((ToolBar) tb).getItems().stream()
                            .filter(withMoveButtons().and(withButtonTextName(btRot)))
                            .findFirst().ifPresent(n -> rubik.isHoveredOnClick().set(n.isHover()));
                });
        rubik.rotateFace(btRot);
    }

    // called on button hover
    private void updateArrow(String face, boolean hover) {
        rubik.updateArrow(face, hover);
    }

    // called from button Scramble
    private void doScramble() {
        pane.getChildren().stream().filter(withToolbars()).forEach(setDisable(true));
        rubik.doScramble();
        rubik.isOnScrambling().addListener((ov, v, v1) -> {
            if (v && !v1) {
                System.out.println("scrambled!");
                pane.getChildren().stream().filter(withToolbars()).forEach(setDisable(false));
                //moves = new Moves();
                time = LocalTime.now();
                timer.playFromStart();
            }
        });
    }


    // some predicates for readability
    private static Predicate<Node> withToolbars() {
        return n -> (n instanceof ToolBar);
    }

    private static Predicate<Node> withMoveButtons() {
        return n -> (n instanceof Button) && ((Button) n).getText().length() <= 2;
    }

    private static Predicate<Node> withButtonTextName(String text) {
        return n -> ((Button) n).getText().equals(text);
    }

    private static Predicate<Node> isButtonHovered() {
        return Node::isHover;
    }

    private static Consumer<Node> setDisable(boolean disable) {
        return n -> n.setDisable(disable);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
