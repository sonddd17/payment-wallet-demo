package com.sonddd.paywallet.service.impl;

import com.sonddd.paywallet.dto.response.PageResponse;
import com.sonddd.paywallet.dto.response.TransactionResponse;
import com.sonddd.paywallet.dto.response.TransactionTypeStat;
import com.sonddd.paywallet.repository.TransactionRepository;
import com.sonddd.paywallet.service.AdminService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final TransactionRepository transactionRepository;

    public AdminServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> listAllTransactions(Pageable pageable) {
        return PageResponse.of(transactionRepository.findAll(pageable), TransactionResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionTypeStat> report() {
        return transactionRepository.summarizeByType();
    }
}
