package psneo.services.digest2;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
     P10GpWc  C2018 01 20.57915 10 54 45.786+02 32 13.71         22.0 w      F51
     P10GpWc  C2018 01 20.59117 10 54 45.501+02 32 13.61         22.0 w      F51
     P10GpWc  C2018 01 20.60324 10 54 45.212+02 32 13.48         22.1 w      F51
 */

/**
 * Unit test for simple App.
 */
public class AppTest {
	/** Logging */
	private final static Logger logger = LoggerFactory.getLogger(AppTest.class);

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
    	logger.info("{}", Duration.ofMinutes(10));
        Assertions.assertTrue( true );
    }
}
