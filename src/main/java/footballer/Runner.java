package footballer;

import java.util.*;
import footballer.ranking.*;
import footballer.structure.*;
import footballer.ranking.system.*;
import footballer.parse.Parser;

/**
 * Runner class for testing functionality.
 */
public class Runner {
    public static void main(String[] args) {
        Season season = Parser.parseCurrentStructure(2017, 9);
//		Season season = Parser.parseCurrentStructure(2016, 17);

        String[] teamNames = Utils.espnPreseasonRankings2017;

        List<Team> teams = season.getTeams();
        RankingSystem evenPlayRankingSystem = new EvenPlay(teams, 1.0);
        RankingSystem adjustedWinsRankingSystem = new AdjustedWins(teams);
        RankingSystem self = new SelfBased(teams);

        evenPlayRankingSystem.generateBaselineRanks(teamNames, 0, 16);
        adjustedWinsRankingSystem.generateBaselineRanks(teamNames, 0, 0);
        self.generateBaselineRanks(teamNames, 0, 16);

        evenPlayRankingSystem.applyGames(season);
        adjustedWinsRankingSystem.applyGames(season);
        self.applyGames(season);

//        System.out.println(evenPlayRankingSystem);
        System.out.println(adjustedWinsRankingSystem);
//        System.out.println(self);

    }
}
