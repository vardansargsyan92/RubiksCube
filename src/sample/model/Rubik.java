package sample.model;


import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.util.Duration;
import sample.math.Rotations;
import sample.model3d.Model3D;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Rubik {

    private final Group cube = new Group();
    private Map<String, MeshView> mapMeshes = new HashMap<>();
    private final double dimCube;

    private final MeshView faceArrow;
    private final MeshView axisArrow;

    private final ContentModel content;

    public Rotations getRotations() {
        return rotations;
    }

    private final Rotations rotations;
    private final Map<String, Transform> mapTransformsScramble = new HashMap<>();
    private final Map<String, Transform> mapTransformsOriginal = new HashMap<>();

    private final List<Integer> orderOriginal;
    private List<Integer> order;
    private List<Integer> reorder, layer, orderScramble;
    private List<String> sequence = new ArrayList<>();

    private boolean secondRotation = false;
    private final DoubleProperty rotation = new SimpleDoubleProperty(0d);
    private final BooleanProperty onRotation = new SimpleBooleanProperty(false);
    private final BooleanProperty onPreview = new SimpleBooleanProperty(false);
    private final BooleanProperty onScrambling = new SimpleBooleanProperty(false);
    private final BooleanProperty onReplaying = new SimpleBooleanProperty(false);
    private final BooleanProperty hoveredOnClick = new SimpleBooleanProperty(false);
    private final BooleanProperty solved = new SimpleBooleanProperty(false);
    private final ObjectProperty<Cursor> cursor = new SimpleObjectProperty<>(Cursor.DEFAULT);
    private Point3D axis = new Point3D(0, 0, 0);
    private final StringProperty previewFace = new SimpleStringProperty("");
    private final StringProperty lastRotation = new SimpleStringProperty("");
    private final ChangeListener<Number> rotMap;
    private final IntegerProperty count = new SimpleIntegerProperty(-1);
    private final LongProperty timestamp = new SimpleLongProperty(0l);

    private double mouseNewX, mouseNewY, mouseIniX, mouseIniY;

    private MeshView pickedMesh;
    private boolean stopEvents = false;
    private String selFaces = "", myFace = "", myFaceOld = "";
    /*
    r<rMin nothing
    rMin<=r<rClick preview with selected rotation, on release revert preview
    rClick<=r preview with selected rotation, and on release click
    */
    private double radius = 0d;

    private static final int MOUSE_OUT = 0;
    private static final int MOUSE_PRESSED = 1;
    private static final int MOUSE_DRAGGED = 2;
    private static final int MOUSE_RELEASED = 3;
    private final IntegerProperty mouse = new SimpleIntegerProperty(MOUSE_OUT);

    private List<String> appliedMoves = new ArrayList<>();

    public Rubik() {
        /*
        Import Rubik's Cube model and arrows
        */
        Model3D model = new Model3D();
        model.importObj();
        mapMeshes = model.getMapMeshes();
        faceArrow = model.getFaceArrow();
        axisArrow = model.getAxisArrow();
        cube.getChildren().setAll(mapMeshes.values());
        cube.getChildren().addAll(faceArrow);
        cube.getChildren().addAll(axisArrow);
        dimCube = cube.getBoundsInParent().getWidth();
        
        /*
        Create content subscene, add cube, set camera and lights
        */
        content = new ContentModel(920, 520, dimCube);
        content.setContent(cube);
        
        /*
        Initialize 3D array of indexes and a copy of original/solved position
        */
        rotations = new Rotations();
        order = rotations.getCube();
        // System.out.println(rotations.getManhattenHeuristic());
//        System.out.println(""+order.stream().mapToLong(o->mapMeshes.keySet().stream().filter(k->k.contains(o.toString())).count()).sum());

        // save original position
        mapMeshes.forEach((k, v) -> mapTransformsOriginal.put(k, v.getTransforms().get(0)));
        orderOriginal = order.stream().collect(Collectors.toList());

        /*
        Listener to perform an animated face rotation.

        Note: by prepending the rotations it is not possible to create the animation with a timeline
        like this:
        Rotate r=new Rotate(0,axis);
        v.getTransforms().add(r);
        Timeline timeline=new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.Seconds(2),new KeyValue(rotation.angle,90)));
        that takes care of the values: 0<=angle<=90º and transforms the cubies smoothly.
        
        So we create the timeline, and listen to how internally it interpolate rotate.angle and perform
        small rotations between the increments of the angle that the timeline generates:
        */
        rotMap = (ov, angOld, angNew) -> {
            mapMeshes.forEach((k, v) -> {
                layer.stream().filter(l -> k.contains(l.toString()))
                        .findFirst().ifPresent(l -> {
                    Affine a = new Affine(v.getTransforms().get(0));
                    a.prepend(new Rotate(angNew.doubleValue() - angOld.doubleValue(), axis));
                    v.getTransforms().setAll(a);
                });
            });
        };
    }

    // called on toolbars buttons click, on mouse released or while scrambling
    public void rotateFace(final String btRot) {
        // then bPreview=false, so a full rotation is performed
        lastRotation.set("");
        lastRotation.set(btRot);
        rotateFace(btRot, false, false);
    }

    // called from updateArrow to show a preview with posible cancellation
    // or from toolbars buttons click, on mouse released or while scrambling to perform rotation
    private void rotateFace(final String btRot, boolean bPreview, boolean bCancel) {
        if (onRotation.get()) {
            return;
        }
        onRotation.set(true);
        System.out.println((bPreview ? (bCancel ? "Cancelling: " : "Simulating: ") : "Rotating: ") + btRot);
        boolean bFaceArrow = !(btRot.startsWith("X") || btRot.startsWith("Y") || btRot.startsWith("Z"));

        if (bPreview || onScrambling.get() || onReplaying.get() || secondRotation) {
            // rotate cube indexes
            rotations.turn(btRot);
            // get new indexes in terms of blocks numbers from original order
            reorder = rotations.getCube();

            // select cubies to rotate: those in reorder different from order.

            if (!bFaceArrow) {
                layer = reorder.stream().collect(Collectors.toList());
            } else {
                AtomicInteger index = new AtomicInteger();
                layer = order.stream()
                        .filter(o -> !Objects.equals(o, reorder.get(index.getAndIncrement())))
                        .collect(Collectors.toList());
                // add central cubie
                layer.add(0, reorder.get(Utils.getCenter(btRot)));
            }
            // set rotation axis            
            axis = Utils.getAxis(btRot);
        }
        // define rotation
        double angIni = (bPreview || onScrambling.get() || onReplaying.get() || secondRotation ? 0d : 5d) * (btRot.endsWith("i") ? 1d : -1d);
        double angEnd = (bPreview ? 5d : 90d) * (btRot.endsWith("i") ? 1d : -1d);

        rotation.set(angIni);
        rotation.addListener(rotMap);

        // create animation
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(onScrambling.get() || onReplaying.get() ? 200 : (bPreview ? 100 : 600)), e -> {
                    rotation.removeListener(rotMap);
                    secondRotation = false;
                    if (bPreview) {
                        if (bCancel) {
                            previewFace.set("");
                            onPreview.set(false);
                        } else if (mouse.get() == MOUSE_RELEASED) { // early released, rotate back
                            mouse.set(MOUSE_OUT);
                            onRotation.set(false);
                            updateArrow(btRot, false);
                        } else {
                            previewFace.set(btRot);
                        }
                    } else if (!(onScrambling.get() || onReplaying.get())) { // complete rotation
                        mouse.set(MOUSE_OUT);
                        previewFace.set("V");
                        if (!hoveredOnClick.get()) {
                            // lost hover event, trigger it to clean up
                            updateArrow(btRot, false);
                        } else {
                            // at the end of rotation, still hovered, if it's clicked again, it's a second rotation
                            secondRotation = true;
                        }
                        hoveredOnClick.set(false);
                    }
                    onRotation.set(false);
                }, new KeyValue(rotation, angEnd)));
        timeline.playFromStart();

        if (bPreview || onScrambling.get() || onReplaying.get() || secondRotation) {
            // update order with last list, to start all over again in the next rotation
            order = reorder.stream().collect(Collectors.toList());
        }
        // count only face rotations not cube rotations
        if (!bPreview && !onScrambling.get() && bFaceArrow) {
            count.set(count.get() + 1);
            // check if solved
            solved.set(Utils.checkSolution(order));
        }

        appliedMoves.add(btRot);
    }

    // arrow over face or axis to guide user with direction of rotation, complementary with preview
    // when mouse hover on toolbar buttons or on mouse_dragged start preview, else it is cancelled.
    public void updateArrow(String face, boolean hover) {
        boolean bFaceArrow = !(face.startsWith("X") || face.startsWith("Y") || face.startsWith("Z"));
        MeshView arrow = bFaceArrow ? faceArrow : axisArrow;

        if (hover && onRotation.get()) {
            return;
        }
        arrow.getTransforms().clear();
        if (hover) {
            double d0 = arrow.getBoundsInParent().getHeight() / 2d;
            Affine aff = Utils.getAffine(dimCube, d0, bFaceArrow, face);
            arrow.getTransforms().setAll(aff);
            arrow.setMaterial(Utils.getMaterial(face));
            if (previewFace.get().isEmpty()) {
                previewFace.set(face);
                onPreview.set(true);
                rotateFace(face, true, false);
            }
        } else if (previewFace.get().equals(face)) {
            rotateFace(Utils.reverseRotation(face), true, true);
        } else if (previewFace.get().equals("V")) {
            previewFace.set("");
            onPreview.set(false);
        }
    }

    // event for mouse picking a cubie and rotate a suitable face
    public EventHandler<MouseEvent> eventHandler = (MouseEvent event) -> {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED ||
                event.getEventType() == MouseEvent.MOUSE_DRAGGED ||
                event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            //acquire the new Mouse coordinates from the recent event
            mouseNewX = event.getSceneX();
            mouseNewY = -event.getSceneY();
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                // pick mesh
                Node picked = event.getPickResult().getIntersectedNode();
                if (null != picked && picked instanceof MeshView) {
                    mouse.set(MOUSE_PRESSED);
                    cursor.set(Cursor.CLOSED_HAND);
                    // stop camera events on subscene
                    stopEventHandling();
                    stopEvents = true;
                    // selected mesh, part of a 6-meshes cubie
                    pickedMesh = (MeshView) picked;
                    // number of block of cubie 46-72
                    String block = pickedMesh.getId().substring(5, 7);
                    // number of cubie 0-26
                    int indexOf = order.indexOf(new Integer(block));
                    // select face from cubie and two suitable rotations
                    selFaces = Utils.getPickedRotation(indexOf, pickedMesh);
                    // starting point on the scene (X,Y)
                    mouseIniX = mouseNewX;
                    mouseIniY = mouseNewY;
                    myFace = "";
                    myFaceOld = "";
                }
            } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                if (stopEvents && !selFaces.isEmpty()) {
                    mouse.set(MOUSE_DRAGGED);
                    Point3D p = new Point3D(mouseNewX - mouseIniX, mouseNewY - mouseIniY, 0);
                    radius = p.magnitude();

                    if (myFaceOld.isEmpty()) {
                        // when r>rMin it selects one of the two rotations based on x,y movement
                        myFace = Utils.getRightRotation(p, selFaces);
                        if (!myFace.isEmpty() && !onRotation.get()) {
                            // rotation preview
                            updateArrow(myFace, true);
                            myFaceOld = myFace;
                        }
                        if (myFace.isEmpty()) {
                            myFaceOld = "";
                        }
                    }
                    // to cancel preselection, just go back to initial click point
                    if (!myFaceOld.isEmpty() && radius < Utils.radMinimum) {
                        //reset, allowing new face selection
                        myFaceOld = "";
                        // rotation preview cancellation
                        updateArrow(myFace, false);
                        myFace = "";
                    }
                }
            } else if (stopEvents && event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                mouse.set(MOUSE_RELEASED);
                if (!onRotation.get() && !myFace.isEmpty() && !myFaceOld.isEmpty()) {
                    if (Utils.radClick < radius) {
                        // if hand is moved far away full rotation
                        rotateFace(myFace);
                    } else {
                        // else preview cancellation
                        updateArrow(myFace, false);
                    }
                }
                myFace = "";
                myFaceOld = "";
                stopEvents = false;
                resumeEventHandling();
                cursor.set(Cursor.DEFAULT);
            }
        }
    };

    private String last = "V", get = "V";

    public void doScramble() {
        StringBuilder sb = new StringBuilder();
        final List<String> movements = Utils.getMovements();
        IntStream.range(0, 25).boxed().forEach(i -> {
            while (last.substring(0, 1).equals(get.substring(0, 1))) {
                // avoid repeating the same/opposite rotations
                get = movements.get((int) (Math.floor(Math.random() * movements.size())));
            }
            last = get;
            if (get.contains("2")) {
                get = get.substring(0, 1);
                sb.append(get).append(" ");
            }
            sb.append(get).append(" ");
        });

        System.out.println("sb: " + sb.toString());
        doSequence(sb.toString().trim());
    }

    public void doSequence(String list) {
        onScrambling.set(true);
        sequence = Utils.unifyNotation(list);
        
        /*
        This is the way to perform several rotations from a list, waiting till each of
        them ends properly. A listener is added to onRotation, so only when the last rotation finishes
        a new rotation is performed. The end of the list is used to stop the listener, by adding 
        a new listener to the index property. Note the size+1, to allow for the last rotation to end.
        */

        IntegerProperty index = new SimpleIntegerProperty(1);
        ChangeListener<Boolean> lis = (ov, b, b1) -> {
            if (!b1) {
                if (index.get() < sequence.size()) {
                    rotateFace(sequence.get(index.get()));
                } else {
                    // save transforms
                    mapMeshes.forEach((k, v) -> mapTransformsScramble.put(k, v.getTransforms().get(0)));
                    orderScramble = reorder.stream().collect(Collectors.toList());
                }
                index.set(index.get() + 1);
            }
        };
        index.addListener((ov, v, v1) -> {
            if (v1.intValue() == sequence.size() + 1) {
                onScrambling.set(false);
                onRotation.removeListener(lis);
                count.set(-1);
            }
        });
        onRotation.addListener(lis);
        rotateFace(sequence.get(0));
    }

    private String getFaceName(int i) {
        switch (i) {
            case 0:
                return "U";
            case 1:
                return "R";
            case 2:
                return "F";
            case 3:
                return "D";
            case 4:
                return "L";
            case 5:
                return "B";
        }
        return "";
    }

    public String[][] getState(){
        String[][] state = new String[6][9];
        for (int i = 0; i < 6; i++) {
            String color = getFaceName(i);
            for (int j = 0; j < 9; j++) {
                state[i][j] = color;
            }
        }

        for (int i = 0; i < sequence.size(); i++) {
            state = processSingleMove(sequence.get(i), state);
        }

        return state;
    }

    private String[][] copyState(String[][] state) {
        String[][] copy = new String[6][9];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                copy[i][j] = state[i][j];
            }
        }

        return copy;
    }

    private String[][] processSingleMove(String move, String[][] state) {
        String[][] newState = copyState(state);
        switch (move) {
            case "U":
                newState[4][0] = state[2][0];
                newState[4][1] = state[2][1];
                newState[4][2] = state[2][2];

                newState[2][0] = state[1][0];
                newState[2][1] = state[1][1];
                newState[2][2] = state[1][2];

                newState[1][0] = state[5][0];
                newState[1][1] = state[5][1];
                newState[1][2] = state[5][2];

                newState[5][0] = state[4][0];
                newState[5][1] = state[4][1];
                newState[5][2] = state[4][2];

                newState[0][0] = state[0][6];
                newState[0][1] = state[0][3];
                newState[0][2] = state[0][0];
                newState[0][3] = state[0][7];
                newState[0][4] = state[0][4];
                newState[0][5] = state[0][1];
                newState[0][6] = state[0][8];
                newState[0][7] = state[0][5];
                newState[0][8] = state[0][2];
                break;
            case "Ui":
                newState[4][0] = state[5][0];
                newState[4][1] = state[5][1];
                newState[4][2] = state[5][2];

                newState[2][0] = state[4][0];
                newState[2][1] = state[4][1];
                newState[2][2] = state[4][2];

                newState[1][0] = state[2][0];
                newState[1][1] = state[2][1];
                newState[1][2] = state[2][2];

                newState[5][0] = state[1][0];
                newState[5][1] = state[1][1];
                newState[5][2] = state[1][2];

                newState[0][0] = state[0][2];
                newState[0][1] = state[0][5];
                newState[0][2] = state[0][8];
                newState[0][3] = state[0][1];
                newState[0][4] = state[0][4];
                newState[0][5] = state[0][7];
                newState[0][6] = state[0][0];
                newState[0][7] = state[0][3];
                newState[0][8] = state[0][6];
                break;

            case "D":
                newState[4][6] = state[5][6];
                newState[4][7] = state[5][7];
                newState[4][8] = state[5][8];

                newState[2][6] = state[4][6];
                newState[2][7] = state[4][7];
                newState[2][8] = state[4][8];

                newState[1][6] = state[2][6];
                newState[1][7] = state[2][7];
                newState[1][8] = state[2][8];

                newState[5][6] = state[1][6];
                newState[5][7] = state[1][7];
                newState[5][8] = state[1][8];

                newState[3][0] = state[3][6];
                newState[3][1] = state[3][3];
                newState[3][2] = state[3][0];
                newState[3][3] = state[3][7];
                newState[3][4] = state[3][4];
                newState[3][5] = state[3][1];
                newState[3][6] = state[3][8];
                newState[3][7] = state[3][5];
                newState[3][8] = state[3][2];
                break;
            case "Di":
                newState[4][6] = state[2][6];
                newState[4][7] = state[2][7];
                newState[4][8] = state[2][8];

                newState[2][6] = state[1][6];
                newState[2][7] = state[1][7];
                newState[2][8] = state[1][8];

                newState[1][6] = state[5][6];
                newState[1][7] = state[5][7];
                newState[1][8] = state[5][8];

                newState[5][6] = state[4][6];
                newState[5][7] = state[4][7];
                newState[5][8] = state[4][8];

                newState[3][0] = state[3][2];
                newState[3][1] = state[3][5];
                newState[3][2] = state[3][8];
                newState[3][3] = state[3][1];
                newState[3][4] = state[3][4];
                newState[3][5] = state[3][7];
                newState[3][6] = state[3][0];
                newState[3][7] = state[3][3];
                newState[3][8] = state[3][6];
                break;

            case "F":
                newState[0][6] = state[4][8];
                newState[0][7] = state[4][5];
                newState[0][8] = state[4][2];

                newState[4][2] = state[3][0];
                newState[4][5] = state[3][1];
                newState[4][8] = state[3][2];

                newState[3][0] = state[1][6];
                newState[3][1] = state[1][3];
                newState[3][2] = state[1][0];

                newState[1][0] = state[0][6];
                newState[1][3] = state[0][7];
                newState[1][6] = state[0][8];

                newState[2][0] = state[2][6];
                newState[2][1] = state[2][3];
                newState[2][2] = state[2][0];
                newState[2][3] = state[2][7];
                newState[2][4] = state[2][4];
                newState[2][5] = state[2][1];
                newState[2][6] = state[2][8];
                newState[2][7] = state[2][5];
                newState[2][8] = state[2][2];
                break;
            case "Fi":
                newState[0][6] = state[1][0];
                newState[0][7] = state[1][3];
                newState[0][8] = state[1][6];

                newState[4][2] = state[0][8];
                newState[4][5] = state[0][7];
                newState[4][8] = state[0][6];

                newState[3][0] = state[4][2];
                newState[3][1] = state[4][5];
                newState[3][2] = state[4][8];

                newState[1][0] = state[3][2];
                newState[1][3] = state[3][1];
                newState[1][6] = state[3][0];

                newState[2][0] = state[2][2];
                newState[2][1] = state[2][5];
                newState[2][2] = state[2][8];
                newState[2][3] = state[2][1];
                newState[2][4] = state[2][4];
                newState[2][5] = state[2][7];
                newState[2][6] = state[2][0];
                newState[2][7] = state[2][3];
                newState[2][8] = state[2][6];
                break;

            case "B":
                newState[0][2] = state[1][8];
                newState[0][1] = state[1][5];
                newState[0][0] = state[1][2];

                newState[4][0] = state[0][2];
                newState[4][3] = state[0][1];
                newState[4][6] = state[0][0];

                newState[3][8] = state[4][6];
                newState[3][7] = state[4][3];
                newState[3][6] = state[4][0];

                newState[1][2] = state[3][8];
                newState[1][5] = state[3][7];
                newState[1][8] = state[3][6];

                newState[5][0] = state[5][6];
                newState[5][1] = state[5][3];
                newState[5][2] = state[5][0];
                newState[5][3] = state[5][7];
                newState[5][4] = state[5][4];
                newState[5][5] = state[5][1];
                newState[5][6] = state[5][8];
                newState[5][7] = state[5][5];
                newState[5][8] = state[5][2];
                break;
            case "Bi":
                newState[0][2] = state[4][0];
                newState[0][1] = state[4][3];
                newState[0][0] = state[4][6];

                newState[4][0] = state[3][6];
                newState[4][3] = state[3][7];
                newState[4][6] = state[3][8];

                newState[3][8] = state[1][2];
                newState[3][7] = state[1][5];
                newState[3][6] = state[1][8];

                newState[1][2] = state[0][0];
                newState[1][5] = state[0][1];
                newState[1][8] = state[0][2];

                newState[5][0] = state[5][2];
                newState[5][1] = state[5][5];
                newState[5][2] = state[5][8];
                newState[5][3] = state[5][1];
                newState[5][4] = state[5][4];
                newState[5][5] = state[5][7];
                newState[5][6] = state[5][0];
                newState[5][7] = state[5][3];
                newState[5][8] = state[5][6];
                break;

            case "L":
                newState[0][0] = state[5][8];
                newState[0][3] = state[5][5];
                newState[0][6] = state[5][2];

                newState[2][0] = state[0][0];
                newState[2][3] = state[0][3];
                newState[2][6] = state[0][6];

                newState[3][0] = state[2][0];
                newState[3][3] = state[2][3];
                newState[3][6] = state[2][6];

                newState[5][2] = state[3][6];
                newState[5][5] = state[3][3];
                newState[5][8] = state[3][0];

                newState[4][0] = state[4][6];
                newState[4][1] = state[4][3];
                newState[4][2] = state[4][0];
                newState[4][3] = state[4][7];
                newState[4][4] = state[4][4];
                newState[4][5] = state[4][1];
                newState[4][6] = state[4][8];
                newState[4][7] = state[4][5];
                newState[4][8] = state[4][2];
                break;
            case "Li":
                newState[0][0] = state[2][0];
                newState[0][3] = state[2][3];
                newState[0][6] = state[2][6];

                newState[2][0] = state[3][0];
                newState[2][3] = state[3][3];
                newState[2][6] = state[3][6];

                newState[3][6] = state[5][2];
                newState[3][3] = state[5][5];
                newState[3][0] = state[5][8];

                newState[5][2] = state[0][6];
                newState[5][5] = state[0][3];
                newState[5][8] = state[0][0];

                newState[4][0] = state[4][2];
                newState[4][1] = state[4][5];
                newState[4][2] = state[4][8];
                newState[4][3] = state[4][1];
                newState[4][4] = state[4][4];
                newState[4][5] = state[4][7];
                newState[4][6] = state[4][0];
                newState[4][7] = state[4][3];
                newState[4][8] = state[4][6];
                break;

            case "R":
                newState[0][8] = state[2][8];
                newState[0][5] = state[2][5];
                newState[0][2] = state[2][2];

                newState[2][2] = state[3][2];
                newState[2][5] = state[3][5];
                newState[2][8] = state[3][8];

                newState[3][2] = state[5][6];
                newState[3][5] = state[5][3];
                newState[3][8] = state[5][0];

                newState[5][0] = state[0][8];
                newState[5][3] = state[0][5];
                newState[5][6] = state[0][2];

                newState[1][0] = state[1][6];
                newState[1][1] = state[1][3];
                newState[1][2] = state[1][0];
                newState[1][3] = state[1][7];
                newState[1][4] = state[1][4];
                newState[1][5] = state[1][1];
                newState[1][6] = state[1][8];
                newState[1][7] = state[1][5];
                newState[1][8] = state[1][2];
                break;
            case "Ri":
                newState[0][8] = state[5][0];
                newState[0][5] = state[5][3];
                newState[0][2] = state[5][6];

                newState[2][2] = state[0][2];
                newState[2][5] = state[0][5];
                newState[2][8] = state[0][8];

                newState[3][2] = state[2][2];
                newState[3][5] = state[2][5];
                newState[3][8] = state[2][8];

                newState[5][0] = state[3][8];
                newState[5][3] = state[3][5];
                newState[5][6] = state[3][2];

                newState[1][0] = state[1][2];
                newState[1][1] = state[1][5];
                newState[1][2] = state[1][8];
                newState[1][3] = state[1][1];
                newState[1][4] = state[1][4];
                newState[1][5] = state[1][7];
                newState[1][6] = state[1][0];
                newState[1][7] = state[1][3];
                newState[1][8] = state[1][6];
                break;
        }
        return newState;
    }

    public void doReplay(List<Move> moves) {
        if (moves.isEmpty()) {
            return;
        }
        content.resetCam();
        //restore scramble
        if (mapTransformsScramble.size() > 0) {
            System.out.println("Restoring scramble");
            mapMeshes.forEach((k, v) -> v.getTransforms().setAll(mapTransformsScramble.get(k)));
            order = orderScramble.stream().collect(Collectors.toList());
            rotations.setCube(order);
            count.set(-1);
        } else {
            // restore original
            doReset();
        }
        onReplaying.set(true);

        IntegerProperty index = new SimpleIntegerProperty(1);
        ChangeListener<Boolean> lis = (ov, v, v1) -> {
            if (!v1 && moves.size() > 1) {
                if (index.get() < moves.size()) {
                    timestamp.set(moves.get(index.get()).getTimestamp());
                    rotateFace(moves.get(index.get()).getFace());
                }
                index.set(index.get() + 1);
            }
        };
        index.addListener((ov, v, v1) -> {
            if (v1.intValue() == moves.size() + 1) {
                onReplaying.set(false);
                onRotation.removeListener(lis);
            }
        });
        onRotation.addListener(lis);
        timestamp.set(moves.get(0).getTimestamp());
        rotateFace(moves.get(0).getFace());
    }

    public void doReset() {
        System.out.println("Reset!");
        content.resetCam();
        mapMeshes.forEach((k, v) -> v.getTransforms().setAll(mapTransformsOriginal.get(k)));
        order = orderOriginal.stream().collect(Collectors.toList());
        rotations.setCube(order);
        count.set(-1);
        appliedMoves = new ArrayList<>();
    }

    public SubScene getSubScene() {
        return content.getSubScene();
    }

    public BooleanProperty isSolved() {
        return solved;
    }

    public BooleanProperty isOnRotation() {
        return onRotation;
    }

    public BooleanProperty isOnPreview() {
        return onPreview;
    }

    public BooleanProperty isOnScrambling() {
        return onScrambling;
    }

    public BooleanProperty isOnReplaying() {
        return onReplaying;
    }

    public BooleanProperty isHoveredOnClick() {
        return hoveredOnClick;
    }

    public IntegerProperty getCount() {
        return count;
    }

    public LongProperty getTimestamp() {
        return timestamp;
    }

    public StringProperty getPreviewFace() {
        return previewFace;
    }

    public StringProperty getLastRotation() {
        return lastRotation;
    }

    public ObjectProperty<Cursor> getCursor() {
        return cursor;
    }

    public void stopEventHandling() {
        content.stopEventHandling();
    }

    public void resumeEventHandling() {
        content.resumeEventHandling();
    }
}
