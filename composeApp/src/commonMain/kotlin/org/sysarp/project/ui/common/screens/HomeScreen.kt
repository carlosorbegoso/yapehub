package org.sysarp.project.ui.common.screens

// Funciones específicas de plataforma
expect fun exportLogs(logsText: String)

expect fun exportTransactions(transactionsText: String, fileName: String)
