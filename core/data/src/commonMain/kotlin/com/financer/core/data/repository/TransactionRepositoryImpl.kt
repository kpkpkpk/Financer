package com.financer.core.data.repository

import com.financer.core.data.db.FinancerDatabase
import com.financer.core.data.model.Transaction
import com.financer.core.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime

class TransactionRepositoryImpl(
    private val database: FinancerDatabase
) : TransactionRepository {

    private val queries get() = database.transactionEntityQueries

    override suspend fun getAll(): List<Transaction> = withContext(Dispatchers.IO) {
        queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun getByPeriod(from: LocalDateTime, to: LocalDateTime): List<Transaction> =
        withContext(Dispatchers.IO) {
            queries.selectByPeriod(from.toString(), to.toString()).executeAsList()
                .map { it.toDomain() }
        }

    override suspend fun getByCategory(categoryId: Long): List<Transaction> =
        withContext(Dispatchers.IO) {
            queries.selectByCategory(categoryId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getById(id: Long): Transaction? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun insert(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            queries.insert(
                type = transaction.type.name,
                amount = transaction.amount,
                category_id = transaction.categoryId,
                date = transaction.date.toString(),
                note = transaction.note
            )
        }
    }

    override suspend fun update(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            queries.update(
                type = transaction.type.name,
                amount = transaction.amount,
                category_id = transaction.categoryId,
                date = transaction.date.toString(),
                note = transaction.note,
                id = transaction.id
            )
        }
    }

    override suspend fun deleteById(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteById(id)
        }
    }

    override suspend fun getSumByType(): Map<TransactionType, Long> = withContext(Dispatchers.IO) {
        queries.sumByType().executeAsList().associate {
            TransactionType.valueOf(it.type) to (it.total ?: 0L)
        }
    }

    override suspend fun getSumByTypeAndPeriod(
        from: LocalDateTime,
        to: LocalDateTime
    ): Map<TransactionType, Long> = withContext(Dispatchers.IO) {
        queries.sumByTypeAndPeriod(from.toString(), to.toString())
            .executeAsList()
            .associate {
                TransactionType.valueOf(it.type) to (it.total ?: 0L)
            }
    }

    override suspend fun getTotalCount(): Long = withContext(Dispatchers.IO) {
        queries.totalCount().executeAsOne()
    }

    override suspend fun getCategoryStats(type: TransactionType): List<CategoryStat> =
        withContext(Dispatchers.IO) {
            val counts = queries.countByCategory().executeAsList()
            val avgs = queries.avgAmountByCategory(type.name).executeAsList()
            val lastDates = queries.lastDateByCategory().executeAsList()

            val avgMap = avgs.associate {
                it.category_id to (it.avg_amount ?: 0L)
            }
            val lastDateMap = lastDates.associate { it.category_id to it.last_date }

            counts.map { countRow ->
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
            date = LocalDateTime.parse(date),
            note = note
        )
    }
}
