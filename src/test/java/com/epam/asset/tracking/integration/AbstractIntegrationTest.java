package com.epam.asset.tracking.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

	@Autowired
	protected MockMvc mockMvc;

	protected ObjectMapper jacksonMapper = new ObjectMapper();
	
	//this was added as fapdoodle is generating temp files for startin a mongo in mem then sometimes for some reason after multiple
	//running is not deleting those files causing problems
	@Before
	public void before() throws IOException {
		String tempFile = System.getenv("temp") + File.separator + "extract-" + System.getenv("USERNAME") + "-extractmongod";
		String executable;
		if (System.getenv("OS") != null && System.getenv("OS").contains("Windows")) {
			executable = tempFile + ".exe";
			Files.deleteIfExists(new File(executable).toPath());
	        Files.deleteIfExists(new File(tempFile + ".pid").toPath());
		} 
	}
	
}
