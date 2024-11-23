package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

    @InjectMocks
    private AccountsService accountsService;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private NotificationService notificationService;

    @Test
    void addAccount() {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));

        // Mock the behavior of the repository to return the account when getAccount is called
        when(accountsRepository.getAccount("Id-123")).thenReturn(account);

        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);

        // Verify that the createAccount method was called on the repository
        verify(accountsRepository).createAccount(account);
    }

    @Test
    void addAccount_failsOnDuplicateId() {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);

        // Mock the behavior to throw DuplicateAccountIdException on the second call
        doNothing().doThrow(new DuplicateAccountIdException("Account id " + uniqueId + " already exists!"))
                .when(accountsRepository).createAccount(account);

        this.accountsService.createAccount(account);

        DuplicateAccountIdException exception = assertThrows(DuplicateAccountIdException.class, () -> this.accountsService.createAccount(account));

        assertThat(exception.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

    //Test Cases for Given Task - Start
    @Test
    @DisplayName("Test Transfer Money - Success")
    void transferMoney_success() {
        Account accountFrom = new Account("Id-123", new BigDecimal("1000"));
        Account accountTo = new Account("Id-456", new BigDecimal("500"));

        when(accountsRepository.getAccount("Id-123")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("Id-456")).thenReturn(accountTo);

        accountsService.transferMoney("Id-123", "Id-456", new BigDecimal("100"));

        verify(accountsRepository).getAccount("Id-123");
        verify(accountsRepository).getAccount("Id-456");
        verify(notificationService).notifyAboutTransfer(accountFrom, "Transferred 100 to account Id-456");
        verify(notificationService).notifyAboutTransfer(accountTo, "Received 100 from account Id-123");

        assertThat(accountFrom.getBalance()).isEqualByComparingTo("900");
        assertThat(accountTo.getBalance()).isEqualByComparingTo("600");
    }

    @Test
    @DisplayName("Test Transfer Money - Insufficient Balance")
    void transferMoney_insufficientBalance() {
        Account accountFrom = new Account("Id-123", new BigDecimal("100"));
        Account accountTo = new Account("Id-456", new BigDecimal("500"));

        when(accountsRepository.getAccount("Id-123")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("Id-456")).thenReturn(accountTo);

        try {
            accountsService.transferMoney("Id-123", "Id-456", new BigDecimal("200"));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Insufficient balance in account Id-123");
        }

        verify(accountsRepository).getAccount("Id-123");
        verify(accountsRepository).getAccount("Id-456");
        verify(notificationService, never()).notifyAboutTransfer(any(), anyString());
    }

    @Test
    @DisplayName("Test Transfer Money - Negative Balance")
    void transferMoney_negativeAmount() {
        Account accountFrom = new Account("Id-123", new BigDecimal("1000"));
        Account accountTo = new Account("Id-456", new BigDecimal("500"));

        when(accountsRepository.getAccount("Id-123")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("Id-456")).thenReturn(accountTo);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> accountsService.transferMoney("Id-123", "Id-456", new BigDecimal("-100")));

        assertThat(exception.getMessage()).isEqualTo("Transfer amount must be positive.");

        verify(accountsRepository, never()).getAccount("Id-123");
        verify(accountsRepository, never()).getAccount("Id-456");
        verify(notificationService, never()).notifyAboutTransfer(any(), anyString());
    }
    //Test Cases for Given Task - End
}
