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
import sample.Kociemba.KociembaSearch;
import sample.model.Move;
import sample.model.Moves;
import sample.model.Rubik;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
            // see the 2D version shared at https://github.com/seda-man/CubeRube-1
        });

        /**
         * Seda
         */
        Button kociembaSearchButton = new Button("Kociemba");
        kociembaSearchButton.setOnAction(e -> {
//			rubik.doSequence("D D Ri D D R R L Di L L R B B R D D U B B U Fi D F Li Ui Fi L Di L U U Di");
			String[][] state = rubik.getState();
			StringBuffer s = new StringBuffer(54);

			for (int i = 0; i < 54; i++)
				s.insert(i, 'B');

			for (int i = 0; i < state.length; i++) {
				for (int j =0; j < state[i].length; j++) {
					s.setCharAt(9 * i + j, state[i][j].charAt(0));
				}
			}
			String stateForKociemba = s.toString();

			KociembaSearch search = new KociembaSearch();
			String result = search.solution(stateForKociemba);
			while (result.startsWith("Error 8")) {
				result = search.next();
			}

			rubik.doSequence(result);

            System.out.println("Solution sequence");
            System.out.println(String.format("%s\n", result));
        });


        /**
         * Tigran
         */
        Button idaSearchButton = new Button("IDA*");
        idaSearchButton.setOnAction(e -> {
            //TODO IDA* search
            //TODO test
            // will not work, couldn't solve the issues
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
