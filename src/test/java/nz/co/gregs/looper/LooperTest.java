/*
 * Copyright 2021 Gregory Graham.
 *
 * Commercial licenses are available, please contact info@gregs.co.nz for details.
 * 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/ 
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 * 
 * You are free to:
 *     Share - copy and redistribute the material in any medium or format
 *     Adapt - remix, transform, and build upon the material
 * 
 *     The licensor cannot revoke these freedoms as long as you follow the license terms.               
 *     Under the following terms:
 *                 
 *         Attribution - 
 *             You must give appropriate credit, provide a link to the license, and indicate if changes were made. 
 *             You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 *         NonCommercial - 
 *             You may not use the material for commercial purposes.
 *         ShareAlike - 
 *             If you remix, transform, or build upon the material, 
 *             you must distribute your contributions under the same license as the original.
 *         No additional restrictions - 
 *             You may not apply legal terms or technological measures that legally restrict others from doing anything the 
 *             license permits.
 * 
 * Check the Creative Commons website for any details, legalese, and updates.
 */
package nz.co.gregs.looper;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.hamcrest.Matcher;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author gregorygraham
 */
public class LooperTest {

	public LooperTest() {
	}

	@Test
	public void testLooperHasDefaultValues() {
		Looper looper = Looper.factory();
		assertThat(looper.attempts(), is(0));
		assertThat(looper.getSuccessfulLoops(), is(0));
		assertThat(looper.hasException(), is(false));
		assertThat(looper.getException(), nullValue());
		assertThat(looper.isInfiniteLoopsPermitted(), is(false));
		assertThat(looper.isLimited(), is(true));
		assertThat(looper.getEndTime(), nullValue());
		assertThat(looper.getStartTime(), greaterThan(Instant.now().minusSeconds(10)));
	}

	@Test
	public void testLoopChangesDefaultValues() {
		Looper looper = Looper.factory(1);
		looper.loop((index) -> {;
		});
		assertThat(looper.attempts(), is(1));
		assertThat(looper.getSuccessfulLoops(), is(1));
		assertThat(looper.hasException(), is(false));
		assertThat(looper.getException(), nullValue());
		assertThat(looper.isInfiniteLoopsPermitted(), is(false));
		assertThat(looper.isLimited(), is(true));
		assertThat(looper.getEndTime(), greaterThan(Instant.now().minusSeconds(10)));
		assertThat(looper.getStartTime(), greaterThan(Instant.now().minusSeconds(20)));
		assertThat(looper.elapsedTime(), greaterThan(Duration.ZERO));
	}

	@Test
	public void testLoopWithExceptionsChangesDefaultValues() {
		Looper looper = Looper.factory(1);
		Exception exc = looper.loopWithExceptionHandling((index) -> {
			return null;
		});
		assertThat(looper.attempts(), is(1));
		assertThat(looper.getSuccessfulLoops(), is(1));
		assertThat(looper.hasException(), is(false));
		assertThat(looper.getException(), nullValue());
		assertThat(looper.isInfiniteLoopsPermitted(), is(false));
		assertThat(looper.isLimited(), is(true));
		assertThat(looper.getEndTime(), greaterThan(Instant.now().minusSeconds(10)));
		assertThat(looper.getStartTime(), greaterThan(Instant.now().minusSeconds(20)));
		assertThat(looper.elapsedTime(), greaterThan(Duration.ZERO));
	}

	@Test
	public void testLoopWithExceptionsThrowingExceptionChangesDefaultValues() {
		Looper looper = Looper.factory(1);
		Exception exc = looper.loopWithExceptionHandling((index) -> {
			return new Exception();
		});
		String message = exc.getMessage();
		assertThat(looper.attempts(), is(1));
		assertThat(looper.getSuccessfulLoops(), is(0));
		assertThat(looper.hasException(), is(true));
		assertThat(looper.getException(), is(exc));
		assertThat(looper.getException().getMessage(), is(message));
		assertThat(looper.isInfiniteLoopsPermitted(), is(false));
		assertThat(looper.isLimited(), is(true));
		assertThat(looper.getEndTime(), greaterThan(Instant.now().minusSeconds(10)));
		assertThat(looper.getStartTime(), greaterThan(Instant.now().minusSeconds(20)));
		assertThat(looper.elapsedTime(), greaterThan(Duration.ZERO));
	}

	@Test
	public void testLoopReachesIntendedMax() {
		final int intendedAttempts = 10;

		Looper looper = Looper.factory(intendedAttempts);
		assertThat(looper.attempts(), is(0));

		looper.loop((index)
				-> System.out.println("testLoop: attempt " + looper.attempts())
		);
		assertThat(looper.attempts(), is(intendedAttempts));
	}

	@Test
	public void testLoopWithExceptionHandlingReachesIntendedMax() {
		final int intendedAttempts = 10;

		Looper looper = Looper.factory(intendedAttempts);

		Exception exc
				= looper.loopWithExceptionHandling((index) -> {
					System.out.println("testLoop: attempt " + looper.attempts());
					return null;
				}
				);
		assertThat(looper.attempts(), is(intendedAttempts));
		assertThat(exc, nullValue());
	}

	@Test
	public void testLoopDefaultStopsAt1000Attempts() {
		Looper looper = Looper.factory();
		assertThat(looper.attempts(), is(0));

		looper.loop((index) -> {
			// do your processing
			// here
		});
		assertThat(looper.attempts(), is(1000));
	}

	@Test
	public void testLoopWithExceptionDefaultStopsAt1000Attempts() {
		Looper looper = Looper.factory();
		assertThat(looper.attempts(), is(0));

		looper.loopWithExceptionHandling((index) -> {
			// do your processing
			// here
			return null;
		});
		assertThat(looper.attempts(), is(1000));
	}

	@Test
	public void testLoopWithSupplierAction() {
		final int intendedAttempts = 10;
		// Create the Looper
		Looper looper = Looper.factory(intendedAttempts);
		// Create the method to loop over
		final Supplier<Exception> action = () -> {
			// do your processing
			// here

			// return null as required by the Java spec
			return null;
		};
		// loop over the action
		looper.loop(action);
		assertThat(looper.attempts(), is(intendedAttempts));
	}

	@Test
	public void testLoopWithTest() {
		final int intendedAttempts = 10;
		Looper looper = Looper.factory(intendedAttempts * 2);
		looper.loop(
				() -> {
					return null;
				},
				() -> {
					return looper.attempts() >= intendedAttempts;
				}
		);
		assertThat(looper.attempts(), is(intendedAttempts));
	}

	@Test
	public void testReloopingWithoutReset() {
		final int intendedAttempts = 10;
		Looper looper = Looper.factory(intendedAttempts * 2);
		looper.loop(
				() -> {
					return null;
				},
				() -> {
					return looper.attempts() >= intendedAttempts;
				}
		);
		assertThat(looper.attempts(), is(intendedAttempts));
		looper.loop(
				(index) -> {
					System.out.println("YOU SHOULD NOT SEE THIS");
				},
				(index) -> {
					return looper.attempts() >= intendedAttempts;
				});
		assertThat(looper.attempts(), is(intendedAttempts));
	}

	@Test
	public void testReloopingWithReset() {
		final int intendedAttempts = 10;
		Looper looper = Looper.factory(intendedAttempts * 2);
		looper.loop(
				() -> {
					return null;
				},
				() -> {
					return looper.attempts() >= intendedAttempts;
				}
		);
		assertThat(looper.attempts(), is(intendedAttempts));

		looper.reset();
		looper.loop(
				(index) -> {
					System.out.println("this is working");
				},
				(index) -> {
					return looper.attempts() >= intendedAttempts;
				});
		assertThat(looper.attempts(), is(intendedAttempts));
	}

	@Test
	public void testLoopStoppedByExceedingMaxAttemptsAllowed() {
		Looper looper = Looper.factory(20);
		looper.loop(() -> {
			return null;
		});
		assertThat(looper.attempts(), is(20));
	}
}
