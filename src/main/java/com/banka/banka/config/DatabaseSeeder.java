package com.banka.banka.config;

import com.banka.banka.entity.Account;
import com.banka.banka.entity.Role;
import com.banka.banka.entity.User;
import com.banka.banka.repository.AccountRepository;
import com.banka.banka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User gonderenUser = User.builder()
                    .firstName("Gönderen")
                    .lastName("Kullanıcı")
                    .username("gonderen")
                    .email("gonderen@banka.com")
                    .password(passwordEncoder.encode("123456"))
                    .phoneNumber("05550000001")
                    .role(Role.USER)
                    .build();

            User aliciUser = User.builder()
                    .firstName("Alıcı")
                    .lastName("Kullanıcı")
                    .username("alici")
                    .email("alici@banka.com")
                    .password(passwordEncoder.encode("123456"))
                    .phoneNumber("05550000002")
                    .role(Role.USER)
                    .build();

            gonderenUser = userRepository.save(gonderenUser);
            aliciUser = userRepository.save(aliciUser);

            Account gonderenAccount = Account.builder()
                    .iban("TR110000000000000000000001")
                    .accountNumber("1100000000000001")
                    .balance(new BigDecimal("10000.00"))
                    .currency("TRY")
                    .active(true)
                    .user(gonderenUser)
                    .build();

            Account aliciAccount = Account.builder()
                    .iban("TR220000000000000000000002")
                    .accountNumber("2200000000000002")
                    .balance(new BigDecimal("500.00"))
                    .currency("TRY")
                    .active(true)
                    .user(aliciUser)
                    .build();

            accountRepository.save(gonderenAccount);
            accountRepository.save(aliciAccount);
        }
    }
}
