package pdcprojectgui;

public class Question
{ 
    // private and final to avoid tampering by other classes
    private String question;
    private String[] answers;
    private int correctAns;
    private int level; 
    
    public Question()
    {
        this.level = 0;
        this.question = "";
        this.answers = new String[4];
        this.correctAns = 0;
    }
    
    public Question(int level, String question, String[] answers, int correctAns)
    {
        this.level = level;
        this.question = question;
        this.answers = answers;
        this.correctAns = correctAns;
    }
    
    public Question(String question, String[] answers, int correctAns)
    {
        this.level = 0;
        this.question = question;
        this.answers = answers;
        this.correctAns = correctAns;
    }
    
    public int getLevel()
    {
        return level;
    }
    
    public String getQuestion()
    {
        return question;
    }
    
    public String[] getAnswers()
    {
        return answers;
    }
    
    public void setQuestion(String question)
    {
        this.question = question;
    }
    
    public void setAnswers(int index, String newAns) 
    {
        answers[index] = newAns;
    }
    
    public void setLevel(int level)
    {
        this.level = level;
    }
    
    public void setCorrectAns(int ans)
    {
        this.correctAns = ans;
    }
    
    public int getCorrectAns()
    {
        return correctAns;
    }
    
    public void printQuestion()
    {
        System.out.println(question);
        
        for (int count = 0; count < 4; count++)
        {
            System.out.println((count+1) + ") " + answers[count]);
        }
    }
}