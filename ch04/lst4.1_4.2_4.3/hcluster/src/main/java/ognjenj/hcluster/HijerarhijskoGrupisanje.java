package ognjenj.hcluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HijerarhijskoGrupisanje {

    private Map<String, Klaster> klasteri = new HashMap<>();
    private Map<String, Integer> dendrogram = new HashMap<>();

    public HijerarhijskoGrupisanje(List<Tacka> tacke) {
        for (Tacka tacka : tacke) {
            klasteri.put(tacka.getLabela(), new Klaster(tacka));
        }
    }

    public void grupisiTacke() {
        double prethodnaDistanca = 10;
        Map<String, Double> matricaDistanci = new HashMap<>();
        // Racunanje inicijalnih distanci izmedju tacaka
        for (Klaster k1 : klasteri.values()) {
            for (Klaster k2 : klasteri.values()) {
                double inicijalnaDistanca = euklid(k1.getTacke().get(0), k2.getTacke().get(0));
                matricaDistanci.put(k1.getLabela() + "/" + k2.getLabela(), inicijalnaDistanca);
            }
            dendrogram.put(k1.getLabela(), dendrogram.size() + 1);
        }
        // kad u matrici bude samo jedan kljuc, to znaci da je kompletan skup grupisan u jedan klaster
        while (matricaDistanci.size() > 1) {
            double najmanjaDistanca = Double.MAX_VALUE;
            Klaster prviElementZaSpoj = null;
            Klaster drugiElementZaSpoj = null;
            for (Map.Entry<String, Double> distanca : matricaDistanci.entrySet()) {
                if (distanca.getValue() > 0 && distanca.getValue() < najmanjaDistanca) {
                    najmanjaDistanca = distanca.getValue();
                    String[] labeleElemenata = distanca.getKey().split("/");
                    prviElementZaSpoj = klasteri.get(labeleElemenata[0]);
                    drugiElementZaSpoj = klasteri.get(labeleElemenata[1]);
                }
            }
            // rasijecanje stabla, ako je potrebno
            if (najmanjaDistanca > 2.0) {
                break;
            } else {
                prethodnaDistanca = najmanjaDistanca;
            }
            // Racunanje nove matrice distanci na osnovu prethodne i odabranih klastera za spajanje
            matricaDistanci = izracunajMatricuDistanci(new ArrayList<>(klasteri.values()), prviElementZaSpoj, drugiElementZaSpoj, matricaDistanci, NacinSpajanja.COMPLETE_LINKAGE);
            Klaster noviKlaster = new Klaster(prviElementZaSpoj.getTacke());
            noviKlaster.spojiKlaster(drugiElementZaSpoj, najmanjaDistanca);
            klasteri.remove(prviElementZaSpoj.getLabela());
            klasteri.remove(drugiElementZaSpoj.getLabela());
            klasteri.put(noviKlaster.getLabela(), noviKlaster);
            dendrogram.put(noviKlaster.getLabela(), dendrogram.size() + 1);

            System.out.println(dendrogram.get(prviElementZaSpoj.getLabela()) + " " + dendrogram.get(drugiElementZaSpoj.getLabela()) + " " + noviKlaster.getVisinaNaDendrogramu() + ";");
        }
        for (String kljuc : matricaDistanci.keySet().stream().map(e -> e.split("/")[0]).distinct().collect(Collectors.toList())) {
            System.out.println(kljuc);
        }
    }

    public static double euklid(Tacka a, Tacka b) {
        double distanca = 0;
        for (int cnt = 0; cnt < a.getDeskriptori().length; cnt++) {
            distanca += Math.pow(a.getDeskriptori()[cnt] - b.getDeskriptori()[cnt], 2);
        }
        return Math.sqrt(distanca);
    }

    public Map<String, Double> izracunajMatricuDistanci(List<Klaster> sviKlasteri, Klaster najblizi1, Klaster najblizi2, Map<String, Double> prethodnaMatrica, NacinSpajanja nacinSpajanja) {
        // Nova matrica distanci sa labelama u formatu klaster1/klaster2
        Map<String, Double> novaMatrica = new HashMap<>();
        // Novi klaster nastao spajanjem dva klastera sa najboljim distancama (najblizi1 i najblizi2)
        Klaster noviSpoj = new Klaster(najblizi1.getTacke());
        noviSpoj.spojiKlaster(najblizi2, prethodnaMatrica.get(najblizi1.getLabela() + "/" + najblizi2.getLabela()));
        novaMatrica.put(noviSpoj.getLabela() + "/" + noviSpoj.getLabela(), 0.0);
        // Prenos distanci koje su ostale iste. Koriste se indeksi jer se obilazi samo gornja trougaona matrica.
        for (int i = 0; i < sviKlasteri.size(); i++) {
            for (int j = i; j < sviKlasteri.size(); j++) {
                Klaster k1 = sviKlasteri.get(i);
                Klaster k2 = sviKlasteri.get(j);
                // U novu matricu se prenose distance onih klastera koji nisu ukljuceni u spajanje.
                // Klasteri koji su ukljuceni u spajanje se preskacu, jer ce za njih distanca biti izracunata naknadno.
                if (k1.equals(najblizi1) || k2.equals(najblizi1) || k1.equals(najblizi2) || k2.equals(najblizi2)) {
                    continue;
                }
                String labela1 = k1.getLabela() + "/" + k2.getLabela();
                String labela2 = k2.getLabela() + "/" + k1.getLabela();
                novaMatrica.put(labela1, prethodnaMatrica.get(labela1));
                novaMatrica.put(labela2, prethodnaMatrica.get(labela2));
            }
        }
        // Racunanje distanci od novog klastera do postojecih u matrici
        for (Klaster klaster : sviKlasteri) {
            if (klaster.equals(najblizi1) || klaster.equals(najblizi2)) {
                continue;
            } else {
                // U zavisnosti od nacina spajanja, koriste se razlicite interpretacije Lens-Vilijamsove formule
                double distanca = 0;
                double distancaIK = prethodnaMatrica.get(najblizi1.getLabela() + "/" + klaster.getLabela());
                double distancaJK = prethodnaMatrica.get(najblizi2.getLabela() + "/" + klaster.getLabela());
                double distancaIJ = prethodnaMatrica.get(najblizi1.getLabela() + "/" + najblizi2.getLabela());
                double ni = najblizi1.getTacke().size();
                double nj = najblizi2.getTacke().size();
                double nk = klaster.getTacke().size();
                switch (nacinSpajanja) {
                    case SINGLE_LINKAGE:
                        distanca = 0.5 * distancaIK + 0.5 * distancaJK - 0.5 * Math.abs(distancaIK - distancaJK);
                        break;
                    case COMPLETE_LINKAGE:
                        distanca = 0.5 * distancaIK + 0.5 * distancaJK + 0.5 * Math.abs(distancaIK - distancaJK);
                        break;
                    case CENTROID:
                        distanca = (ni / (ni + nj)) * distancaIK + (nj / (ni + nj)) * distancaJK + ((-ni * nj) / Math.pow(ni + nj, 2)) * distancaIJ;
                        break;
                    case AVERAGE:
                        distanca = (ni / (ni + nj)) * distancaIK + (nj / (ni + nj)) * distancaJK;
                        break;
                    case WEIGHTED_AVERAGE:
                        distanca = 0.5 * distancaIK + 0.5 * distancaJK;
                        break;
                    case WARD:
                        distanca = ((ni + nk) / (ni + nj + nk)) * distancaIK + ((nj + nk) / (ni + nj + nk)) * distancaJK + (-nk / (ni + nj + nk)) * distancaIJ;
                        break;
                }
                novaMatrica.put(noviSpoj.getLabela() + "/" + klaster.getLabela(), distanca);
                novaMatrica.put(klaster.getLabela() + "/" + noviSpoj.getLabela(), distanca);
            }
        }
        return novaMatrica;
    }

    public enum NacinSpajanja {
        SINGLE_LINKAGE,
        COMPLETE_LINKAGE,
        AVERAGE,
        WEIGHTED_AVERAGE,
        CENTROID,
        WARD
    }

    public static final class Klaster {

        private double visinaNaDendrogramu = 0.0;
        private List<Tacka> tacke = new ArrayList<>();
        private String labela = "";

        public Klaster(Tacka tacka) {
            this.tacke.add(tacka);
            this.visinaNaDendrogramu = 0.0;
            this.labela = this.getTacke().stream().map(Tacka::getLabela).collect(Collectors.joining(","));
        }

        public Klaster(List<Tacka> tacke) {
            this.tacke.addAll(tacke);
            this.visinaNaDendrogramu = 0.0;
            this.labela = this.getTacke().stream().map(Tacka::getLabela).collect(Collectors.joining(","));
        }

        public void spojiKlaster(Klaster klaster, double visinaNaDendrogramu) {
            this.tacke.addAll(klaster.getTacke());
            this.visinaNaDendrogramu = visinaNaDendrogramu;
            this.labela = this.getTacke().stream().map(Tacka::getLabela).collect(Collectors.joining(","));
        }

        public double getVisinaNaDendrogramu() {
            return visinaNaDendrogramu;
        }

        public void setVisinaNaDendrogramu(double visinaNaDendrogramu) {
            this.visinaNaDendrogramu = visinaNaDendrogramu;
        }

        public List<Tacka> getTacke() {
            return tacke;
        }

        public void setTacke(ArrayList<Tacka> tacke) {
            this.tacke = tacke;
        }

        public String getLabela() {
            return labela;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 71 * hash + Objects.hashCode(this.labela);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Klaster other = (Klaster) obj;
            return Objects.equals(this.labela, other.labela);
        }
    }

    public static final class Tacka {

        private final String labela;
        private final double[] deskriptori;

        public Tacka(String labela, double[] deskriptori) {
            this.labela = labela;
            this.deskriptori = deskriptori;
        }

        public String getLabela() {
            return labela;
        }

        public double[] getDeskriptori() {
            return deskriptori;
        }

    }

    public static void main(String[] args) {

        List<Tacka> tacke = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("data/iris.csv")))) {
            String line;
            boolean firstLine = true;
            int i = 1;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] split = line.split(",");
                Tacka t = new Tacka(String.valueOf(i++), new double[]{Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3])});
                tacke.add(t);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        HijerarhijskoGrupisanje grupisanje = new HijerarhijskoGrupisanje(tacke);
        grupisanje.grupisiTacke();
    }
}
