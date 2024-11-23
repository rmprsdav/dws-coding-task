package com.dws.challenge.web;

import com.dws.challenge.config.ApiResponse;
import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

    private final AccountsService accountsService;

    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
        log.info("Creating account {}", account);

        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountIdException daie) {
            return new ResponseEntity<>(new ApiResponse(daie.getMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new ApiResponse("Account created successfully"), HttpStatus.CREATED);
    }

    @GetMapping(path = "/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        log.info("Retrieving account for id {}", accountId);
        return this.accountsService.getAccount(accountId);
    }

    //Post Method to handle transfer of Payment between 2 different account
    @PostMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> transferMoney(@RequestBody Map<String, Object> transferRequest) {
        String accountFromId = (String) transferRequest.get("accountFromId");
        String accountToId = (String) transferRequest.get("accountToId");
        BigDecimal amount = new BigDecimal((String) transferRequest.get("amount"));

        String logMessage = String.format("Transferring %s from account %s to account %s", amount, accountFromId, accountToId);
        log.info(logMessage);

        try {
            this.accountsService.transferMoney(accountFromId, accountToId, amount);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new ApiResponse(logMessage), HttpStatus.OK);
    }
}