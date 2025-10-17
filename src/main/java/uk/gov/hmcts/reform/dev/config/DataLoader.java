package uk.gov.hmcts.reform.dev.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.dev.entities.Caseworker;
import uk.gov.hmcts.reform.dev.repository.CaseworkerRepository;

import java.time.LocalDateTime;

/**
 * Configuration class for preloading initial data.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    /**
     * Preload caseworkers into the database.
     *
     * @param caseworkerRepository the caseworker repository
     * @return CommandLineRunner
     */
    @Bean
    public CommandLineRunner loadData(CaseworkerRepository caseworkerRepository) {
        return args -> {
            if (caseworkerRepository.count() == 0) {
                log.info("Preloading caseworkers...");

                Caseworker caseworker1 = Caseworker.builder()
                        .firstName("Alice")
                        .lastName("Johnson")
                        .email("alice.johnson@hmcts.gov.uk")
                        .role("Senior Caseworker")
                        .createdAt(LocalDateTime.now())
                        .build();

                Caseworker caseworker2 = Caseworker.builder()
                        .firstName("Bob")
                        .lastName("Smith")
                        .email("bob.smith@hmcts.gov.uk")
                        .role("Caseworker")
                        .createdAt(LocalDateTime.now())
                        .build();

                Caseworker caseworker3 = Caseworker.builder()
                        .firstName("Carol")
                        .lastName("Williams")
                        .email("carol.williams@hmcts.gov.uk")
                        .role("Lead Caseworker")
                        .createdAt(LocalDateTime.now())
                        .build();

                Caseworker caseworker4 = Caseworker.builder()
                        .firstName("David")
                        .lastName("Brown")
                        .email("david.brown@hmcts.gov.uk")
                        .role("Caseworker")
                        .createdAt(LocalDateTime.now())
                        .build();

                Caseworker caseworker5 = Caseworker.builder()
                        .firstName("Emma")
                        .lastName("Davis")
                        .email("emma.davis@hmcts.gov.uk")
                        .role("Senior Caseworker")
                        .createdAt(LocalDateTime.now())
                        .build();

                caseworkerRepository.save(caseworker1);
                caseworkerRepository.save(caseworker2);
                caseworkerRepository.save(caseworker3);
                caseworkerRepository.save(caseworker4);
                caseworkerRepository.save(caseworker5);

                log.info("Preloaded 5 caseworkers successfully");
            } else {
                log.info("Caseworkers already exist, skipping preload");
            }
        };
    }
}
