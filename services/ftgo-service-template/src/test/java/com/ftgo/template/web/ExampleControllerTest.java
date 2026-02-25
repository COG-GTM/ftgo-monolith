package com.ftgo.template.web;

// =============================================================================
// TEMPLATE: Unit Test for REST Controller
// =============================================================================
// Copy this file when creating a new microservice.
// Replace "template" with your service name and "Example" with your resource.
//
// This template demonstrates:
//   - MockMvc for HTTP testing without a running server
//   - @WebMvcTest for slice testing (loads only web layer)
//   - @MockBean for mocking service dependencies
//   - Rest-Assured Spring MockMvc integration
//   - JSON response assertions with jsonPath
//   - Testing various HTTP methods (GET, POST, PUT, DELETE)
//   - Testing error responses and validation
//   - JWT/Security testing patterns
//
// Two approaches are shown:
//   1. Spring MockMvc (standard Spring approach)
//   2. Rest-Assured MockMvc (BDD-style, compatible with existing FTGO patterns)
// =============================================================================

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller unit tests using Spring MockMvc.
 *
 * <p>Uses {@code @WebMvcTest} to load only the web layer, making tests fast.
 * Service dependencies are mocked with {@code @MockBean}.
 *
 * <p><b>Note:</b> For security-enabled endpoints, add
 * {@code @WithMockUser(roles = "USER")} or provide a JWT token header.
 * If security is not relevant to the test, you can disable it with
 * {@code @WebMvcTest(properties = "ftgo.security.jwt.enabled=false")}.
 *
 * <p><b>Location:</b> {@code src/test/java/com/ftgo/{service}/web/}
 */
// TODO: Uncomment and replace with your controller class
// @WebMvcTest(controllers = ExampleController.class,
//     properties = "ftgo.security.jwt.enabled=false")
@DisplayName("ExampleController")
class ExampleControllerTest {

    // @Autowired
    // private MockMvc mockMvc;

    // @Autowired
    // private ObjectMapper objectMapper;

    // TODO: Mock your service dependencies
    // @MockBean
    // private ExampleService exampleService;

    // -------------------------------------------------------------------------
    // GET endpoints
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/examples/{id}")
    class GetById {

        @Test
        @DisplayName("should return 200 with entity when found")
        void shouldReturnEntityWhenFound() throws Exception {
            // Arrange
            // var entity = ExampleEntityTestBuilder.anEntity()
            //     .withId(1L)
            //     .withName("Test Entity")
            //     .build();
            // when(exampleService.findById(1L)).thenReturn(entity);

            // Act & Assert
            // mockMvc.perform(get("/api/v1/examples/1")
            //         .accept(MediaType.APPLICATION_JSON))
            //     .andExpect(status().isOk())
            //     .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            //     .andExpect(jsonPath("$.status").value("success"))
            //     .andExpect(jsonPath("$.data.id").value(1))
            //     .andExpect(jsonPath("$.data.name").value("Test Entity"));
        }

        @Test
        @DisplayName("should return 404 when entity not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Arrange
            // when(exampleService.findById(999L))
            //     .thenThrow(new EntityNotFoundException("Example", 999L));

            // Act & Assert
            // mockMvc.perform(get("/api/v1/examples/999")
            //         .accept(MediaType.APPLICATION_JSON))
            //     .andExpect(status().isNotFound())
            //     .andExpect(jsonPath("$.status").value("error"))
            //     .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/examples")
    class GetAll {

        @Test
        @DisplayName("should return paginated list of entities")
        void shouldReturnPaginatedList() throws Exception {
            // Arrange
            // var entities = List.of(
            //     ExampleEntityTestBuilder.anEntity().withId(1L).withName("A").build(),
            //     ExampleEntityTestBuilder.anEntity().withId(2L).withName("B").build()
            // );
            // var page = new PageImpl<>(entities, PageRequest.of(0, 20), 2);
            // when(exampleService.findAll(any(Pageable.class))).thenReturn(page);

            // Act & Assert
            // mockMvc.perform(get("/api/v1/examples")
            //         .param("page", "0")
            //         .param("size", "20")
            //         .accept(MediaType.APPLICATION_JSON))
            //     .andExpect(status().isOk())
            //     .andExpect(jsonPath("$.data", hasSize(2)))
            //     .andExpect(jsonPath("$.data[0].name").value("A"))
            //     .andExpect(jsonPath("$.data[1].name").value("B"));
        }
    }

    // -------------------------------------------------------------------------
    // POST endpoints
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/examples")
    class Create {

        @Test
        @DisplayName("should return 201 when entity created successfully")
        void shouldCreateEntity() throws Exception {
            // Arrange
            // var request = new CreateExampleRequest("New Entity", "Description");
            // var created = ExampleEntityTestBuilder.anEntity()
            //     .withId(1L)
            //     .withName("New Entity")
            //     .build();
            // when(exampleService.create(any(CreateExampleRequest.class))).thenReturn(created);

            // Act & Assert
            // mockMvc.perform(post("/api/v1/examples")
            //         .contentType(MediaType.APPLICATION_JSON)
            //         .content(objectMapper.writeValueAsString(request)))
            //     .andExpect(status().isCreated())
            //     .andExpect(jsonPath("$.status").value("success"))
            //     .andExpect(jsonPath("$.data.id").value(1))
            //     .andExpect(jsonPath("$.data.name").value("New Entity"));
        }

        @Test
        @DisplayName("should return 400 when request body is invalid")
        void shouldReturn400WhenInvalid() throws Exception {
            // Arrange: send request with missing required field
            // var invalidRequest = new CreateExampleRequest("", null);

            // Act & Assert
            // mockMvc.perform(post("/api/v1/examples")
            //         .contentType(MediaType.APPLICATION_JSON)
            //         .content(objectMapper.writeValueAsString(invalidRequest)))
            //     .andExpect(status().isBadRequest())
            //     .andExpect(jsonPath("$.status").value("error"))
            //     .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
        }
    }

    // -------------------------------------------------------------------------
    // PUT endpoints
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/v1/examples/{id}")
    class Update {

        @Test
        @DisplayName("should return 200 when entity updated")
        void shouldUpdateEntity() throws Exception {
            // Arrange
            // var request = new UpdateExampleRequest("Updated Name", "Updated Desc");
            // var updated = ExampleEntityTestBuilder.anEntity()
            //     .withId(1L)
            //     .withName("Updated Name")
            //     .build();
            // when(exampleService.update(eq(1L), any())).thenReturn(updated);

            // Act & Assert
            // mockMvc.perform(put("/api/v1/examples/1")
            //         .contentType(MediaType.APPLICATION_JSON)
            //         .content(objectMapper.writeValueAsString(request)))
            //     .andExpect(status().isOk())
            //     .andExpect(jsonPath("$.data.name").value("Updated Name"));
        }
    }

    // -------------------------------------------------------------------------
    // DELETE endpoints
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/v1/examples/{id}")
    class Delete {

        @Test
        @DisplayName("should return 204 when entity deleted")
        void shouldDeleteEntity() throws Exception {
            // Arrange
            // doNothing().when(exampleService).delete(1L);

            // Act & Assert
            // mockMvc.perform(delete("/api/v1/examples/1"))
            //     .andExpect(status().isNoContent());
            //
            // verify(exampleService).delete(1L);
        }
    }

    // -------------------------------------------------------------------------
    // REST-Assured MockMvc style (alternative approach)
    // -------------------------------------------------------------------------
    // The legacy monolith uses Rest-Assured for controller tests.
    // This style is also valid for new microservices:
    //
    //   import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
    //
    //   @Test
    //   void shouldFindOrderUsingRestAssured() {
    //       when(exampleService.findById(1L)).thenReturn(entity);
    //
    //       given()
    //           .mockMvc(mockMvc)
    //       .when()
    //           .get("/api/v1/examples/1")
    //       .then()
    //           .statusCode(200)
    //           .body("data.name", equalTo("Test Entity"));
    //   }
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Security-aware test examples
    // -------------------------------------------------------------------------
    // When testing with security enabled, use @WithMockUser or provide JWT:
    //
    //   @Test
    //   @WithMockUser(username = "admin", roles = {"ADMIN"})
    //   void shouldAllowAdminAccess() throws Exception {
    //       mockMvc.perform(get("/api/v1/admin/examples"))
    //           .andExpect(status().isOk());
    //   }
    //
    //   @Test
    //   void shouldRejectUnauthenticatedAccess() throws Exception {
    //       mockMvc.perform(get("/api/v1/examples"))
    //           .andExpect(status().isUnauthorized());
    //   }
    //
    //   @Test
    //   void shouldAuthenticateWithJwtToken() throws Exception {
    //       String token = jwtTokenProvider.generateAccessToken(
    //           1L, "user", List.of("ROLE_USER"), List.of());
    //
    //       mockMvc.perform(get("/api/v1/examples")
    //               .header("Authorization", "Bearer " + token))
    //           .andExpect(status().isOk());
    //   }
    // -------------------------------------------------------------------------
}
