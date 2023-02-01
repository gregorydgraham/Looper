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

/**
 * Implements a while/for loop combination.
 *
 * @author gregorygraham
 */
public class LoopVariable implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean needed = true;
	private int maxAttemptsAllowed = 1000;
	private boolean limitMaxAttempts = true;
	private int tries = 0;
	private Instant startTime = Instant.now();
	private Instant endTime = null;
	private int successfulLoops = 0;
	

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
	 * Checks the whether the loop is still needed (that is {@link #done()} has
	 * not been called) and if the loop has exceeded the maximum attempts (if a
	 * max is defined).
	 *
	 * @return true if the loop is still needed.
	 */
	public boolean isNeeded() {
		if (limitMaxAttempts) {
			return needed && attempts() + 1 <= maxAttemptsAllowed;
		} else {
			return needed;
		}
	}

	/**
	 * Checks the whether the loop is still needed (that is {@link #done()} has
	 * not been called) and if the loop has exceeded the maximum attempts (if a
	 * max is defined).
	 *
	 * @return true if the loop is no longer needed.
	 */
	public boolean isNotNeeded() {
		return !isNeeded();
	}

	/**
	 * Synonym for {@link #isNotNeeded() }.
	 *
	 * @return true if the loop is no longer needed.
	 */
	public boolean hasHappened() {
		return isNotNeeded();
	}

	/**
	 * Synonym for {@link #isNeeded() }.
	 *
	 * @return true if the loop is still needed.
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
		needed = false;
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
	 */
	public void attempt() {
		tries++;
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
	 *
	 * @return
	 */
	public Instant getStartTime() {
		return startTime;
	}

	public Duration elapsedTime() {
		Duration duration = Duration.between(getStartTime(), getEndTime());
		return duration;
	}

	public void reset() {
		tries = 0;
		needed = true;
		startTime = Instant.now();
		endTime = null;
	}

	protected boolean isLimited() {
		return limitMaxAttempts;
	}

	protected int maxAttemptsAllowed() {
		return maxAttemptsAllowed;
	}

	public void startTimer() {
		startTime = Instant.now();
	}

	public void stopTimer() {
		endTime = Instant.now();
	}

	int getSuccessfulLoops() {
		return successfulLoops;
	}

	void incrementSuccessLoops() {
		successfulLoops++;
	}

	public Instant getEndTime() {
		return endTime;
	}
}
