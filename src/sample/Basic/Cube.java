package sample.Basic;

import java.awt.Color;

public class Cube {

	//Stores the state of the cube as an object of 26 cubies
	private Cubie[][][] cubiePos = new Cubie[3][3][3];

	/**
	 * Constructs the Cube object by instantiating a Cubie for each position in three-dimensional space
	 * When the cube is held with Yellow facing up and Green facing front, x increases going from left to right,
	 * y increases going from the front to the back, and z increases going from the top to the bottom.
	 * x, y, and z are zero-indexed.
	 * The core of the cube is not an actual cubie, but is instantiated as one to prevent runtime error
	 */
	public Cube() {
		//Up, Front Row
		cubiePos[0][0][0] = new Cubie(0,0,0,
				new CubieColor[]{ new CubieColor('Y','U') , new CubieColor('R','L'), new CubieColor('G','F')}, true, false);
		cubiePos[1][0][0] = new Cubie(1,0,0,
				new CubieColor[]{ new CubieColor('Y','U') , new CubieColor('G','F')}, false, true);
		cubiePos[2][0][0] = new Cubie(2,0,0,
				new CubieColor[]{ new CubieColor('Y','U') , new CubieColor('G','F'), new CubieColor('O','R')}, true, false);

		//Front, E Row
		cubiePos[0][0][1] = new Cubie(0,0,1,
				new CubieColor[]{ new CubieColor('R','L'), new CubieColor('G','F')}, false, true);
		cubiePos[1][0][1] = new Cubie(1,0,1,
				new CubieColor[]{ new CubieColor('G','F')}, false, false);
		cubiePos[2][0][1] = new Cubie(2,0,1,
				new CubieColor[]{ new CubieColor('G','F'), new CubieColor('O','R')}, false, true);

		//Down, Front Row
		cubiePos[0][0][2] = new Cubie(0,0,2,
				new CubieColor[]{ new CubieColor('W','D') , new CubieColor('R','L'), new CubieColor('G','F')}, true, false);
		cubiePos[1][0][2] = new Cubie(1,0,2,
				new CubieColor[]{ new CubieColor('W','D') , new CubieColor('G','F')}, false, true);
		cubiePos[2][0][2] = new Cubie(2,0,2,
				new CubieColor[]{ new CubieColor('W','D') , new CubieColor('G','F'), new CubieColor('O','R')}, true, false);

		//Up, S Row
		cubiePos[0][1][0] = new Cubie(0,1,0,
				new CubieColor[]{ new CubieColor('R','L'), new CubieColor('Y','U')}, false, true);
		cubiePos[1][1][0] = new Cubie(1,1,0,
				new CubieColor[]{ new CubieColor('Y','U')}, false, false);
		cubiePos[2][1][0] = new Cubie(2,1,0,
				new CubieColor[]{ new CubieColor('Y','U'), new CubieColor('O','R')}, false, true);

		//E, S Row
		cubiePos[0][1][1] = new Cubie(0,1,1,
				new CubieColor[]{ new CubieColor('R','L')}, false, false);
		cubiePos[1][1][1] = new Cubie(1,1,1,
				new CubieColor[]{ new CubieColor('A','A')}, //Just giving random, non-legitimate values for color and direction
				false, false);
		cubiePos[2][1][1] = new Cubie(2,1,1,
				new CubieColor[]{ new CubieColor('O','R')}, false, false);

		//Down, S Row
		cubiePos[0][1][2] = new Cubie(0,1,2,
				new CubieColor[]{ new CubieColor('R','L'), new CubieColor('W','D')}, false, true);
		cubiePos[1][1][2] = new Cubie(1,1,2,
				new CubieColor[]{ new CubieColor('W','D')}, false, false);
		cubiePos[2][1][2] = new Cubie(2,1,2,
				new CubieColor[]{ new CubieColor('W','D'), new CubieColor('O','R')}, false, true);

		//Up, Back Row
		cubiePos[0][2][0] = new Cubie(0,2,0,
				new CubieColor[]{ new CubieColor('Y','U') , new CubieColor('R','L'), new CubieColor('B','B')}, true, false);
		cubiePos[1][2][0] = new Cubie(1,2,0,
				new CubieColor[]{ new CubieColor('Y','U') , new CubieColor('B','B')}, false, true);
		cubiePos[2][2][0] = new Cubie(2,2,0,
				new CubieColor[]{ new CubieColor('Y','U') , new CubieColor('B','B'), new CubieColor('O','R')}, true, false);

		//E, Back Row
		cubiePos[0][2][1] = new Cubie(0,2,1,
				new CubieColor[]{ new CubieColor('R','L'), new CubieColor('B','B')}, false, true);
		cubiePos[1][2][1] = new Cubie(1,2,1,
				new CubieColor[]{ new CubieColor('B','B')}, false, false);
		cubiePos[2][2][1] = new Cubie(2,2,1,
				new CubieColor[]{ new CubieColor('B','B'), new CubieColor('O','R')}, false, true);

		//Down, Back Row
		cubiePos[0][2][2] = new Cubie(0,2,2,
				new CubieColor[]{ new CubieColor('W','D') , new CubieColor('R','L'), new CubieColor('B','B')}, true, false);
		cubiePos[1][2][2] = new Cubie(1,2,2,
				new CubieColor[]{ new CubieColor('W','D') , new CubieColor('B','B')}, false, true);
		cubiePos[2][2][2] = new Cubie(2,2,2,
				new CubieColor[]{ new CubieColor('W','D') , new CubieColor('B','B'), new CubieColor('O','R')}, true, false);

	}

	/**
	 * Takes a String value of a turn or rotation in standard Rubik's Cube notation and applies the turn or rotation to
	 * the cube. Valid turns currently include any turn in the following planes: U, D, F, B, L, R, M, E, S
	 * Valid rotations are x, y, and z rotations.
	 * @param turn the turn to be performed
	 */
	public void turn (String turn) {
		//See the first case (B) to understand how all cases work
		char[] preChange; //Directions prior to turning
		char[] postChange; //What the directions change to after the turn
		Cubie[][] matrix = new Cubie[3][3]; //matrix to be rotated

		switch(turn) {
			case "B":
				preChange = new char[] {'B', 'U', 'R', 'D', 'L'};
				postChange = new char[] {'B', 'L', 'U', 'R', 'D'};
				//Transfer cubie data into matrix to be rotated
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[Math.abs(j-2)][2][i];
					}
				}
				//Rotate the matrix
				matrix = rotateMatrix(matrix, 90, preChange, postChange);
				//Reset the actual cube's cubies to those of the rotated matrix
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[Math.abs(j-2)][2][i] = matrix[i][j];
					}
				}
				break;

			case "Bi":
				preChange = new char[] {'B', 'U', 'R', 'D', 'L'};
				postChange = new char[] {'B', 'R', 'D', 'L', 'U'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[Math.abs(j-2)][2][i];
					}
				}
				matrix = rotateMatrix(matrix, -90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[Math.abs(j-2)][2][i] = matrix[i][j];
					}
				}
				break;

			case "D" :
				preChange = new char[] {'D', 'L', 'B', 'R', 'F'};
				postChange = new char[] {'D', 'F', 'L', 'B', 'R'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[j][i][2];
					}
				}
				matrix = rotateMatrix(matrix, 90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[j][i][2] = matrix[i][j];
					}
				}
				break;

			case "Di" :
				preChange = new char[] {'D', 'F', 'L', 'B', 'R'};
				postChange = new char[] {'D', 'L', 'B', 'R', 'F'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[j][i][2];
					}
				}
				matrix = rotateMatrix(matrix, -90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[j][i][2] = matrix[i][j];
					}
				}
				break;

			case "E" :
				preChange = new char[] {'L', 'B', 'R', 'F'};
				postChange = new char[] {'F', 'L', 'B', 'R'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[j][i][1];
					}
				}
				matrix = rotateMatrix(matrix, 90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[j][i][1] = matrix[i][j];
					}
				}
				break;

			case "Ei" :
				preChange = new char[] {'F', 'L', 'B', 'R'};
				postChange = new char[] {'L', 'B', 'R', 'F'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[j][i][1];
					}
				}
				matrix = rotateMatrix(matrix, -90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[j][i][1] = matrix[i][j];
					}
				}
				break;

			case "F":
				preChange = new char[] {'F', 'U', 'R', 'D', 'L'};
				postChange = new char[] {'F', 'R', 'D', 'L', 'U'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[j][0][i];
					}
				}
				matrix = rotateMatrix(matrix, 90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[j][0][i] = matrix[i][j];
					}
				}
				break;

			case "Fi":
				preChange = new char[] {'F', 'U', 'R', 'D', 'L'};
				postChange = new char[] {'F', 'L', 'U', 'R', 'D'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[j][0][i];
					}
				}
				matrix = rotateMatrix(matrix, -90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[j][0][i] = matrix[i][j];
					}
				}
				break;

			case "L":
				preChange = new char[] {'L', 'B', 'D', 'F', 'U'};
				postChange = new char[] {'L', 'U', 'B', 'D', 'F'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[0][Math.abs(j-2)][i];
					}
				}
				matrix = rotateMatrix(matrix, 90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[0][Math.abs(j-2)][i] = matrix[i][j];
					}
				}
				break;

			case "Li":
				preChange = new char[] {'L', 'U', 'B', 'D', 'F'};
				postChange = new char[] {'L', 'B', 'D', 'F', 'U'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[0][Math.abs(j-2)][i];
					}
				}
				matrix = rotateMatrix(matrix, -90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[0][Math.abs(j-2)][i] = matrix[i][j];
					}
				}
				break;

			case "M":
				preChange = new char[] {'B', 'D', 'F', 'U'};
				postChange = new char[] {'U', 'B', 'D', 'F'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[1][Math.abs(j-2)][i];
					}
				}
				matrix = rotateMatrix(matrix, 90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[1][Math.abs(j-2)][i] = matrix[i][j];
					}
				}
				break;

			case "Mi":
				preChange = new char[] {'U', 'B', 'D', 'F'};
				postChange = new char[] {'B', 'D', 'F', 'U'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[1][Math.abs(j-2)][i];
					}
				}
				matrix = rotateMatrix(matrix, -90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[1][Math.abs(j-2)][i] = matrix[i][j];
					}
				}
				break;

			case "R":
				preChange = new char[] {'R', 'U', 'B', 'D', 'F'};
				postChange = new char[] {'R', 'B', 'D', 'F', 'U'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[2][j][i];
					}
				}
				matrix = rotateMatrix(matrix, 90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[2][j][i] = matrix[i][j];
					}
				}
				break;

			case "Ri":
				preChange = new char[] {'R', 'B', 'D', 'F', 'U'};
				postChange = new char[] {'R', 'U', 'B', 'D', 'F'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[2][j][i];
					}
				}
				matrix = rotateMatrix(matrix, -90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[2][j][i] = matrix[i][j];
					}
				}
				break;

			case "S":
				preChange = new char[] {'U', 'R', 'D', 'L'};
				postChange = new char[] {'R', 'D', 'L', 'U'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[j][1][i];
					}
				}
				matrix = rotateMatrix(matrix, 90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[j][1][i] = matrix[i][j];
					}
				}
				break;

			case "Si":
				preChange = new char[] {'U', 'R', 'D', 'L'};
				postChange = new char[] {'L', 'U', 'R', 'D'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[j][1][i];
					}
				}
				matrix = rotateMatrix(matrix, -90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[j][1][i] = matrix[i][j];
					}
				}
				break;

			case "U" :
				preChange = new char[] {'U', 'F', 'L', 'B', 'R'};
				postChange = new char[] {'U', 'L', 'B', 'R', 'F'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[j][Math.abs(i-2)][0];
					}
				}
				matrix = rotateMatrix(matrix, 90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[j][Math.abs(i-2)][0] = matrix[i][j];
					}
				}
				break;

			case "Ui" :
				preChange = new char[] {'U', 'L', 'B', 'R', 'F'};
				postChange = new char[] {'U', 'F', 'L', 'B', 'R'};
				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						matrix[i][j] = cubiePos[j][Math.abs(i-2)][0];
					}
				}
				matrix = rotateMatrix(matrix, -90, preChange, postChange);

				for(int i = 0; i<3; i++) {
					for(int j = 0; j<3; j++) {
						cubiePos[j][Math.abs(i-2)][0] = matrix[i][j];
					}
				}
				break;

			case "X":
				performMoves("R Mi Li");
				//turn("R"); turn("M'"); turn("L'");
				break;

			case "Xi":
				performMoves("Ri M L");
				//turn("R'"); turn("M"); turn("L");
				break;

			case "Y":
				performMoves("U Ei Di");
				//turn("U"); turn("E'"); turn("D'");
				break;

			case "Yi":
				performMoves("Ui E D");
				//turn("U'"); turn("E"); turn("D");
				break;

			case "Z":
				performMoves("F S Bi");
				//turn("F"); turn("S"); turn("B'");
				break;

			case "Zi":
				performMoves("Fi Si B");
				//turn("F'"); turn("S'"); turn("B");
				break;

		}


	}

	/**
	 * Rotates a given 2D matrix as specified by {@code degrees}, where {@code degrees}
	 * can either be 90, indicating a clockwise rotation, or -90, indicating a counterclockwise
	 * rotation. {@code postChange} dictates how the direction of a cubie's colors should
	 * change after the rotation, the original directions being denoted by {@code preChange}.
	 * @param orig the original matrix
	 * @param degrees degrees by which to rotate, can be 90 or -90
	 * @param preChange the set of direction prior to rotation
	 * @param postChange the corresponding set of direction to change the {@code preChange} directions to
	 * @return the rotated matrix
	 */
	private Cubie[][] rotateMatrix(Cubie[][] orig, int degrees, char[] preChange,
								   char[] postChange) {
		Cubie[][] rotated = new Cubie[3][3];
		if(degrees == 90) {
			//Transpose the matrix
			for(int i = 0; i<3; i++) {
				for(int j = 0; j<3; j++) {
					rotated[i][j] = orig[j][i];
				}
			}
			//Reverse all the rows
			for(int i = 0; i<3; i++) {
				for(int j = 0; j<rotated[0].length/2; j++) {
					Cubie tempCubie = rotated[i][3-j-1];
					rotated[i][3-j-1] = rotated[i][j];
					rotated[i][j] = tempCubie;
				}
			}
		}
		else if(degrees == -90) {
			//Transpose the matrix
			for(int i = 0; i<3; i++) {
				for(int j = 0; j<3; j++) {
					rotated[i][j] = orig[j][i];
				}
			}

			//Reverse all the columns
			for(int i = 0; i<rotated[0].length/2; i++) {
				for(int j = 0; j<3; j++) {
					Cubie tempCubie = rotated[3-i-1][j];
					rotated[3-i-1][j] = rotated[i][j];
					rotated[i][j] = tempCubie;
				}
			}
		}

		//Change the direction of all colors appropriately as well before returning the array
		for(int i = 0; i<3; i++) {
			for(int j = 0; j<3; j++) {
				CubieColor[] tempColors = rotated[i][j].getColors();
				for(int k = 0; k<tempColors.length; k++) {
					int index = 6;
					for(int x = 0; x < preChange.length; x++) {
						if(tempColors[k].getDir() == preChange[x]) {
							index = x;
						}
					}
					if(index<postChange.length)
						tempColors[k].setDir(postChange[index]);
				}
				rotated[i][j].setColors(tempColors);
			}
		}
		return rotated;
	}

	/**
	 * Loops through the characters in a String of standard turning notation to apply the set of moves to the cube
	 * Checks for clockwise, double, and counterclockwise turns
	 * @param moves the moves to be applied to the cube
	 * @return the moves performed on the cube (same as {@code moves})
	 */
	public String performMoves(String moves) {
		for(int i = 0; i<moves.length(); i++) {
			if(moves.substring(i, i+1) != " ") { //Only check if there is a meaningful character
				if(i != moves.length()-1) {
					if(moves.substring(i+1,i+2).compareTo("i") == 0) {
						//Making a counterclockwise turn ex. Ui
						turn(moves.substring(i, i+2));
						i++; //Skip the apostrophe for the next iteration
					}
					else {
						//Regular clockwise turning
						turn(moves.substring(i, i+1));
					}
				}
				else {
					//Nothing is after the turn letter, so just perform the turn
					turn(moves.substring(i, i+1));
				}
			}
		}
		return moves;
	}

	/**
	 * Scrambles a cube according to WCA rules (White on top, Green in front).
	 * After scrambling, returns the cube to the original position (Yellow on top, Green in front) form
	 * which a solution can be generated.
	 * @param scramble the scramble to be performed
	 */
	public void scramble(String scramble) {
		//Rotate the cube to get white on top, then return cube to original position at end of scramble
		performMoves(scramble);
	}

	/**
	 * Once the sunflower is made, this method matches white edges to their respective faces and turns them down
	 * one at a time, creating the white cross.
	 * @return the moves used to create the white cross
	 */
	public String makeWhiteCross() {
		String moves = new String();

		while(numWhiteEdgesOriented() != 0) { //Turn sunflower into cross until no white edges remain in the U layer
			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 3; j++) {
					if(cubiePos[i][j][0].isEdgeCubie()) {
						CubieColor[] tempColors = cubiePos[i][j][0].getColors();
						if(tempColors[0].getColor() == 'W' || tempColors[1].getColor() == 'W') {
							for(int k = 0; k<2; k++) {
								//Check for when the white edge is matched up with the respective face and turn it down
								if((tempColors[k].getColor() == 'R' && tempColors[k].getDir() == 'L') ||
										(tempColors[k].getColor() == 'G' && tempColors[k].getDir() == 'F') ||
										(tempColors[k].getColor() == 'O' && tempColors[k].getDir() == 'R')||
										(tempColors[k].getColor() == 'B' && tempColors[k].getDir() == 'B')) {
									moves+=performMoves(cubiePos[i][j][0].verticalFace(i, j) + " " + cubiePos[i][j][0].verticalFace(i, j)) ;
								}
							}
						}
					}
				}
			}
			//Turn U to try lining up edges that have not been turned down yet
			moves+=performMoves("U ");
		}
		return moves;
	}

	/**
	 * Makes the sunflower (yellow center in the  middle with 4 white edges surrounding it).
	 * The sunflower can then be used by makeCross() to make the white cross
	 * @return moves used to make sunflower
	 */
	public String makeSunflower() {
		String moves = new String();

		//Brings up white edges in D Layer with white facing down
		if(numWhiteEdgesOriented() < 5) {
			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 3; j++) {
					if(cubiePos[i][j][2].isEdgeCubie()) {
						if(cubiePos[i][j][2].getDirOfColor('W') == 'D') {
							moves += prepareSlot(i, j, 0, 'W');
							//Get the vertical plane in which the cubie lies
							char turnToMake = cubiePos[i][j][2].verticalFace(i, j);
							moves += performMoves("" + turnToMake + " " + turnToMake);
						}
					}
				}
			}
		}

		//Orients white edges in D Layer with white NOT facing down
		if(numWhiteEdgesOriented() < 5) {
			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 3; j++) {
					if(cubiePos[i][j][2].isEdgeCubie()) {
						if(cubiePos[i][j][2].getDirOfColor('W') != 'A' && cubiePos[i][j][2].getDirOfColor('W') != 'D') {
							char vert = cubiePos[i][j][2].verticalFace(i, j);
							moves += prepareSlot(i, j, 0, 'W');
							if(vert == 'F') {
								moves += performMoves("Fi Ui R ");
							} else if(vert == 'R') {
								moves += performMoves("Ri Ui B ");
							} else if(vert == 'B') {
								moves += performMoves("Bi Ui L ");
							} else if(vert == 'L') {
								moves += performMoves("Li Ui F ");
							}
						}
					}
				}

			}
		}

		//Brings up white edges in E Layer
		//This one is filled with many if blocks because there are eight different possible orientations for
		//white edges in the E Layer, with none sharing a common move to bring it into the U layer.
		if(numWhiteEdgesOriented() < 5) {
			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 3; j++) {
					if(cubiePos[i][j][1].isEdgeCubie()) {
						CubieColor[] tempColors = cubiePos[i][j][1].getColors();
						for(int k = 0; k<2; k++) {
							if(tempColors[k].getColor() == 'W') {
								/* Depending on the position of the edge, one of the vertical planes it lies
								 * in must be cleared of white edges before bringing it up */
								if(i == 0 && j == 0) {
									if(tempColors[k].getDir() == 'L') {
										moves += prepareSlot(1, 0, 0, 'W') + performMoves("F ");
									} else {
										moves += prepareSlot(0, 1, 0, 'W') + performMoves("Li ");
									}
								}
								else if(i == 2 && j == 0) {
									if(tempColors[k].getDir() == 'F') {
										moves += prepareSlot(2, 1, 0, 'W') + performMoves("R ");
									} else {
										moves += prepareSlot(1, 0, 0, 'W') + performMoves("Fi ");
									}
								}
								else if(i == 2 && j == 2) {
									if(tempColors[k].getDir() == 'B') {
										moves += prepareSlot(2, 1, 0, 'W') + performMoves("Ri ");
									} else {
										moves += prepareSlot(1, 2, 0, 'W') + performMoves("B ");
									}
								}
								else {
									if(tempColors[k].getDir() == 'B') {
										moves += prepareSlot(0, 1, 0, 'W') + performMoves("L ");
									} else {
										moves += prepareSlot(1, 2, 0, 'W') + performMoves("Bi ");
									}
								}
							}
						}
					}
				}

			}
		}

		//Fix any edges that are incorrectly oriented in the U Layer
		//For the sake of reducing movecount, I assigned a set of moves for each position,
		//but a solver may simply make U turns to bring the edge in front and perform "F U' R"
		if(numWhiteEdgesOriented() < 5) {
			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 3; j++) {
					if(cubiePos[i][j][0].isEdgeCubie()) {
						if(cubiePos[i][j][0].getDirOfColor('W') != 'A' && cubiePos[i][j][0].getDirOfColor('W') != 'U') {
							char vert = cubiePos[i][j][0].verticalFace(i, j);
							if(vert == 'F') {
								moves += performMoves("F Ui R ");
							} else if(vert == 'R') {
								moves += performMoves("R Ui B ");
							} else if(vert == 'B') {
								moves += performMoves("B Ui L ");
							} else if(vert == 'L') {
								moves += performMoves("L Ui F ");
							}
						}
					}
				}

			}
		}

		//If fewer than 4 white edges reached the top layer by the end of this, some white edge was missed
		//(This might happen, say, if bringing an edge up from the E Layer unintentionally brings down an incorrectly
		// oriented edge in the U Layer)
		//Recurse to oriented remaining white edges
		if(numWhiteEdgesOriented() < 4) {
			moves += makeSunflower();
		}

		return moves;
	}

	/**
	 * Utility method for makeSunflower()
	 * Prepares a slot in the U face for white edges to be brought up into the U layer without misorienting white
	 * edges already in the U layer
	 * @param x the x position of the cubie to prepare
	 * @param y the y position
	 * @param z the z position
	 * @param color the color which should not remain in the prepared slot
	 * @return moves used to prepare the edge slot
	 */
	public String prepareSlot(int x, int y, int z, char color) {
		int numUTurns = 0;
		CubieColor[] tempColor = cubiePos[x][y][z].getColors();
		while((tempColor[0].getColor() == color || tempColor[1].getColor() == color) && numUTurns < 5){
			//Keep turning U until the position (x, y, z) is not occupied by a white edge
			performMoves("U");
			tempColor = cubiePos[x][y][z].getColors();
			numUTurns++;
		}

		//Return appropriate amount of U turns
		if(numUTurns == 0 || numUTurns == 4) {
			return "";
		}
		else if(numUTurns == 1) {
			return "U ";
		}
		else if (numUTurns == 2) {
			return "U U ";
		}
		else return "U' ";
	}

	/**
	 * Utility method for makeSunflower()
	 * @return the number of white edges that are currently in the U layer
	 */
	public int numWhiteEdgesOriented() {
		int numOriented = 0;
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(cubiePos[i][j][0].isEdgeCubie()) {
					if(cubiePos[i][j][0].getDirOfColor('W') == 'U') {
						numOriented++;
					}
				}
			}
		}
		return numOriented;
	}

	/**
	 * Completes the white layer by inserting any white corners in the U layer and fixing misoriented
	 * white corners until there are no more white corners in the U layer.
	 * @return the moves used to complete the white layer
	 */
	public String finishWhiteLayer() {
		String moves = new String();
		//At least check once for corners to be inserted/fixed, and repeat as necessary
		moves+=insertCornersInU();
		moves+="\n";
		moves+=insertMisorientedCorners();
		moves+="\n";
		while(whiteCornerinU()) {
			moves+=insertCornersInU();
			moves+="\n";
			moves+=insertMisorientedCorners();
			moves+="\n";
		}
		return moves;
	}

	/**
	 * Utility method for insertCornersinU()
	 * @return if there are any white corners in the U layer
	 */
	public boolean whiteCornerinU() {
		for(int i = 0; i<3; i++) {
			for(int j = 0; j<3; j++) {
				if(cubiePos[i][j][0].isCornerCubie()) {
					//If a cubie does not have a color, getDirOfColor returns 'A'
					if(cubiePos[i][j][0].getDirOfColor('W') != 'A')
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Inserts any white corners that are in the U layer. First positions them to the position (2, 0, 0), then
	 * makes U turns and y rotations until the white corner is above its respective slot, and finally inserts
	 * the corner by repetitively executing R U R' U'. This is repeated of all whit corners in the U layer.
	 * @return moves used to insert white corners that are in the U layer
	 */
	public String insertCornersInU() {
		String moves = new String();

		for(int y = 0; y<3; y++) {
			for(int x = 0; x<3; x++) {
				if(cubiePos[x][y][0].isCornerCubie() && cubiePos[x][y][0].isWhiteCorner()) {
					//Make U turns until cubie is at (2, 0, 0)
					if(x==0) {
						if(y==0) {
							moves+=performMoves("Ui ");
						}
						else {
							moves+=performMoves("U U ");
						}
					}
					else {
						if(y==2) {
							moves+=performMoves("U ");
						}
					}
					//Set x and y = 0 for the next loop to avoid using while loop
					y=0; x=0;

					//Get cubie above respective slot in first layer
					int numUTurns = 0;
					int yRotations = 0;
					while(!whiteCornerPrepared()) {
						performMoves("U Yi"); numUTurns++; yRotations++;
					}
					if(numUTurns == 1) {
						moves += "U ";
					} else if(numUTurns == 2) {
						moves += "U U ";
					} else if(numUTurns == 3) {
						moves += "Ui ";
					}
					if(yRotations == 1) {
						moves+="Yi ";
					} else if(yRotations == 2) {
						moves += "Y Y ";
					} else if(yRotations == 3) {
						moves += "Y ";
					}

					//Insert the cubie
					int numSexyMoves = 0;
					//Don't worry, the algorithm "R U R' U'" is commonly referred to as the sexy move in the cubing community
					while(!cornerInserted(2, 0, 2)){
						performMoves("R U Ri Ui"); numSexyMoves++;
					}
					if(numSexyMoves == 5) { //5 sexy moves can be condensed into "U R U' R'"
						moves += "U R Ui Ri ";
					}
					else {
						for(int k = 0; k<numSexyMoves; k++) {
							moves += "R U Ri Ui ";
						}
					}
				}
			}
		}

		return moves;
	}

	/**
	 * Properly inserts white corners that are in the first layer but not oriented correctly
	 * @return moves used to properly orient misoriented white corners
	 */
	public String insertMisorientedCorners() {
		String moves = new String();
		for(int i = 0; i<4; i++) {
			moves += performMoves("Y ");
			if(!cornerInserted(2,0,2)) {
				if(cubiePos[2][0][2].isWhiteCorner()) {
					if(!cornerInserted(2,0,2)) {
						//Use R U R' U' to get corner to U layer, then insert it in appropriate slot
						moves+=performMoves("R U Ri Ui ");
						moves+=insertCornersInU();
					}
				}
			}
		}
		return moves;
	}

	/**
	 * Utility method for insertCornersInU().
	 * Checks for whether the corner cubie at (2, 0, 0) belongs in (2, 0, 2).
	 * @return true if cubie at (2, 0, 0) belongs in (2, 0, 2), else false
	 */
	public boolean whiteCornerPrepared() {
		boolean whiteUp = false;

		//Figure out whether the corner cubie is even a white corner
		if(cubiePos[2][0][0].isCornerCubie() && cubiePos[2][0][0].getDirOfColor('W') == 'A') {
			return false;
		}

		//If the cubie is a white corner, figure out whether the white sticker is facing up
		if(cubiePos[2][0][0].getDirOfColor('W') == 'U')
			whiteUp = true;

		//Based on whether white is up, check accordingly if the corner is above the appropriate slot
		if(whiteUp) {
			return (cubiePos[2][0][0].getColorOfDir('R') == cubiePos[1][0][1].getColors()[0].getColor() &&
					cubiePos[2][0][0].getColorOfDir('F') == cubiePos[2][1][1].getColors()[0].getColor()	);
		}
		else {
			/*Either the color on the right of the cubie matches its respective center piece OR
			 *the color on the front of the cubie matches its respective center piece
			 *It is not possible for both to match because if white is not facing up, it will either be facing front or right
			 */
			return (cubiePos[2][0][0].getColorOfDir('R') == cubiePos[2][1][1].getColors()[0].getColor() ||
					cubiePos[2][0][0].getColorOfDir('F') == cubiePos[1][0][1].getColors()[0].getColor());
		}
	}

	/**
	 * Correctly checks whether the corner at (2, 0, 2) is solved.
	 * @param x the position to check for (although this method is only called with (2, 0, 2).
	 * @param y see above
	 * @param z see above
	 * @return true if corner is solved, else false
	 */
	public boolean cornerInserted(int x, int y, int z) {
		if(cubiePos[x][y][z].isCornerCubie()) {
			return (cubiePos[x][y][z].getColorOfDir('D') == cubiePos[1][1][2].getColors()[0].getColor() &&
					cubiePos[x][y][z].getColorOfDir('F') == cubiePos[1][0][1].getColors()[0].getColor() &&
					cubiePos[x][y][z].getColorOfDir('R') == cubiePos[2][1][1].getColors()[0].getColor());
		}
		return false;
	}

	/**
	 * Utilizes the methods insertEdgesInU() and insertMisorientedEdges() to complete the second layer
	 * @return A String for the moves used to complete the second layer
	 */
	public String insertAllEdges() {
		String moves = new String();
		//At least check once for edges to be inserted/fixed, and repeat as necessary
		moves+=insertEdgesInU();
		moves+="\n";
		moves+=insertMisorientedEdges();
		moves+="\n";
		while(nonYellowEdgesInU()) {
			moves+=insertEdgesInU();
			moves+="\n";
			moves+=insertMisorientedEdges();
			moves+="\n";
		}
		return moves;
	}

	/**
	 * Checks whether any non-yellow edges remain in the U layer.
	 * (Any such edges need to be inserted into their respective slot in the second layer)
	 * @return whether there is/are non-yellow edges in the U layer
	 */
	public boolean nonYellowEdgesInU() {
		for(int i = 0; i<3; i++) {
			for(int j = 0; j<3; j++) {
				if(cubiePos[i][j][0].isEdgeCubie()) {
					//If a cubie does not have a color, getDirOfColor returns 'A'
					if(cubiePos[i][j][0].getDirOfColor('Y') == 'A')
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Inserts all non-yellow edges in the U layer into their respective slots in the
	 * second layer using one of two algorithms
	 * @return moves used to insert non-yellow edges in the U layer
	 */
	public String insertEdgesInU() {
		String moves = new String();
		for(int i = 0; i<3; i++) {
			for(int j = 0; j<3; j++) {
				if(cubiePos[i][j][0].isEdgeCubie() && cubiePos[i][j][0].getDirOfColor('Y') == 'A') {
					//Make U turns to get cubie to (1, 0, 0)
					if(j==1) {
						if(i==0) {
							moves+=performMoves("Ui ");
						} else {
							moves+=performMoves("U ");
						}
					}
					else if(j==2){
						moves+=performMoves("U U ");
					}

					//Get cubie above respective slot in second layer
					int numUTurns = 0;
					int yRotations = 0;
					while(cubiePos[1][0][0].getColorOfDir('F') != cubiePos[1][0][1].getColors()[0].getColor()) {
						performMoves("U Yi "); numUTurns++; yRotations++;
					}
					//Add appropriate amount of U turns to moves
					if(numUTurns == 1) {
						moves += "U ";
					} else if(numUTurns == 2) {
						moves += "U U ";
					} else if(numUTurns == 3) {
						moves += "Ui ";
					}
					if(yRotations == 1) {
						moves+="Yi ";
					} else if(yRotations == 2) {
						moves += "Y Y ";
					} else if(yRotations == 3) {
						moves += "Y ";
					}

					//Insert the cubie in appropriate direction
					if(cubiePos[1][0][0].getColorOfDir('U') == cubiePos[0][1][1].getColors()[0].getColor()) {
						moves += performMoves("Ui Li U L U F Ui Fi ");
					}
					else if(cubiePos[1][0][0].getColorOfDir('U') == cubiePos[2][1][1].getColors()[0].getColor()){
						moves += performMoves("U R Ui Ri Ui Fi U F ");
					}

					//Reset i and j to continue checking for edges to be inserted (foregoes use of while loop)
					i = 0; j = 0;
				}
			}
		}
		return moves;
	}

	/**
	 * If there are any edges in the second layer that were inserted in the incorrect
	 * orientation, this method re-inserts them in the correct orientation
	 * @return moves used to fix edge orientations in second layer
	 */
	public String insertMisorientedEdges() {
		String moves = new String();
		for(int i = 0; i<4; i++) {
			moves += performMoves("Y ");
			if(cubiePos[2][0][1].getDirOfColor('Y') == 'A' &&
					cubiePos[2][0][1].getColorOfDir('F') != cubiePos[1][0][1].getColors()[0].getColor()) {
				//If the edge is the the correct slot but oriented incorrectly, perform an algorithm for this special case
				if(cubiePos[2][0][1].getColorOfDir('F') == cubiePos[2][1][1].getColors()[0].getColor() &&
						cubiePos[2][0][1].getColorOfDir('R') == cubiePos[1][0][1].getColors()[0].getColor()) {
					moves += performMoves("R U Ri U U R U U Ri U Fi Ui F ");
				}
				else {
					//If there is an edge that does not belong in the slot at all, take it out and insert in correct slot
					moves+=performMoves("R U Ri Ui Fi Ui F ");
					moves+=insertEdgesInU();
				}
			}
		}
		return moves;
	}

	/**
	 * Utility method for yellowEdgeOrientation() and makeYellowCross()
	 * @return the number of yellow edges that are already oriented in the U layer
	 */
	public int numYellowEdgesOriented(){
		int numOriented = 0;
		for(int i = 0; i<3; i++) {
			for(int j = 0; j<3; j++) {
				if(cubiePos[i][j][0].isEdgeCubie() && cubiePos[i][j][0].getDirOfColor('Y') == 'U')
					numOriented++;
			}
		}
		return numOriented;
	}

	/**
	 * Utility method for orientLastLayer()
	 * @return the number of yellow corners that are already oriented in the U layer
	 */
	public int numYellowCornersOriented(){
		int numOriented = 0;
		for(int i = 0; i<3; i++) {
			for(int j = 0; j<3; j++) {
				if(cubiePos[i][j][0].isCornerCubie() && cubiePos[i][j][0].getDirOfColor('Y') == 'U')
					numOriented++;
			}
		}
		return numOriented;
	}

	/**
	 * Utility method for makeYellowCross(). Determines the shape that the oriented
	 * yellow edges make.
	 * @return Dot, L, Bar, or Cross
	 */
	public String yellowEdgeOrientation() {
		String status = new String();
		int numOriented = numYellowEdgesOriented();

		if(numOriented == 4) { //The cross has already been made
			status = "Cross";
		}
		else if(numOriented == 0) { //No edges are oriented
			status = "Dot";
		}
		else if(numOriented == 2) {
			//If two edges are oriented, they either form an L-shape or a Bar
			int[] xValues = new int[2];
			int index = 0;
			for(int i = 0; i<3; i++) {
				for(int j = 0; j<3; j++) {
					if(cubiePos[i][j][0].isEdgeCubie() && cubiePos[i][j][0].getDirOfColor('Y') == 'U') {
						xValues[index] = i; index++;
					}
				}
			}
			if(Math.abs(xValues[0]-xValues[1])%2 == 0) {
				status = "Bar";
			} else {
				status = "L";
			}
		}

		return status;
	}

	/**
	 * Orients all yellow edges in the U layer based on their current state.
	 * @return moves used to make the yellow cross
	 */
	public String makeYellowCross() {
		String moves = new String();
		String status = yellowEdgeOrientation();

		if(status.compareTo("Dot") == 0) {
			//Make an L and then subsequently use the algorithm to orient the edges
			moves += performMoves("F R U Ri Ui Fi U U F U R Ui Ri Fi ");
		}
		else if(status.compareTo("L") == 0) {
			//Position the L appropriately first
			while(cubiePos[0][1][0].getDirOfColor('Y') != 'U' || cubiePos[1][2][0].getDirOfColor('Y') != 'U') {
				moves += performMoves("U ");
			}
			moves += performMoves("F U R Ui Ri Fi ");
		}
		else if(status.compareTo("Bar") == 0) {
			//Position the Bar appropriately first
			while(cubiePos[0][1][0].getDirOfColor('Y') != 'U' || cubiePos[2][1][0].getDirOfColor('Y') != 'U') {
				moves += performMoves("U ");
			}
			moves += performMoves("F R U Ri Ui Fi ");
		}
		return moves;
	}

	/**
	 * Finishes the step of orienting the last layer by orienting all yellow corners using
	 * a beginner's method algorithm. (This has been left separate from makeYellowCross()
	 * to help beginners easily follow the steps to orient the last layer completely.)
	 * @return moves used to orient last layer pieces
	 */
	public String orientLastLayer() {
		String moves = new String();
		int numOriented = numYellowCornersOriented();
		//Used while loop since Antisune case requires Sune algorithm to be perform twice for proper orientation
		while(numOriented != 4) {
			if(numOriented == 0){
				//Turn until there is a yellow sticker on the left of the ULF piece
				while(cubiePos[0][0][0].getDirOfColor('Y') != 'L') {
					moves += performMoves("U ");
				}
				//Perform Sune algorithm to orient one corner
				moves += performMoves("R U Ri U R U U Ri ");
			}
			else if(numOriented == 1){
				//Sune case
				while(cubiePos[0][0][0].getDirOfColor('Y') != 'U') {
					moves += performMoves("U ");
				}
				moves += performMoves("R U Ri U R U U Ri ");
			}
			else if(numOriented == 2){
				//Turn until there is a yellow sticker on the front of the ULF piece
				while(cubiePos[0][0][0].getDirOfColor('Y') != 'F') {
					moves += performMoves("U ");
				}
				//Perform Sune algorithm to orient one corner
				moves += performMoves("R U Ri U R U U Ri ");
			}
			//Re-check the number of corners oriented
			numOriented = numYellowCornersOriented();
		}
		return moves;
	}

	/**
	 * Permutes the last layer such that all oriented pieces are in the correct positions
	 * relative to each other. First permutes the corners, then the edges.
	 * @return the moves used to permute the last layer
	 */
	public String permuteLastLayer() {
		String moves = new String();
		//Check the number of "headlights" that exist, i.e. adjacent corners with the same color facing one direction
		//If there are 4 headlights, the corners are already permuted
		int numHeadlights = 0;
		for(int i = 0; i<4; i++) {
			turn("Y"); //Since we are rotating 4 times, the cube is unaffected in the end
			if(cubiePos[0][0][0].getColorOfDir('F') == cubiePos[2][0][0].getColorOfDir('F'))
				numHeadlights++;
		}

		//Permute the corners
		if(numHeadlights == 0){ //If no headlights, create headlights first
			moves += performMoves("Ri F Ri B B R Fi Ri B B R R ");
			numHeadlights = 1;
		}
		if(numHeadlights == 1) {
			while(cubiePos[0][2][0].getColorOfDir('B') != cubiePos[2][2][0].getColorOfDir('B')) {
				moves += performMoves("U ");
			}
			moves += performMoves("Ri F Ri B B R Fi Ri B B R R ");
		}

		//Now permute the edges after finding out how many edges are already solved
		int numSolved = 0;
		for(int i = 0; i<4; i++) {
			turn("Y");
			if(cubiePos[0][0][0].getColorOfDir('F') == cubiePos[1][0][0].getColorOfDir('F'))
				numSolved++;
		}
		if(numSolved == 0) { //If no edges are solved, this will solve one edge
			moves += performMoves("R R U R U Ri Ui Ri Ui Ri U Ri ");
			numSolved = 1;
		}
		if(numSolved == 1){
			//Use either the clockwise or counterclockwise edge rotation algorithm to solve all corners
			while(cubiePos[0][2][0].getColorOfDir('B') != cubiePos[1][2][0].getColorOfDir('B')) {
				moves += performMoves("U ");
			}
			if(cubiePos[1][0][0].getColorOfDir('F') == cubiePos[0][0][0].getColorOfDir('L')) {
				moves += performMoves("R R U R U Ri Ui Ri Ui Ri U Ri ");
			}
			else {
				moves += performMoves("R Ui R U R U R Ui Ri Ui R R ");
			}
		}

		//Adjust the U layer to finish the cube!
		while(cubiePos[0][0][0].getColorOfDir('F') != cubiePos[1][0][1].getColors()[0].getColor()) {
			moves += performMoves("U ");
		}

		return moves;
	}
}
