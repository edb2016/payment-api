package com.eb.revolut.payments.db.model.types;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

    private int payerAccountId;

    private int payeeAccountId;

    private BigDecimal amount;

    private String payerComment;

}
