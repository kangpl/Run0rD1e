//package ch.epfl.sdp;
//
//import android.app.Activity;
//import android.app.Instrumentation;
//import android.content.Intent;
//
//import androidx.test.espresso.intent.Intents;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.rule.ActivityTestRule;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import ch.epfl.sdp.database.firebase.api.CommonDatabaseAPI;
//import ch.epfl.sdp.login.LoginFormActivity;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.typeText;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//
//@RunWith(AndroidJUnit4.class)
//public class LoginTest {
//    private String email;
//    private String password;
//    private Instrumentation.ActivityResult result;
//    private CommonDatabaseAPI commonDatabaseAPI;
//
//    @Rule
//    public final ActivityTestRule<LoginFormActivity> mActivityRule =
//            new ActivityTestRule<>(LoginFormActivity.class);
//
//    @Before
//    public void setUp() {
//        email = "amro.abdrabo@gmail.com";
//        password = "password";
//
//        Intents.init();
//        Intent resultData = new Intent();
//        resultData.putExtra("resultData", "fancyData");
//
//        result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
//        commonDatabaseAPI = new MockCommonDatabaseAPI();
//        mActivityRule.getActivity().authenticationAPI = new MockAuthenticationAPI(commonDatabaseAPI);
//    }
//
//    @After
//    public void tearDown() {
//        Intents.release();
//    }
//
//    @Test
//    public void writingEmail_Works() {
//        onView(withId(R.id.emaillog)).perform(typeText(email)).check(matches(withText(email)));
//    }
//
//    @Test
//    public void writingPassword_Works() {
//        onView(withId(R.id.passwordlog)).perform(typeText(password)).check(matches(withText(password)));
//    }
//
//    @Test
//    public void login_shouldWorkWithRegisteredUser() {
//        MissingFieldTestFactory.testFieldTwoActionsCloseKeyboard(typeText(email), typeText(password), R.id.emaillog, R.id.passwordlog);
//        onView(withId(R.id.loginButton)).perform(click());
//        onView(withId(R.id.logoutBt));
//    }
//
//   /* @Test
//    public void loginUnregisteredUserGivesAnError(){
//        MissingFieldTestFactory.testFieldTwoActionsCloseKeyboard(typeText("NotAUser@mail.com"), typeText("12345678"), R.id.emaillog, R.id.passwordlog);
//        onView(withId(R.id.loginButton)).perform(click());
//    }*/
//
//    @Test
//    public void loginWithAnEmptyEmailGivesAnError(){
//        onView(withId(R.id.loginButton)).perform(click());
//        String text = "Email can't be empty";
//        onView(withId(R.id.emaillog)).check(matches(hasErrorText(text)));
//    }
//
//    @Test
//    public void loginWithAnEmptyPasswordGivesAnError(){
//        MissingFieldTestFactory.testFieldTwoActions(typeText("amro.abdrabo@gmail.com"), click(), R.id.emaillog, R.id.loginButton);
//        String text = "Password can't be empty";
//        onView(withId(R.id.passwordlog)).check(matches(hasErrorText(text)));
//    }
//
//  /*  @Test
//    public void loginUnregisteredUserGivesAnError(){
//        typingEmailPasswordAndCheckHasError("NotAUser@mail.com", "12345678", "User not exist!", R.id.emaillog);
//    }
//
//    @Test
//    public void loginOnPasswordSmallerThan8CharsGivesAnError(){
//        typingEmailPasswordAndCheckHasError("amro.abdrabo@gmail.com", "1234567", "Password not correct", R.id.passwordlog);
//    }
//
//    private void typingEmailPasswordAndCheckHasError(String email, String password, String error, int view) {
//        MissingFieldTestFactory.testFieldTwoActionsCloseKeyboard(typeText(email),typeText(password), R.id.emaillog, R.id.passwordlog);
//        onView(withId(R.id.loginButton)).perform(click());
//        onView(withId(view)).check(matches(hasErrorText(error)));
//    }*/
//}