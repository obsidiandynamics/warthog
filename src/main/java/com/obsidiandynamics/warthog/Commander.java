package com.obsidiandynamics.warthog;

import java.util.function.*;

import com.obsidiandynamics.shell.*;

public final class Commander {
  private final Shell shell = new BourneShell().withPath("/usr/local/bin");
  
  private final ProcessExecutor executor = ProcessExecutor.getDefault();
  
  private Sink sink = System.out::print;
  
  private String workingDirectory = ".";
  
  public Commander withSink(Sink sink) {
    this.sink = sink;
    return this;
  }
  
  public Commander withWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
    return this;
  }
  
  public void gitPull() throws CommandExecuteException {
    run("git pull").verifyExitCode(0).printCommand(sink).printOutput(sink);
  }
  
  public boolean gitHasUntracked() throws CommandExecuteException {
    final var gitOutput = run("git status").printCommand(sink).printOutput(sink).verifyExitCode(0);
    return gitOutput.getOutput().contains("Untracked files");
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
  
  public void gitCommitAll(String message) throws CommandExecuteException {
    run("git commit -am \"" + message + "\"").verifyExitCode(0).printCommand(sink).printOutput(sink);
  }
  
  public void gitPush() throws CommandExecuteException {
    run("git push").verifyExitCode(0).printCommand(sink).printOutput(sink);
  }
  
  public void gitTag(String name, String message) throws CommandExecuteException {
    run("git tag -a " + name + " -m \"" + message + "\"").verifyExitCode(0).printCommand(sink).printOutput(sink);
  }
  
  public void gitPushTag(String name) throws CommandExecuteException {
    run("git push origin " + name).verifyExitCode(0).printCommand(sink).printOutput(sink);
  }
  
  public void runCommand(String buildCommand) throws CommandExecuteException {
    run(buildCommand).printCommand(sink).printOutput(sink).verifyExitCode(0);
  }
  
  public static final class CommandExecuteException extends Exception {
    private static final long serialVersionUID = 1L;
    
    CommandExecuteException(String command, String output, int code) { 
      super(String.format("Command '%s' terminated with code %d, output: %s", command, code, output)); 
    }
  }
  
  private static final class Execution {
    private final String command;
    private final String output;
    private final int exitCode;
    
    Execution(String command, String output, int exitCode) {
      this.command = command;
      this.output = output;
      this.exitCode = exitCode;
    }

    public String getCommand() {
      return command;
    }
    
    public Execution printCommand(Consumer<String> sink) {
      sink.accept(command);
      return this;
    }

    public String getOutput() {
      return output;
    }
    
    public Execution printOutput(Consumer<String> sink) {
      sink.accept(output);
      return this;
    }

    public int getExitCode() {
      return exitCode;
    }
    
    public Execution verifyExitCode(int expectedExitCode) throws CommandExecuteException {
      if (getExitCode() != expectedExitCode) {
        throw new CommandExecuteException(command, output, exitCode);
      }
      return this;
    }
  }
  
  private Execution run(String command) {
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
    return new Execution(commandEcho.toString(), outputEcho.toString(), exitCode);
  }
}
