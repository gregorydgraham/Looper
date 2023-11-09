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

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements the variables need for a while/for loop combination with start and
 * end times, counting of loop attempts and successes, and more.
 *
 * @author gregorygraham
 */
public class LoopVariable implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean done = false;
	private int maxAttemptsAllowed = 1000;
	private boolean limitMaxAttempts = true;
	private int tries = 0;
	private int index = 0;
	private boolean allTestsSuccessful = true;
	private boolean someTestSuccessful = false;
	private boolean allTestsFailed = true;
	private boolean someTestFailed = false;
	ArrayList<Boolean> testResults = new ArrayList<Boolean>(0);
	HashMap<Integer, Boolean> successfulTests = new HashMap<Integer, Boolean>(0);
	HashMap<Integer, Boolean> failedTests = new HashMap<Integer, Boolean>(0);
	private StopWatch stopWatch = StopWatch.unstarted();
	private boolean failed = false;

	protected LoopVariable copy() {
		LoopVariable result = new LoopVariable();
		result.done = done;
		result.failed = failed;
		result.maxAttemptsAllowed = maxAttemptsAllowed;
		result.limitMaxAttempts = limitMaxAttempts;
		result.tries = tries;
		result.stopWatch = stopWatch.copy();
		result.index = index;
		result.allTestsSuccessful = allTestsSuccessful;
		result.someTestSuccessful = someTestSuccessful;
		result.allTestsFailed = allTestsFailed;
		result.someTestFailed = someTestFailed;
		result.testResults.addAll(testResults);

		return result;
	}

	public static LoopVariable withMaxAttempts(int size) {
		LoopVariable newLoop = LoopVariable.factory();
		newLoop.setMaxAttemptsAllowed(size);
		return newLoop;
	}

	public static LoopVariable withInfiniteLoopsPermitted(int size) {
		LoopVariable newLoop = LoopVariable.factory();
		newLoop.setInfiniteLoopPermitted();
		return newLoop;
	}

	public static LoopVariable factory() {
		return new LoopVariable();
	}

	public static LoopVariable factory(int max) {
		LoopVariable loopVariable = new LoopVariable();
		loopVariable.setMaxAttemptsAllowed(max);
		return loopVariable;
	}

	/**
	 * Checks the whether the loop is still done (that is {@link #done()} has not
	 * been called) and if the loop has exceeded the maximum attempts (if a max is
	 * defined).
	 *
	 * @return true if the loop is still done.
	 */
	public boolean isNeeded() {
		final boolean needed = !done && !failed && !tooManyAttempts() && !stopWatch.timedOut();
		return needed;
	}

	/**
	 * Checks the whether the loop is still done (that is {@link #done()} has not
	 * been called) and if the loop has exceeded the maximum attempts (if a max is
	 * defined).
	 *
	 * @return true if the loop is no longer done.
	 */
	public boolean isNotNeeded() {
		return !isNeeded();
	}

	/**
	 * Synonym for {@link #isNotNeeded() }.
	 *
	 * @return true if the loop is no longer done.
	 */
	public boolean hasHappened() {
		return isNotNeeded();
	}

	/**
	 * Synonym for {@link #isNeeded() }.
	 *
	 * @return true if the loop is still done.
	 */
	public boolean hasNotHappened() {
		return isNeeded();
	}

	/**
	 * Informs the LoopVariable that the loop has been successful and is no longer
	 * needed.
	 * <p>
	 * This method is used to indicate that a loop that takes multiple attempts to
	 * complete one task, has successfully completed that task.</p>
	 */
	public void done() {
		done(true);
	}

	/**
	 * Decides whether the LoopVariable has been successful and is no longer
	 * needed.
	 * <p>
	 * This method is used to indicate that a loop, that takes multiple attempts
	 * to complete one task, has successfully completed that task.</p>
	 *
	 * @param done the value to set done to, if the expression is TRUE then the
	 * loo is done and no longer done.
	 */
	public void done(boolean done) {
		this.done = done;
	}

	/**
	 * Indicates that an attempt has been started.
	 *
	 * <p>
	 * This method is used to indicate that a loop that takes multiple attempts to
	 * complete one task, has started an attempt to complete that task. Each cvall
	 * of {@link #attempt() } counts towards the
	 * {@link #setMaxAttemptsAllowed(int) maximum attempts} if a maximum has been
	 * set.</p>
	 *
	 * @return
	 */
	public boolean attempt() {
		stopWatch.startIfNeeded();
		final boolean needed = isNeeded(); // check if it's allowed
		if (needed) {
			tries++; // increment the counter as we will make an attempt
		} else {
			stopWatch.end();
		}
		return needed;
	}

	/**
	 * The number of attempts recorded using {@link #attempt() }.
	 *
	 * @return the number of attempts started.
	 */
	public int attempts() {
		return tries;
	}

	/**
	 * Sets the maximum attempts allowed for this loop variable.
	 *
	 * <p>
	 * Maximum attempts will stop a correctly used LoopVariable after the maximum
	 * attempts by changing {@link #isNeeded() } to false</p>
	 *
	 * <p>
	 * Attempts are registered by calling {@link #attempt() } at the start of each
	 * loop.</p>
	 *
	 * @param maxAttemptsAllowed the number of attempts after which the loop will
	 * abort.
	 * @return this object with the configuration changed
	 */
	public LoopVariable setMaxAttemptsAllowed(int maxAttemptsAllowed) {
		if (maxAttemptsAllowed > 0) {
			limitMaxAttempts = true;
			this.maxAttemptsAllowed = maxAttemptsAllowed;
		} else {
			limitMaxAttempts = false;
		}
		return this;
	}

	/**
	 * Removes the default limit from the LoopVariable.
	 *
	 * <p>
	 * By default {@link #isNeeded() } will return false after 1000 attempts. Use
	 * this method to remove the limit and permit infinite loops.</p>
	 *
	 * <p>
	 * Alternatively you can seta higher, or lower, limit with {@link #setMaxAttemptsAllowed(int)
	 * }.</p>
	 *
	 * @return this object with the configuration changed
	 */
	public LoopVariable setInfiniteLoopPermitted() {
		limitMaxAttempts = false;
		return this;
	}

	/**
	 * Returns the time recorded at the start of the loop
	 *
	 * @return the time that the loop was initiated
	 */
	public Instant getStartTime() {
		return stopWatch.startTime();
	}

	/**
	 * Returns the time recorded at the end of the loop
	 *
	 * @return the time that the loop was finished
	 */
	public Duration elapsedTime() {
		Duration duration = Duration.between(getStartTime(), getEndTime());
		return duration;
	}

	/**
	 * Resets all the loop variables to their default settings so the loop can be
	 * re-run
	 *
	 * Max attempts is not reset.
	 */
	public void reset() {
		tries = 0;
		done = false;
		stopWatch.blank();
	}

	/**
	 * Returns TRUE if the loop will stop after a maximum number of attempts
	 *
	 * @return return true is the number of loop attempts is limited
	 */
	protected boolean isLimited() {
		return limitMaxAttempts;
	}

	/**
	 * the number of times the loop will be attempted
	 *
	 * <p>
	 * Maybe zero (0) if the loop is set to allow infinite loops</p>
	 *
	 * @return the maximum number of loops
	 */
	protected int maxAttemptsAllowed() {
		return maxAttemptsAllowed;
	}

	/**
	 * Similar to {@link #attempts() }, this reports the number of loops that have
	 * been successfully completed.
	 *
	 * <p>
	 * Attempts should be incremented at the start of a loop and successfulLoops
	 * should be incremented at the end, so attempts and successfulLoops may not
	 * be the same.
	 *
	 * @return the number of successful loops
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * increments the successful loop counter
	 */
	protected void incrementIndex() {
		index++;
	}

	/**
	 * Returns the end time of the loop
	 *
	 * @return and instant recorded at the end of the loop
	 */
	public Instant getEndTime() {
		return stopWatch.endTime();
	}

	void addTestResult(Boolean testSuccessful) {
		allTestsSuccessful &= testSuccessful;
		someTestSuccessful |= testSuccessful;
		allTestsFailed &= !testSuccessful;
		someTestFailed |= !testSuccessful;
		testResults.add(this.index, testSuccessful);
		if (testSuccessful) {
			successfulTests.put(this.index, testSuccessful);
		} else {
			failedTests.put(this.index, testSuccessful);
		}
	}

	/**
	 * @return the allTestSuccessful
	 */
	public boolean isAllTestsSuccessful() {
		return allTestsSuccessful;
	}

	/**
	 * @return the anyTestSuccessful
	 */
	public boolean isSomeTestsSuccessful() {
		return someTestSuccessful;
	}

	/**
	 * @return the allTestFailed
	 */
	public boolean isAllTestsFailed() {
		return allTestsFailed;
	}

	/**
	 * @return the anyTestFailed
	 */
	public boolean isSomeTestsFailed() {
		return someTestFailed;
	}

	public void stopTimer() {
		stopWatch.end();
	}

	private boolean tooManyAttempts() {
		if (limitMaxAttempts) {
			return attempts() >= maxAttemptsAllowed;
		} else {
			return false;
		}
	}

	public void failed() {
		this.failed = true;
	}

	public void setTimeout(Duration limit) {
		stopWatch.setTimeout(limit);
	}

	public void setTimeout(Instant limit) {
		stopWatch.setTimeout(limit);
	}

	public StopWatch getStopWatch() {
		return stopWatch;
	}
	
	public boolean hasFailed(){
		return failed;
	}
	
	public boolean hasSucceeded(){
		return done;
	}
}
