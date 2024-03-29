package com.tegapp.motari;

import android.widget.EditText;
import android.widget.ImageButton;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

//There is already a user which was stored in firebase
//email:testforrider@gmail.com
//password:123456
//name:Rider Test1
//phone:5879888888
//rider

/**
 * Test class for MainMenuR Activity
 */
public class MainMenuRTest {
    private Solo solo;
    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * Runs before all tests and creates solo instance.
     * The user logs in successfully
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        solo.assertCurrentActivity("Not in Main Activity", MainActivity.class);
        solo.enterText((EditText) solo.getView(R.id.login_email), "testforrider@gmail.com");
        solo.enterText((EditText) solo.getView(R.id.login_password), "123456");
        solo.clickOnButton("LOGIN");
    }

    /**
     * Check whether activity correctly switched
     * Go to MainActivity after logout
     */
    @Test
    public void checkActivity(){
        solo.assertCurrentActivity("Not in correct Activity", Activity_MainMenuR.class);
        ImageButton br = (ImageButton) solo.getView(R.id.logout_r_image_button);
        solo.clickOnView(br);
        solo.assertCurrentActivity("Not in right Activity", MainActivity.class);
    }

    /**
     * checks if warning toast appears when clicking on my current request when no request is active
     * @throws Exception
     */
    @Test
    public void checkNoCurrentRequestToast() throws Exception {
        solo.assertCurrentActivity("Wrong Activity", Activity_MainMenuR.class);
        solo.clickOnText("My Current Requests");
        assertTrue("Toast does not show", solo.waitForText("No Current Active Requests"));
    }

    /**
     * Close activity after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
