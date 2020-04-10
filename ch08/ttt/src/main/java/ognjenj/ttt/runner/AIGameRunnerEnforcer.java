package ognjenj.ttt.runner;

import ognjenj.ttt.game.Igra;
import ognjenj.ttt.player.AutoPlayer;
import ognjenj.ttt.player.IgracQ;

import java.io.File;
import java.util.Optional;

/**
 * @author ognjen
 */
public class AIGameRunnerEnforcer {

    private static final int TOTAL_GAME_COUNT = 10_000_000;

    public static void main(String[] args) {
        Optional<File> existingModelPlayerx = Optional.empty();
        if (args.length > 0) {
            File possibleModelFileX = new File(args[0]);
            if (possibleModelFileX.exists() && possibleModelFileX.isFile()) {
                existingModelPlayerx = Optional.of(possibleModelFileX);
            }
        }
        IgracQ player1 = new IgracQ("x", 0, 0, existingModelPlayerx);
        AutoPlayer player2 = new AutoPlayer("o");
        for (int gameCounter = 0; gameCounter < TOTAL_GAME_COUNT; gameCounter++) {
            System.out.println("Starting game " + (gameCounter + 1));
            Igra game = new Igra(player1, player2);
            game.pokreniIgru();
        }
    }
}
