package com.epam.asset.tracking.web.balance;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import com.epam.asset.tracking.web.AbstractWebTest;


public class GetBalanceTest extends AbstractWebTest {

	@Test
	public void shouldReturnDefaultMessage() throws Exception {
		mockMvc.perform(get("/blockchain/account/a/balance")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("{\"balance\":\"375\",\"name\":\"a\"}")));
	}

}
