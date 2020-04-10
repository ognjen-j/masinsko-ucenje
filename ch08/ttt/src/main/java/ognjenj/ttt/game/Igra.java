package ognjenj.ttt.game;

import ognjenj.ttt.player.IgracApstraktni;
import ognjenj.ttt.player.IgracCovjek;

import java.util.LinkedList;

/**
 * @author ognjen
 */
public class Igra {

    private final IgracApstraktni x;
    private final IgracApstraktni o;
    private Tabla tabla;
    private IgracApstraktni igracNaRedu;
    private LinkedList<Potez> istorijaPoteza = new LinkedList<>();

    public Igra(IgracApstraktni x, IgracApstraktni y) {
        this.x = x;
        this.o = y;
        this.igracNaRedu = x;
        tabla = new Tabla();
        istorijaPoteza.clear();
    }

    public void pokreniIgru() {
        x.resetujIstorijuPoteza();
        o.resetujIstorijuPoteza();
        while (true) {
            int sljedecaPozicija = igracNaRedu.izaberiPotez(tabla);
            if (tabla.daLiJePotezDozvoljen(sljedecaPozicija)) {
                Potez noviPotez = tabla.odigrajPotez(igracNaRedu, sljedecaPozicija);

                if (x instanceof IgracCovjek || o instanceof IgracCovjek) {
                    System.out.println("Igrac " + igracNaRedu.getMojZnak() + " odigrao:\n" + tabla);
                }
                if (igracNaRedu.getMojZnak().equals("x")) {
                    igracNaRedu = o;
                } else {
                    igracNaRedu = x;
                }

                istorijaPoteza.add(noviPotez);
                x.obradiOdigraniPotez(noviPotez);
                o.obradiOdigraniPotez(noviPotez);
                if (noviPotez.daLiJeIgraZavrsena()) {
                    break;
                }
            } else {
                System.out.println("Move (" + sljedecaPozicija + " by " + igracNaRedu.getMojZnak() + ") has already been played or is not allowed.");
            }
        }
        if (!(x instanceof IgracCovjek || o instanceof IgracCovjek)) {
            System.out.println(tabla);
        }
    }

    public Tabla getTabla() {
        return tabla;
    }

    public void setTabla(Tabla tabla) {
        this.tabla = tabla;
    }

    public IgracApstraktni getX() {
        return x;
    }

    public IgracApstraktni getO() {
        return o;
    }

    public LinkedList<Potez> getIstorijaPoteza() {
        return istorijaPoteza;
    }

    public void setIstorijaPoteza(LinkedList<Potez> istorijaPoteza) {
        this.istorijaPoteza = istorijaPoteza;
    }
}
