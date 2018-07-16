package com.obsidiandynamics.warthog;

import java.util.function.*;

import com.obsidiandynamics.shell.*;

public final class Commander {
  private final Shell shell = new BourneShell().withPath("/usr/local/bin");
  
  private final ProcessExecutor executor = ProcessExecutor.getDefault();
  
  private final Sink sink = System.out::print;
  
  private String workingDirectory = ".";
  
  public Commander withWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
    return this;
  }
  
  public void gitPull() throws CommandExecuteException {
    run("git pull").verifyExitCode(0).printCommand(sink).printOutput(sink);
  }
  
  public boolean gitHasUncommitted() throws CommandExecuteException {
    final var gitOutput = run("git status").printCommand(sink).printOutput(sink).verifyExitCode(0);
    return 
        gitOutput.getOutput().contains("Changes to be committed") || 
        gitOutput.getOutput().contains("Untracked files") ||
        gitOutput.getOutput().contains("Changes not staged for commit");
  }
  
  public boolean gitIsAhead() throws CommandExecuteException {
    final var gitOutput = run("git status").printCommand(sink).printOutput(sink).verifyExitCode(0);
    return gitOutput.getOutput().contains("Your branch is ahead");
  }
  
  public static final class CommandExecuteException extends Exception {
    private static final long serialVersionUID = 1L;
    
    CommandExecuteException(String command, String output, int code) { 
      super(String.format("Command '%s' terminated with code %d, output: %s", command, code, output)); 
    }
  }
  
  private static final class ExecutionRun {
    private final String command;
    private final String output;
    private final int exitCode;
    
    ExecutionRun(String command, String output, int exitCode) {
      this.command = command;
      this.output = output;
      this.exitCode = exitCode;
    }

    public String getCommand() {
      return command;
    }
    
    public ExecutionRun printCommand(Consumer<String> sink) {
      sink.accept(command);
      return this;
    }

    public String getOutput() {
      return output;
    }
    
    public ExecutionRun printOutput(Consumer<String> sink) {
      sink.accept(output);
      return this;
    }

    public int getExitCode() {
      return exitCode;
    }
    
    public ExecutionRun verifyExitCode(int expectedExitCode) throws CommandExecuteException {
      if (exitCode != expectedExitCode) {
        throw new CommandExecuteException(command, output, exitCode);
      }
      return this;
    }
  }
  
  private ExecutionRun run(String command) {
    final var fullCommand = "cd " + workingDirectory + " && " + command;
    
    final var commandEcho = new StringBuilder();
    final var outputEcho = new StringBuilder();
    final var proc = Shell.builder()
        .withShell(shell)
        .withExecutor(executor)
        .execute(fullCommand);
    proc.echo(commandEcho::append);
    
    final var exitCode = proc        
        .pipeTo(outputEcho::append)
        .await();
    return new ExecutionRun(commandEcho.toString(), outputEcho.toString(), exitCode);
  }
}
