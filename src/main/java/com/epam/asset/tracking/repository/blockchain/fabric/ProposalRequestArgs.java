package com.epam.asset.tracking.repository.blockchain.fabric;

import static java.util.Objects.requireNonNull;

import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.LinkedList;

public class ProposalRequestArgs {

  private final String[] args;

  private ProposalRequestArgs(String[] args) {
    this.args = args;
  }

  public String[] getArgs() {
    return args;
  }

  public static class Builder {
    private String chaincodeMethod;
    private LinkedList<String> args;

    public Builder() {
      args = new LinkedList<>();
    }

    public Builder args(String[] args) {
      if (!isEmpty(args)) {
        for (String arg : args) {
          if (isNotBlank(arg)) this.args.add(arg);
        }
      }
      return this;
    }

    public Builder args(String arg1, String... otherArgs) {
      if (isNotBlank(arg1)) args.add(arg1);
      return args(otherArgs);
    }

    public Builder chaincodeMethod(String chaincodeMethod) {
      this.chaincodeMethod = requireNonNull(chaincodeMethod);
      return this;
    }

    public ProposalRequestArgs build() {
      return new ProposalRequestArgs(buildArgs());
    }

    private String[] buildArgs() {
      if (isNotBlank(chaincodeMethod)) args.addFirst(chaincodeMethod);
      return args.toArray(new String[args.size()]);
    }
  }
}
