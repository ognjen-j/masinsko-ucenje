package ognjenj.ttt.game;

import ognjenj.ttt.player.IgracApstraktni;

/**
 * @author ognjen
 */
public class Tabla {

    private final int[] polja = new int[9];
    private int kodovanoX = 0;
    private int kodovanoY = 0;

    public Tabla() {
    }

    public Tabla(Tabla kopija) {
        for (int cnt = 0; cnt < 9; cnt++) {
            this.polja[cnt] = kopija.getPolja()[cnt];
        }
        this.kodovanoX = kopija.getKodovanoX();
        this.kodovanoY = kopija.getKodovanoY();
    }

    /**
     * Method that places the sign on the board. X is encoded as 1, Y is encoded as -1.
     *
     * @param igracNaRedu   One of the players in the game
     * @param odigranoPolje Number from the interval [0, 8]
     * @return A move that was played.
     */
    public Potez odigrajPotez(IgracApstraktni igracNaRedu, int odigranoPolje) {
        Tabla prijePoteza = new Tabla(this);
        if (igracNaRedu.getMojZnak().equals("x")) {
            polja[odigranoPolje] = 1;
            kodovanoX += (int) (Math.pow(2, odigranoPolje));
        } else {
            polja[odigranoPolje] = -1;
            kodovanoY += (int) (Math.pow(2, odigranoPolje));
        }
        boolean dobitniX = daLiJePotezDobitni(kodovanoX);
        boolean dobitniY = daLiJePotezDobitni(kodovanoY);
        boolean igraZavrsena = dobitniX || dobitniY || (kodovanoY + kodovanoX == 511);
        return new Potez(igracNaRedu, odigranoPolje, prijePoteza, igraZavrsena, !dobitniX && !dobitniY & igraZavrsena);
    }

    private boolean daLiJePotezDobitni(int kodovanaVrijednost) {
        return (kodovanaVrijednost & 73) == 73
                || (kodovanaVrijednost & 146) == 146
                || (kodovanaVrijednost & 292) == 292
                || (kodovanaVrijednost & 7) == 7
                || (kodovanaVrijednost & 56) == 56
                || (kodovanaVrijednost & 448) == 448
                || (kodovanaVrijednost & 273) == 273
                || (kodovanaVrijednost & 84) == 84;
    }

    public boolean daLiJePotezDozvoljen(int pozicija) {
        return pozicija >= 0 && polja[pozicija] == 0;
    }

    @Override
    public String toString() {
        String tekstualniOpis = "";
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tekstualniOpis += (polja[3 * i + j] == 1 ? "x" : (polja[3 * i + j] == -1 ? "o" : " ")) + (j < 2 ? " |" : "");
            }
            if (i < 2) {
                tekstualniOpis += "\n";
            }
        }
        return tekstualniOpis;
    }

    public String jedinstveniOpisStanja(String znak) {
        if (znak.equals("x")) {
            return kodovanoX + "#" + kodovanoY;
        } else {
            return kodovanoY + "#" + kodovanoX;
        }
    }

    public int getKodovanoX() {
        return kodovanoX;
    }

    public int getKodovanoY() {
        return kodovanoY;
    }

    public int[] getPolja() {
        return polja;
    }

    /**
     * Returns the polja encoded so that the current player's placements are always marked by 1, and the other player's
     * placements by -1. The enables the network to use the same model, regardless of whose turn is next.
     *
     * @param mojZnak "x" or "y"
     * @return Returns polja (the original array) if mySign.equals("x"). Otherwise, newFields[i] = 0 - polja[i].
     */
    public int[] getPolja(String mojZnak) {
        if (mojZnak.equals("x")) {
            return polja;
        } else {
            int[] novaPolja = new int[9];
            for (int cnt = 0; cnt < 9; cnt++) {
                novaPolja[cnt] = -polja[cnt];
            }
            return novaPolja;
        }
    }

}
