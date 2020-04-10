package ognjenj.ttt.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ognjenj.ttt.game.Potez;
import ognjenj.ttt.game.Tabla;
import ognjenj.ttt.util.MathUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.DoubleFunction;

/**
 * @author ognjen
 */
public class IgracANN extends IgracApstraktni {

    private final static int VELICINA_ISKUSTVENE_MEMORIJE = 1000;
    private final static int VELICINA_BLOKA_ZA_OBUCAVANJE_IZ_ISKUSTVENE_MEMORIJE = 200;
    private final LinkedList<Potez> iskustvenaMemorija = new LinkedList<>();
    private final double raspadanjeFaktoraIstrazivanja;
    private final double gama = 0.9;
    private final Gson gson;
    private TroslojnaMreza mreza = new TroslojnaMreza(9, 15, 9);
    private double faktorIstrazivanja = 0.0;

    public IgracANN(String mojZnak, double inicijalniFaktorIstrazivanja, double raspadanjeFaktoraIstrazivanja, Optional<File> lokacijaPostojecegModela) {
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
                mreza = gson.fromJson(model, TroslojnaMreza.class);
                if (mreza.aktivacionaFunkcija == null) {
                    mreza.aktivacionaFunkcija = (value) -> {
                        return MathUtil.sigmoid(value);
                    };
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    @Override
    public int izaberiPotez(final Tabla trenutnoStanjeIgre) {
        double maksimalnaVrijednost = -Double.MAX_VALUE;
        int najboljaPozicijaZaIgru = -1;
        // input array always contains 1's on this player's positions, regardless of the sign
        // the sign is only for display purposes
        int[] ulazi = trenutnoStanjeIgre.getPolja(this.getMojZnak());
        double[] izlazi;
        if (Math.random() < this.faktorIstrazivanja) {
            // pick a random position, to explore additional moves
            izlazi = new double[9];
            for (int brPozicija = 0; brPozicija < 9; brPozicija++) {
                izlazi[brPozicija] = Math.random() / 100.0;
            }
        } else {
            izlazi = mreza.izracunajIzlaz(ulazi);
        }
        for (int brPozicija = 0; brPozicija < 9; brPozicija++) {
            if (trenutnoStanjeIgre.daLiJePotezDozvoljen(brPozicija)) {
                // we will prevent illegal moves from being played, since handling the error response would be more complicated
                if (izlazi[brPozicija] > maksimalnaVrijednost) {
                    maksimalnaVrijednost = izlazi[brPozicija];
                    najboljaPozicijaZaIgru = brPozicija;
                }
            }
        }
        return najboljaPozicijaZaIgru;
    }

    @Override
    public void obradiOdigraniPotez(Potez potez) {
        if (!potez.daLiJeIgraZavrsena() && potez.getIgracOdigrao().equals(this)) {
            istorijaPoteza.addLast(potez);
        }
        if (potez.daLiJeIgraZavrsena()) {
            double nagrada = 0.0;
            if (!potez.isNerijeseno() && potez.getIgracOdigrao().equals(this)) {
                nagrada = 1.0;
            } else if (!potez.isNerijeseno() && !potez.getIgracOdigrao().equals(this)) {
                nagrada = 0.0;
            }
            for (int cnt = istorijaPoteza.size() - 1; cnt >= istorijaPoteza.size() - 1; cnt--) {
                //for (int cnt = istorijaPoteza.size() - 1; cnt >= 0; cnt--) {
                Potez jedanPotezIzIstorije = istorijaPoteza.get(cnt);
                jedanPotezIzIstorije.setNagrada(nagrada);
                iskustvenaMemorija.offer(jedanPotezIzIstorije);
                nagrada *= 0.5;
                if (iskustvenaMemorija.size() > VELICINA_ISKUSTVENE_MEMORIJE) {
                    iskustvenaMemorija.poll();
                }
                if (iskustvenaMemorija.size() == VELICINA_ISKUSTVENE_MEMORIJE) {
                    // pick a random batch and send to network for training
                    ArrayList<Potez> kopijaListe = new ArrayList<>(iskustvenaMemorija);
                    Collections.shuffle(kopijaListe);
                    // static network, for stability
                    //ThreeLayerNetwork targetNetwork = new TroslojnaMreza(network);
                    TroslojnaMreza targetNetwork = mreza;
                    for (int replayMemoryCnt = 0; replayMemoryCnt < VELICINA_BLOKA_ZA_OBUCAVANJE_IZ_ISKUSTVENE_MEMORIJE; replayMemoryCnt++) {
                        double largestQValueForNextState = -Double.MAX_VALUE;
                        Potez gameState = kopijaListe.get(cnt);
                        Tabla gameBoardBeforeTheMove = gameState.getStanjeIgrePrijePoteza();
                        Tabla gameBoardAfterTheMove = new Tabla(gameBoardBeforeTheMove);
                        int[] input = gameState.getStanjeIgrePrijePoteza().getPolja(this.getMojZnak());
                        double[] qValuesForCurrentState = targetNetwork.izracunajIzlaz(input);
                        gameBoardAfterTheMove.odigrajPotez(this, gameState.getOdigranaPozicija());
                        double[] qValuesForNextState = targetNetwork.izracunajIzlaz(input);
                        for (int qcnt = 0; qcnt < qValuesForNextState.length; qcnt++) {
                            if (qValuesForNextState[qcnt] > largestQValueForNextState && gameBoardAfterTheMove.daLiJePotezDozvoljen(qcnt)) {
                                largestQValueForNextState = qValuesForNextState[qcnt];
                            }
                        }
                        double[] idealQValuesForCurrentState = new double[qValuesForCurrentState.length];
                        for (int qcnt = 0; qcnt < qValuesForCurrentState.length; qcnt++) {
                            if (qcnt == gameState.getOdigranaPozicija()) {
                                //idealQValuesForCurrentState[qcnt] = gameState.getReward() + discount * largestQValueForNextState;
                                //idealQValuesForCurrentState[qcnt] = gameState.getReward() + discount * largestQValueForNextState;
                                idealQValuesForCurrentState[qcnt] = gameState.getNagrada();
                            } else {
                                idealQValuesForCurrentState[qcnt] = qValuesForCurrentState[qcnt];
                            }
                        }
                        mreza.propagirajGresku(input, qValuesForCurrentState, idealQValuesForCurrentState);
                    }
                }
            }
            istorijaPoteza.clear();
            if (this.faktorIstrazivanja > 0.1) {
                this.faktorIstrazivanja -= this.raspadanjeFaktoraIstrazivanja;
            }
            File modelFile = new File(this.getMojZnak() + ".model");
	    if(modelFile.exists()) {
                try {
                    Files.copy(modelFile.toPath(), new File(this.getMojZnak() + ".model.backup").toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
	    }
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(this.getMojZnak() + ".model"), false)), true)) {
                writer.println(gson.toJson(mreza));
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public static class TroslojnaMreza implements Serializable {

        private static final double korakUcenja = 0.5;
        private final int brojNeuronaIzUlaznogSloja;
        private final int brojNeuronaIzSkrivenogSloja;
        private final int brojNeuronaIzIzlaznogSloja;

        private final double[][] tezineVezaOdUlaznogDoSkrivenogSloja;
        private final double[][] tezineVezaOdSkrivenogDoIzlaznogSloja;

        private transient DoubleFunction<Double> aktivacionaFunkcija;

        public TroslojnaMreza(int brojNeuronaIzUlaznogSloja, int brojNeuronaIzSkrivenogSloja, int brojNeuronaIzIzlaznogSloja) {
            this.brojNeuronaIzUlaznogSloja = brojNeuronaIzUlaznogSloja;
            this.brojNeuronaIzSkrivenogSloja = brojNeuronaIzSkrivenogSloja;
            this.brojNeuronaIzIzlaznogSloja = brojNeuronaIzIzlaznogSloja;
            this.tezineVezaOdUlaznogDoSkrivenogSloja = new double[brojNeuronaIzUlaznogSloja][brojNeuronaIzSkrivenogSloja];
            this.tezineVezaOdSkrivenogDoIzlaznogSloja = new double[brojNeuronaIzSkrivenogSloja][brojNeuronaIzIzlaznogSloja];
            this.aktivacionaFunkcija = (value) -> {
                return 1.0 / 1.0 + Math.exp(-value);
            };
            for (int i = 0; i < brojNeuronaIzUlaznogSloja; i++) {
                for (int j = 0; j < brojNeuronaIzSkrivenogSloja; j++) {
                    tezineVezaOdUlaznogDoSkrivenogSloja[i][j] = Math.random() / 100.0;
                }
            }
            for (int i = 0; i < brojNeuronaIzSkrivenogSloja; i++) {
                for (int j = 0; j < brojNeuronaIzIzlaznogSloja; j++) {
                    tezineVezaOdSkrivenogDoIzlaznogSloja[i][j] = Math.random() / 100.0;
                }
            }
        }

        public TroslojnaMreza(TroslojnaMreza original) {
            this.brojNeuronaIzUlaznogSloja = original.brojNeuronaIzUlaznogSloja;
            this.brojNeuronaIzSkrivenogSloja = original.brojNeuronaIzSkrivenogSloja;
            this.brojNeuronaIzIzlaznogSloja = original.brojNeuronaIzIzlaznogSloja;
            this.tezineVezaOdUlaznogDoSkrivenogSloja = new double[brojNeuronaIzUlaznogSloja][brojNeuronaIzSkrivenogSloja];
            this.tezineVezaOdSkrivenogDoIzlaznogSloja = new double[brojNeuronaIzSkrivenogSloja][brojNeuronaIzIzlaznogSloja];
            this.aktivacionaFunkcija = original.aktivacionaFunkcija;
            for (int i = 0; i < brojNeuronaIzUlaznogSloja; i++) {
                System.arraycopy(original.tezineVezaOdUlaznogDoSkrivenogSloja[i], 0, this.tezineVezaOdUlaznogDoSkrivenogSloja[i], 0, brojNeuronaIzSkrivenogSloja);
            }
            for (int i = 0; i < brojNeuronaIzSkrivenogSloja; i++) {
                System.arraycopy(original.tezineVezaOdSkrivenogDoIzlaznogSloja[i], 0, this.tezineVezaOdSkrivenogDoIzlaznogSloja[i], 0, brojNeuronaIzIzlaznogSloja);
            }
        }

        public TroslojnaMreza(double[][] tezineVezaOdUlaznogDoSkrivenogSloja, double[][] tezineVezaOdSkrivenogDoIzlaznogSloja, DoubleFunction<Double> aktivacionaFunkcija) {
            this.tezineVezaOdSkrivenogDoIzlaznogSloja = tezineVezaOdSkrivenogDoIzlaznogSloja;
            this.tezineVezaOdUlaznogDoSkrivenogSloja = tezineVezaOdUlaznogDoSkrivenogSloja;
            this.brojNeuronaIzUlaznogSloja = tezineVezaOdUlaznogDoSkrivenogSloja.length;
            this.brojNeuronaIzSkrivenogSloja = tezineVezaOdSkrivenogDoIzlaznogSloja.length;
            this.brojNeuronaIzIzlaznogSloja = tezineVezaOdSkrivenogDoIzlaznogSloja[0].length;
            this.aktivacionaFunkcija = aktivacionaFunkcija;
        }

        public double[] izracunajIzlaz(int[] ulazi) {
            double[] izlaziSkrivenogSloja = izracunajIzlazeSkrivenogSloja(ulazi);
            double[] izlaziIzlaznogSloja = new double[brojNeuronaIzIzlaznogSloja];
            for (int brojacIzlaznihNeurona = 0; brojacIzlaznihNeurona < brojNeuronaIzIzlaznogSloja; brojacIzlaznihNeurona++) {
                double ulazNeurona = 0;
                for (int brojacSkrivenihNeurona = 0; brojacSkrivenihNeurona < brojNeuronaIzSkrivenogSloja; brojacSkrivenihNeurona++) {
                    ulazNeurona += izlaziSkrivenogSloja[brojacSkrivenihNeurona] * tezineVezaOdSkrivenogDoIzlaznogSloja[brojacSkrivenihNeurona][brojacIzlaznihNeurona];
                }
                izlaziIzlaznogSloja[brojacIzlaznihNeurona] = aktivacionaFunkcija.apply(ulazNeurona);
            }
            return izlaziIzlaznogSloja;
        }

        public double[] izracunajIzlazeSkrivenogSloja(int[] ulazi) {
            double[] izlaziSkrivenogSloja = new double[brojNeuronaIzSkrivenogSloja];
            for (int brojacSkrivenihNeurona = 0; brojacSkrivenihNeurona < brojNeuronaIzSkrivenogSloja; brojacSkrivenihNeurona++) {
                double ulazNeurona = 0;
                for (int brojacUlaznihNeurona = 0; brojacUlaznihNeurona < brojNeuronaIzUlaznogSloja; brojacUlaznihNeurona++) {
                    ulazNeurona += ulazi[brojacUlaznihNeurona] * tezineVezaOdUlaznogDoSkrivenogSloja[brojacUlaznihNeurona][brojacSkrivenihNeurona];
                }
                izlaziSkrivenogSloja[brojacSkrivenihNeurona] = aktivacionaFunkcija.apply(ulazNeurona);
            }
            return izlaziSkrivenogSloja;
        }

        /**
         * Backpropagation implementation.
         *
         * @param ulazi         Array of inputs into the network that produced the error. The length of the array is equal to the
         *                      number of neurons in the input layer.
         * @param stvarniIzlazi The output of the network for the given input. If the array is null, then the output will
         *                      be calculated by feedforward.
         * @param idealniIzlazi The ideal output used to calculate the error. The length must be equal to the number of
         *                      neurons in the output layer.
         */
        public void propagirajGresku(int[] ulazi, double[] stvarniIzlazi, double[] idealniIzlazi) {
            double[] izlaziSkrivenogSloja = izracunajIzlazeSkrivenogSloja(ulazi);
            if (stvarniIzlazi == null) {
                stvarniIzlazi = izracunajIzlaz(ulazi);
            }
            // weights going into the output layer
            double[] deltaIzlaznihNeurona = new double[brojNeuronaIzIzlaznogSloja];
            double[] deltaSkrivenihNeurona = new double[brojNeuronaIzSkrivenogSloja];
            double[] uticajTezinaNaIzlazniSloj = new double[brojNeuronaIzSkrivenogSloja];
            double[][] promjeneTezinaOdSkrivenogDoIzlaznogSloja = new double[brojNeuronaIzSkrivenogSloja][brojNeuronaIzIzlaznogSloja];
            double[][] promjeneTezinaOdUlaznogDoSkrivenogSloja = new double[brojNeuronaIzUlaznogSloja][brojNeuronaIzSkrivenogSloja];
            for (int brojacIzlaznihNeurona = 0; brojacIzlaznihNeurona < brojNeuronaIzIzlaznogSloja; brojacIzlaznihNeurona++) {
                // for sigmoid
                double izvodAktivacioneFunkcije = stvarniIzlazi[brojacIzlaznihNeurona] * (1 - stvarniIzlazi[brojacIzlaznihNeurona]);
                deltaIzlaznihNeurona[brojacIzlaznihNeurona] = (stvarniIzlazi[brojacIzlaznihNeurona] - idealniIzlazi[brojacIzlaznihNeurona]) * izvodAktivacioneFunkcije;
                for (int brojacSkrivenihNeurona = 0; brojacSkrivenihNeurona < brojNeuronaIzSkrivenogSloja; brojacSkrivenihNeurona++) {
                    double promjenaTezineOdSkrivenogDoIzlaznogSloja = deltaIzlaznihNeurona[brojacIzlaznihNeurona] * izlaziSkrivenogSloja[brojacSkrivenihNeurona];
                    promjeneTezinaOdSkrivenogDoIzlaznogSloja[brojacSkrivenihNeurona][brojacIzlaznihNeurona] = promjenaTezineOdSkrivenogDoIzlaznogSloja;
                    uticajTezinaNaIzlazniSloj[brojacSkrivenihNeurona] += promjenaTezineOdSkrivenogDoIzlaznogSloja;
                }
            }
            // weights going into the hidden layer
            for (int brojacSkrivenihNeurona = 0; brojacSkrivenihNeurona < brojNeuronaIzSkrivenogSloja; brojacSkrivenihNeurona++) {
                // for sigmoid
                deltaSkrivenihNeurona[brojacSkrivenihNeurona] = izlaziSkrivenogSloja[brojacSkrivenihNeurona] * (1 - izlaziSkrivenogSloja[brojacSkrivenihNeurona]);
                for (int brojacUlaznihNeurona = 0; brojacUlaznihNeurona < brojNeuronaIzUlaznogSloja; brojacUlaznihNeurona++) {
                    promjeneTezinaOdUlaznogDoSkrivenogSloja[brojacUlaznihNeurona][brojacSkrivenihNeurona] = ulazi[brojacUlaznihNeurona]
                            * deltaSkrivenihNeurona[brojacSkrivenihNeurona]
                            * uticajTezinaNaIzlazniSloj[brojacSkrivenihNeurona];
                }
            }
            // after all the weight changes have been calculated, we can update them
            // first, weights from input to hidden layer
            for (int brojacSkrivenihNeurona = 0; brojacSkrivenihNeurona < brojNeuronaIzSkrivenogSloja; brojacSkrivenihNeurona++) {
                for (int brojacUlaznihNeurona = 0; brojacUlaznihNeurona < brojNeuronaIzUlaznogSloja; brojacUlaznihNeurona++) {
                    tezineVezaOdUlaznogDoSkrivenogSloja[brojacUlaznihNeurona][brojacSkrivenihNeurona] -= korakUcenja * promjeneTezinaOdUlaznogDoSkrivenogSloja[brojacUlaznihNeurona][brojacSkrivenihNeurona];
                }
            }
            // then, weights from hidden to output layer
            for (int brojacIzlaznihNeurona = 0; brojacIzlaznihNeurona < brojNeuronaIzIzlaznogSloja; brojacIzlaznihNeurona++) {
                for (int brojacSkrivenihNeurona = 0; brojacSkrivenihNeurona < brojNeuronaIzSkrivenogSloja; brojacSkrivenihNeurona++) {
                    tezineVezaOdSkrivenogDoIzlaznogSloja[brojacSkrivenihNeurona][brojacIzlaznihNeurona] -= korakUcenja * promjeneTezinaOdSkrivenogDoIzlaznogSloja[brojacSkrivenihNeurona][brojacIzlaznihNeurona];
                }
            }
        }

        public double[][] getTezineVezaOdUlaznogDoSkrivenogSloja() {
            return tezineVezaOdUlaznogDoSkrivenogSloja;
        }

        public double[][] getTezineVezaOdSkrivenogDoIzlaznogSloja() {
            return tezineVezaOdSkrivenogDoIzlaznogSloja;
        }
    }
}
