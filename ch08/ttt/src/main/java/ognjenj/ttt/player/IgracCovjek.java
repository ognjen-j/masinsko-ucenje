package ognjenj.ttt.player;

import ognjenj.ttt.game.Potez;
import ognjenj.ttt.game.Tabla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author ognjen
 */
public class IgracCovjek extends IgracApstraktni {

    private final BufferedReader consoleIn;

    public IgracCovjek(String mySign) {
        super(mySign);
        this.consoleIn = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public int izaberiPotez(final Tabla trenutnoStanjeIgre) {
        int numerickaOznakaPoteza = -1;
        try {
            String potez;
            boolean ispravno;
            do {
                System.out.println("Igrac " + this.getMojZnak() + ", unesi poziciju (0-8): ");
                potez = consoleIn.readLine();
                try {
                    numerickaOznakaPoteza = Integer.parseInt(potez);
                    ispravno = true;
                } catch (NumberFormatException ex) {
                    ispravno = false;
                    System.out.println("Potez je nevalidan, pokusajte ponovo.");
                }
            } while (!ispravno);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return numerickaOznakaPoteza;
    }

    @Override
    public void obradiOdigraniPotez(Potez potez) {
        this.istorijaPoteza.addLast(potez);
        if (potez.daLiJeIgraZavrsena()) {
            if (potez.isNerijeseno() && potez.getIgracOdigrao().equals(this)) {
                System.out.println("Igra je nerijesena.");
            } else {
                if (potez.getIgracOdigrao().equals(this)) {
                    System.out.println("Igrac " + this.getMojZnak() + " je pobijedio.");
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IgracCovjek) {
            IgracCovjek h = (IgracCovjek) o;
            return h.getMojZnak().equals(this.getMojZnak());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getMojZnak().hashCode();
    }

}
