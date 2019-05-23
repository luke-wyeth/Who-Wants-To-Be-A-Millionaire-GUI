package Tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pdcprojectgui.PAF;
import pdcprojectgui.Question;

/**
 *
 * @author Luke
 */
public class PAFTest
{
    public PAF paf;
    public Question testQuestion;
    
    @Before
    public void setUp()
    {
        paf = new PAF();
        testQuestion = new Question(); 
    }

    /*
        test functionality of trying to use unused lifeline
        - checking used state
    */
    @Test
    public void testUse()
    {
        boolean expectedUsedState = false;
        boolean actualUsedState = paf.getUsed();
        
        Assert.assertEquals(expectedUsedState, actualUsedState);
        
        // lifelines return 1 after successfully completing
        // they return -1 if lifeline had already been  used, didn't successfully complete
        int expectedReturn = 1;
        int actualReturn = paf.use(testQuestion);
        
        Assert.assertEquals(expectedReturn, actualReturn);
    }
    
    /*
        test functionality of trying to use already used lifeline
        - checking used state
        - checking lifeline can't be used again
    */
    @Test
    public void testAlreadyUsed()
    {
        paf.setUsed(); // set to already used
        
        boolean expectedUsedState = true;
        boolean actualUsedState = paf.getUsed();
        
        Assert.assertEquals(expectedUsedState, actualUsedState);
        
        // lifelines return 1 after successfully completing
        // they return -1 if lifeline had already been  used, didn't successfully complete
        int expectedReturn = -1;
        int actualReturn = paf.use(testQuestion);
        
        Assert.assertEquals(expectedReturn, actualReturn);
    }
}
