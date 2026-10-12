package com.sonddd.paywallet.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Sinh mã tham chiếu giao dịch dạng TXN-yyyyMMdd-XXXXXX. */
public final class References {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private References() {
    }

    public static String newTransactionRef() {
        StringBuilder sb = new StringBuilder("TXN-");
        sb.append(LocalDate.now().format(DATE_FMT)).append('-');
        for (int i = 0; i < 6; i++) {
            sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }
}
