package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Modelos para API de Análisis Financiero
@Serializable
data class FinancialAnalysisResponse(
    val success: Boolean,
    val message: String,
    val data: FinancialAnalysisData,
    val error: Boolean
)

@Serializable
data class FinancialAnalysisData(
    val taxRate: Double,
    val taxAmount: Double,
    val currency: String,
    val averageTransactionValue: Double,
    val include: String,
    val transactions: Int,
    val totalRevenue: Double,
    val confirmedTransactions: Int,
    val netRevenue: Double,
    val period: FinancialPeriod
)

@Serializable
data class FinancialPeriod(
    val start: String,
    val end: String
)

// Modelos para API de Transparencia de Pagos
@Serializable
data class PaymentTransparencyResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentTransparencyData,
    val error: Boolean
)

@Serializable
data class PaymentTransparencyData(
    val totalTransactions: Int,
    val taxRate: Double,
    val lastUpdated: String,
    val period: FinancialPeriod,
    val sellerCommissionAmount: Double,
    val processingFees: Double,
    val transparencyScore: Double,
    val sellerCommissionRate: Double,
    val totalRevenue: Double,
    val taxAmount: Double,
    val confirmedTransactions: Int,
    val platformFees: Double
)

// Modelos para API de Análisis Financiero de Vendedores
@Serializable
data class SellerFinancialAnalysisResponse(
    val success: Boolean,
    val message: String,
    val data: SellerFinancialAnalysisData,
    val error: Boolean
)

@Serializable
data class SellerFinancialAnalysisData(
    val netEarnings: Double,
    val include: String,
    val commissionRate: Double,
    val period: FinancialPeriod,
    val sellerId: Int,
    val averageTransactionValue: Double,
    val sellerName: String,
    val totalSales: Double,
    val currency: String,
    val transactions: Int,
    val confirmedTransactions: Int,
    val commissionAmount: Double
)

// Parámetros para APIs financieras
@Serializable
data class FinancialAnalysisParams(
    val include: String? = null, // "revenue,taxes,commissions"
    val currency: String? = null, // "PEN", "USD", etc.
    val taxRate: Double? = null // 0.18, 0.19, etc.
)

@Serializable
data class PaymentTransparencyParams(
    val includeFees: Boolean? = null,
    val includeTaxes: Boolean? = null,
    val includeCommissions: Boolean? = null
)

@Serializable
data class SellerFinancialAnalysisParams(
    val include: String? = null, // "earnings,commissions"
    val currency: String? = null, // "PEN", "USD", etc.
    val commissionRate: Double? = null // 0.10, 0.15, etc.
)

// Enums para tipos seguros
enum class FinancialInclude(val value: String) {
    REVENUE("revenue"),
    TAXES("taxes"),
    COMMISSIONS("commissions"),
    ALL("revenue,taxes,commissions")
}

enum class SellerFinancialInclude(val value: String) {
    EARNINGS("earnings"),
    COMMISSIONS("commissions"),
    ALL("earnings,commissions")
}

enum class Currency(val value: String) {
    PEN("PEN"),
    USD("USD"),
    EUR("EUR")
}

enum class TaxRate(val value: Double) {
    STANDARD_PERU(0.18),
    STANDARD_USA(0.08),
    STANDARD_EU(0.19)
}

// Configuraciones predefinidas para análisis financiero
object FinancialConfigs {
    
    val PERU_STANDARD = FinancialAnalysisConfig(
        include = FinancialInclude.ALL,
        currency = Currency.PEN,
        taxRate = TaxRate.STANDARD_PERU
    )
    
    val USA_STANDARD = FinancialAnalysisConfig(
        include = FinancialInclude.ALL,
        currency = Currency.USD,
        taxRate = TaxRate.STANDARD_USA
    )
    
    val EU_STANDARD = FinancialAnalysisConfig(
        include = FinancialInclude.ALL,
        currency = Currency.EUR,
        taxRate = TaxRate.STANDARD_EU
    )
    
    val REVENUE_ONLY = FinancialAnalysisConfig(
        include = FinancialInclude.REVENUE,
        currency = Currency.PEN,
        taxRate = TaxRate.STANDARD_PERU
    )
    
    val TAXES_ONLY = FinancialAnalysisConfig(
        include = FinancialInclude.TAXES,
        currency = Currency.PEN,
        taxRate = TaxRate.STANDARD_PERU
    )
    
    val COMMISSIONS_ONLY = FinancialAnalysisConfig(
        include = FinancialInclude.COMMISSIONS,
        currency = Currency.PEN,
        taxRate = TaxRate.STANDARD_PERU
    )
}

data class FinancialAnalysisConfig(
    val include: FinancialInclude,
    val currency: Currency,
    val taxRate: TaxRate
) {
    fun toParams(): FinancialAnalysisParams {
        return FinancialAnalysisParams(
            include = include.value,
            currency = currency.value,
            taxRate = taxRate.value
        )
    }
}

// Configuraciones predefinidas para transparencia de pagos
object TransparencyConfigs {
    
    val FULL_TRANSPARENCY = PaymentTransparencyConfig(
        includeFees = true,
        includeTaxes = true,
        includeCommissions = true
    )
    
    val FEES_ONLY = PaymentTransparencyConfig(
        includeFees = true,
        includeTaxes = false,
        includeCommissions = false
    )
    
    val TAXES_ONLY = PaymentTransparencyConfig(
        includeFees = false,
        includeTaxes = true,
        includeCommissions = false
    )
    
    val COMMISSIONS_ONLY = PaymentTransparencyConfig(
        includeFees = false,
        includeTaxes = false,
        includeCommissions = true
    )
}

data class PaymentTransparencyConfig(
    val includeFees: Boolean,
    val includeTaxes: Boolean,
    val includeCommissions: Boolean
) {
    fun toParams(): PaymentTransparencyParams {
        return PaymentTransparencyParams(
            includeFees = includeFees,
            includeTaxes = includeTaxes,
            includeCommissions = includeCommissions
        )
    }
}

// Configuraciones predefinidas para análisis financiero de vendedores
object SellerFinancialConfigs {
    
    val PERU_STANDARD = SellerFinancialAnalysisConfig(
        include = SellerFinancialInclude.ALL,
        currency = Currency.PEN,
        commissionRate = 0.10
    )
    
    val USA_STANDARD = SellerFinancialAnalysisConfig(
        include = SellerFinancialInclude.ALL,
        currency = Currency.USD,
        commissionRate = 0.15
    )
    
    val EARNINGS_ONLY = SellerFinancialAnalysisConfig(
        include = SellerFinancialInclude.EARNINGS,
        currency = Currency.PEN,
        commissionRate = 0.10
    )
    
    val COMMISSIONS_ONLY = SellerFinancialAnalysisConfig(
        include = SellerFinancialInclude.COMMISSIONS,
        currency = Currency.PEN,
        commissionRate = 0.10
    )
    
    val HIGH_COMMISSION = SellerFinancialAnalysisConfig(
        include = SellerFinancialInclude.ALL,
        currency = Currency.PEN,
        commissionRate = 0.20
    )
}

data class SellerFinancialAnalysisConfig(
    val include: SellerFinancialInclude,
    val currency: Currency,
    val commissionRate: Double
) {
    fun toParams(): SellerFinancialAnalysisParams {
        return SellerFinancialAnalysisParams(
            include = include.value,
            currency = currency.value,
            commissionRate = commissionRate
        )
    }
}
