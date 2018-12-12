package sample.Kociemba;

public class KociembaSearch {

	//Options for research purpose.
	static final int MOVE_NUMBER = 21; //  defines the maximal allowed maneuver length\
	static final int PROBE_MAX = 100; // the maximum number of the probes of phase 2
	static final int PROBE_MIN = 0; // the minimum number of the probes of phase 2
	static final int MAX_PRE_MOVES = 20;

	protected static int MIN_P1LENGTH_PRE = 7;
	protected static int MAX_DEPTH2 = 13;

	static boolean inited = false;

	protected int[] move = new int[31];
	protected int[] moveSol = new int[31];

	protected CoordCube[] nodeUD = new CoordCube[21];
	protected CoordCube[] nodeRL = new CoordCube[21];
	protected CoordCube[] nodeFB = new CoordCube[21];

	protected long selfSym;
	protected int conjMask;
	protected int urfIdx;
	protected int length1;
	protected int depth1;
	protected int maxDep2;
	protected int sol;
	protected String solution;
	protected long probe;
	protected long probeMax;
	protected long probeMin;
	protected int valid1;
	protected boolean allowShorter = false;
	protected CubieCube cc = new CubieCube();
	protected CubieCube[] urfCubieCube = new CubieCube[6];
	protected CoordCube[] urfCoordCube = new CoordCube[6];
	protected CubieCube[] phase1Cubie = new CubieCube[21];

	CubieCube[] preMoveCubes = new CubieCube[MAX_PRE_MOVES + 1];
	int[] preMoves = new int[MAX_PRE_MOVES];
	int preMoveLen = 0;
	int maxPreMoves = 0;
	CubieCube phase2Cubie;

	protected boolean isRec = false;

	public KociembaSearch() {
		for (int i = 0; i < 21; i++) {
			nodeUD[i] = new CoordCube();
			nodeRL[i] = new CoordCube();
			nodeFB[i] = new CoordCube();
			phase1Cubie[i] = new CubieCube();
		}
		for (int i = 0; i < 6; i++) {
			urfCubieCube[i] = new CubieCube();
			urfCoordCube[i] = new CoordCube();
		}
		for (int i = 0; i < MAX_PRE_MOVES; i++) {
			preMoveCubes[i + 1] = new CubieCube();
		}
	}

	/**
	 * Computes the solver string for a given cube.
	 *
	 * @param facelets
	 *      is the cube definition string format.<br>
	 * The names of the facelet positions of the cube:
	 * <pre>
	 *             |************|
	 *             |*U1**U2**U3*|
	 *             |************|
	 *             |*U4**U5**U6*|
	 *             |************|
	 *             |*U7**U8**U9*|
	 *             |************|
	 * ************|************|************|************|
	 * *L1**L2**L3*|*F1**F2**F3*|*R1**R2**R3*|*B1**B2**B3*|
	 * ************|************|************|************|
	 * *L4**L5**L6*|*F4**F5**F6*|*R4**R5**R6*|*B4**B5**B6*|
	 * ************|************|************|************|
	 * *L7**L8**L9*|*F7**F8**F9*|*R7**R8**R9*|*B7**B8**B9*|
	 * ************|************|************|************|
	 *             |************|
	 *             |*D1**D2**D3*|
	 *             |************|
	 *             |*D4**D5**D6*|
	 *             |************|
	 *             |*D7**D8**D9*|
	 *             |************|
	 * </pre>
	 * A cube definition string "UBL..." means for example: In position U1 we have the U-color, in position U2 we have the
	 * B-color, in position U3 we have the L color etc.
	 *
	 * @return The solution string or an error code:<br>
	 *      Error 1: There is not exactly one facelet of each colour<br>
	 *      Error 2: Not all 12 edges exist exactly once<br>
	 *      Error 3: Flip error: One edge has to be flipped<br>
	 *      Error 4: Not all corners exist exactly once<br>
	 *      Error 5: Twist error: One corner has to be twisted<br>
	 *      Error 6: Parity error: Two corners or two edges have to be exchanged<br>
	 *      Error 7: No solution exists for the given maxDepth<br>
	 *      Error 8: Probe limit exceeded, no solution within given probMax
	 */
	public synchronized String solution(String facelets) {
		boolean check = initCube(facelets);
		if (!check) {
			return "Error";
		}
		this.sol = MOVE_NUMBER;
		this.probe = 0;
		this.probeMax = PROBE_MAX;
		this.probeMin = Math.min(probeMin, probeMax);
		this.solution = null;
		this.isRec = false;

		init();

		initSearch();

		return search();
	}

	protected void initSearch() {
		conjMask = 0;
		selfSym = cc.selfSymmetry();
		conjMask |= (selfSym >> 16 & 0xffff) != 0 ? 0x12 : 0;
		conjMask |= (selfSym >> 32 & 0xffff) != 0 ? 0x24 : 0;
		conjMask |= (selfSym >> 48 & 0xffff) != 0 ? 0x38 : 0;
		selfSym &= 0xffffffffffffL;
		maxPreMoves = conjMask > 7 ? 0 : MAX_PRE_MOVES;

		for (int i = 0; i < 6; i++) {
			urfCubieCube[i].copy(cc);
			urfCoordCube[i].setWithPrun(urfCubieCube[i], 20);
			cc.URFConjugate();
			if (i % 3 == 2) {
				cc.invCubieCube();
			}
		}
	}

	public synchronized String next() {
		this.probe = 0;
		this.probeMax = PROBE_MAX;
		this.probeMin = PROBE_MIN;
		this.solution = null;
		this.isRec = true;

		return  search();
	}

	public synchronized static void init() {
		if (!inited) {
			CubieCube.initMove();
			CubieCube.initSym();
		}

		CoordCube.init();

		inited = true;
	}

	boolean initCube(String facelets) {
		int count = 0x000000;
		byte[] f = new byte[54];

			String center = new String(
					new char[] {
							facelets.charAt(Util.U5),
							facelets.charAt(Util.R5),
							facelets.charAt(Util.F5),
							facelets.charAt(Util.D5),
							facelets.charAt(Util.L5),
							facelets.charAt(Util.B5)
					}
			);
			for (int i = 0; i < 54; i++) {
				f[i] = (byte) center.indexOf(facelets.charAt(i));
				count += 1 << (f[i] << 2);
			}

		if (count != 0x999999) {
			return false;
		}
		Util.toCubieCube(f, cc);

		return true;
	}

	protected int phase1PreMoves(int maxl, int lm, CubieCube cc, int ssym) {
		preMoveLen = maxPreMoves - maxl;
		if (isRec ? depth1 == length1 - preMoveLen
				: (preMoveLen == 0 || (0x36FB7 >> lm & 1) == 0)) {
			depth1 = length1 - preMoveLen;
			phase1Cubie[0] = cc;
			allowShorter = depth1 == MIN_P1LENGTH_PRE && preMoveLen != 0;

			if (nodeUD[depth1 + 1].setWithPrun(cc, depth1)
					&& phase1(nodeUD[depth1 + 1], ssym, depth1, -1) == 0) {
				return 0;
			}
		}

		if (maxl == 0 || preMoveLen + MIN_P1LENGTH_PRE >= length1) {
			return 1;
		}

		int skipMoves = CubieCube.getSkipMoves(ssym);
		if (maxl == 1 || preMoveLen + 1 + MIN_P1LENGTH_PRE >= length1) { //last pre move
			skipMoves |= 0x36FB7; // 11 0110 1111 1011 0111
		}

		lm = lm / 3 * 3;
		for (int m = 0; m < 18; m++) {
			if (m == lm || m == lm - 9 || m == lm + 9) {
				m += 2;
				continue;
			}
			if (isRec && m != preMoves[maxPreMoves - maxl] || (skipMoves & 1 << m) != 0) {
				continue;
			}
			CubieCube.CornMult(CubieCube.moveCube[m], cc, preMoveCubes[maxl]);
			CubieCube.EdgeMult(CubieCube.moveCube[m], cc, preMoveCubes[maxl]);
			preMoves[maxPreMoves - maxl] = m;
			int ret = phase1PreMoves(maxl - 1, m, preMoveCubes[maxl], ssym & (int) CubieCube.moveCubeSym[m]);
			if (ret == 0) {
				return 0;
			}
		}
		return 1;
	}

	protected String search() {
		for (length1 = isRec ? length1 : 0; length1 < sol; length1++) {
			maxDep2 = Math.min(MAX_DEPTH2, sol - length1);
			for (urfIdx = isRec ? urfIdx : 0; urfIdx < 6; urfIdx++) {
				if ((conjMask & 1 << urfIdx) != 0) {
					continue;
				}
				if (phase1PreMoves(maxPreMoves, -30, urfCubieCube[urfIdx], (int) (selfSym & 0xffff)) == 0) {
					return solution == null ? "Error 8" : solution;
				}
			}
		}
		return solution == null ? "Error" : solution;
	}

	protected int initPhase2Pre() {
		isRec = false;
		if (probe >= (solution == null ? probeMax : probeMin)) {
			return 0;
		}
		++probe;

		for (int i = valid1; i < depth1; i++) {
			CubieCube.CornMult(phase1Cubie[i], CubieCube.moveCube[move[i]], phase1Cubie[i + 1]);
			CubieCube.EdgeMult(phase1Cubie[i], CubieCube.moveCube[move[i]], phase1Cubie[i + 1]);
		}
		valid1 = depth1;
		phase2Cubie = phase1Cubie[depth1];

		int ret = initPhase2();
		if (ret == 0 || preMoveLen == 0 || ret == 2) {
			return ret;
		}

		int m = preMoves[preMoveLen - 1] / 3 * 3 + 1;
		phase2Cubie = new CubieCube();
		CubieCube.CornMult(CubieCube.moveCube[m], phase1Cubie[depth1], phase2Cubie);
		CubieCube.EdgeMult(CubieCube.moveCube[m], phase1Cubie[depth1], phase2Cubie);

		preMoves[preMoveLen - 1] += 2 - preMoves[preMoveLen - 1] % 3 * 2;
		ret = initPhase2();
		preMoves[preMoveLen - 1] += 2 - preMoves[preMoveLen - 1] % 3 * 2;
		return ret;
	}

	protected int initPhase2() {
		int p2corn = phase2Cubie.getCPermSym();
		int p2csym = p2corn & 0xf;
		p2corn >>= 4;
		int p2edge = phase2Cubie.getEPermSym();
		int p2esym = p2edge & 0xf;
		p2edge >>= 4;
		int p2mid = phase2Cubie.getMPerm();

		int prun = Math.max(
				CoordCube.getPruning(CoordCube.EPermCCombPPrun,
						p2edge * CoordCube.N_COMB + CoordCube.CCombPConj[CubieCube.Perm2CombP[p2corn] & 0xff][CubieCube.SymMultInv[p2esym][p2csym]]),
				CoordCube.getPruning(CoordCube.MCPermPrun,
						p2corn * CoordCube.N_MPERM + CoordCube.MPermConj[p2mid][p2csym]));

		if (prun >= maxDep2) {
			return prun > maxDep2 ? 2 : 1;
		}

		int depth2;
		for (depth2 = maxDep2 - 1; depth2 >= prun; depth2--) {
			int ret = phase2(p2edge, p2esym, p2corn, p2csym, p2mid, depth2, depth1, 10);
			if (ret < 0) {
				break;
			}
			depth2 -= ret;
			sol = 0;
			for (int i = 0; i < depth1 + depth2; i++) {
				appendSolMove(move[i]);
			}
			for (int i = preMoveLen - 1; i >= 0; i--) {
				appendSolMove(preMoves[i]);
			}
			solution = solutionToString();
		}

		if (depth2 != maxDep2 - 1) { //At least one solution has been found.
			maxDep2 = Math.min(MAX_DEPTH2, sol - length1);
			return probe >= probeMin ? 0 : 1;
		} else {
			return 1;
		}
	}

	/**
	 * @return
	 *      0: Found or Probe limit exceeded
	 *      1: Try Next Power
	 *      2: Try Next Axis
	 */
	protected int phase1(CoordCube node, int ssym, int maxl, int lm) {
		if (node.prun == 0 && maxl < 5) {
			if (allowShorter || maxl == 0) {
				depth1 -= maxl;
				int ret = initPhase2Pre();
				depth1 += maxl;
				return ret;
			} else {
				return 1;
			}
		}

		int skipMoves = CubieCube.getSkipMoves(ssym);

		for (int axis = 0; axis < 18; axis += 3) {
			if (axis == lm || axis == lm - 9) {
				continue;
			}
			for (int power = 0; power < 3; power++) {
				int m = axis + power;

				if (isRec && m != move[depth1 - maxl]
						|| skipMoves != 0 && (skipMoves & 1 << m) != 0) {
					continue;
				}

				int prun = nodeUD[maxl].doMovePrun(node, m, true);
				if (prun > maxl) {
					break;
				} else if (prun == maxl) {
					continue;
				}

				prun = nodeUD[maxl].doMovePrunConj(node, m);
				if (prun > maxl) {
					break;
				} else if (prun == maxl) {
					continue;
				}


				move[depth1 - maxl] = m;
				valid1 = Math.min(valid1, depth1 - maxl);
				int ret = phase1(nodeUD[maxl], ssym & (int) CubieCube.moveCubeSym[m], maxl - 1, axis);
				if (ret == 0) {
					return 0;
				} else if (ret == 2) {
					break;
				}
			}
		}
		return 1;
	}

	void appendSolMove(int curMove) {
		if (sol == 0) {
			moveSol[sol++] = curMove;
			return;
		}
		int axisCur = curMove / 3;
		int axisLast = moveSol[sol - 1] / 3;
		if (axisCur == axisLast) {
			int pow = (curMove % 3 + moveSol[sol - 1] % 3 + 1) % 4;
			if (pow == 3) {
				sol--;
			} else {
				moveSol[sol - 1] = axisCur * 3 + pow;
			}
			return;
		}
		if (sol > 1
				&& axisCur % 3 == axisLast % 3
				&& axisCur == moveSol[sol - 2] / 3) {
			int pow = (curMove % 3 + moveSol[sol - 2] % 3 + 1) % 4;
			if (pow == 3) {
				moveSol[sol - 2] = moveSol[sol - 1];
				sol--;
			} else {
				moveSol[sol - 2] = axisCur * 3 + pow;
			}
			return;
		}
		moveSol[sol++] = curMove;
	}

	//-1: no solution found
	// X: solution with X moves shorter than expectation. Hence, the length of the solution is  depth - X
	protected int phase2(int edge, int esym, int corn, int csym, int mid, int maxl, int depth, int lm) {
		if (edge == 0 && corn == 0 && mid == 0) {
			return maxl;
		}
		int moveMask = Util.ckmv2bit[lm];
		for (int m = 0; m < 10; m++) {
			if ((moveMask >> m & 1) != 0) {
				m += 0x42 >> m & 3;
				continue;
			}
			int midx = CoordCube.MPermMove[mid][m];
			int cornx = CoordCube.CPermMove[corn][CubieCube.SymMoveUD[csym][m]];
			int csymx = CubieCube.SymMult[cornx & 0xf][csym];
			cornx >>= 4;
			int edgex = CoordCube.EPermMove[edge][CubieCube.SymMoveUD[esym][m]];
			int esymx = CubieCube.SymMult[edgex & 0xf][esym];
			edgex >>= 4;
			int edgei = CubieCube.getPermSymInv(edgex, esymx, false);
			int corni = CubieCube.getPermSymInv(cornx, csymx, true);

			int prun = CoordCube.getPruning(CoordCube.EPermCCombPPrun,
					(edgei >> 4) * CoordCube.N_COMB + CoordCube.CCombPConj[CubieCube.Perm2CombP[corni >> 4] & 0xff][CubieCube.SymMultInv[edgei & 0xf][corni & 0xf]]);
			if (prun > maxl + 1) {
				break;
			} else if (prun >= maxl) {
				m += 0x42 >> m & 3 & (maxl - prun);
				continue;
			}
			prun = Math.max(
					CoordCube.getPruning(CoordCube.MCPermPrun,
							cornx * CoordCube.N_MPERM + CoordCube.MPermConj[midx][csymx]),
					CoordCube.getPruning(CoordCube.EPermCCombPPrun,
							edgex * CoordCube.N_COMB + CoordCube.CCombPConj[CubieCube.Perm2CombP[cornx] & 0xff][CubieCube.SymMultInv[esymx][csymx]]));
			if (prun >= maxl) {
				m += 0x42 >> m & 3 & (maxl - prun);
				continue;
			}
			int ret = phase2(edgex, esymx, cornx, csymx, midx, maxl - 1, depth + 1, m);
			if (ret >= 0) {
				move[depth] = Util.ud2std[m];
				return ret;
			}
		}
		return -1;
	}

	protected String solutionToString() {
		StringBuffer sb = new StringBuffer();
		if (urfIdx < 3) {
			for (int s = 0; s < sol; s++) {
				sb.append(Util.move2str[CubieCube.urfMove[urfIdx][moveSol[s]]]).append(' ');
			}
		} else {
			for (int s = sol - 1; s >= 0; s--) {
				sb.append(Util.move2str[CubieCube.urfMove[urfIdx][moveSol[s]]]).append(' ');
			}
		}
		return sb.toString();
	}
}
