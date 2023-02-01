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
import java.util.function.Supplier;
import java.util.function.Consumer;

/**
 * Implements a while/for loop combination.
 *
 * @author gregorygraham
 */
public class Looper implements Serializable {

	private static final long serialVersionUID = 1L;

	private final LoopVariable state = new LoopVariable();
	
	private Exception exception = null;
	private Function<Integer, Exception> action;
	private Function<Integer, Boolean> test;
	private Consumer<Integer> successfulCompletionAction;
	private Consumer<Integer> unsuccessfulCompletionAction;

	private Looper() {
	}

	public static Looper withMaxAttempts(int size) {
		Looper newLoop = Looper.factory();
		newLoop.setMaxAttemptsAllowed(size);
		return newLoop;
	}

	public static Looper withInfiniteLoopsPermitted() {
		Looper newLoop = Looper.factory();
		newLoop.setInfiniteLoopPermitted();
		return newLoop;
	}

	public static Looper factory() {
		return new Looper();
	}

	public static Looper factory(int max) {
		Looper loopVariable = new Looper();
		loopVariable.setMaxAttemptsAllowed(max);
		return loopVariable;
	}

	public void reset() {
		state.reset();
	}

	/**
	 * Checks the whether the loop is still needed (that is {@link #done()} has
	 * not been called) and if the loop has exceeded the maximum attempts (if a
	 * max is defined).
	 *
	 * @return true if the loop is still needed.
	 */
	private boolean isNeeded() {
		return state.isNeeded();
	}

	/**
	 * The number of attempts recorded using {@link #attempt() }.
	 *
	 * @return the number of attempts started.
	 */
	public int attempts() {
		return state.attempts();
	}

	public Duration elapsedTime() {
		return state.elapsedTime();
	}

	/**
	 * Sets the maximum attempts allowed for this loop variable.
	 *
	 * <p>
	 * Maximum attempts will stop a correctly used Looper after the maximum
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
	public Looper setMaxAttemptsAllowed(int maxAttemptsAllowed) {
		state.setMaxAttemptsAllowed(maxAttemptsAllowed);
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
	public Looper setInfiniteLoopPermitted() {
		state.setInfiniteLoopPermitted();
		return this;
	}

	private static Consumer<Integer> doNothingOnCompletion() {
		return (index) -> {
		};
	}

	private static Function<Integer, Boolean> returnFalse() {
		return (d) -> {
			return false;
		};
	}

	public void loop(Supplier<Exception> action) {
		Consumer<Integer> function = (index) -> action.get();
		loop(function, returnFalse());
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
	 *				},
	 *				() -&gt; {
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
	public void loop(Supplier<Exception> action, Supplier<Boolean> test) {
		Consumer<Integer> function = (index) -> action.get();
		Function<Integer, Boolean> testFunction = (index) -> test.get();
		loop(function, testFunction);
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
	public void loop(Consumer<Integer> action) {
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
	 * @return
	 */
	public Exception loopWithExceptionHandling(Function<Integer, Exception> action) {
		return loopWithExceptionHandling(action, returnFalse());
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
	public void loop(Consumer<Integer> action, Function<Integer, Boolean> test) {
		loop(action, test, doNothingOnCompletion(), doNothingOnCompletion());
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
	 * @return
	 */
	public Exception loopWithExceptionHandling(Function<Integer, Exception> action, Function<Integer, Boolean> test) {
		return loopWithExceptionHandling(action, test, doNothingOnCompletion(), doNothingOnCompletion());
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
	public void loop(Consumer<Integer> action, Function<Integer, Boolean> test, Consumer<Integer> completion) {
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
	public void loop(Consumer<Integer> action, Function<Integer, Boolean> test, Consumer<Integer> successfulCompletion, Consumer<Integer> unsuccessfulCompletion) {
		loopWithExceptionHandling(
						(Integer index) -> {
							action.accept(index);
							return null;
						},
						test,
						successfulCompletion,
						unsuccessfulCompletion
				);
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
	 * @return 
	 */
	public Exception loopWithExceptionHandling(Function<Integer, Exception> action, Function<Integer, Boolean> test, Consumer<Integer> successfulCompletion, Consumer<Integer> unsuccessfulCompletion) {
		setAction(action);
		setTest(test);
		setSuccessfulCompletionAction(successfulCompletion);
		setUnsuccessfulCompletionAction(unsuccessfulCompletion);
		Boolean testSuccessful = false;
		state.startTimer();
		while (isNeeded()) {
			state.attempt();
			Exception exc = getAction().apply(getSuccessfulLoops());
			setException(exc);
			if (hasException()) {
				state.stopTimer();
				return getException();
			}
			testSuccessful = getTest().apply(getSuccessfulLoops());
			if (testSuccessful) {
				state.done();
			}
			increaseIndex();
		}
		if (testSuccessful) {
			getSuccessfulCompletionAction().accept(getSuccessfulLoops());
		} else {
			getUnsuccessfulCompletionAction().accept(getSuccessfulLoops());
		}
		state.stopTimer();
		return null;
	}

	public int getSuccessfulLoops() {
		return state.getSuccessfulLoops();
	}

	private void increaseIndex() {
		state.incrementSuccessLoops();
	}

	public boolean hasException() {
		return exception != null;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exc) {
		exception = exc;
	}

	private void setAction(Function<Integer, Exception> action) {
		this.action = action;
	}

	private void setTest(Function<Integer, Boolean> test) {
		this.test = test;
	}

	private void setSuccessfulCompletionAction(Consumer<Integer> successfulCompletion) {
		this.successfulCompletionAction = successfulCompletion;
	}

	private void setUnsuccessfulCompletionAction(Consumer<Integer> unsuccessfulCompletion) {
		this.unsuccessfulCompletionAction = unsuccessfulCompletion;
	}

	public Function<Integer, Exception> getAction() {
		return action;
	}

	public Function<Integer, Boolean> getTest() {
		return test;
	}

	public Consumer<Integer> getSuccessfulCompletionAction() {
		return successfulCompletionAction;
	}

	private Consumer<Integer> getUnsuccessfulCompletionAction() {
		return unsuccessfulCompletionAction;
	}

	public Boolean isLimited() {
		return state.isLimited();
	}

	public Boolean isInfiniteLoopsPermitted() {
		return !state.isLimited();
	}
	
	public Instant getStartTime(){
		return state.getStartTime();
	}
	
	public Instant getEndTime(){
		return state.getEndTime();
	}
}
