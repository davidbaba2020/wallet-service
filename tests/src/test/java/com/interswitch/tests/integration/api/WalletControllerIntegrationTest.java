//package com.interswitch.tests.integration.api;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.interswitch.model.dtos.request.CreateWalletRequest;
//import com.interswitch.model.dtos.request.UpdateWalletRequest;
//import com.interswitch.model.enums.WalletType;
//import com.interswitch.tests.config.IntegrationTestConfig;
//import com.interswitch.web.WebApplication;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest(classes = {WebApplication.class, IntegrationTestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class WalletControllerIntegrationTest{
//
//    @Autowired
//    private WebApplicationContext webApplicationContext;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private MockMvc mockMvc;
//
//    @BeforeEach
//    void setup() {
//        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//    }
//
//    @Test
//    void shouldCreateAndRetrieveWalletSuccessfully() throws Exception {
//        // Given
//        UUID userId = UUID.randomUUID();
//        UUID accountId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//
//        Map<String, String> metadata = new HashMap<>();
//        metadata.put("source", "integration-test");
//
//        CreateWalletRequest request = CreateWalletRequest.builder()
//                .userId(userId)
//                .accountId(accountId)
//                .walletType(WalletType.PERSONAL)
//                .currency("NGN")
//                .walletName("Integration Test Wallet")
//                .description("Test wallet for integration testing")
//                .isDefault(true)
//                .metadata(metadata)
//                .performedBy(performedBy)
//                .build();
//
//        // When - Create wallet
//        String createResponse = mockMvc.perform(post("/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.message").value("Wallet created successfully"))
//                .andExpect(jsonPath("$.statusCode").value(201))
//                .andExpect(jsonPath("$.data.currency").value("NGN"))
//                .andExpect(jsonPath("$.data.walletName").value("Integration Test Wallet"))
//                .andExpect(jsonPath("$.data.isDefault").value(true))
//                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
//                .andReturn().getResponse().getContentAsString();
//
//        // Extract wallet ID from response
//        UUID walletId = extractWalletIdFromResponse(createResponse);
//
//        // Then - Retrieve wallet by ID
//        mockMvc.perform(get("/wallets/{walletId}", walletId))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.id").value(walletId.toString()))
//                .andExpect(jsonPath("$.data.currency").value("NGN"))
//                .andExpect(jsonPath("$.data.walletName").value("Integration Test Wallet"));
//    }
//
//    @Test
//    void shouldGetUserWalletsSuccessfully() throws Exception {
//        // Given - Create multiple wallets for same user
//        UUID userId = UUID.randomUUID();
//        UUID accountId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//
//        // Create NGN wallet
//        CreateWalletRequest ngnRequest = CreateWalletRequest.builder()
//                .userId(userId)
//                .accountId(accountId)
//                .walletType(WalletType.PERSONAL)
//                .currency("NGN")
//                .walletName("NGN Wallet")
//                .description("NGN wallet")
//                .isDefault(true)
//                .performedBy(performedBy)
//                .build();
//
//        mockMvc.perform(post("/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(ngnRequest)))
//                .andExpect(status().isCreated());
//
//        // Create USD wallet
//        CreateWalletRequest usdRequest = CreateWalletRequest.builder()
//                .userId(userId)
//                .accountId(accountId)
//                .walletType(WalletType.PERSONAL)
//                .currency("USD")
//                .walletName("USD Wallet")
//                .description("USD wallet")
//                .isDefault(false)
//                .performedBy(performedBy)
//                .build();
//
//        mockMvc.perform(post("/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(usdRequest)))
//                .andExpect(status().isCreated());
//
//        // When - Get user wallets
//        mockMvc.perform(get("/wallets/user/{userId}", userId))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("User wallets retrieved successfully"))
//                .andExpect(jsonPath("$.data", hasSize(2)))
//                .andExpect(jsonPath("$.data[*].currency", containsInAnyOrder("NGN", "USD")));
//    }
//
//    @Test
//    void shouldUpdateWalletSuccessfully() throws Exception {
//        // Given - Create a wallet first
//        UUID userId = UUID.randomUUID();
//        UUID accountId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//
//        CreateWalletRequest createRequest = CreateWalletRequest.builder()
//                .userId(userId)
//                .accountId(accountId)
//                .walletType(WalletType.PERSONAL)
//                .currency("EUR")
//                .walletName("Original Wallet")
//                .description("Original description")
//                .isDefault(false)
//                .performedBy(performedBy)
//                .build();
//
//        String createResponse = mockMvc.perform(post("/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createRequest)))
//                .andExpect(status().isCreated())
//                .andReturn().getResponse().getContentAsString();
//
//        UUID walletId = extractWalletIdFromResponse(createResponse);
//
//        // When - Update wallet
//        Map<String, String> updatedMetadata = new HashMap<>();
//        updatedMetadata.put("updated", "true");
//        updatedMetadata.put("version", "2.0");
//
//        UpdateWalletRequest updateRequest = UpdateWalletRequest.builder()
//                .walletName("Updated Wallet Name")
//                .description("Updated description")
//                .metadata(updatedMetadata)
//                .performedBy(performedBy)
//                .build();
//
//        mockMvc.perform(put("/wallets/{walletId}", walletId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Wallet updated successfully"))
//                .andExpect(jsonPath("$.data.walletName").value("Updated Wallet Name"))
//                .andExpect(jsonPath("$.data.description").value("Updated description"));
//
//        // Then - Verify update by retrieving wallet
//        mockMvc.perform(get("/wallets/{walletId}", walletId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.walletName").value("Updated Wallet Name"))
//                .andExpect(jsonPath("$.data.description").value("Updated description"));
//    }
//
//    @Test
//    void shouldUpdateWalletStatusSuccessfully() throws Exception {
//        // Given - Create a wallet first
//        UUID userId = UUID.randomUUID();
//        UUID accountId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//
//        CreateWalletRequest createRequest = CreateWalletRequest.builder()
//                .userId(userId)
//                .accountId(accountId)
//                .walletType(WalletType.PERSONAL)
//                .currency("GBP")
//                .walletName("Status Test Wallet")
//                .description("Wallet for status testing")
//                .isDefault(false)
//                .performedBy(performedBy)
//                .build();
//
//        String createResponse = mockMvc.perform(post("/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createRequest)))
//                .andExpect(status().isCreated())
//                .andReturn().getResponse().getContentAsString();
//
//        UUID walletId = extractWalletIdFromResponse(createResponse);
//
//        // When - Update wallet status to SUSPENDED
//        mockMvc.perform(patch("/wallets/{walletId}/status", walletId)
//                        .param("status", "SUSPENDED")
//                        .param("performedBy", performedBy.toString()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Wallet status updated successfully"));
//
//        // Then - Verify status was updated
//        mockMvc.perform(get("/wallets/{walletId}", walletId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
//    }
//
//    @Test
//    void shouldSetDefaultWalletSuccessfully() throws Exception {
//        // Given - Create two wallets for same user
//        UUID userId = UUID.randomUUID();
//        UUID accountId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//
//        // Create first wallet as default
//        CreateWalletRequest firstRequest = CreateWalletRequest.builder()
//                .userId(userId)
//                .accountId(accountId)
//                .walletType(WalletType.PERSONAL)
//                .currency("NGN")
//                .walletName("First Wallet")
//                .description("First wallet")
//                .isDefault(true)
//                .performedBy(performedBy)
//                .build();
//
//        mockMvc.perform(post("/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(firstRequest)))
//                .andExpect(status().isCreated());
//
//        // Create second wallet
//        CreateWalletRequest secondRequest = CreateWalletRequest.builder()
//                .userId(userId)
//                .accountId(accountId)
//                .walletType(WalletType.PERSONAL)
//                .currency("USD")
//                .walletName("Second Wallet")
//                .description("Second wallet")
//                .isDefault(false)
//                .performedBy(performedBy)
//                .build();
//
//        String secondResponse = mockMvc.perform(post("/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(secondRequest)))
//                .andExpect(status().isCreated())
//                .andReturn().getResponse().getContentAsString();
//
//        UUID secondWalletId = extractWalletIdFromResponse(secondResponse);
//
//        // When - Set second wallet as default
//        mockMvc.perform(patch("/wallets/{walletId}/default", secondWalletId)
//                        .param("performedBy", performedBy.toString()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Default wallet set successfully"));
//
//        // Then - Verify default wallet
//        mockMvc.perform(get("/wallets/user/{userId}/default", userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.id").value(secondWalletId.toString()))
//                .andExpect(jsonPath("$.data.currency").value("USD"));
//    }
//
//    @Test
//    void shouldHandleWalletNotFoundError() throws Exception {
//        // Given
//        UUID nonExistentWalletId = UUID.randomUUID();
//
//        // When & Then
//        mockMvc.perform(get("/wallets/{walletId}", nonExistentWalletId))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void shouldHandleValidationErrorsInIntegrationTest() throws Exception {
//        // Given - Invalid request
//        CreateWalletRequest invalidRequest = CreateWalletRequest.builder()
//                .userId(null) // Invalid - required field
//                .currency("") // Invalid - empty
//                .build();
//
//        // When & Then
//        mockMvc.perform(post("/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void shouldCheckWalletExistsCorrectly() throws Exception {
//        // Given - Create a wallet
//        UUID userId = UUID.randomUUID();
//        UUID accountId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//
//        CreateWalletRequest request = CreateWalletRequest.builder()
//                .userId(userId)
//                .accountId(accountId)
//                .walletType(WalletType.PERSONAL)
//                .currency("CAD")
//                .walletName("CAD Wallet")
//                .description("Canadian Dollar wallet")
//                .isDefault(false)
//                .performedBy(performedBy)
//                .build();
//
//        mockMvc.perform(post("/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated());
//
//        // When & Then - Check existing wallet
//        mockMvc.perform(get("/wallets/user/{userId}/exists", userId)
//                        .param("currency", "CAD"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data").value(true));
//
//        // When & Then - Check non-existing wallet
//        mockMvc.perform(get("/wallets/user/{userId}/exists", userId)
//                        .param("currency", "JPY"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data").value(false));
//    }
//
//    // Helper method to extract wallet ID from JSON response
//    private UUID extractWalletIdFromResponse(String jsonResponse) throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        return UUID.fromString(mapper.readTree(jsonResponse).get("data").get("id").asText());
//    }
//}