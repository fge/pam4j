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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log LOG = LogFactory.getLog(AbstractPamTest.class.getName());
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
        final long startingSize = measureMemoryUse();

        //Create a list of threads
        final List executables = new ArrayList();

        //Add threads for user1 authentication
        for (int i = 0; i < 15; i++) {
            final Executable executable = new Executable() {
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
                public void execute() throws Exception {
                    LOG.info("Running 2");
                    PamReturnValue value = pam.authenticate(user2Name, user2Credentials);
                    checkReturnValue(expectedReturnValues, value);
                }
            };
            executables.add(executable1);
        }

        List errors = runThreads(executables);

        long finishingSize = measureMemoryUse();
        long difference = finishingSize - startingSize;
        LOG.info("Memory Change is: " + difference);

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
        if (!match) {
            throw new PamException("Test failure. Return expectedReturnValues was: " + pamReturnValue);
        }
    }

    /**
     * Measures the memory use. Because Garbage Collection is done, memory should
     * not increase.
     *
     * @return the memory increase/- decrease in bytes
     * @throws InterruptedException
     */
    protected long measureMemoryUse() throws InterruptedException {
        System.gc();
        Thread.sleep(3000);
        System.gc();
        long startingSize = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return startingSize;
    }


    /**
     * Runs a set of threads, for a fixed amount of time.
     */
    protected List runThreads(final List executables) throws InterruptedException {
        final Counter counter = new Counter();
        final long endTime = System.currentTimeMillis() + 10000;
        final List errors = new ArrayList();

        // Spin up the threads
        final Thread[] threads = new Thread[executables.size()];
        for (int i = 0; i < threads.length; i++) {
            final Executable executable = (Executable) executables.get(i);
            threads[i] = new Thread() {
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
