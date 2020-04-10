package ognjenj.bayesian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BajesovKlasifikator {
    public static class Primjer {

        private final String[] vrijednosti;
        private final String klasa;

        public Primjer(String[] vrijednosti, String klasa) {
            this.vrijednosti = vrijednosti;
            this.klasa = klasa;
        }

        public String[] getVrijednosti() {
            return vrijednosti;
        }

        public String getKlasa() {
            return klasa;
        }
    }

    public static class Deskriptor {

        private Map<String, Long> kardinalnostPoKlasama = new HashMap<>();

        public Map<String, Long> getKardinalnostPoKlasama() {
            return kardinalnostPoKlasama;
        }

    }

    public List<Map<String, Deskriptor>> uslovneVjerovatnoce(List<Primjer> primjeriZaObucavanje) {
        List<Map<String, Deskriptor>> model = new ArrayList<>();
        primjeriZaObucavanje.forEach((primjer) -> {
            for (int brojac = 0; brojac < primjer.getVrijednosti().length; brojac++) {
                if (model.size() < brojac + 1) {
                    model.add(new HashMap<>());
                }
                String vrijednostDeskriptora = primjer.getVrijednosti()[brojac];
                model.get(brojac).computeIfAbsent(vrijednostDeskriptora, e -> new Deskriptor())
                        .getKardinalnostPoKlasama()
                        .merge(primjer.getKlasa(), 1L, Long::sum);
            }
        });
        return model;
    }

    public String klasifikuj(Primjer testniPrimjer, List<Map<String, Deskriptor>> model, Map<String, Double> apriorneVjerovatnoce) {
        Map<String, Double> vjerovatnocePoKlasama = new HashMap<>();
        apriorneVjerovatnoce.entrySet().forEach((apriornaVjerovatnocaZaKlasu) -> {
            vjerovatnocePoKlasama.merge(
                    apriornaVjerovatnocaZaKlasu.getKey(),
                    Math.log(apriornaVjerovatnocaZaKlasu.getValue()),
                    (a, b) -> a);
        });
        for (int brojac = 0; brojac < testniPrimjer.getVrijednosti().length; brojac++) {
            Deskriptor deskriptor = model.get(brojac).get(testniPrimjer.getVrijednosti()[brojac]);
            long kardinalnostUkupno = deskriptor.getKardinalnostPoKlasama().values().stream().reduce(0L, Long::sum);
            deskriptor.getKardinalnostPoKlasama().entrySet().forEach((kardinalnostZaKlasu) -> {
                vjerovatnocePoKlasama.merge(
                        kardinalnostZaKlasu.getKey(),
                        Math.log((double) kardinalnostZaKlasu.getValue() / kardinalnostUkupno),
                        Double::sum);
            });
        }
        double najvecaVjerovatnoca = Double.NEGATIVE_INFINITY;
        String odabranaKlasa = "";
        for (Map.Entry<String, Double> vjerovatnocaZaKlasu : vjerovatnocePoKlasama.entrySet()) {
            if (vjerovatnocaZaKlasu.getValue() > najvecaVjerovatnoca) {
                najvecaVjerovatnoca = vjerovatnocaZaKlasu.getValue();
                odabranaKlasa = vjerovatnocaZaKlasu.getKey();
            }
        }
        return odabranaKlasa;
    }

    public static void validiraj(List<Primjer> testniPrimjeri, List<Map<String, Deskriptor>> model, Map<String, Double> apriori) {
        testniPrimjeri.forEach((primjer) -> {
            Map<String, Double> vjerovatnocePoKlasama = new HashMap<>();
            for (Map.Entry<String, Double> aprioriZaKlasu : apriori.entrySet()) {
                vjerovatnocePoKlasama.merge(aprioriZaKlasu.getKey(), Math.log(aprioriZaKlasu.getValue()), (a, b) -> a);
            }
            for (int brojac = 0; brojac < primjer.getVrijednosti().length; brojac++) {
                Map<String, Deskriptor> vrijednostiDeskriptora = model.get(brojac);
                Deskriptor deskriptor = vrijednostiDeskriptora.get(primjer.getVrijednosti()[brojac]);
                long ukupanBrojElemenataZaVrijednostDeskriptora = deskriptor.getKardinalnostPoKlasama().values().stream().reduce(0L, Long::sum);
                for (Map.Entry<String, Long> pojavljivanjaZaKlasu : deskriptor.getKardinalnostPoKlasama().entrySet()) {
                    vjerovatnocePoKlasama.merge(pojavljivanjaZaKlasu.getKey(), Math.log((double) pojavljivanjaZaKlasu.getValue() / ukupanBrojElemenataZaVrijednostDeskriptora), Double::sum);
                }
            }
            double najvecaVjerovatnoca = Double.NEGATIVE_INFINITY;
            String klasaZaNajvecuVjerovatnocu = "acc";
            for (Map.Entry<String, Double> vjerovatnocaZaKlasu : vjerovatnocePoKlasama.entrySet()) {
                if (vjerovatnocaZaKlasu.getValue() > najvecaVjerovatnoca) {
                    najvecaVjerovatnoca = vjerovatnocaZaKlasu.getValue();
                    klasaZaNajvecuVjerovatnocu = vjerovatnocaZaKlasu.getKey();
                }
            }
            System.out.println(primjer.getKlasa() + ":" + klasaZaNajvecuVjerovatnocu + " " + vjerovatnocePoKlasama.entrySet().stream().map(e -> e.getKey() + "/" + e.getValue()).collect(Collectors.joining(", ", "(", ")")));
        });
    }

    public static void main(String[] args) {
        List<Primjer> primjeri = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("data/mushrooms_train.csv")))) {
            String line;
            boolean prvaLinija = true;
            while ((line = reader.readLine()) != null) {
                if (prvaLinija) {
                    prvaLinija = false;
                    continue;
                }
                String[] lineSplit = line.split(",");
                primjeri.add(new Primjer(Arrays.copyOfRange(lineSplit, 1, lineSplit.length - 1), lineSplit[lineSplit.length - 1]));
            }

        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        BajesovKlasifikator klasifikator = new BajesovKlasifikator();
        List<Map<String, Deskriptor>> uslovneVjerovatnoce = klasifikator.uslovneVjerovatnoce(primjeri);
        double ukupno = primjeri.size();
        Map<String, Double> apriori = new HashMap<>();
        primjeri.stream().collect(Collectors.groupingBy(e -> e.getKlasa())).entrySet().forEach((t) -> {
            apriori.put(t.getKey(), (double) t.getValue().size() / ukupno);
        });
        List<Primjer> primjeriTest = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("data/mushrooms_test.csv")))) {
            String line;
            boolean prvaLinija = true;
            while ((line = reader.readLine()) != null) {
                if (prvaLinija) {
                    prvaLinija = false;
                    continue;
                }
                String[] lineSplit = line.split(",");
                primjeriTest.add(new Primjer(Arrays.copyOfRange(lineSplit, 1, lineSplit.length - 1), lineSplit[lineSplit.length - 1]));
            }

        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        validiraj(primjeriTest, uslovneVjerovatnoce, apriori);
    }
}
