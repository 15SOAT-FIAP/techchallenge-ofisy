package br.com.ofisy.application.serviceorder.approvequote;

import br.com.ofisy.domain.quote.Quote;

import java.util.UUID;

public interface ApproveServiceOrderQuoteUseCase {

    Quote execute(UUID quoteId);
}