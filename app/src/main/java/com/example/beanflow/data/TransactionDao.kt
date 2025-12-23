package com.example.beanflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert
    suspend fun insertDetails(details: List<TransactionDetail>)

    @Query("SELECT * FROM transactions ORDER BY transaction_date DESC")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT * FROM transaction_details WHERE transaction_id = :transactionId")
    suspend fun getTransactionDetails(transactionId: Int): List<TransactionDetail>

    // --- PASTIKAN INI ADA ---
    @Query("SELECT SUM(total_amount) FROM transactions WHERE transaction_date BETWEEN :startDate AND :endDate")
    fun getTotalSales(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE transaction_date BETWEEN :start AND :end ORDER BY transaction_date DESC")
    suspend fun getTransactionsByDate(start: Long, end: Long): List<Transaction>

    @Query("SELECT product_name as name, SUM(quantity) as totalQty FROM transaction_details GROUP BY product_name ORDER BY totalQty DESC LIMIT 5")
    fun getTopSellingProducts(): kotlinx.coroutines.flow.Flow<List<TopProduct>>
}