package pdcprojectgui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.*;
import javax.swing.JButton;
import javax.swing.JOptionPane;
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
    private Connection conn;
    private Statement state;
    
    public Game()
    {
        conn = null;
        state = null;
        
        connToDB(); 
        questions = loadQuestions(); 
        
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
       
        getScores();
        
        window = new GUI();
        window.setVisible(true);
        window.setGame(this);
        
        printScoreBoard();
    }
    
    public void resetGame()
    {
        currentLevel = 0; // start on base level & progression
        qNum = 0;
        levelProgression = 1;
        isPlaying = true;
        prizeNum = 0; // incremement for each correct question, correspond to prize amount
        // setup lifelines
        ATA = new ATA();
        PAF = new PAF();
        fiftyFifty = new FiftyFifty();
        
        // setup end conditions
        walkedAway = false;
        lost = false;
        won = false;   
        
        DefaultTableModel dtm = (DefaultTableModel) window.jTable1.getModel();
        dtm.setRowCount(0);
        dtm.setColumnCount(0);
        
        for (JButton btnLifeline : window.btnLifelines)
        {
            btnLifeline.setEnabled(true);
        }
        
        scores = new ArrayList<HighScore>();
        connToDB();
        getScores();
        
        questions = new ArrayList<ArrayList>();
        questions = loadQuestions(); 
        
        printScoreBoard();
        
        play();
    }
    
    /*
        - start playing game
        - game will loop asking + validating questions/answers until end of game reached
            or player loses
    */
    public void play()
    {
        askQuestion();
    }
    
    /*
        - selects question from within current level
        - sets question and answers on GUI
        - this method should be repeated as long as the game is still running, until
          end condition reached
    */
    public void askQuestion()
    {
        // select random question from current level
        
        if (questions.get(currentLevel).size() > 1)
        {
           qNum = rand.nextInt(questions.get(currentLevel).size()); 
        }
        else
        {
            qNum = 0;
            incrementProg();
        }
        
        selectedQ = (Question) questions.get(currentLevel).get(qNum);
        
        window.setQuestionText(selectedQ.getQuestion());
        
        for (int i = 0; i < window.qButtons.size(); i++)
        {
            window.qButtons.get(i).setText((i+1) + ") " + selectedQ.getAnswers()[i]);
        }
        
        window.setPrize("For " + prize[prizeNum]);
    }
    
    /*
        - asks a specified question to the user
        - main use: re-ask a question that has been altered by 50/50 class
    */
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
            switch (currentLevel) 
            {
                case 0:
                    winningMoney = "$0";
                    congrats = "That is incorrect! You lose, and unfortunately you walk away with ";
                    break;
                case 1:
                    winningMoney = prize[5]; // winnings checkpoint
                    congrats = "That is incorrect! You lose, but you still get to walk away with ";
                    break;
                case 2:
                    winningMoney = prize[10]; // 2nd winnings checkpoint
                    congrats = "That is incorrect! You lose, but you still get to walk away with ";
                    break;
                default:
                    break;
            }
        }
        if (won)
        {
            congrats = "CONGRATULATIONS! You have won $1 MILLION.";
        }
        
        congrats += winningMoney;
        window.lblEndMessage.setText(congrats);
       
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
    
    public void setLevel(int level)
    {
        currentLevel = level;
    }
    
    public boolean getPlaying()
    {
        return isPlaying;
    }
    
    public void setPlaying(boolean play)
    {
        isPlaying = play;
    }
    
    //--------- SCORE + DATABASE MANAGEMENT  ---------
    
    /*
        - load scores from DB into score list
    */
    private void getScores()
    {
        scores = new ArrayList<HighScore>();
        ResultSet rs = null;
        
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
        - save existing scores + new score to DB
        - calls method to print score board after saving to file
    */
    public void saveScore()
    {
        // calculate score
        int score = (prizeNum * 5) - (lifelinesUsed * 3);
        String saveName = JOptionPane.showInputDialog(window, "Enter your name:");
        if (saveName != null)
        {
            // if name is longer than 50 chars, abbreviate to ensure fits in DB field
            saveName = saveName.substring(0, Math.min(saveName.length(), 50));
            finalScore = new HighScore(saveName, score);
            scores.add(finalScore); // add users score to list
            Collections.sort(scores); // sort low to high
            Collections.reverse(scores); // reverse to high to low

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

            } catch (SQLException ex)
            {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }

            printScoreBoard(); // update score board

            closeDBConn();
        }
    }
    
    /*
        disconnects from the DB and shuts it down
    */
    public void closeDBConn()
    {
        try
        {
            state.close();
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException ex)
        {
            // ignore exception XJ015 which is always thrown on shutdown
        }
    }
    
    /*
        - prints score list formatted
    */
    public void printScoreBoard()
    {   
        Integer[] nums = new Integer[scores.size()];
        String[] names = new String[scores.size()];
        Integer[] scoreArr = new Integer[scores.size()];
        
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
    
    /*
        - connects to database
        - creates statement
    */
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
    
    /*
        - loads questions from database
        - returns 1 arraylist made up of 3 lists (one for each level)
        - should only ever be called after connToDB() 
        - if DB connection not already established, will try to connect
        - if connecting to DB succeeds, will try again to load questions
        - if fails, will end without loading questions and return empty lists
    */
    private ArrayList loadQuestions()
    {
        ArrayList<Question> level0 = new ArrayList();
        ArrayList<Question> level1 = new ArrayList();
        ArrayList<Question> level2 = new ArrayList();
        
        ResultSet rs = null;

        if (conn != null && state != null)
        {
            try
            {
                rs = state.executeQuery("SELECT * FROM QUESTIONS");

                while(rs.next())
                {
                    String[] answers = {rs.getString("ANS1"),rs.getString("ANS2"),rs.getString("ANS3"),rs.getString("ANS4")};
                    Question q = new Question(rs.getInt("QLEVEL"), rs.getString("QUESTION"),answers,rs.getInt("CORRANS"));

                    switch (q.getLevel()) 
                    {
                        case 0:
                            level0.add(q);
                            break;
                        case 1:
                            level1.add(q);
                            break;
                        case 2:
                            level2.add(q);
                            break;
                        default:
                            break;
                    }
                }

            } catch (SQLException ex)
            {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                connToDB();
            }
          }
        else
        {
            connToDB();
            // have to check conn succeeded before calling loadQuestions() otherwise 
            // loadQuestions() can recurse infinitely
            if (conn != null && state != null)
            {
               loadQuestions(); 
            }
        }
        
        ArrayList lists = new ArrayList<>();
        lists.add(level0);
        lists.add(level1);
        lists.add(level2);
        
        return lists;
    }
    

}