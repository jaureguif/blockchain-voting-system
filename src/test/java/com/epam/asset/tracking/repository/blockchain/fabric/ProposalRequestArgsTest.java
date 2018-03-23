package com.epam.asset.tracking.repository.blockchain.fabric;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ProposalRequestArgsTest {

  @Test
  @Parameters(method = "buildArgs")
  public void shouldBuildRequestArgsWithValues(String[] args, String chaincodeMethod, String[] expectedArgs) {
    String[] actualArgs = new ProposalRequestArgs.Builder()
        .args(args)
        .chaincodeMethod(chaincodeMethod)
        .build()
        .getArgs();

    assertThat(actualArgs).isNotNull().hasSameSizeAs(expectedArgs).containsExactly(expectedArgs);
  }

  @Test
  @Parameters(method = "buildArgs2")
  public void shouldBuildRequestArgsWithValues2(String arg0, String[] otherArgs, String[] expectedArgs) {
    String[] actualArgs = new ProposalRequestArgs.Builder()
        .args(arg0, otherArgs)
        .build()
        .getArgs();

    assertThat(actualArgs).isNotNull().hasSameSizeAs(expectedArgs).containsExactly(expectedArgs);
  }

  public Object[] buildArgs() {
    return new Object[][]{
        {new String[]{"arg0"}, "method0", new String[]{"method0", "arg0"}},
        {new String[]{"arg0"}, null, new String[]{"arg0"}},
        {null, "method0", new String[]{"method0"}},
        {null, null, new String[0]},
        {new String[]{null, "", " ", "\t", "\n"}, "method0", new String[]{"method0"}},
        {new String[]{null, "", " ", "\t", "\n"}, null, new String[0]},
        {new String[]{null, "", " ", "\t", "\n"}, "", new String[0]},
        {new String[]{null, "", " ", "\t", "\n"}, " ", new String[0]},
        {new String[]{null, "", " ", "\t", "\n"}, "\t", new String[0]},
        {new String[]{null, "", " ", "\t", "\n"}, "\n", new String[0]},
        {new String[]{"arg0"}, null, new String[]{"arg0"}},
        {new String[]{"arg0"}, "", new String[]{"arg0"}},
        {new String[]{"arg0"}, " ", new String[]{"arg0"}},
        {new String[]{"arg0"}, "\t", new String[]{"arg0"}},
        {new String[]{"arg0"}, "\n", new String[]{"arg0"}}
    };
  }

  public Object[] buildArgs2() {
    return new Object[][] {
        {null, new String[]{"arg1"}, new String[]{"arg1"}},
        {"", new String[]{"arg1"}, new String[]{"arg1"}},
        {" ", new String[]{"arg1"}, new String[]{"arg1"}},
        {"\t", new String[]{"arg1"}, new String[]{"arg1"}},
        {"\n", new String[]{"arg1"}, new String[]{"arg1"}},
        {null, null, new String[0]},
        {"", null, new String[0]},
        {" ", null, new String[0]},
        {"\t", null, new String[0]},
        {"\n", null, new String[0]},
        {"arg0", new String[]{"arg1"}, new String[]{"arg0", "arg1"}},
        {"arg0", null, new String[]{"arg0"}},
    };
  }
}
