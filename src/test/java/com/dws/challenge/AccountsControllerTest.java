package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    void createAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

        Account account = accountsService.getAccount("Id-123");
        assertThat(account.getAccountId()).isEqualTo("Id-123");
        assertThat(account.getBalance()).isEqualByComparingTo("1000");
    }

    @Test
    void createDuplicateAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    void createAccountNoAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    void createAccountNoBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void createAccountNoBody() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccountNegativeBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
    }

    @Test
    void createAccountEmptyAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    void getAccount() throws Exception {
        String uniqueAccountId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
        this.accountsService.createAccount(account);
        this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
                .andExpect(status().isOk())
                .andExpect(
                        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
    }

    //Test Cases for Given Task - Start
    @Test
    @DisplayName("Test Transfer Money - Success")
    void transferMoney() throws Exception {
        Account accountFrom = new Account("Id-123", new BigDecimal("1000"));
        Account accountTo = new Account("Id-456", new BigDecimal("500"));
        this.accountsService.createAccount(accountFrom);
        this.accountsService.createAccount(accountTo);

        this.mockMvc.perform(post("/v1/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-456\",\"amount\":\"100\"}"))
                .andExpect(status().isOk());

        assertThat(this.accountsService.getAccount("Id-123").getBalance()).isEqualByComparingTo("900");
        assertThat(this.accountsService.getAccount("Id-456").getBalance()).isEqualByComparingTo("600");
    }

    @Test
    @DisplayName("Test Transfer Money - Insufficient Balance")
    void transferMoneyInsufficientBalance() throws Exception {
        Account accountFrom = new Account("Id-123", new BigDecimal("100"));
        Account accountTo = new Account("Id-456", new BigDecimal("500"));
        this.accountsService.createAccount(accountFrom);
        this.accountsService.createAccount(accountTo);

        this.mockMvc.perform(post("/v1/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-456\",\"amount\":\"200\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test Transfer Money - Negative Amount")
    void transferMoneyNegativeAmount() throws Exception {
        Account accountFrom = new Account("Id-123", new BigDecimal("1000"));
        Account accountTo = new Account("Id-456", new BigDecimal("500"));
        this.accountsService.createAccount(accountFrom);
        this.accountsService.createAccount(accountTo);

        this.mockMvc.perform(post("/v1/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-456\",\"amount\":\"-100\"}"))
                .andExpect(status().isBadRequest());
    }
    //Test Cases for Given Task - End
}
