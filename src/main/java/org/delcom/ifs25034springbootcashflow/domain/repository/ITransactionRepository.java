package org.delcom.ifs25034springbootcashflow.domain.repository;

import org.delcom.ifs25034springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25034springbootcashflow.domain.entity.TransactionType;
import java.time.LocalDateTime;
import java.util.List;

public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    List<Transaction> findByUserId(int userId);

    default Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        return save(new Transaction(null, userId, description, amount, type, proofPath, LocalDateTime.now()));
    }
}
