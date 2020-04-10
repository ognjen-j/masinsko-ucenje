package ognjenj.kmeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KMeans {
    public static class Tacka {

        private final double[] deskriptori;

        public Tacka(int brojDeskriptora) {
            deskriptori = new double[brojDeskriptora];
        }

        public Tacka(double[] deskriptori) {
            this.deskriptori = deskriptori;
        }

        public double[] getDeskriptori() {
            return deskriptori;
        }

        public Tacka podijeli(double faktor) {
            for (int i = 0; i < deskriptori.length; i++) {
                deskriptori[i] /= faktor;
            }
            return this;
        }

	public String toString() {
            return Arrays.stream(deskriptori)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(",", "[", "]"));
	}
    }

    public static class Klaster {

        private Tacka centroid;
        private List<Tacka> tacke = new ArrayList<>();

        public Tacka getCentroid() {
            return centroid;
        }

        public Klaster(Tacka centroid) {
            this.centroid = centroid;
        }

        public void setCentroid(Tacka centroid) {
            this.centroid = centroid;
        }

        public List<Tacka> getTacke() {
            return tacke;
        }

        public void setTacke(List<Tacka> tacke) {
            this.tacke = tacke;
        }
    }

    public static enum NacinOdredjivanjaCentroida {
        KMEANS_PLUS_PLUS,
        RANDOM,
        CUSTOM_FIXED
    }

    private Klaster izaberiKlasterZaTacku(Tacka tacka, List<Klaster> klasteri) {
        double najbliziCentroid = Double.MAX_VALUE;
        Klaster odabraniKlaster = null;
        for (Klaster klaster : klasteri) {
            double distanca = euklid(tacka, klaster.getCentroid());
            if (distanca < najbliziCentroid) {
                najbliziCentroid = distanca;
                odabraniKlaster = klaster;
            }
        }
        return odabraniKlaster;
    }

    public List<Klaster> odrediInicijalneCentroide(List<Tacka> tacke, int brojKlastera, NacinOdredjivanjaCentroida nacinOdredjivanja) {
        List<Tacka> centroidi = new ArrayList<>();
        List<Klaster> klasteri = new ArrayList<>();
        if (nacinOdredjivanja == NacinOdredjivanjaCentroida.KMEANS_PLUS_PLUS) {
            centroidi.add(tacke.get(0));
            while (klasteri.size() < brojKlastera) {
                klasteri.clear();
                centroidi.forEach((centroid) -> {
                    klasteri.add(new Klaster(centroid));
                });
                double najvecaDistancaOdSvogCentroida = 0;
                Tacka sljedeciCentroid = null;
                for (Tacka tacka : tacke) {
                    Klaster odabraniKlaster = izaberiKlasterZaTacku(tacka, klasteri);
                    if (odabraniKlaster != null) {
                        odabraniKlaster.getTacke().add(tacka);
                        double distancaDoSvogCentroida = euklid(tacka, odabraniKlaster.getCentroid());
                        if (distancaDoSvogCentroida > najvecaDistancaOdSvogCentroida) {
                            najvecaDistancaOdSvogCentroida = distancaDoSvogCentroida;
                            sljedeciCentroid = tacka;
                        }
                    }
                }
                if (sljedeciCentroid != null) {
                    centroidi.add(sljedeciCentroid);
                }
            }
            return klasteri;
        } else if (nacinOdredjivanja == NacinOdredjivanjaCentroida.CUSTOM_FIXED) {
            Tacka c1 = new Tacka(new double[]{7.0, 4.0});
            Tacka c2 = new Tacka(new double[]{7.0, 5.0});
            centroidi.add(c1);
            centroidi.add(c2);
            klasteri.clear();
            centroidi.forEach((centroid) -> {
                klasteri.add(new Klaster(centroid));
            });
            double najvecaDistancaOdSvogCentroida = 0;
            for (Tacka tacka : tacke) {
                Klaster odabraniKlaster = izaberiKlasterZaTacku(tacka, klasteri);
                if (odabraniKlaster != null) {
                    odabraniKlaster.getTacke().add(tacka);
                    double distancaDoSvogCentroida = euklid(tacka, odabraniKlaster.getCentroid());
                    if (distancaDoSvogCentroida > najvecaDistancaOdSvogCentroida) {
                        najvecaDistancaOdSvogCentroida = distancaDoSvogCentroida;
                    }
                }
            }
        } else {
            centroidi.addAll(tacke.subList(0, brojKlastera));
            centroidi.forEach((centroid) -> {
                klasteri.add(new Klaster(centroid));
            });
        }
        return klasteri;
    }

    public static double euklid(Tacka a, Tacka b) {
        double distanca = 0;
        for (int cnt = 0; cnt < a.getDeskriptori().length; cnt++) {
            distanca += Math.pow(a.getDeskriptori()[cnt] - b.getDeskriptori()[cnt], 2);
        }
        return Math.sqrt(distanca);
    }

    public List<Klaster> kMeans(List<Tacka> tacke, int brojKlastera, double epsilon, long maxIter) {
        List<Klaster> klasteri = odrediInicijalneCentroide(tacke, brojKlastera, NacinOdredjivanjaCentroida.KMEANS_PLUS_PLUS);
        long iter = 0;
        boolean konvergirao;
        do {
            konvergirao = true;
            for (Klaster klaster : klasteri) {
                int brojDeskriptora = klaster.getCentroid().getDeskriptori().length;
                Tacka noviCentroid = klaster.getTacke().stream()
                        .reduce(new Tacka(brojDeskriptora), (t, u) -> {
                            Tacka nova = new Tacka(brojDeskriptora);
                            for (int i = 0; i < brojDeskriptora; i++) {
                                nova.getDeskriptori()[i] = t.getDeskriptori()[i] + u.getDeskriptori()[i];
                            }
                            return nova;
                        }).podijeli(klaster.getTacke().size());
                klaster.getTacke().clear();
                if (euklid(noviCentroid, klaster.getCentroid()) < epsilon) {
                    konvergirao &= true;
                } else {
                    konvergirao = false;
                }
                klaster.setCentroid(noviCentroid);

            }
            tacke.forEach((tacka) -> {
                Klaster odabraniKlaster = izaberiKlasterZaTacku(tacka, klasteri);
                odabraniKlaster.getTacke().add(tacka);
            });
        } while (!konvergirao && (++iter) < maxIter);

        NumberFormat format = new DecimalFormat("0.00");

        return klasteri;
    }

    public static void main(String[] args) {
        List<Tacka> tacke = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("data/iris.csv")))) {
            String line;
            boolean prviRed = true;
            while ((line = reader.readLine()) != null) {
                if (prviRed) {
                    prviRed = false;
                    continue;
                }
                String[] lineSplit = line.split(",");
                Tacka t = new Tacka(new double[]{Double.parseDouble(lineSplit[0]), Double.parseDouble(lineSplit[1]), Double.parseDouble(lineSplit[2]), Double.parseDouble(lineSplit[3])});
                tacke.add(t);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        KMeans kmeans = new KMeans();
        List<Klaster> klasteri = kmeans.kMeans(tacke, 5, 0.0001, 1000);
	for(Klaster klaster : klasteri) {
            System.out.println("===================================================================");
            System.out.println("Centroid: " + klaster.getCentroid().toString());
            System.out.println("===================================================================");
            for(Tacka tacka : klaster.getTacke()) {
                System.out.println(tacka.toString());
            }
        }
	//List<Double> siluete = silueta(klasteri);
        //for (Double d : siluete) {
        //    System.out.println(d);
        //}
    }

    public static double daviesBouldin(List<Klaster> klasteriSaTackama) {
        double db = 0;
        for (int i = 0; i < klasteriSaTackama.size(); i++) {
            double najveciR = 0;
            for (int j = 0; j < klasteriSaTackama.size(); j++) {
                if (i == j) {
                    continue;
                }
                Klaster k1 = klasteriSaTackama.get(i);
                Klaster k2 = klasteriSaTackama.get(j);
                double mij = Math.sqrt(Math.pow(euklid(k1.getCentroid(), k2.getCentroid()), 2));
                double sumaZaTacke = 0;
                for (Tacka t : k1.getTacke()) {
                    sumaZaTacke += Math.pow(euklid(t, k1.getCentroid()), 2);
                }
                double s1 = Math.sqrt(sumaZaTacke / k1.getTacke().size());

                sumaZaTacke = 0;
                for (Tacka t : k2.getTacke()) {
                    sumaZaTacke += Math.pow(euklid(t, k2.getCentroid()), 2);
                }
                double s2 = Math.sqrt(sumaZaTacke / k2.getTacke().size());

                double ri = (s1 + s2) / mij;
                if (ri > najveciR) {
                    najveciR = ri;
                }
            }
            db += najveciR;
        }
        return db / klasteriSaTackama.size();
    }

    public static double dunnIndex(List<Klaster> klasteriSaTackama) {
        double dunn = 0;
        double najveciPrecnik = 0;
        int ukupanBrojKlastera = klasteriSaTackama.size();
        for (int cnt = 0; cnt < ukupanBrojKlastera; cnt++) {
            List<Tacka> tackeIzKlastera = klasteriSaTackama.get(cnt).getTacke();
            for (int i = 0; i < tackeIzKlastera.size(); i++) {
                for (int j = i + 1; j < tackeIzKlastera.size(); j++) {
                    double distanca = euklid(tackeIzKlastera.get(i), tackeIzKlastera.get(j));
                    if (distanca > najveciPrecnik) {
                        najveciPrecnik = distanca;
                    }
                }
            }
        }
        double najmanjaDistancaTacaka = Double.MAX_VALUE;
        for (int a = 0; a < ukupanBrojKlastera; a++) {
            for (int b = 0; b < ukupanBrojKlastera; b++) {
                if (a == b) {
                    continue;
                }
                List<Tacka> tackeIzKlastera1 = klasteriSaTackama.get(a).getTacke();
                List<Tacka> tackeIzKlastera2 = klasteriSaTackama.get(b).getTacke();
                for (int i = 0; i < tackeIzKlastera1.size(); i++) {
                    for (int j = 0; j < tackeIzKlastera2.size(); j++) {
                        double distanca = euklid(tackeIzKlastera1.get(i), tackeIzKlastera2.get(j));
                        if (distanca < najmanjaDistancaTacaka) {
                            najmanjaDistancaTacaka = distanca;
                        }
                    }
                }
            }
        }
        dunn = najmanjaDistancaTacaka / najveciPrecnik;
        return dunn;
    }

    public static List<Double> silueta(List<Klaster> klasteriSaTackama) {
        List<Double> sveSiluete = new ArrayList<>();
        for (int k = 0; k < klasteriSaTackama.size(); k++) {
            Klaster klaster = klasteriSaTackama.get(k);
            for (int i = 0; i < klaster.getTacke().size(); i++) {
                double distanceZaIstiKlaster = 0;
                Tacka t1 = klaster.getTacke().get(i);
                for (int j = 0; j < klaster.getTacke().size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    Tacka t2 = klaster.getTacke().get(j);
                    distanceZaIstiKlaster += euklid(t1, t2);
                }
                distanceZaIstiKlaster /= (klaster.getTacke().size() - 1);

                double najmanjaDistancaDoDrugogKlastera = Double.MAX_VALUE;
                for (int n = 0; n < klasteriSaTackama.size(); n++) {
                    if (n == k) {
                        continue;
                    }
                    double distanceZaDrugeKlastere = 0;
                    Klaster k2 = klasteriSaTackama.get(n);
                    for (int m = 0; m < k2.getTacke().size(); m++) {
                        Tacka t3 = k2.getTacke().get(m);
                        distanceZaDrugeKlastere += euklid(t1, t3);
                    }
                    distanceZaDrugeKlastere /= k2.getTacke().size();
                    if (najmanjaDistancaDoDrugogKlastera > distanceZaDrugeKlastere) {
                        najmanjaDistancaDoDrugogKlastera = distanceZaDrugeKlastere;
                    }
                }
                sveSiluete.add((najmanjaDistancaDoDrugogKlastera - distanceZaIstiKlaster) / Math.max(distanceZaIstiKlaster, najmanjaDistancaDoDrugogKlastera));
            }
        }
        return sveSiluete;
    }
}
