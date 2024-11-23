package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  private final NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  //Method to transfer money between 2 different account with concurrency control
  public void transferMoney(String accountFromId, String accountToId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Transfer amount must be positive.");
    }

    Account accountFrom = this.accountsRepository.getAccount(accountFromId);
    Account accountTo = this.accountsRepository.getAccount(accountToId);

    if (accountFrom == null) {
      throw new IllegalArgumentException("Account " + accountFromId + " does not exist.");
    }
    if (accountTo == null) {
      throw new IllegalArgumentException("Account " + accountToId + " does not exist.");
    }
    //To ensure thread safety when multiple threads access and modify the same accounts concurrently.
    synchronized (accountFrom) {
      synchronized (accountTo) {
        if (accountFrom.getBalance().compareTo(amount) < 0) {
          throw new IllegalArgumentException("Insufficient balance in account " + accountFromId);
        }
        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
        accountTo.setBalance(accountTo.getBalance().add(amount));
      }
    }

    this.notificationService.notifyAboutTransfer(accountFrom, "Transferred " + amount + " to account " + accountToId);
    this.notificationService.notifyAboutTransfer(accountTo, "Received " + amount + " from account " + accountFromId);
  }
}