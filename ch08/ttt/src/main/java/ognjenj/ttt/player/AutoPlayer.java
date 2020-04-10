package ognjenj.ttt.player;

import ognjenj.ttt.game.Potez;
import ognjenj.ttt.game.Tabla;

/**
 * @author ognjen
 */
public class AutoPlayer extends IgracApstraktni {

    private final int[] moveSequence = new int[]{8, 2, 5, 0, 1, 2, 3, 4, 5};
    private int moveIndex = 0;

    public AutoPlayer(String mySign) {
        super(mySign);
    }

    @Override
    public void resetujIstorijuPoteza() {
        super.resetujIstorijuPoteza();
        moveIndex = 0;
    }

    @Override
    public int izaberiPotez(Tabla currentBoardState) {
        int selectedMove = 6;
        if (currentBoardState.daLiJePotezDozvoljen(moveSequence[moveIndex])) {
            selectedMove = moveSequence[moveIndex];
        } else {
            for (int cnt = 0; cnt < 9; cnt++) {
                if (currentBoardState.daLiJePotezDozvoljen(cnt)) {
                    selectedMove = cnt;
                    break;
                }

            }
        }
        moveIndex++;
        return selectedMove;
    }

    @Override
    public void obradiOdigraniPotez(Potez move) {

    }

}
