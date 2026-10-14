package com.banka.banka.mapper;

import com.banka.banka.dto.response.TransactionResponse;
import com.banka.banka.entity.TransactionHistory;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-14T01:09:51+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionResponse toResponse(TransactionHistory transactionHistory) {
        if ( transactionHistory == null ) {
            return null;
        }

        TransactionResponse.TransactionResponseBuilder transactionResponse = TransactionResponse.builder();

        transactionResponse.id( transactionHistory.getId() );
        transactionResponse.sourceIban( transactionHistory.getSourceIban() );
        transactionResponse.targetIban( transactionHistory.getTargetIban() );
        transactionResponse.fromIban( transactionHistory.getFromIban() );
        transactionResponse.toIban( transactionHistory.getToIban() );
        transactionResponse.amount( transactionHistory.getAmount() );
        transactionResponse.transactionType( transactionHistory.getTransactionType() );
        transactionResponse.description( transactionHistory.getDescription() );
        transactionResponse.status( transactionHistory.getStatus() );
        transactionResponse.transactionDate( transactionHistory.getTransactionDate() );

        return transactionResponse.build();
    }

    @Override
    public List<TransactionResponse> toResponseList(List<TransactionHistory> transactionHistories) {
        if ( transactionHistories == null ) {
            return null;
        }

        List<TransactionResponse> list = new ArrayList<TransactionResponse>( transactionHistories.size() );
        for ( TransactionHistory transactionHistory : transactionHistories ) {
            list.add( toResponse( transactionHistory ) );
        }

        return list;
    }
}
