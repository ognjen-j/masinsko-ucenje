package ognjenj.ttt.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ognjenj.ttt.game.Potez;
import ognjenj.ttt.game.Tabla;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * @author ognjen
 */
public class IgracQ extends IgracApstraktni {

    private final double raspadanjeFaktoraIstrazivanja;
    private final double korakUcenja = 0.5;
    private final double gama = 0.9;
    private Map<String, double[]> qTabela = new HashMap<>();
    private double faktorIstrazivanja = 0.0;
    private Gson gson;

    public IgracQ(String mojZnak, double inicijalniFaktorIstrazivanja, double raspadanjeFaktoraIstrazivanja, Optional<File> lokacijaPostojecegModela) {
        super(mojZnak);
        this.faktorIstrazivanja = inicijalniFaktorIstrazivanja;
        this.raspadanjeFaktoraIstrazivanja = raspadanjeFaktoraIstrazivanja;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .serializeSpecialFloatingPointValues()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping()
                .create();
        if (lokacijaPostojecegModela.isPresent() && lokacijaPostojecegModela.get().exists() && lokacijaPostojecegModela.get().isFile()) {
            try {
                String model = FileUtils.readFileToString(lokacijaPostojecegModela.get(), "UTF-8");
                Type tipMape = new TypeToken<Map<String, double[]>>() {
                }.getType();
                qTabela = gson.fromJson(model, tipMape);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    @Override
    public int izaberiPotez(Tabla trenutnoStanjeIgre) {
        double najvecaVrijednost = -Double.MAX_VALUE;
        int najboljaPozicija = -1;
        String idTable = trenutnoStanjeIgre.jedinstveniOpisStanja(this.getMojZnak());
        double[] izlazi = qTabela.computeIfAbsent(idTable, e -> inicijalizujPotezeZaStanje());
        if (idTable.equals("3#260")) {
            for (int i = 0; i < izlazi.length; i++) {
                System.out.format("Confidence" + i + ": %1.10f", izlazi[i]);
                System.out.println();
            }
        }
        if (Math.random() < this.faktorIstrazivanja) {
            // pick a random position, to explore additional moves
            do {
                najboljaPozicija = new Random().nextInt(9);
            } while (!trenutnoStanjeIgre.daLiJePotezDozvoljen(najboljaPozicija));
        } else {
            for (int brPozicije = 0; brPozicije < 9; brPozicije++) {
                if (trenutnoStanjeIgre.daLiJePotezDozvoljen(brPozicije)) {
                    // we will prevent illegal moves from being played, since handling the error response would be more complicated
                    if (izlazi[brPozicije] > najvecaVrijednost) {
                        najvecaVrijednost = izlazi[brPozicije];
                        najboljaPozicija = brPozicije;
                    }
                }
            }
        }
        return najboljaPozicija;
    }

    @Override
    public void obradiOdigraniPotez(Potez potez) {
        if (potez.getIgracOdigrao().equals(this)) {
            istorijaPoteza.addLast(potez);
        }
        if (potez.daLiJeIgraZavrsena()) {
            double nagrada = 0.0;
            if (!potez.isNerijeseno() && potez.getIgracOdigrao().equals(this)) {
                nagrada = 1.0;
            } else if (!potez.isNerijeseno() && !potez.getIgracOdigrao().equals(this)) {
                nagrada = -1.0;
            }
            for (int brPoteza = istorijaPoteza.size() - 1; brPoteza >= 0; brPoteza--) {
                Potez jedanPotez = istorijaPoteza.get(brPoteza);
                jedanPotez.setNagrada(nagrada);
                nagrada *= 0.8;

                Potez stanjeIgre = istorijaPoteza.get(brPoteza);
                Tabla tablaPrijePoteza = stanjeIgre.getStanjeIgrePrijePoteza();
                Tabla tablaPoslijePoteza = new Tabla(tablaPrijePoteza);
                tablaPoslijePoteza.odigrajPotez(this, stanjeIgre.getOdigranaPozicija());
                String trenutnoStanjeTable = tablaPrijePoteza.jedinstveniOpisStanja(this.getMojZnak());
                String sljedeceStanjeTable = tablaPoslijePoteza.jedinstveniOpisStanja(this.getMojZnak());
                double qTrenutno = qTabela.computeIfAbsent(trenutnoStanjeTable, e -> inicijalizujPotezeZaStanje())[stanjeIgre.getOdigranaPozicija()];

                double qSljedece = -Double.MAX_VALUE;
                double[] izlaziSljedecegStanja = qTabela.computeIfAbsent(sljedeceStanjeTable, e -> inicijalizujPotezeZaStanje());
                if (jedanPotez.daLiJeIgraZavrsena()) {
                    qSljedece = nagrada * 10.0;
                } else {
                    for (int qBrojac = 0; qBrojac < izlaziSljedecegStanja.length; qBrojac++) {
                        if (qSljedece < izlaziSljedecegStanja[qBrojac] && tablaPoslijePoteza.daLiJePotezDozvoljen(qBrojac)) {
                            qSljedece = izlaziSljedecegStanja[qBrojac];
                        }
                    }
                }
                double qNovo = qTrenutno + korakUcenja * (stanjeIgre.getNagrada() + gama * qSljedece - qTrenutno);
                qTabela.computeIfAbsent(trenutnoStanjeTable, e -> inicijalizujPotezeZaStanje())[stanjeIgre.getOdigranaPozicija()] = qNovo;
            }
            istorijaPoteza.clear();
            if (this.faktorIstrazivanja > 0.1) {
                this.faktorIstrazivanja -= this.raspadanjeFaktoraIstrazivanja;
            }
            File modelFile = new File(this.getMojZnak() + ".q.model");
            if(modelFile.exists()) {
	        try {
                    Files.copy(modelFile.toPath(), new File(this.getMojZnak() + ".q.model.backup").toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
	    }
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(this.getMojZnak() + ".q.model"), false)), true)) {
                writer.println(gson.toJson(qTabela));
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private double[] inicijalizujPotezeZaStanje() {
        double[] rezultat = new double[9];
        for (int i = 0; i < 9; i++) {
            rezultat[i] = 0.0;
        }
        return rezultat;
    }
}
