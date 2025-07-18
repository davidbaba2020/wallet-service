//package com.interswitch.tests.unit.api;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.interswitch.core.services.WalletService;
//import com.interswitch.model.dtos.request.CreateWalletRequest;
//import com.interswitch.model.dtos.request.UpdateWalletRequest;
//import com.interswitch.model.entities.Wallet;
//import com.interswitch.model.enums.WalletStatus;
//import com.interswitch.model.enums.WalletType;
//import com.interswitch.web.controller.WalletController;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.hamcrest.Matchers.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(WalletController.class)
//public class WalletControllerUnitTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private WalletService walletService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void shouldCreateWalletSuccessfully() throws Exception {
//        // Given
//        UUID userId = UUID.randomUUID();
//        UUID accountId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//
//        CreateWalletRequest request = CreateWalletRequest.builder()
//            .userId(userId)
//            .accountId(accountId)
//            .walletType(WalletType.PERSONAL)
//            .currency("NGN")
//            .walletName("Test Wallet")
//            .description("Test Description")
//            .isDefault(true)
//            .metadata(Map.of("source", "test"))
//            .performedBy(performedBy)
//            .build();
//
//        Wallet mockWallet = Wallet.builder()
//            .id(UUID.randomUUID())
//            .userId(userId)
//            .accountId(accountId)
//            .walletType(WalletType.PERSONAL)
//            .currency("NGN")
//            .walletName("Test Wallet")
//            .status(WalletStatus.ACTIVE)
//            .isDefault(true)
//            .build();
//
//        when(walletService.createWallet(any(), any(), any(), anyString(), anyString(), anyString(), any(), any(), any()))
//            .thenReturn(mockWallet);
//
//        // When & Then
//        mockMvc.perform(post("/wallets")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.message").value("Wallet created successfully"))
//                .andExpect(jsonPath("$.statusCode").value(201))
//                .andExpect(jsonPath("$.data.id").value(mockWallet.getId().toString()))
//                .andExpect(jsonPath("$.data.currency").value("NGN"))
//                .andExpect(jsonPath("$.data.walletName").value("Test Wallet"))
//                .andExpect(jsonPath("$.data.isDefault").value(true));
//
//        verify(walletService).createWallet(userId, accountId, WalletType.PERSONAL, "NGN",
//            "Test Wallet", "Test Description", true, request.getMetadata(), performedBy);
//    }
//
//    @Test
//    void shouldGetWalletSuccessfully() throws Exception {
//        // Given
//        UUID walletId = UUID.randomUUID();
//        Wallet mockWallet = Wallet.builder()
//            .id(walletId)
//            .userId(UUID.randomUUID())
//            .currency("USD")
//            .walletName("USD Wallet")
//            .status(WalletStatus.ACTIVE)
//            .build();
//
//        when(walletService.getWallet(walletId)).thenReturn(mockWallet);
//
//        // When & Then
//        mockMvc.perform(get("/wallets/{walletId}", walletId))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Wallet retrieved successfully"))
//                .andExpect(jsonPath("$.data.id").value(walletId.toString()))
//                .andExpect(jsonPath("$.data.currency").value("USD"));
//
//        verify(walletService).getWallet(walletId);
//    }
//
//    @Test
//    void shouldGetUserWalletsSuccessfully() throws Exception {
//        // Given
//        UUID userId = UUID.randomUUID();
//        List<Wallet> mockWallets = Arrays.asList(
//            Wallet.builder().id(UUID.randomUUID()).userId(userId).currency("NGN").build(),
//            Wallet.builder().id(UUID.randomUUID()).userId(userId).currency("USD").build()
//        );
//
//        when(walletService.getUserWallets(userId)).thenReturn(mockWallets);
//
//        // When & Then
//        mockMvc.perform(get("/wallets/user/{userId}", userId))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("User wallets retrieved successfully"))
//                .andExpect(jsonPath("$.data", hasSize(2)))
//                .andExpect(jsonPath("$.data[0].currency").value("NGN"))
//                .andExpect(jsonPath("$.data[1].currency").value("USD"));
//
//        verify(walletService).getUserWallets(userId);
//    }
//
//    @Test
//    void shouldGetUserWalletsByStatusSuccessfully() throws Exception {
//        // Given
//        UUID userId = UUID.randomUUID();
//        WalletStatus status = WalletStatus.ACTIVE;
//        List<Wallet> mockWallets = Collections.singletonList(
//                Wallet.builder().id(UUID.randomUUID()).userId(userId).status(status).build()
//        );
//
//        when(walletService.getUserWalletsByStatus(userId, status)).thenReturn(mockWallets);
//
//        // When & Then
//        mockMvc.perform(get("/wallets/user/{userId}/status/{status}", userId, status))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data", hasSize(1)));
//
//        verify(walletService).getUserWalletsByStatus(userId, status);
//    }
//
//    @Test
//    void shouldGetWalletsByStatusWithPaginationSuccessfully() throws Exception {
//        // Given
//        WalletStatus status = WalletStatus.ACTIVE;
//        List<Wallet> mockWallets = Collections.singletonList(
//                Wallet.builder().id(UUID.randomUUID()).status(status).build()
//        );
//        Page<Wallet> mockPage = new PageImpl<>(mockWallets, PageRequest.of(0, 10), 1);
//
//        when(walletService.getWalletsByStatus(eq(status), any())).thenReturn(mockPage);
//
//        // When & Then
//        mockMvc.perform(get("/wallets/status/{status}", status)
//                .param("page", "0")
//                .param("size", "10"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.content", hasSize(1)))
//                .andExpect(jsonPath("$.data.totalElements").value(1));
//
//        verify(walletService).getWalletsByStatus(eq(status), any());
//    }
//
//    @Test
//    void shouldUpdateWalletSuccessfully() throws Exception {
//        // Given
//        UUID walletId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//
//        UpdateWalletRequest request = UpdateWalletRequest.builder()
//            .walletName("Updated Wallet")
//            .description("Updated Description")
//            .metadata(Map.of("updated", "true"))
//            .performedBy(performedBy)
//            .build();
//
//        Wallet updatedWallet = Wallet.builder()
//            .id(walletId)
//            .walletName("Updated Wallet")
//            .description("Updated Description")
//            .build();
//
//        when(walletService.updateWallet(eq(walletId), anyString(), anyString(), any(), eq(performedBy)))
//            .thenReturn(updatedWallet);
//
//        // When & Then
//        mockMvc.perform(put("/wallets/{walletId}", walletId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Wallet updated successfully"))
//                .andExpect(jsonPath("$.data.walletName").value("Updated Wallet"));
//
//        verify(walletService).updateWallet(walletId, "Updated Wallet", "Updated Description",
//            request.getMetadata(), performedBy);
//    }
//
//    @Test
//    void shouldUpdateWalletStatusSuccessfully() throws Exception {
//        // Given
//        UUID walletId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//        WalletStatus status = WalletStatus.SUSPENDED;
//
//        doNothing().when(walletService).updateWalletStatus(walletId, status, performedBy);
//
//        // When & Then
//        mockMvc.perform(patch("/wallets/{walletId}/status", walletId)
//                .param("status", status.toString())
//                .param("performedBy", performedBy.toString()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Wallet status updated successfully"));
//
//        verify(walletService).updateWalletStatus(walletId, status, performedBy);
//    }
//
//    @Test
//    void shouldSetDefaultWalletSuccessfully() throws Exception {
//        // Given
//        UUID walletId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//
//        doNothing().when(walletService).setDefaultWallet(walletId, performedBy);
//
//        // When & Then
//        mockMvc.perform(patch("/wallets/{walletId}/default", walletId)
//                .param("performedBy", performedBy.toString()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Default wallet set successfully"));
//
//        verify(walletService).setDefaultWallet(walletId, performedBy);
//    }
//
//    @Test
//    void shouldDeleteWalletSuccessfully() throws Exception {
//        // Given
//        UUID walletId = UUID.randomUUID();
//        UUID performedBy = UUID.randomUUID();
//
//        doNothing().when(walletService).deleteWallet(walletId, performedBy);
//
//        // When & Then
//        mockMvc.perform(delete("/wallets/{walletId}", walletId)
//                .param("performedBy", performedBy.toString()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Wallet deleted successfully"));
//
//        verify(walletService).deleteWallet(walletId, performedBy);
//    }
//
//    @Test
//    void shouldCheckWalletExistsSuccessfully() throws Exception {
//        // Given
//        UUID userId = UUID.randomUUID();
//        String currency = "NGN";
//
//        when(walletService.walletExists(userId, currency)).thenReturn(true);
//
//        // When & Then
//        mockMvc.perform(get("/wallets/user/{userId}/exists", userId)
//                .param("currency", currency))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data").value(true));
//
//        verify(walletService).walletExists(userId, currency);
//    }
//
//    @Test
//    void shouldGetWalletsCreatedBetweenSuccessfully() throws Exception {
//        // Given
//        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
//        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
//
//        List<Wallet> mockWallets = Collections.singletonList(
//                Wallet.builder().id(UUID.randomUUID()).build()
//        );
//
//        when(walletService.getWalletsCreatedBetween(startDate, endDate)).thenReturn(mockWallets);
//
//        // When & Then
//        mockMvc.perform(get("/wallets/created-between")
//                .param("startDate", "2024-01-01T00:00:00")
//                .param("endDate", "2024-12-31T23:59:00"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data", hasSize(1)));
//
//        verify(walletService).getWalletsCreatedBetween(startDate, endDate);
//    }
//
//    @Test
//    void shouldHandleValidationErrors() throws Exception {
//        // Given - Invalid request with missing required fields
//        CreateWalletRequest invalidRequest = CreateWalletRequest.builder()
//            .currency("") // Invalid empty currency
//            .build();
//
//        // When & Then
//        mockMvc.perform(post("/wallets")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(invalidRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//
//        verify(walletService, never()).createWallet(any(), any(), any(), any(), any(), any(), any(), any(), any());
//    }
//}
