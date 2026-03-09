package com.simats.poultrysuite.data.model

data class AnalyticsResponse(
    val revenue: Double,
    val expenses: Double,
    val netProfit: Double,
    val expenseBreakdown: List<ExpenseBreakdownItem>,
    val revenueTrend: List<Double>
)

data class ExpenseBreakdownItem(
    val category: String,
    val amount: Double
)
