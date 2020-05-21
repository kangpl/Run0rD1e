package ch.epfl.sdp.database.databaseUtils;

import org.junit.Test;

import ch.epfl.sdp.database.utils.CustomResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CustomResultTest {
    @Test
    public void CustomResultTest() {
        CustomResult<String> customResultTest = new CustomResult<>("", true, null);
        customResultTest.setResult("test");
        assertEquals("test", customResultTest.getResult());

        customResultTest.setSuccessful(false);
        assertEquals(false, customResultTest.isSuccessful());

        customResultTest.setException(null);
        assertNull(customResultTest.getException());
    }
}