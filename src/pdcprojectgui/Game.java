package pdcprojectgui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.*;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

public class Game
{
    public ArrayList<ArrayList> questions;
    private LifeLine ATA; // ask the audience
    private LifeLine PAF; // phone a friend
    private LifeLine fiftyFifty; // 50:50 lifeline
    private int currentLevel;
    private int levelProgression; // how far through each level player is [1-5]
    private final Random rand;
    private boolean isPlaying;
    private final String[] prize; // corresponds to prizeNum
    private int prizeNum; // corresponds to current prize player is on 
    public ArrayList<HighScore> scores;
    private int lifelinesUsed; // tally lifelines used to calculate score
    private HighScore finalScore;
    // end conditions, use to determine game end state
    private boolean walkedAway;
    private boolean lost;
    private boolean won;
    private GUI window;
    public Question selectedQ;
    private int qNum; // number in list of currently being asked question
    
    Connection conn;
    Statement state;
    
    public Game()
    {
        conn = null;
        state = null;
        
        connToDB(); 
        questions = getQuestions(); 
        
        currentLevel = 0; // start on base level & progression
        levelProgression = 1;
        rand = new Random();
        isPlaying = true;
        
        prizeNum = 0; // incremement for each correct question, correspond to prize amount
        prize = new String[]{"$100","$200","$300","$500","$1,000","$2,000",
            "$4,000","$8,000","$16,000","$32,000","$64,000","$125,000","$250,000","$500,000","$1 MILLION"};
        
        // setup lifelines
        ATA = new ATA();
        PAF = new PAF();
        fiftyFifty = new FiftyFifty();
        
        // setup end conditions
        walkedAway = false;
        lost = false;
        won = false;
        
        // load in previous scores to scores list
        scores = new ArrayList<HighScore>();
        getScores();
        
        window = new GUI();
        window.setVisible(true);
        window.setGame(this);
        
        printScoreBoard();
    }
    
    public void resetGame()
    {
        // questions = createQuestions();
        currentLevel = 0; // start on base level & progression
        levelProgression = 1;
        isPlaying = true;
        prizeNum = 0; // incremement for each correct question, correspond to prize amount
        // setup lifelines
        ATA.setUsed();
        PAF = new PAF();
        fiftyFifty = new FiftyFifty();
        
        // setup end conditions
        walkedAway = false;
        lost = false;
        won = false;   
        
        DefaultTableModel dtm = (DefaultTableModel) window.jTable1.getModel();
        dtm.setRowCount(0);
        dtm.setColumnCount(0);
        
        for (int i = 0; i < window.btnLifelines.size(); i++)
        {
            window.btnLifelines.get(i).setEnabled(true);
        }
        
        scores = new ArrayList<HighScore>();
        connToDB();
        getScores();
        
        printScoreBoard();
        
        play();
    }
    
    public void play()
    {
        askQuestion();
    }
    
    /*
        - selects question from within current level
        - prints question out and calls method to get + process user answer
        - this method should be repeated as long as the game is still running, until
          end condition reached
    */
    public void askQuestion()
    {
         // select random question from current level
        qNum = rand.nextInt(questions.get(currentLevel).size());
        selectedQ = (Question) questions.get(currentLevel).get(qNum);
        
        window.setQuestionText(selectedQ.getQuestion());
        
        for (int i = 0; i < window.qButtons.size(); i++)
        {
            window.qButtons.get(i).setText((i+1) + ") " + selectedQ.getAnswers()[i]);
        }
        
        window.setPrize("For " + prize[prizeNum]);
    }
    
     public void askSpecificQuestion(int specQNum)
    {
        selectedQ = (Question) questions.get(currentLevel).get(specQNum);
        
        window.setQuestionText(selectedQ.getQuestion());
        
        for (int i = 0; i < window.qButtons.size(); i++)
        {
            window.qButtons.get(i).setText((i+1) + ") " + selectedQ.getAnswers()[i]);
        }
    }
    
    
    
    /*
        - used to check user answer against correct answer
        - if answered correctly. progress through level is incremented and
          the question is removed from the list, so it can't be asked again
        - if the user answer is wrong, set LOST end state and call end method
    */
    public void checkAnswer(int pAns)
    {
        if (pAns == selectedQ.getCorrectAns()) // answer is CORRECT
        {
            System.out.println("Correct!");
            incrementProg();
            questions.get(currentLevel).remove(qNum); // remove used question from list
            askQuestion();
        }
        else // answer is INCORRECT
        {
            lost = true;
            end();
        }   
    }
    
    public void useLifeLine(char lifeline)
    {
      switch(lifeline)
      {
          case 'A':
              ATA.use(selectedQ);
              break;
          case 'F':
              fiftyFifty.use(selectedQ);
              askSpecificQuestion(qNum);
              break;
          case 'P':
              PAF.use(selectedQ);
              break;
          case 'W':
              String optionString = "Are you sure you want to walk away?";
              int reply = JOptionPane.showConfirmDialog(window, optionString, "Confirm Walk Away", JOptionPane.YES_NO_OPTION);
              if (reply == JOptionPane.YES_OPTION)
              {
                  walkedAway = true;
                  end();
              }
              break;
          default:
              break;
      }
    }
 
    /* 
        - process each end status
        - calculates user's score
        - if user selects to save score, will call saveScore method
    */
    private void end()
    {
        window.changeCard("card5");
        
        String winningMoney = "";
        String congrats = "";
        
        if (walkedAway)
        {   
            if (prizeNum != 0)
            {
                congrats = "Congratulations! You walked away with ";
                winningMoney = prize[prizeNum-1];
            }
            else
            {
                winningMoney = "$0";
                congrats = "You quit before it even started. You walked away with ";
            } 
        }
        if (lost)
        {
            if (currentLevel == 0)
            {
                winningMoney = "$0";
                congrats = "That is incorrect! You lose, and unfortunately you walk away with ";
            }
            else if (currentLevel == 1)
            {
                winningMoney = prize[5]; // winnings checkpoint
                congrats = "That is incorrect! You lose, but you still get to walk away with ";
            }
            else if (currentLevel == 3)
            {
                winningMoney = prize[10]; // 2nd winnings checkpoint
                congrats = "That is incorrect! You lose, but you still get to walk away with ";
            }
        }
        if (won)
        {
            congrats = "CONGRATULATIONS! You have won $1 MILLION.";
        }
        
        congrats += winningMoney;
        window.lblEndMessage.setText(congrats);
        
        try
        {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException ex)
        {
            // ignore exception XJ015 which is always thrown on shutdown
        }
       
        isPlaying = false;
    }
    
    /*
        - should only be called when a question has been answered CORRECTLY
        - increments user's progress through each level (0-2)
        - if reached top of level 0 or 1, move to next level + reset progress
        - if reach top of level 2, set end status and end game
    */
   private void incrementProg() // used to move progress OR move up a level
    {
        if (levelProgression < 5) // user cannot progress to next level
        {
            levelProgression++;
            prizeNum++; // increment prize num player is currently on
        }
        else if (levelProgression == 5 && prizeNum < 14)
        {
            System.out.println("level moved up");
            currentLevel++; // move up a level
            levelProgression = 1; // reset progression of current level
            prizeNum++; // increment prize num player is currently on
        }
        else if (prizeNum == 14)
        {
            won = true;
            end();
        }
    }
    
    public int getLevel()
    {
        return currentLevel;
    }
    
    public boolean isPlaying()
    {
        return isPlaying;
    }
    
    //--------- SCORE MANAGEMENT  ---------
    
    /*
        - load scores from file into score list
        - if no scores to load, do nothing (file will be created when save score)
    */
    private void getScores()
    {
        ResultSet rs = null;
        scores.clear();
        
        try
        {
            rs = state.executeQuery("SELECT * FROM SCORES");
            
            while(rs.next())
            {
                HighScore s = new HighScore(rs.getString("scorename"), rs.getInt("score"));
                scores.add(s);
            }
                    
        } catch (SQLException ex)
        {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Collections.sort(scores); // sort low to high
        Collections.reverse(scores); // reverse to high to low
        
    }
    
    /*
        - save existing scores + new score to file
        - if no file exists, new one will be created
        - will only save top 15 scores, any below will be removed
        - calls method to print score board after saving to file
    */
    public void saveScore()
    {
        int score = (prizeNum * 5) - (lifelinesUsed * 3);
        String saveName = JOptionPane.showInputDialog(window, "Enter your name:");
        if (saveName != null)
        {
            finalScore = new HighScore(saveName, score);
            scores.add(finalScore); // add users score to list
            Collections.sort(scores); // sort low to high
            Collections.reverse(scores); // reverse to high to low

            // if adding new score will make list > 15, remove lowest score to 
            // keep score list 15 scores maximum
            if (scores.size() > 16) 
            {
                scores.remove(16);
            }

            try
            {
                HighScore s = (HighScore) finalScore;
                ResultSet row = state.executeQuery("SELECT MAX(SCOREID) FROM APP.SCORES");
                int scoreID = 1;

                if (row.next())
                {
                    scoreID = row.getInt(1) + 1;
                }

                state.executeUpdate("INSERT INTO APP.SCORES (SCOREID, SCORENAME, SCORE) VALUES ("+scoreID+",'"+s.getName()+"',"+s.getScore()+")");
//          }

            } catch (SQLException ex)
            {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }

            printScoreBoard(); // update score board
            
            try
            {
                state.close();
                conn.close();
            } catch (SQLException ex)
            {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    /*
        - prints score list formatted
    */
    public void printScoreBoard()
    {   
        Integer[] nums = new Integer[15];
        String[] names = new String[15];
        Integer[] scoreArr = new Integer[15];
        
        DefaultTableModel dtm = (DefaultTableModel) window.jTable1.getModel();
        dtm.setColumnCount(0);
        dtm.setRowCount(0);
        
        for (int i = 0; i < scores.size(); i++)
        {
           nums[i] = i+1;
           names[i] = scores.get(i).getName();
           scoreArr[i] = scores.get(i).getScore();
        }
        
        dtm.addColumn("Place",nums);
        dtm.addColumn("Name",names);
        dtm.addColumn("Score",scoreArr);
    }
    
    private void connToDB()
    {

        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        
        try
        {
            Class.forName(driver).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }

        String url = "jdbc:derby:HighScores";
        
        try 
        {
            conn = DriverManager.getConnection(url);

        } catch (Throwable ex) {
            System.err.println("SQL Exception: " + ex.getMessage());
        }
        
        try
        {
            state = conn.createStatement();
        } catch (SQLException ex)
        {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private ArrayList getQuestions()
    {
        ArrayList<Question> level0 = new ArrayList();
        ArrayList<Question> level1 = new ArrayList();
        ArrayList<Question> level2 = new ArrayList();
        
        ResultSet rs = null;
        
        try
        {
            rs = state.executeQuery("SELECT * FROM QUESTIONS");
            
            while(rs.next())
            {
                String[] answers = {rs.getString("ANS1"),rs.getString("ANS2"),rs.getString("ANS3"),rs.getString("ANS4")};
                Question q = new Question(rs.getInt("QLEVEL"), rs.getString("QUESTION"),answers,rs.getInt("CORRANS"));
                
                if (q.getLevel() == 0)
                {
                    level0.add(q);
                }
                else if (q.getLevel() == 1)
                {
                    level1.add(q);
                }
                else if (q.getLevel() == 2)
                {
                    level2.add(q);
                }
            }
                    
        } catch (SQLException ex)
        {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ArrayList lists = new ArrayList<>();
        lists.add(level0);
        lists.add(level1);
        lists.add(level2);
        
        return lists;
    }
    

}