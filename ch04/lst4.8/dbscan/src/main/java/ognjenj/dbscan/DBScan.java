package ognjenj.dbscan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DBScan {
    private static final int KLASTER_ID_NEOBRADJENA_TACKA = -1;
    private static final int KLASTER_ID_SUM = 0;

    public static class Tacka {

        private final int tackaId;
        private final double[] deskriptori;
        private int klasterId = -1;

        public Tacka(int tackaId, int brojDeskriptora) {
            this.deskriptori = new double[brojDeskriptora];
            this.tackaId = tackaId;
        }

        public Tacka(int tackaId, double[] deskriptori) {
            this.deskriptori = deskriptori;
            this.tackaId = tackaId;
        }

        public double[] getDeskriptori() {
            return deskriptori;
        }

        public int getKlasterId() {
            return klasterId;
        }

        public void setKlasterId(int klasterId) {
            this.klasterId = klasterId;
        }

        public int getTackaId() {
            return tackaId;
        }
    }

    public void dbscan(List<Tacka> tacke, double epsilon, int minimalnaGustina) {
        int trenutniKlaster = 0;
        for (Tacka tacka : tacke) {
            if (tacka.getKlasterId() == KLASTER_ID_NEOBRADJENA_TACKA) {
                // pocetna tacka je prva u novom klasteru
                // tacke koje na kraju ostanu same u svojim klasterima su, zapravo, sum
                trenutniKlaster++;
                tacka.setKlasterId(trenutniKlaster);
                // rekurzivno prosirivanje klastera
                prosiriKlaster(tacke, tacka, epsilon, minimalnaGustina, trenutniKlaster);
            }
        }
    }

    public void prosiriKlaster(List<Tacka> tacke, Tacka tacka, double epsilon, int minimalnaGustina, int trenutniKlaster) {
        // sve tacke unutar distance koje su neobradjene, ranije oznacene kao sum
        // ili vec pripadaju istom klasteru
        List<Tacka> susjedneTacke = tacke.stream()
                .filter(e -> euklid(tacka, e) < epsilon
                        && (e.getKlasterId() == KLASTER_ID_NEOBRADJENA_TACKA
                        || e.getKlasterId() == KLASTER_ID_SUM
                        || e.getKlasterId() == tacka.getKlasterId()))
                .collect(Collectors.toList());
        if (susjedneTacke.size() >= minimalnaGustina) {
            // svaka neobradjena tacka se pripoji trenutnom klasteru
            // a zatim se klaster rekurzivno prosiruje
            for (Tacka susjednaTacka : susjedneTacke) {
                if (susjednaTacka.getKlasterId() == KLASTER_ID_NEOBRADJENA_TACKA) {
                    susjednaTacka.setKlasterId(trenutniKlaster);
                    prosiriKlaster(tacke, susjednaTacka, epsilon, minimalnaGustina, trenutniKlaster);
                }
            }
        } else {
            // ako nema dovoljno susjednih, a ova tacka jos nije clan klastera
            if (tacka.getKlasterId() == KLASTER_ID_NEOBRADJENA_TACKA) {
                // ova tacka ce za ovaj klaster biti oznacena kao sum
                tacka.setKlasterId(KLASTER_ID_SUM);
            }
        }
    }

    public static double euklid(Tacka a, Tacka b) {
        double distanca = 0;
        for (int cnt = 0; cnt < a.getDeskriptori().length; cnt++) {
            distanca += Math.pow(a.getDeskriptori()[cnt] - b.getDeskriptori()[cnt], 2);
        }
        return Math.sqrt(distanca);
    }

    public static void main(String[] args) {
        List<Tacka> tacke = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("data/dbscan_dataset.csv")))) {
            String line;
            boolean prviRed = true;
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split(",");
                if (prviRed) {
                    prviRed = false;
                    continue;
                }
                double[] deskriptori = new double[lineSplit.length - 1];
                for (int i = 0, j = 1; j < lineSplit.length; i++, j++) {
                    deskriptori[i] = Double.parseDouble(lineSplit[j]);
                }
                tacke.add(new Tacka(Integer.parseInt(lineSplit[0]), deskriptori));
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        DBScan d = new DBScan();
        d.dbscan(tacke, 2.4, 2);
        mjereRazlicitihDistanci(tacke);
        System.out.println("======================================================================");
	for (Tacka tacka : tacke) {
            System.out.println(tacka.getTackaId() + ":" + tacka.getKlasterId());
        }
    }

    public static TreeMap<Integer, Integer> histogramBrojaSusjednihTacaka(List<Tacka> tacke, double epsilon) {
        TreeMap<Integer, Integer> brojNajblizihSusjeda = new TreeMap<>();
        for (Tacka tacka : tacke) {
            long brojTacaka = tacke.stream()
                    .filter(e -> euklid(tacka, e) <= epsilon)
                    .count() - 1;
            brojNajblizihSusjeda.merge((int) brojTacaka, 1, Integer::sum);
        }
        return brojNajblizihSusjeda;
    }

    public static Map<Integer, Double> mjereRazlicitihDistanci(List<Tacka> tacke) {
        Map<Integer, Double> brojNajblizihSusjeda = new HashMap<>();
        for (Tacka tacka : tacke) {
            List<Tacka> najblize = tacke.stream()
                    .filter(e -> e.getTackaId() != tacka.getTackaId())
                    .sorted((o1, o2) -> {
                        return Double.compare(euklid(tacka, o1), euklid(tacka, o2));
                    })
                    .limit(4)
                    .collect(Collectors.toList());
            System.out.println(tacka.getTackaId() + ":" + euklid(tacka, najblize.get(najblize.size() - 1)));
        }
        return brojNajblizihSusjeda;
    }
}
