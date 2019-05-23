package Tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pdcprojectgui.Game;

/**
 *
 * @author Luke
 */
public class GameTest
{
    public Game game;
        
    @Before
    public void setUp()
    {
        game = new Game();
    }
    
    @After
    public void tearDown()
    {
        game.closeDBConn();
    }
    
    /*
        tests that important values for game have been reset after calling
        resetGame() method
    */
    @Test
    public void testResetGame()
    {
        game.setLevel(2); // set to last level
        game.askQuestion(); // progress 1 question through last level
        game.setPlaying(false); // end game
        
        game.resetGame();
        
        boolean actualPlaying = game.getPlaying();
        Assert.assertTrue(actualPlaying); // check game is playing again
        
        int actualLevel = game.getLevel();
        int expectedLevel = 0;
        Assert.assertEquals(expectedLevel, actualLevel); // check level has been reset
    }
    
    /*
        tests that DB connection has been successful and the game correctly loads
        in 3 lists of questions in 1 list
        - if connection to DB or loading questions failed, list size would be 0
    */
    @Test
    public void testLoadQuestionsFromDB()
    {
        // game questions should consist of 3 elements (3 lists, 1 for each level)
        // connToDB() and loadQuestions() already called when Game object instantiated
        
        int expectedSize = 3;
        int actualSize = game.questions.size();
        Assert.assertEquals(expectedSize, actualSize);
    }
    
    /*
        tests that list for scores has been correctly created in loadScores()
        loadScores() already called in Game object instantiation
        cannot test that any scores have been loaded because it's valid to have 0 scores
    */
    @Test
    public void testLoadScores()
    {
        Assert.assertNotNull(game.scores);
    }

}
