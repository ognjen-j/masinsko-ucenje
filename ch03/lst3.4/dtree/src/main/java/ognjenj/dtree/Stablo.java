package ognjenj.dtree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Stablo {

    private Cvor korjenskiCvor = new Cvor();
    private List<Primjer> skupZaObucavanje = new ArrayList<>();
    private Map<String, Set<String>> atributi = new HashMap<>();

    public void inicijalizujStablo(Cvor trenutniCvor, List<Primjer> preostaliPodaci, Collection<String> preostaliAtributi) {
        double ukupnaEntropija = 0;
        Map<String, List<Primjer>> grupisaniPodaci = preostaliPodaci.stream().collect(Collectors.groupingBy(Primjer::getKlasa));
        for (Map.Entry<String, List<Primjer>> primjeriZaKlasu : grupisaniPodaci.entrySet()) {
            double p = primjeriZaKlasu.getValue().size() / (double) preostaliPodaci.size();
            ukupnaEntropija += -(p * (Math.log(p) / Math.log(2.0)));
        }
        Map<String, Double> informacioneDobiti = new HashMap<>();
        for (String atribut : preostaliAtributi) {
            double entropijaAtributa = 0;
            for (String vrijednostAtributa : atributi.get(atribut)) {
                double entropijaZaJednuVrijednost = 0;
                Map<String, List<Primjer>> grupisaniPrimjeriZaAtribut = preostaliPodaci.stream()
                        .filter(primjer -> primjer.getAtributi().getOrDefault(atribut, "---").equals(vrijednostAtributa))
                        .collect(Collectors.groupingBy(Primjer::getKlasa));
                long ukupnoPrimjeraZaJednuVrijednost = grupisaniPrimjeriZaAtribut.values().stream().flatMap(Collection::stream).count();
                for (Map.Entry<String, List<Primjer>> primjeriZaKlasu : grupisaniPrimjeriZaAtribut.entrySet()) {
                    double p = primjeriZaKlasu.getValue().size() / (double) ukupnoPrimjeraZaJednuVrijednost;
                    entropijaZaJednuVrijednost += -(p * (Math.log(p) / Math.log(2.0)));
                }
                entropijaAtributa += (double) ukupnoPrimjeraZaJednuVrijednost / preostaliPodaci.size() * entropijaZaJednuVrijednost;
            }
            informacioneDobiti.put(atribut, ukupnaEntropija - entropijaAtributa);
        }
        String atributZaNajvecuDobit = null;
        double najvecaDobit = -1;
        for (Map.Entry<String, Double> informacionaDobit : informacioneDobiti.entrySet()) {
            if (informacionaDobit.getValue() > najvecaDobit) {
                najvecaDobit = informacionaDobit.getValue();
                atributZaNajvecuDobit = informacionaDobit.getKey();
            }
        }
        Set<String> vrijednostiOdabranogAtributa = atributi.get(atributZaNajvecuDobit);
        for (final String vrijednostAtributa : vrijednostiOdabranogAtributa) {
            final String atributZaParticionisanje = atributZaNajvecuDobit;
            Cvor noviCvor = new Cvor();
            noviCvor.setOdabraniAtribut(atributZaNajvecuDobit);
            noviCvor.setVrijednostOdabranogAtributa(vrijednostAtributa);
            List<Primjer> podskupPrimjera = preostaliPodaci.stream()
                    .filter(primjer -> primjer.getAtributi().getOrDefault(atributZaParticionisanje, "---").equals(vrijednostAtributa))
                    .collect(Collectors.toList());
            List<String> podskupAtributa = preostaliAtributi.stream().filter(atr -> !atr.equals(atributZaParticionisanje)).collect(Collectors.toList());
            noviCvor.setKlasifikovaniPrimjeri(podskupPrimjera);
            noviCvor.setList(podskupPrimjera.stream().map(Primjer::getKlasa).distinct().count() == 1);
            if (!noviCvor.isList() && !podskupPrimjera.isEmpty() && !podskupAtributa.isEmpty()) {
                this.inicijalizujStablo(noviCvor, podskupPrimjera, podskupAtributa);
            }
            if (!podskupPrimjera.isEmpty()) {
                trenutniCvor.getDjeca().add(noviCvor);
            }
        }
    }

    public static class Primjer {

        private String naziv = "";
        private Map<String, String> atributi = new HashMap<>();
        private String klasa = "";

        public String getNaziv() {
            return naziv;
        }

        public void setNaziv(String naziv) {
            this.naziv = naziv;
        }

        public Map<String, String> getAtributi() {
            return atributi;
        }

        public void setAtributi(Map<String, String> atributi) {
            this.atributi = atributi;
        }

        public String getKlasa() {
            return klasa;
        }

        public void setKlasa(String klasa) {
            this.klasa = klasa;
        }
    }

    public static class Cvor {

        private List<Cvor> djeca = new ArrayList<>();
        private String odabraniAtribut;
        private List<Primjer> klasifikovaniPrimjeri = new ArrayList<>();
        private String vrijednostOdabranogAtributa;
        private boolean list = false;

        public List<Cvor> getDjeca() {
            return djeca;
        }

        public void setDjeca(List<Cvor> djeca) {
            this.djeca = djeca;
        }

        public String getOdabraniAtribut() {
            return odabraniAtribut;
        }

        public void setOdabraniAtribut(String odabraniAtribut) {
            this.odabraniAtribut = odabraniAtribut;
        }

        public List<Primjer> getKlasifikovaniPrimjeri() {
            return klasifikovaniPrimjeri;
        }

        public void setKlasifikovaniPrimjeri(List<Primjer> klasifikovaniPrimjeri) {
            this.klasifikovaniPrimjeri = klasifikovaniPrimjeri;
        }

        public String getVrijednostOdabranogAtributa() {
            return vrijednostOdabranogAtributa;
        }

        public void setVrijednostOdabranogAtributa(String vrijednostOdabranogAtributa) {
            this.vrijednostOdabranogAtributa = vrijednostOdabranogAtributa;
        }

        public boolean isList() {
            return list;
        }

        public void setList(boolean list) {
            this.list = list;
        }
    }

    public static void main(String[] args) {
	if(args.length<2) {
	    System.err.println("Please provide the location of the training data set and the testing data set");
	    System.exit(1);
	}
        Stablo stablo = new Stablo();
        String[] naziviKolona = new String[0];
        boolean prviRed = true;
        try (BufferedReader treningSkup = new BufferedReader(new FileReader(new File(args[0])))) {
            String linija;
            while ((linija = treningSkup.readLine()) != null) {
                String[] sadrzaj = linija.replace("\"", "").split(",");
                if (prviRed) {
                    naziviKolona = sadrzaj;
                    for (int cnt = 0; cnt < naziviKolona.length; cnt++) {
                        if (cnt > 0 && cnt < naziviKolona.length - 1) {
                            stablo.atributi.computeIfAbsent(naziviKolona[cnt], e -> new HashSet<>());
                        }
                    }
                    prviRed = false;
                } else {
                    Primjer primjer = new Primjer();
                    primjer.naziv = sadrzaj[0];
                    primjer.klasa = sadrzaj[naziviKolona.length - 1];
                    for (int cnt = 0; cnt < naziviKolona.length; cnt++) {

                        if (cnt > 0 && cnt < naziviKolona.length - 1) {
                            primjer.atributi.put(naziviKolona[cnt], sadrzaj[cnt]);
                            stablo.atributi.get(naziviKolona[cnt]).add(sadrzaj[cnt]);
                        }
                    }
                    stablo.skupZaObucavanje.add(primjer);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        stablo.inicijalizujStablo(stablo.korjenskiCvor, stablo.skupZaObucavanje, stablo.atributi.keySet());
        System.out.println(stablo.korjenskiCvor);
        printTree(stablo.korjenskiCvor, 0);
        testModel(stablo, args[1]);
    }

    private static void printTree(Cvor cvor, int nivo) {
        for (int cnt = 0; cnt < cvor.getDjeca().size(); cnt++) {
            Cvor child = cvor.getDjeca().get(cnt);
            for (int i = 0; i < nivo; i++) {
                System.out.print("   ");
            }
            String collect = child.getKlasifikovaniPrimjeri().stream()
                    .collect(Collectors.groupingBy(Primjer::getKlasa, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", ", " (", ")"));
            System.out.println(" " + child.getOdabraniAtribut() + ": " + child.getVrijednostOdabranogAtributa() + collect);
            printTree(child, nivo + 1);
        }
    }

    private static void testModel(Stablo stablo, String trainingDatasetPath) {
        long tp = 0, tn = 0, fp = 0, fn = 0;
        String[] naziviKolona = new String[0];
        boolean prviRed = true;
        try (BufferedReader testniSkup = new BufferedReader(new FileReader(new File(trainingDatasetPath)))) {
            String linija;
            while ((linija = testniSkup.readLine()) != null) {
                String[] sadrzaj = linija.replace("\"", "").split(",");
                if (prviRed) {
                    naziviKolona = sadrzaj;
                    for (int cnt = 0; cnt < naziviKolona.length; cnt++) {
                        if (cnt > 0 && cnt < naziviKolona.length - 1) {
                            stablo.atributi.computeIfAbsent(naziviKolona[cnt], e -> new HashSet<>());
                        }
                    }
                    prviRed = false;
                } else {
                    Primjer primjer = new Primjer();
                    primjer.naziv = sadrzaj[0];
                    primjer.klasa = sadrzaj[naziviKolona.length - 1];
                    for (int cnt = 0; cnt < naziviKolona.length; cnt++) {

                        if (cnt > 0 && cnt < naziviKolona.length - 1) {
                            primjer.atributi.put(naziviKolona[cnt], sadrzaj[cnt]);
                            stablo.atributi.get(naziviKolona[cnt]).add(sadrzaj[cnt]);
                        }
                    }
                    stablo.skupZaObucavanje.add(primjer);
                    String odabranaKlasa = predictClass(stablo, primjer);
                    if (primjer.klasa.equals("1")) {
                        if (odabranaKlasa.equals("1")) {
                            tp++;
                        } else {
                            fn++;
                        }
                    } else {
                        if (odabranaKlasa.equals("0")) {
                            tn++;
                        } else {
                            fp++;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        System.out.println("TP: " + tp + ", TN: " + tn + ", FP: " + fp + ", FN: " + fn);
    }

    private static String predictClass(Stablo stablo, Primjer primjer) {
        Cvor trenutniCvor = stablo.korjenskiCvor;
        boolean nastavi = true;
        do {
            boolean nasao = false;
            for (Cvor cvor : trenutniCvor.getDjeca()) {
                if (primjer.atributi.getOrDefault(cvor.odabraniAtribut, "---").equals(cvor.getVrijednostOdabranogAtributa())) {
                    nasao = true;
                    trenutniCvor = cvor;
                    nastavi = !cvor.isList();
                    break;
                }
            }
            if (!nasao) {
                nastavi = false;
            }

        } while (nastavi);
        Map<String, Long> brojPoKlasama = trenutniCvor.getKlasifikovaniPrimjeri().stream().collect(Collectors.groupingBy(Primjer::getKlasa, Collectors.counting()));
        String najzastupljenijaKlasa = "";
        long zastupljenostKlase = -1;
        for (Map.Entry<String, Long> klasa : brojPoKlasama.entrySet()) {
            if (klasa.getValue() > zastupljenostKlase) {
                najzastupljenijaKlasa = klasa.getKey();
                zastupljenostKlase = klasa.getValue();
            }
        }
        return najzastupljenijaKlasa;
    }

    public Cvor getKorjenskiCvor() {
        return korjenskiCvor;
    }

    public void setKorjenskiCvor(Cvor korjenskiCvor) {
        this.korjenskiCvor = korjenskiCvor;
    }

    public List<Primjer> getSkupZaObucavanje() {
        return skupZaObucavanje;
    }

    public void setSkupZaObucavanje(List<Primjer> skupZaObucavanje) {
        this.skupZaObucavanje = skupZaObucavanje;
    }

    public Map<String, Set<String>> getAtributi() {
        return atributi;
    }

    public void setAtributi(Map<String, Set<String>> atributi) {
        this.atributi = atributi;
    }
}

