package ognjenj.ttt.runner;

import ognjenj.ttt.game.Igra;
import ognjenj.ttt.player.IgracCovjek;
import ognjenj.ttt.player.IgracQ;

import java.io.File;
import java.util.Optional;

/**
 * @author ognjen
 */
public class HumanGameRunner {

    public static void main(String[] args) {
        Optional<File> existingModel = Optional.empty();
        if (args.length > 0) {
            File possibleModelFile = new File(args[0]);
            if (possibleModelFile.exists() && possibleModelFile.isFile()) {
                existingModel = Optional.of(possibleModelFile);
            }
        }
        IgracQ player1 = new IgracQ("x", 0, 0, existingModel);
        //AIPlayer player1 = new ANNPlayer("x", 0.2, Optional.empty());
        IgracCovjek player2 = new IgracCovjek("o");
        for (int i = 0; i < 100; i++) {
            Igra game = new Igra(player1, player2);
            game.pokreniIgru();
        }
    }
}
