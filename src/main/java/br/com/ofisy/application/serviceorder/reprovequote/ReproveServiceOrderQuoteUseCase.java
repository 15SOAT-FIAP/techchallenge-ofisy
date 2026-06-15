package br.com.ofisy.application.serviceorder.reprovequote;

import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.quote.dto.ReproveQuoteRequestDTO;

import java.util.UUID;

public interface ReproveServiceOrderQuoteUseCase {

    QuoteResponseDTO execute(ReproveQuoteCommand cmd);

    record ReproveQuoteCommand(
            UUID quoteId,
            ReproveQuoteRequestDTO requestDTO
    ) {}
}