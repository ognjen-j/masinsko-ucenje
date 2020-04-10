package ognjenj.xgboost;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.util.HashMap;
import java.util.Map;

public class XGBoostTest {
    public static void main(String[] args) {
        try {
            // matrice sa podacima za obučavanje i testiranje u SVM formatu
            DMatrix skupZaObucavanje = new DMatrix(args[0]);
            DMatrix testniSkup = new DMatrix(args[1]);
            // mapa sa parametrima
            Map<String, Object> parametri = new HashMap<>();
            parametri.put("objective", "binary:logistic");
            parametri.put("eval_metric", "logloss");
            // mapa sa opcionim objektima za praćenje toka obučavanja
            Map<String, DMatrix> watches = new HashMap<>();

            int brojIteracija = 100;
            // instanciranje prediktora
            Booster prediktor = XGBoost.train(skupZaObucavanje, parametri, brojIteracija, watches, null, null);

            // testiranje modela
            float[][] predikcije = prediktor.predict(testniSkup);
            // rezultat je dat u formi vjerovatnoća u intervalu [0,1]
            for (int i = 0; i < predikcije.length; i++) {
                System.out.println(predikcije[i][0]);
            }
        } catch (XGBoostError ex) {
            ex.printStackTrace(System.err);
        }

    }
}
