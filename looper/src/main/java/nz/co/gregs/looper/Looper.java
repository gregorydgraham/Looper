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

	private transient final State state = new State();

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
	public boolean isNeeded() {
		if (state.limitMaxAttempts) {
			return state.needed && attempts() + 1 <= state.maxAttemptsAllowed;
		} else {
			return state.needed;
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
	private void done() {
		state.needed = false;
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
	private void attempt() {
		state.increaseTries();
	}

	/**
	 * The number of attempts recorded using {@link #attempt() }.
	 *
	 * @return the number of attempts started.
	 */
	public int attempts() {
		return state.getTries();
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
		if (maxAttemptsAllowed > 0) {
			state.limitMaxAttempts = true;
			state.maxAttemptsAllowed = maxAttemptsAllowed;
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
	public Looper setInfiniteLoopPermitted() {
		state.limitMaxAttempts = false;
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

	public void loop(Supplier<Void> action) {
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
	public void loop(Supplier<Void> action, Supplier<Boolean> test) {
		Consumer<Integer> function = (index) -> action.get();
		Function<Integer, Boolean> testFunction = (index) -> test.get();
		loop(function, testFunction, doNothingOnCompletion(), doNothingOnCompletion());
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
		loop(action, returnFalse(), doNothingOnCompletion(), doNothingOnCompletion());
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
		Exception loopWithExceptionHandling = loopWithExceptionHandling((Integer index) -> {
			action.accept(index);
			return null;
		}, test, successfulCompletion, unsuccessfulCompletion);
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
	public Exception loopWithExceptionHandling(Function<Integer, Exception> action, Function<Integer, Boolean> test, Consumer<Integer> successfulCompletion, Consumer<Integer> unsuccessfulCompletion) {
		state.setAction(action);
		state.setTest(test);
		state.setSuccessfulCompletionAction(successfulCompletion);
		state.setUnsuccessfulCompletionAction(unsuccessfulCompletion);
		Boolean testSuccessful = false;
		state.startTimer();
		while (isNeeded()) {
			attempt();
			Exception exc = action.apply(getIndex());
			state.setException(exc);
			if (state.hasException()) {
				return state.getException();
			}
			testSuccessful = test.apply(getIndex());
			if (testSuccessful) {
				done();
			}
			increaseIndex();
		}
		if (testSuccessful) {
			successfulCompletion.accept(getIndex());
		} else {
			unsuccessfulCompletion.accept(getIndex());
		}
		state.stopTimer();
		return null;
	}

	public int getIndex() {
		return state.index();
	}

	private void increaseIndex() {
		state.increaseIndex();
	}

	public static class State {

		private int tries;
		private int index;
		private boolean needed;
		private int maxAttemptsAllowed;
		private boolean limitMaxAttempts;
		private Exception exception = null;
		private Instant startTime;
		private Instant endTime;
		private Function<Integer, Exception> action;
		private Function<Integer, Boolean> test;
		private Consumer<Integer> successfulCompletionAction;
		private Consumer<Integer> unsuccessfulCompletionAction;

		private State() {
			reset();
		}
		
		private void startTimer(){
			startTime = Instant.now();
		}

		private void stopTimer(){
			endTime = Instant.now();
		}

		public void increaseTries() {
			tries++;
		}

		public int getTries() {
			return tries;
		}

		public Instant getStartTime() {
			return startTime;
		}

		public Duration elapsedTime() {
			Duration duration = Duration.between(getStartTime(), Instant.now());
			return duration;
		}

		public int index() {
			return index;
		}

		public void increaseIndex() {
			index++;
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

		private void reset() {
			tries = 0;
			index = 0;
			needed = true;
			maxAttemptsAllowed = 1000;
			limitMaxAttempts = true;
			exception = null;
			startTime = null;
			endTime = null;
		}
	}
}
