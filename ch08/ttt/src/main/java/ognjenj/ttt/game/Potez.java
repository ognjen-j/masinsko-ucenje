package ognjenj.ttt.game;

import ognjenj.ttt.player.IgracApstraktni;

/**
 * @author ognjen
 */
public class Potez {

    private final IgracApstraktni igracOdigrao;
    private final int odigranaPozicija;
    private final Tabla stanjeIgrePrijePoteza;
    private final boolean igraZavrsena;
    private final boolean nerijeseno;
    private double nagrada = 0.0;

    public Potez(IgracApstraktni igracOdigrao, int odigranaPozicija, Tabla stanjeIgrePrijePoteza, boolean igraZavrsena, boolean nerijeseno) {
        this.igracOdigrao = igracOdigrao;
        this.odigranaPozicija = odigranaPozicija;
        this.stanjeIgrePrijePoteza = stanjeIgrePrijePoteza;
        this.igraZavrsena = igraZavrsena;
        this.nerijeseno = nerijeseno;
    }

    public IgracApstraktni getIgracOdigrao() {
        return igracOdigrao;
    }

    public int getOdigranaPozicija() {
        return odigranaPozicija;
    }

    public Tabla getStanjeIgrePrijePoteza() {
        return stanjeIgrePrijePoteza;
    }

    public boolean daLiJeIgraZavrsena() {
        return igraZavrsena;
    }

    public boolean isNerijeseno() {
        return nerijeseno;
    }

    public double getNagrada() {
        return nagrada;
    }

    public void setNagrada(double nagrada) {
        this.nagrada = nagrada;
    }
}
