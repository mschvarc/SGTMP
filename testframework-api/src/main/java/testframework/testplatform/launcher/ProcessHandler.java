/*
 * Copyright 2017 Martin Schvarcbacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testframework.testplatform.launcher;

import org.apache.log4j.Logger;
import testframework.testplatform.exceptions.ProcessRuntimeException;
import testframework.testplatform.exceptions.TestRunException;
import testframework.testplatform.dal.entities.ProcessReturnInformation;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static testframework.testplatform.PlatformConfiguration.PROCESS_QUEUE_SHUTDOWN_TIMEOUT;
import static testframework.testplatform.PlatformConfiguration.PROCESS_QUEUE_SHUTDOWN_TIMEOUT_UNIT;

/**
 * Wrapper around a Process to launch with given arguments
 * Thread safe
 */
public final class ProcessHandler {

    private static final Logger logger = Logger.getLogger(ProcessHandler.class);
    private static final boolean ATTEMPT_STREAM_CLOSING = false;
    private final ProcessBuilder processBuilder;
    private final ProcessReturnInformation information = new ProcessReturnInformation();
    private final ExecutorService blockingExecutor = Executors.newFixedThreadPool(2);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private String standardOutput;
    private String errorOutput;
    private Process process;
    private Integer exitCode;
    private ExecutorService nonBlockingExecutor;


    /**
     * Initializes this process wrapper with arguments for a process to start
     *
     * @param processArgs         first argument is given process, others are process arguments
     * @param redirectErrorStream redirect stderr to stdout
     */
    public ProcessHandler(String[] processArgs, boolean redirectErrorStream) {
        this.processBuilder = new ProcessBuilder(processArgs);
        this.processBuilder.redirectErrorStream(redirectErrorStream);
        information.setProcessArgs(Arrays.asList(processArgs));
    }


    /**
     * Starts the process in blocking mode, will return only after it is finished
     *
     * @return process information
     * @throws IOException if process fails to start
     */
    public synchronized ProcessReturnInformation startBlocking() throws IOException, TestRunException {
        if (process != null || finished.get()) {
            throw new IllegalStateException("Process finished");
        }
        if (started.get()) {
            throw new IllegalStateException("Process already started");
        }
        process = processBuilder.start();

        Callable<String> standardOutputCallable = new CallableProcessStreamReader<>(process, process.getInputStream());
        Callable<String> errorOutputCallable = new CallableProcessStreamReader<>(process, process.getErrorStream());

        Future<String> stdoutFuture = blockingExecutor.submit(standardOutputCallable);
        Future<String> erroutFuture = blockingExecutor.submit(errorOutputCallable);
        started.set(true);

        try {
            this.standardOutput = stdoutFuture.get(); //blocking
            this.errorOutput = erroutFuture.get(); //blocking

            assert stdoutFuture.isDone();
            assert erroutFuture.isDone();

            blockingExecutor.shutdown(); //non-forceful exit
            blockingExecutor.awaitTermination(PROCESS_QUEUE_SHUTDOWN_TIMEOUT, PROCESS_QUEUE_SHUTDOWN_TIMEOUT_UNIT); //await exit

            assert blockingExecutor.isTerminated();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            throw new TestRunException("Process failed to execute", ex);
        }

        this.exitCode = process.exitValue();
        this.finished.set(!process.isAlive());
        this.information.setErrout(errorOutput);
        this.information.setStdout(standardOutput);
        this.information.setExitCode(exitCode);
        return information;
    }

    /**
     * Starts the process and immediately returns Future containing the process state
     *
     * @return process information
     */
    public synchronized Future<ProcessReturnInformation> startNonBlocking() {
        if (process != null || finished.get()) {
            throw new IllegalStateException("Process finished");
        }
        if (started.get()) {
            throw new IllegalStateException("Process already started");
        }
        if (nonBlockingExecutor != null) {
            throw new IllegalStateException("Blocking executor in already use");
        }

        nonBlockingExecutor = Executors.newFixedThreadPool(1);
        Callable<ProcessReturnInformation> task = this::startBlocking;
        assert !this.started.get() : "must remain as not started till started by ThreadExecutorPool";
        return nonBlockingExecutor.submit(task);
    }

    public boolean isFinished() {
        return finished.get();
    }

    /**
     * You must always call awaitNonBlockingExit to dispose of inner task executors after process is finished
     */
    public synchronized void awaitNonBlockingExit() {
        if (nonBlockingExecutor == null) {
            throw new IllegalStateException("Process was launched in blocking mode");
        }
        try {
            nonBlockingExecutor.shutdown(); //non-forceful exit
            nonBlockingExecutor.awaitTermination(PROCESS_QUEUE_SHUTDOWN_TIMEOUT, PROCESS_QUEUE_SHUTDOWN_TIMEOUT_UNIT); //await exit
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (!nonBlockingExecutor.isTerminated()) {
            throw new ProcessRuntimeException("Failed to shutdown non-blocking executor");
        }
    }

    public void terminateForcefully() {
        if (ATTEMPT_STREAM_CLOSING) {
            try {
                if (process.getErrorStream() != null) {
                    process.getErrorStream().close();
                }
                if (process.getOutputStream() != null) {
                    process.getOutputStream().close();
                }
                if (process.getInputStream() != null) {
                    process.getInputStream().close();
                }
            } catch (IOException ex) {
                //expected
                logger.warn(ex);
            }
        }

        Process destroyed = this.process.destroyForcibly();
        try {
            destroyed.waitFor(PROCESS_QUEUE_SHUTDOWN_TIMEOUT, PROCESS_QUEUE_SHUTDOWN_TIMEOUT_UNIT);
            if (!blockingExecutor.isShutdown()) {
                blockingExecutor.shutdownNow();
            }

            if (destroyed.isAlive()) {
                throw new ProcessRuntimeException("Could not kill process: " + processBuilder.toString());
            }
            exitCode = destroyed.exitValue();
        } catch (InterruptedException e) {
            //ignore any exceptions
            Thread.currentThread().interrupt();
            logger.error(e);
        }
    }

    public synchronized String processStandardOutput() {
        return standardOutput;
    }

    public synchronized String processErrorOutput() {
        return errorOutput;
    }

    public synchronized Integer getExitCode() {
        return exitCode;
    }
}
