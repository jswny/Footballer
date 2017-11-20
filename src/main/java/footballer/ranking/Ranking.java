package footballer.ranking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import footballer.structure.Game;
import footballer.structure.Season;
import footballer.structure.Team;
import footballer.structure.Week;

/**
 * Defines the structure of a system to rank NFL {@link Team}s.
 *
 * All rankings must inherit this class to be used as a ranking.
 *
 * The concept of a ranking is as follows:
 * <ul>
 *     <li>Each {@link Team}'s {@link Rank} is an arbitrary {@link Double} which can signify anything, depending on the ranking system.</li>
 *     <li>A {@link Team}'s overall ranking in the {@link footballer.structure.League} is determined by its position compared to other teams' ranks.</li>
 *     <li>Ranks are calculated on a per-game basis, which means that ranks can only change through the context of finished game.</li>
 *     <li>For a given {@link Game}, a ranking uses both {@link Team}s' initial {@link Rank}, and the result of the game to determine the change (if any) in ranks.</li>
 *     <li>The initial {@link Rank}s of each team, and their new ranks are saved to the {@link RankLog} to maintain a history of rank changes</li>
 * </ul>
 */
public abstract class Ranking {
    protected List<Rank> ranks = new ArrayList<>();
    protected RankLog log;

    public Ranking(List<Team> teams) {
        for (Team team : teams) ranks.add(new Rank(team));
        log = new RankLog(teams);
    }

    /**
     * Applies all of the {@link Game}s in a given {@link Season} to this ranking.
     * @param season the {@link Season} to draw the {@link Game}s from
     */
    public void applyGames(Season season) {
        for (Week week : season.getWeeks()) {
            for (Game game : week.getGames()) {
                RankLogEntry entry = applyGame(game);
                log.addEntry(week.number, entry);
            }
        }
    }

    /**
     * Applies the results of a given {@link Game} to this ranking.
     *
     * This is the most important method, and the only one which needs to be implemented by an inheriting ranking.
     * This method uses a game to determine what the new {@link Rank} of each participating {@link Team} should be.
     *
     * @param game the {@link Game} to use to calculate each {@link Team}'s new {@link Rank}
     * @return a {@link RankLogEntry} which represents the results of calculating each {@link Team}'s change in {@link Rank} based on {@code game} for this ranking
     */
    protected abstract RankLogEntry applyGame(Game game);

    /**
     * Generates baseline {@link Rank}s for each given {@link Team} to be used in this ranking.
     *
     * <h2>Explanation</h2>
     * This method is primarily used to populate a ranking with pre-season {@link Rank}s for ranking systems which require
     * some kind of initial differentiation between {@link Team}s.
     * For these purposes, a default initial ranking based on ESPN's final pre-season rankings is provided here: {@link footballer.Utils#espnPreseasonRankings}.
     * The baseline ranking will be taken as the initial {@link Rank} when the ranking applies the team's first {@link Game}.
     *
     * <h2>Methodology</h2>
     * <ul>
     *     <li>The baseline ranks will be applied from the first {@link Team} in {@code teamNames} to the last.</li>
     *     <li>The first {@link Team} in this array is the best team, while the last team is the worst team.</li>
     *     <li>The first {@link Team} will be assigned a {@link Rank} of {@code max}.</li>
     *     <li>
     *         Each successive {@link Team} will be assigned a rank of {@code max - (position * increment)}
     *         where {@code position} is the index of the team in the array {@code teamNames}.
     *     </li>
     * </ul>
     *
     * <h2>Example</h2>
     * With the following arguments:
     * <ul>
     *     <li>{@code teamNames = {"Patriots", "Eagles", "Saints", "Vikings"}}</li>
     *     <li>{@code increment = 1.0}</li>
     *     <li>{@code max = 32}</li>
     * </ul>
     * Calling {@code generateBaselineRanking(teamNames, increment, max)} would generate the following {@link Rank}s:
     * <ol>
     *     <li>{@code Patriots: rank 32.0}</li>
     *     <li>{@code Eagles: rank 31.0}</li>
     *     <li>{@code Saints: rank 30.0}</li>
     *     <li>{@code Vikings: rank 29.0}</li>
     * </ol>
     *
     * @param teamNames the names (in order) of the {@link Team}s to apply baseline {@link Rank}s to.
     * @param increment the increment to be used between successive {@link Team}s
     * @param max the maximum (starting) {@link Rank} to be assigned to a {@link Team}.
     */
    public void generateBaselineRanking(String[] teamNames, double increment, double max) {
        double multiplier = 0;

        for (String teamName : teamNames) {
            getRank(teamName).setValue(max - (multiplier * increment));
            multiplier++;
        }
    }

    public RankLog getLog() {
        return log;
    }

    public String getLogForTeam(String teamName) {
        String result = teamName + " log:\n\n";
        for (EntryPair pair : log.getPairsForTeam(teamName)) {
            if (pair.entry != null) result += pair.entry + "\n";
        }
        return result;
    }

    public List<Double> getTeamValues(String teamName) {
        return log.getTeamValues(teamName);
    }

    public Map<String, List<Double>> getAllValues() {
        return log.getAllValues();
    }

    public String getCSVData() {
        return log.getCSVData();
    }

    public EntryPair getGreatestChange(String teamName) {
        return log.getGreatestChange(teamName);
    }

    public Rank getGreaterRank(Team first, Team second) {
        Rank firstRank = getRank(first.name);
        Rank secondRank = getRank(second.name);
        return firstRank.getValue() > secondRank.getValue() ? firstRank : secondRank;
    }

    public Rank getRank(String teamName) {
        for (Rank rank : ranks) {
            if (rank.team.name.equals(teamName)) return rank;
        }
        return null;
    }

    public String toString() {
        String result = "#Ranking<[\n";
        List<Rank> tempList = new ArrayList<>(ranks); // Copy the list so we don't reorder the original
        tempList.sort(Rank.valueComparator);
        int count = 1;
        for (Rank rank : tempList) {
            result += "    " + count + ": " + rank + ",\n";
            count++;
        }
        result = result.substring(0, result.length() - 2);
        return result + "\n]>";
    }
}
