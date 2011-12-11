/**
 *  Copyright 2003-2007 Greg Luck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.sf.jpam;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs tests on the Pam class.
 * <p/>
 * Create the following users before running tests:
 * <ol>
 * <li>user test with password test01
 * <li>user test2 with password test02
 * </ol>
 * Linux systems may need /etc/shadow to be made readable by the user
 *
 * @author <a href="mailto:gregluck@users.sourceforge.net">Greg Luck</a>
 * @version $Id$
 */
public class AbstractPamTest extends TestCase {
    private static final Logger LOG
        = LoggerFactory.getLogger(AbstractPamTest.class);
    /**
     * user 1 name
     */
    protected String user1Name = "test";
    /**
     * user 1 credentials
     */
    protected String user1Credentials = "test01";
    /**
     * user 1 bad credentials
     */
    protected String user1BadCredentials = "test01bad";
    /**
     * user 2 name
     */
    protected String user2Name = "test2";
    /**
     * user 2 credentials
     */
    protected String user2Credentials = "test02";
    /**
     * user 2 bad credentials
     */
    protected String user2BadCredentials = "test02bad";


    /**
     * An empty test to keep IntelliJ Test Runner happy
     */
    public void testPlaceHolder() {
        //
    }


    /**
     * Multi-threaded PAM test.
     */
    public void concurrentPamStressTest(final Pam pam, final PamReturnValue[] expectedReturnValues) throws InterruptedException {

        //Create a list of threads
        final List<Executable> executables = new ArrayList<Executable>();

        //Add threads for user1 authentication
        for (int i = 0; i < 15; i++) {
            final Executable executable = new Executable() {
                @Override
                public void execute() throws Exception {
                    LOG.info("Running 1");
                    PamReturnValue value = pam.authenticate(user1Name, user1Credentials);
                    checkReturnValue(expectedReturnValues, value);
                }
            };
            executables.add(executable);
        }

        //Add threads for user2 authentication
        for (int i = 0; i < 15; i++) {
            final Executable executable1 = new Executable() {
                @Override
                public void execute() throws Exception {
                    LOG.info("Running 2");
                    PamReturnValue value = pam.authenticate(user2Name, user2Credentials);
                    checkReturnValue(expectedReturnValues, value);
                }
            };
            executables.add(executable1);
        }

        List errors = runThreads(executables);

        // Throw any error that happened
        if (errors.size() != 0) {
            LOG.error("" + errors.size() + " failures in run.");
            for (int i = 0; i < errors.size(); i++) {
                Throwable throwable = (Throwable) errors.get(i);
                LOG.error("Error " + i + ": " + throwable);
            }
            assertEquals("Errors occurred during run", 0, errors.size());
        }
    }

    private void checkReturnValue(PamReturnValue[] expectedReturnValues, PamReturnValue pamReturnValue)
            throws PamException {
        boolean match = false;
        for (int i = 0; i < expectedReturnValues.length; i++) {
            PamReturnValue returnValue = expectedReturnValues[i];
            if (expectedReturnValues[i].equals(pamReturnValue)) {
                match = true;
            }
        }
        StringBuffer expected = new StringBuffer();
        for (int i = 0; i < expectedReturnValues.length; i++) {
            PamReturnValue expectedReturnValue = expectedReturnValues[i];
            expected.append("\"").append(expectedReturnValue).append("\"").append(' ');
        }

        if (!match) {
            throw new PamException("Test failure. Return expected one of " + expected
                    + " Actual return value was: " + pamReturnValue);
        }
    }

    /**
     * Runs a set of threads, for a fixed amount of time.
     */
    protected List runThreads(final List<Executable> executables) throws
        InterruptedException {
        final Counter counter = new Counter();
        final long endTime = System.currentTimeMillis() + 10000;
        final List<Throwable> errors = new ArrayList<Throwable>();

        // Spin up the threads
        final Thread[] threads = new Thread[executables.size()];
        for (int i = 0; i < threads.length; i++) {
            final Executable executable = executables.get(i);
            threads[i] = new Thread() {
                @Override
                public void run() {
                    try {
                        // Run the thread until the given end time
                        while (System.currentTimeMillis() < endTime) {
                            executable.execute();
                            counter.increment();
                        }
                    } catch (Throwable t) {
                        // Hang on to any errors
                        errors.add(t);
                    }
                }
            };
            threads[i].start();
        }

        // Wait for the threads to finish
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        LOG.info("Number of executes run: " + counter.counter);
        return errors;
    }

    /**
     * A runnable, that can throw an exception.
     */
    protected interface Executable {

        /**
         * Executes this object
         */
        void execute() throws Exception;
    }

    /**
     * Counts the tests
     */
    protected class Counter {
        private int counter;

        private synchronized void increment() {
            counter++;
        }
    }
}
