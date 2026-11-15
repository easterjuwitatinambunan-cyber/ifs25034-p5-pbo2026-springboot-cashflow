package org.delcom.ifs25034springbootcashflow.adapter.repository;

import org.delcom.ifs25034springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25034springbootcashflow.domain.repository.ITransactionRepository;
import java.util.List;

public class TransactionRepository extends InMemoryRepository<Transaction> implements ITransactionRepository {

    @Override
    protected Integer idOf(Transaction entity) {
        return entity.getId();
    }

    @Override
    protected void assignId(Transaction entity, int id) {
        entity.setId(id);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(t -> t.getUserId() == userId);
    }
}
