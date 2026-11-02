package net.chrisrichardson.ftgo;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

  @RestController
  public static class TestController {
    @RequestMapping(path = "/things/{id}", method = RequestMethod.GET)
    public String get(@PathVariable("id") long id) {
      return "ok";
    }

    @RequestMapping(path = "/things", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String create(@RequestBody Map<String, Object> body) {
      return "ok";
    }

    @RequestMapping(path = "/boom", method = RequestMethod.GET)
    public String boom() {
      throw new IllegalStateException("internal detail");
    }
  }

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  public void typeMismatchedPathVariableIsBadRequest() throws Exception {
    mockMvc.perform(get("/things/abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("The request could not be processed"));
  }

  @Test
  public void malformedJsonIsBadRequest() throws Exception {
    mockMvc.perform(post("/things").contentType(MediaType.APPLICATION_JSON).content("{bad"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  public void unsupportedMediaTypeIs415() throws Exception {
    mockMvc.perform(post("/things").contentType(MediaType.TEXT_PLAIN).content("x"))
            .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  public void unsupportedMethodIs405WithAllowHeader() throws Exception {
    mockMvc.perform(delete("/things/1"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(header().string("Allow", "GET"));
  }

  @Test
  public void unexpectedExceptionIsStill500WithoutDetails() throws Exception {
    mockMvc.perform(get("/boom"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }
}
