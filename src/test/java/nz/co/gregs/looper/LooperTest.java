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
import java.util.function.Consumer;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author gregorygraham
 */
public class LooperTest {

  public LooperTest() {
  }

  @Test
  public void testLoopUntilSuccessHasDefaultValues() {
    Looper looper = Looper.loopUntilSuccess();
    assertThat(looper.attempts(), is(0));
    assertThat(looper.getSuccessfulLoops(), is(0));
    assertThat(looper.isInfiniteLoopsPermitted(), is(true));
    assertThat(looper.isLimited(), is(false));
    assertThat(looper.getEndTime(), nullValue());
    assertThat(looper.getStartTime(), nullValue());
  }

  @Test
  public void testLoopUntilSuccessOrLimitHasDefaultValues() {
    Looper looper = Looper.loopUntilSuccessOrLimit();
    assertThat(looper.attempts(), is(0));
    assertThat(looper.getSuccessfulLoops(), is(0));
    assertThat(looper.isInfiniteLoopsPermitted(), is(false));
    assertThat(looper.isLimited(), is(true));
    assertThat(looper.getEndTime(), nullValue());
    assertThat(looper.getStartTime(), nullValue());
  }

  @Test
  public void testLoopUntilFailureHasDefaultValues() {
    Looper looper = Looper.loopUntilFailure();
    assertThat(looper.attempts(), is(0));
    assertThat(looper.getSuccessfulLoops(), is(0));
    assertThat(looper.isInfiniteLoopsPermitted(), is(true));
    assertThat(looper.isLimited(), is(false));
    assertThat(looper.getEndTime(), nullValue());
    assertThat(looper.getStartTime(), nullValue());
  }

  @Test
  public void testLoopUntilFailureOrLimitHasDefaultValues() {
    Looper looper = Looper.loopUntilFailureOrLimit();
    assertThat(looper.attempts(), is(0));
    assertThat(looper.getSuccessfulLoops(), is(0));
    assertThat(looper.isInfiniteLoopsPermitted(), is(false));
    assertThat(looper.isLimited(), is(true));
    assertThat(looper.getEndTime(), nullValue());
    assertThat(looper.getStartTime(), nullValue());
  }

  @Test
  public void testLoopChangesDefaultValues() {
    Looper looper = Looper.loopUntilSuccessOrLimit(1);
    looper.loop((index) -> {
    });
    assertThat(looper.attempts(), is(1));
    assertThat(looper.getSuccessfulLoops(), is(1));
    assertThat(looper.isInfiniteLoopsPermitted(), is(false));
    assertThat(looper.isLimited(), is(true));
    assertThat(looper.getEndTime(), greaterThan(Instant.now().minusSeconds(10)));
    assertThat(looper.getStartTime(), greaterThan(Instant.now().minusSeconds(20)));
    assertThat(looper.elapsedTime(), greaterThan(Duration.ZERO));
  }

  @Test
  public void testLoopReachesIntendedMax() {
    final int intendedAttempts = 10;

    Looper looper = Looper.loopUntilSuccessOrLimit(intendedAttempts);
    assertThat(looper.attempts(), is(0));

    looper.loop((index)
            -> System.out.println("testLoop: attempt " + looper.attempts())
    );
    assertThat(looper.attempts(), is(intendedAttempts));
  }

  @Test
  public void testLoopDefaultStopsAt1000Attempts() {
    Looper looper = Looper.loopUntilSuccessOrLimit();
    assertThat(looper.attempts(), is(0));

    looper.loop((index) -> {
    });
    assertThat(looper.attempts(), is(1000));
  }

  @Test
  public void testLoopWithSupplierAction() {
    final int intendedAttempts = 10;
    // Create the Looper
    Looper looper = Looper.loopUntilSuccessOrLimit(intendedAttempts);
    // Create the method to loop over
    final Consumer<LoopVariable> action = (state) -> {
    };
    // loop over the action
    looper.loop(action);
    assertThat(looper.attempts(), is(intendedAttempts));
  }

  @Test
  public void testLoopWithTest() {
    final int intendedAttempts = 10;
    Looper looper = Looper
            .loopUntilSuccessOrLimit(intendedAttempts * 2)
            .withStopOnSuccess(true);
    looper.loop(
            (state) -> {
            },
            (state) -> {
              return state.attempts() >= intendedAttempts;
            }
    );
    assertThat(looper.attempts(), is(intendedAttempts));
  }

  @Test
  public void testLoopWithTestButDontStopOnSuccess() {
    final int intendedAttempts = 10;
    Looper looper = Looper
            .loopUntilSuccessOrLimit(intendedAttempts * 2)
            .withStopOnSuccess(false);
    looper.loop(
            (state) -> {
            },
            (state) -> {
              return state.attempts() >= intendedAttempts;
            }
    );
    assertThat(looper.attempts(), is(intendedAttempts * 2));
  }

  @Test
  public void testReloopingWithoutReset() {
    final int intendedAttempts = 10;
    Looper looper = Looper.loopUntilSuccessOrLimit(intendedAttempts * 2);
    looper.loop(
            (state) -> {
            },
            (state) -> {
              return state.attempts() == intendedAttempts;
            }
    );
    assertThat(looper.attempts(), is(intendedAttempts));
    looper.loop(
            (state) -> {
              System.out.println("YOU SHOULD NOT SEE THIS");
              assertThat("ALREADY EXHAUSTED THE ATTEMPTS", false);
            },
            (state) -> {
              return looper.attempts() >= intendedAttempts;
            });
    assertThat(looper.attempts(), is(intendedAttempts));
  }

  @Test
  public void testReloopingWithReset() {
    final int intendedAttempts = 10;
    Looper looper = Looper.loopUntilSuccessOrLimit(intendedAttempts * 2);
    looper.loop(
            (state) -> {
            },
            (state) -> {
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
    Looper looper = Looper.loopUntilSuccessOrLimit(20);
    looper.loop((state) -> {
    });
    assertThat(looper.attempts(), is(20));
  }

  @Test
  public void testLoopStoppedByExceedingTimeoutInstant() {
    final int millis = 50;
    Looper looper = Looper.loopUntilSuccessOrLimit(Instant.now().plusMillis(millis));
    looper.loop((state) -> {
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException ex) {
//				Logger.getLogger(LooperTest.class.getName()).log(Level.SEVERE, null, ex);
//			}
    });
    final Instant now = Instant.now();
    final StopWatch stopWatch = looper.getLoopVariable().getStopWatch();
    final Instant timeout = stopWatch.getTimeout();
    assertThat(now, is(greaterThanOrEqualTo(timeout)));
    assertThat(looper.getEndTime(), greaterThanOrEqualTo(timeout));
    assertThat(looper.getLoopVariable().hasSucceeded(), is(false));
    assertThat(looper.getLoopVariable().hasFailed(), is(false));
    assertThat(looper.getLoopVariable().attempts(), is(not(1000)));
    assertThat(looper.getLoopVariable().getStopWatch().timedOut(), is(true));
  }

  @Test
  public void testLoopStoppedByExceedingTimeoutDuration() {
    final int millis = 50;
    Looper looper = Looper.loopUntilSuccessOrLimit(Duration.ofMillis(millis));
    looper.loop((state) -> {
    });
    final Instant now = Instant.now();
    final StopWatch stopWatch = looper.getLoopVariable().getStopWatch();
    final Instant timeout = stopWatch.getTimeout();
    assertThat(now, is(greaterThanOrEqualTo(timeout)));
    assertThat(looper.getEndTime(), greaterThanOrEqualTo(timeout));
    assertThat(looper.getLoopVariable().hasSucceeded(), is(false));
    assertThat(looper.getLoopVariable().hasFailed(), is(false));
    assertThat(looper.getLoopVariable().attempts(), is(not(1000)));
    assertThat(looper.getLoopVariable().getStopWatch().timedOut(), is(true));
  }
  
  @Test
  public void testIndexMethod(){
    String[] values = {"10\\00", "117090058", "117970084", "170,9 + 58", "179,7 + 84", "Flensburg Weiche, W 203 - Flensburg Grenze", "Flensburg-Weiche - Flensb. Gr", "Albert \"The Pain\" Hallsburg"};
    Looper looper = Looper.loopUntilLimit(values.length);
    looper.loop(
            (in) -> {
              System.out.println(in.attempts()+"/"+(in.index())+" |values: "+values[in.index()]);
              assertThat(values[in.index()], is(values[in.index()]));
              assertThat(in.index(),is(in.attempts()-1));
            });
    assertThat(looper.attempts(), is(values.length));
  }
}
