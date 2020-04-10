package ognjenj.ttt.runner;

import ognjenj.ttt.game.Igra;
import ognjenj.ttt.player.IgracQ;

import java.io.File;
import java.util.Optional;

/**
 * @author ognjen
 */
public class QGameRunner {

    private static final int TOTAL_GAME_COUNT = 10_000_000;

    public static void main(String[] args) {
       Optional<File> existingModelPlayerx = Optional.empty();
       if (args.length > 0) {
          File possibleModelFileX = new File(args[0]);
          if (possibleModelFileX.exists() && possibleModelFileX.isFile()) {
             existingModelPlayerx = Optional.of(possibleModelFileX);
          }
       }
       Optional<File> existingModelPlayero = Optional.empty();
       if (args.length > 1) {
          File possibleModelFileO = new File(args[0]);
          if (possibleModelFileO.exists() && possibleModelFileO.isFile()) {
             existingModelPlayero = Optional.of(possibleModelFileO);
          }
       }
        IgracQ player1 = new IgracQ("x", 1.0, 0.0000001, existingModelPlayerx);
        IgracQ player2 = new IgracQ("o", 1.0, 0.0000001, existingModelPlayero);
        for (int gameCounter = 0; gameCounter < TOTAL_GAME_COUNT; gameCounter++) {
            System.out.println("Starting game " + (gameCounter + 1));
            Igra game = new Igra(player1, player2);
            game.pokreniIgru();
        }
    }
}
