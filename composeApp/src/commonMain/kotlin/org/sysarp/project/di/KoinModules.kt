package org.sysarp.project.di

import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.service.CredentialStorageService
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.SimpleNotificationService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.http.StatsApiClient
import org.sysarp.project.service.http.billing.BillingApiClient
import org.sysarp.project.service.http.payment.PaymentApiClient
import org.sysarp.project.service.notification.HybridNotificationManager
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.qr.QRService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.viewmodel.YapeViewModel

val appModule = module {
    // Singletons for services and repositories
    single { UserProfileRepository() }
    single { AuthService.getInstance() } // Assuming getInstance returns a singleton
    single { SellerService(get()) } // Koin will inject AuthService
    single { AffiliationService() }
    single { QRService() }
    single { BranchService() }
    single { HttpClient() } // Ktor HttpClient
    single { PaymentApiClient(get()) } // Koin will inject HttpClient
    single { PaymentService(get()) } // Koin will inject PaymentApiClient
    single { StatsApiClient() }
    single { StatsService(get()) } // Koin will inject StatsApiClient
    single { PaymentWebSocketService(get()) } // Koin will inject AuthService
    single { HybridNotificationManager(get()) } // Injects AuthService and PaymentWebSocketService
    single { BillingApiClient() }
    single { BillingService(get(), get()) } // Injects BillingApiClient and AuthService
    single { CredentialStorageService } // Assuming it's an object

    // ViewModel Factory
    factory { YapeViewModel(get(), get()) } // Injects SimpleNotificationService and UserProfileRepository

    // Dummy/Placeholder for SimpleNotificationService
    single<SimpleNotificationService> {
        object : SimpleNotificationService {
            override fun startCapture() {}
            override fun stopCapture() {}
            override fun isCapturing(): Boolean = false
        }
    }
}
