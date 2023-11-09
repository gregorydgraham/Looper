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
import java.util.function.Function;
import java.util.function.Consumer;

/**
 * Implements a while/for loop combination.
 *
 * <p>
 * Looper attempts to use functional interfaces to implement an improved looping
 * system.
 *
 * @author gregorygraham
 */
public class Looper implements Serializable {

	private static final long serialVersionUID = 1L;

	private final LoopVariable currentState = new LoopVariable();

	private Consumer<LoopVariable> action = doNothing();
	private Function<LoopVariable, Boolean> test = returnFalse();
	private Boolean stopOnSuccess = true;
	private Boolean stopOnFailure = false;
	private Consumer<LoopVariable> allTestsSuccessfulAction;
	private Consumer<LoopVariable> someTestsSuccessfulAction;
	private Consumer<LoopVariable> allTestsFailedAction;
	private Consumer<LoopVariable> someTestsFailedAction;
	private Consumer<LoopVariable> successfulTestAction = doNothing();
	private Consumer<LoopVariable> failedTestAction = doNothing();

	private Looper() {
	}

	public static Looper loopUntilSuccess() {
		Looper newLoop
				= Looper.factory()
						.withStopOnSuccess(true)
						.withStopOnFailure(false)
						.withInfiniteLoopPermitted();
		return newLoop;
	}

	public static Looper loopUntilLimit(int limit) {
		Looper newLoop
				= Looper.factory()
						.withStopOnSuccess(false)
						.withStopOnFailure(false)
						.withMaxAttemptsAllowed(limit);
		return newLoop;
	}

	public static Looper loopUntilSuccessOrLimit() {
		Looper result = loopUntilSuccess()
				.withMaxAttemptsAllowed(1000);
		return result;
	}

	public static Looper loopUntilSuccessOrLimit(int limit) {
		Looper result = loopUntilSuccess()
				.withMaxAttemptsAllowed(limit);
		return result;
	}

	public static Looper loopUntilSuccessOrLimit(Duration limit) {
		Looper result = loopUntilSuccess()
				.withInfiniteLoopsPermitted()
				.withTimeout(limit);
		return result;
	}

	public static Looper loopUntilSuccessOrLimit(Instant limit) {
		Looper result = loopUntilSuccess()
				.withInfiniteLoopsPermitted()
				.withTimeout(limit);
		return result;
	}

	public static Looper loopUntilFailure() {
		Looper newLoop
				= Looper.factory()
						.withStopOnSuccess(false)
						.withStopOnFailure(true)
						.withInfiniteLoopPermitted();
		return newLoop;
	}

	public static Looper loopUntilFailureOrLimit(int limit) {
		Looper result = loopUntilFailure()
				.withMaxAttemptsAllowed(limit);
		return result;
	}

	public static Looper loopUntilFailureOrLimit() {
		Looper result = loopUntilFailure()
				.withMaxAttemptsAllowed(1000);
		return result;
	}

	private static Looper factory() {
		return new Looper();
	}

	private static Consumer<LoopVariable> doNothing() {
		return (index) -> {
		};
	}

	private static Function<LoopVariable, Boolean> returnFalse() {
		return (d) -> {
			return false;
		};
	}

	public void reset() {
		currentState.reset();
	}

//	/**
//	 * Checks the whether the loop is still needed (that is {@link #done()} has
//	 * not been called) and if the loop has exceeded the maximum attempts (if a
//	 * max is defined).
//	 *
//	 * @return true if the loop is still needed.
//	 */
//	private boolean isNeeded() {
//		return currentState.isNeeded();
//	}
	/**
	 * The number of loops started.
	 *
	 * This may be different from the number of loops successfully completed, use {@link #getSuccessfulLoops()
	 * } to for that.
	 *
	 * @return the number of attempts started.
	 */
	public int attempts() {
		return currentState.attempts();
	}

	public Duration elapsedTime() {
		return currentState.elapsedTime();
	}

	/**
	 * Sets the maximum attempts allowed for this loop variable.
	 *
	 * <p>
	 * Maximum attempts will stop looping after trying this many times</p>
	 *
	 * @param maxAttemptsAllowed the number of attempts after which the loop will
	 * abort.
	 * @return this object with the configuration changed
	 */
	public Looper withMaxAttemptsAllowed(int maxAttemptsAllowed) {
		currentState.setMaxAttemptsAllowed(maxAttemptsAllowed);
		return this;
	}

	/**
	 * Removes the default limit from the LoopVariable.
	 *
	 * <p>
	 * By default the loop will terminate after 1000 attempts. Use this method to
	 * remove the limit and permit infinite loops.</p>
	 *
	 * <p>
	 * Alternatively you can seta higher, or lower, limit with {@link #withMaxAttemptsAllowed(int)
	 * }.</p>
	 *
	 * @return this object with the configuration changed
	 */
	public Looper withInfiniteLoopPermitted() {
		currentState.setInfiniteLoopPermitted();
		return this;
	}

	public Looper withMaxAttempts(int size) {
		withMaxAttemptsAllowed(size);
		return this;
	}

	/**
	 * Removes the default limit from the LoopVariable.
	 *
	 * <p>
	 * By default the loop will terminate after 1000 attempts. Use this method to
	 * remove the limit and permit infinite loops.</p>
	 *
	 * <p>
	 * Alternatively you can seta higher, or lower, limit with {@link #withMaxAttemptsAllowed(int)
	 * }.</p>
	 *
	 * @return this object with the configuration changed
	 */
	public Looper withInfiniteLoopsPermitted() {
		currentState.setInfiniteLoopPermitted();
		return this;
	}

	/**
	 * Performs action until test returns true.
	 *
	 * <p>
	 * This is a re-implementation of the while loop mechanism. Probably not as
	 * good as the actual while loop but it was fun to do.</p>
	 *
	 * <pre>
	 * Looper looper = new Looper();
	 * final int intendedAttempts = 10;
	 * looper.loop(
	 * () -&gt; {
	 *					// do your processing here
	 *
	 *					// return null as required by Java
	 *					return null;
	 *				}
	 *		);
	 * </pre>
	 *
	 * @param action the action to perform with a loop
	 */
	public void loop(Consumer<LoopVariable> action) {
		loop(action, returnFalse());
	}

	/**
	 * Performs action until test returns true.
	 *
	 * <p>
	 * This is a re-implementation of the while loop mechanism. Probably not as
	 * good as the actual while loop but it was fun to do.</p>
	 *
	 * <pre>
	 * Looper looper = new Looper();
	 * looper.loop(
	 * (index) -&gt; {
	 *					// do your processing here
	 *
	 *					// return null as required by Java
	 *					return null;
	 *				},
	 *				(index) -&gt; {
	 *					// Check for termination conditions here
	 *					return trueIfTaskCompletedOtherwiseFalse();
	 *				}
	 *		);
	 * </pre>
	 *
	 * @param action the action to perform with a loop
	 * @param test the test to check, if TRUE the loop will be terminated, if
	 * FALSE the loop will continue
	 */
	public void loop(Consumer<LoopVariable> action, Function<LoopVariable, Boolean> test) {
		loop(action, test, doNothing(), doNothing());
	}

	/**
	 * Performs action until test returns true.
	 *
	 * <p>
	 * This is a re-implementation of the while loop mechanism. Probably not as
	 * good as the actual while loop but it was fun to do.</p>
	 *
	 * <pre>
	 * Looper looper = new Looper();
	 * looper.loop(
	 * (index) -&gt; {
	 *					// do your processing here
	 *
	 *					// return null as required by Java
	 *					return null;
	 *				},
	 *				(index) -&gt; {
	 *					// Check for termination conditions here
	 *					return trueIfTaskCompletedOtherwiseFalse();
	 *				},
	 *				(index) -&gt; {
	 *					// perform any post loop operations here
	 *					System.out.println("Completed loop after "+attempts()+" attempts");
	 *				}
	 *		);
	 * </pre>
	 *
	 * @param action the action to perform with a loop
	 * @param test the test to check, if TRUE the loop will be terminated, if
	 * FALSE the loop will continue
	 * @param completion the action to perform immediately after the loop
	 */
	public void loop(Consumer<LoopVariable> action, Function<LoopVariable, Boolean> test, Consumer<LoopVariable> completion) {
		loop(action, test, completion, completion);
	}

	/**
	 * Performs action until test returns true.
	 *
	 * <p>
	 * This is a re-implementation of the while loop mechanism. Probably not as
	 * good as the actual while loop but it was fun to do.</p>
	 *
	 * <p>
	 * This method is particularly good for re-attempting operations that failed
	 * but may have been affected by transient issues (network packet loss, broken
	 * database, etc).</p>
	 *
	 * <pre>
	 * Looper looper = new Looper();
	 * looper.loop(
	 * (index) -&gt; {
	 *					// do your processing here
	 *
	 *					// return null as required by Java
	 *					return null;
	 *				},
	 *				(index) -&gt; {
	 *					// Check for termination conditions here
	 *					return trueIfTaskCompletedOtherwiseFalse();
	 *				},
	 *				(index) -&gt; {
	 *					// perform any post successful loop operations here
	 *					System.out.println("Completed loop successfully after "+attempts()+" attempts");
	 *				}
	 *				(index) -&gt; {
	 *					// perform any post failed loop operations here
	 *					System.out.println("Completed loop without success after "+attempts()+" attempts");
	 *				}
	 *		);
	 * </pre>
	 *
	 * @param action the action to perform with a loop
	 * @param test the test to check, if TRUE the loop will be terminated, if
	 * FALSE the loop will continue
	 * @param successfulCompletion the action to perform immediately after the
	 * loop if the test is true after completion
	 * @param unsuccessfulCompletion the action to perform immediately after the
	 * loop if the test fails after completion
	 */
	public void loop(Consumer<LoopVariable> action, Function<LoopVariable, Boolean> test, Consumer<LoopVariable> successfulCompletion, Consumer<LoopVariable> unsuccessfulCompletion) {
		this.withAction(action)
				.withTest(test)
				.withSomeTestsSuccessfulAction(successfulCompletion)
				.withAllTestsFailedAction(unsuccessfulCompletion)
				.loop();
	}

	public void loop() {
		Boolean testSuccessful;
		while (currentState.attempt()) {
//			currentState.attempt();
			action.accept(currentState.copy());
			testSuccessful = test.apply(currentState.copy());
			currentState.addTestResult(testSuccessful);
			if (testSuccessful) {
				if (stopOnSuccess) {
					currentState.done();
				}
				if (successfulTestAction != null) {
					successfulTestAction.accept(currentState.copy());
				}
			} else {
				if (stopOnFailure) {
					currentState.failed();
				}
				if (failedTestAction != null) {
					failedTestAction.accept(currentState.copy());
				}
			}
			incrementIndex();
		}
		currentState.stopTimer();
		if (currentState.isAllTestsSuccessful() && (allTestsSuccessfulAction != null)) {
			allTestsSuccessfulAction.accept(currentState.copy());
		}
		if (currentState.isAllTestsFailed() && (allTestsFailedAction != null)) {
			allTestsFailedAction.accept(currentState.copy());
		}
		if (currentState.isSomeTestsSuccessful() && (someTestsSuccessfulAction != null)) {
			someTestsSuccessfulAction.accept(currentState.copy());
		}
		if (currentState.isSomeTestsFailed() && (someTestsFailedAction != null)) {
			someTestsFailedAction.accept(currentState.copy());
		}
	}

	public int getSuccessfulLoops() {
		return currentState.getIndex();
	}

	private void incrementIndex() {
		currentState.incrementIndex();
	}

	public Looper withAction(Consumer<LoopVariable> action) {
		this.action = action;
		return this;
	}

	public Looper withSuccessfulTestAction(Consumer<LoopVariable> action) {
		this.successfulTestAction = action;
		return this;
	}

	public Looper withFailedTestAction(Consumer<LoopVariable> action) {
		this.failedTestAction = action;
		return this;
	}

	public Looper withStopOnSuccess(boolean stopOnSuccess) {
		this.stopOnSuccess = stopOnSuccess;
		return this;
	}

	public Looper withStopOnFailure(boolean stopOnFailure) {
		this.stopOnFailure = stopOnFailure;
		return this;
	}

	public Looper withTest(Function<LoopVariable, Boolean> test) {
		this.test = test;
		return this;
	}

	public Looper withAllTestsSuccessfulAction(Consumer<LoopVariable> action) {
		this.allTestsSuccessfulAction = action;
		return this;
	}

	public Looper withSomeTestsSuccessfulAction(Consumer<LoopVariable> action) {
		this.someTestsSuccessfulAction = action;
		return this;
	}

	public Looper withAllTestsFailedAction(Consumer<LoopVariable> action) {
		this.allTestsFailedAction = action;
		return this;
	}

	public Looper withSomeTestsFailedAction(Consumer<LoopVariable> action) {
		this.someTestsFailedAction = action;
		return this;
	}

	public Boolean isLimited() {
		return currentState.isLimited();
	}

	public Boolean isInfiniteLoopsPermitted() {
		return !currentState.isLimited();
	}

	public Instant getStartTime() {
		return currentState.getStartTime();
	}

	public Instant getEndTime() {
		return currentState.getEndTime();
	}

	public Looper withTimeout(Duration limit) {
		this.currentState.setTimeout(limit);
		return this;
	}

	public Looper withTimeout(Instant limit) {
		this.currentState.setTimeout(limit);
		return this;
	}

	public LoopVariable getLoopVariable() {
		return this.currentState;
	}
}
