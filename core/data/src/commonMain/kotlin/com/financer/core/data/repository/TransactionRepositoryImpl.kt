package com.financer.core.data.repository

import com.financer.core.data.db.FinancerDatabase
import com.financer.core.data.model.Transaction
import com.financer.core.data.model.TransactionType

class TransactionRepositoryImpl(
    private val database: FinancerDatabase
) : TransactionRepository {

    private val queries get() = database.transactionEntityQueries

    override fun getAll(): List<Transaction> {
        return queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override fun getByPeriod(from: String, to: String): List<Transaction> {
        return queries.selectByPeriod(from, to).executeAsList().map { it.toDomain() }
    }

    override fun getByCategory(categoryId: Long): List<Transaction> {
        return queries.selectByCategory(categoryId).executeAsList().map { it.toDomain() }
    }

    override fun getById(id: Long): Transaction? {
        return queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override fun insert(transaction: Transaction) {
        queries.insert(
            type = transaction.type.name,
            amount = transaction.amount,
            category_id = transaction.categoryId,
            date = transaction.date,
            note = transaction.note
        )
    }

    override fun update(transaction: Transaction) {
        queries.update(
            type = transaction.type.name,
            amount = transaction.amount,
            category_id = transaction.categoryId,
            date = transaction.date,
            note = transaction.note,
            id = transaction.id
        )
    }

    override fun deleteById(id: Long) {
        queries.deleteById(id)
    }

    override fun getSumByType(): Map<TransactionType, Long> {
        return queries.sumByType().executeAsList().associate {
            TransactionType.valueOf(it.type) to (it.total ?: 0L)
        }
    }

    override fun getSumByTypeAndPeriod(from: String, to: String): Map<TransactionType, Long> {
        return queries.sumByTypeAndPeriod(from, to).executeAsList().associate {
            TransactionType.valueOf(it.type) to (it.total ?: 0L)
        }
    }

    override fun getTotalCount(): Long {
        return queries.totalCount().executeAsOne()
    }

    override fun getCategoryStats(type: TransactionType): List<CategoryStat> {
        val counts = queries.countByCategory().executeAsList()
        val avgs = queries.avgAmountByCategory(type.name).executeAsList()
        val lastDates = queries.lastDateByCategory().executeAsList()

        val avgMap = avgs.associate {
            it.category_id to (it.avg_amount ?: 0L)
        }
        val lastDateMap = lastDates.associate { it.category_id to it.last_date }

        return counts.map { countRow ->
            CategoryStat(
                categoryId = countRow.category_id,
                count = countRow.cnt,
                avgAmount = avgMap[countRow.category_id] ?: 0L,
                lastDate = lastDateMap[countRow.category_id]
            )
        }
    }

    private fun com.financer.core.data.db.TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            type = TransactionType.valueOf(type),
            amount = amount,
            categoryId = category_id,
            date = date,
            note = note
        )
    }
}
