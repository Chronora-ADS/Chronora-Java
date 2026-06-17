package br.com.senai.model.DTO.payment;

import java.math.BigDecimal;

public record PlatformStatsDTO(
        long totalUsuarios,
        long totalPedidos,
        long pedidosCriados,
        long pedidosEmAndamento,
        long pedidosConcluidos,
        long pedidosCancelados,
        long totalTransacoes,
        long transacoesPagas,
        long transacoesPendentes,
        long transacoesFalhas,
        long totalChronosComprados,
        long totalChronosVendidos,
        BigDecimal volumeFinanceiroTotal
) {}
