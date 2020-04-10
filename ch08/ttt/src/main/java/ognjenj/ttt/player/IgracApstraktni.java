package ognjenj.ttt.player;

import ognjenj.ttt.game.Potez;
import ognjenj.ttt.game.Tabla;

import java.util.LinkedList;

/**
 * @author ognjen
 */
public abstract class IgracApstraktni {

    private final String mojZnak;
    protected LinkedList<Potez> istorijaPoteza = new LinkedList<>();

    public IgracApstraktni(String mySign) {
        this.mojZnak = mySign;
    }

    public void resetujIstorijuPoteza() {
        istorijaPoteza.clear();
    }

    /**
     * Method used by the game to signal to the player that he has to pick the next move.
     *
     * @param trenutnoStanjeIgre The current state of the board before the move is played
     * @return The index of the field on the board on which the sign is placed.
     */
    public abstract int izaberiPotez(final Tabla trenutnoStanjeIgre);

    /**
     * Method used by the game to notify players of moves.
     *
     * @param potez The move that was performed.
     */
    public abstract void obradiOdigraniPotez(Potez potez);

    public String getMojZnak() {
        return mojZnak;
    }
}
