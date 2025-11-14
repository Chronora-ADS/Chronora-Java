package br.com.senai.config;

import br.com.senai.model.DTO.DocumentDTO;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.*;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.AuthService;
import br.com.senai.service.SupabaseAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final SupabaseAuthService supabaseAuthService;
    private final AuthService authService;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            if (userRepository.count() == 0) {
                // Cria usuário padrão
                UserDTO defaultUser = new UserDTO();
                defaultUser.setName("Bertrania Dude");
                defaultUser.setEmail("email@email.com");
                defaultUser.setPhoneNumber(5547912345678L);
                defaultUser.setPassword(passwordEncoder.encode("123123"));

                // Configura documento padrão
                DocumentDTO documentDTO = new DocumentDTO();
                documentDTO.setName("documento_padrao");
                documentDTO.setType("application/png");
                byte[] pdfBytes = Files.readAllBytes(Paths.get("C:/Users/lucasmader/Downloads/images.png"));
                documentDTO.setData(Base64.getEncoder().encodeToString(pdfBytes));
                defaultUser.setDocument(documentDTO);

                Map<String, Object> userMetadata = new HashMap<>();
                userMetadata.put("name", defaultUser.getName());
                userMetadata.put("phone", defaultUser.getPhoneNumber());

                SupabaseUserDTO supabaseUserDTO = supabaseAuthService.signUp(
                        defaultUser.getEmail(),
                        defaultUser.getPassword(),
                        userMetadata
                );

                // Registrar no banco local com o ID do Supabase
                UserEntity createdUser = authService.register(defaultUser, supabaseUserDTO.getId());

                // Configura categoria
                CategoryEntity categoryManutencao = new CategoryEntity();
                categoryManutencao.setName("Manutenção");
                CategoryEntity categoryEncanamento = new CategoryEntity();
                categoryEncanamento.setName("Encanamento");
                CategoryEntity categoryEletrica = new CategoryEntity();
                categoryEletrica.setName("Elétrica");
                CategoryEntity categoryPintura = new CategoryEntity();
                categoryPintura.setName("Pintura");

                // Imagem padrão em Base64
                byte[] serviceImageBytes = Files.readAllBytes(Paths.get("C:/Users/lucasmader/Downloads/images.png"));

                // Cria serviço padrão
                ServiceDTO service1 = new ServiceDTO(
                        "Manutenção Preventiva de Eletrodomésticos",
                        "Realizamos manutenção preventiva em geladeiras, lavadoras, micro-ondas e outros eletrodomésticos. Inclui limpeza, lubrificação e ajustes necessários para prolongar a vida útil do equipamento.",
                        6,
                        "PRESENCIAL",
                        LocalDate.now().plusDays(15),
                        List.of(categoryManutencao),
                        Base64.getEncoder().encodeToString(serviceImageBytes)
                );
                ServiceDTO service2 = new ServiceDTO(
                        "Desentupimento de Pia e Vaso Sanitário",
                        "Serviço de desentupimento rápido e eficaz para pias, ralos, vasos sanitários e tubulações. Utilizamos equipamentos modernos sem danificar a estrutura do local.",
                        4,
                        "PRESENCIAL",
                        LocalDate.now().plusDays(7),
                        List.of(categoryEncanamento),
                        Base64.getEncoder().encodeToString(serviceImageBytes)
                );
                ServiceDTO service3 = new ServiceDTO(
                        "Instalação de Tomadas e Interruptores",
                        "Instalação elétrica residencial e comercial. Inclui troca de interruptores, tomadas, quadro de luz e adequação à norma técnica. Garantia de segurança e qualidade.",
                        8,
                        "PRESENCIAL",
                        LocalDate.now().plusDays(10),
                        List.of(categoryEletrica),
                        Base64.getEncoder().encodeToString(serviceImageBytes)
                );
                ServiceDTO service4 = new ServiceDTO(
                        "Pintura Interna de Quarto (12m²)",
                        "Pintura completa de quarto com aplicação de massa corrida e duas demãos de tinta acrílica. Inclui proteção de móveis e limpeza pós-serviço.",
                        16,
                        "PRESENCIAL",
                        LocalDate.now().plusDays(20),
                        List.of(categoryPintura),
                        Base64.getEncoder().encodeToString(serviceImageBytes)
                );
                ServiceDTO service5 = new ServiceDTO(
                        "Limpeza e Manutenção de Ar Condicionado Split",
                        "Limpeza profunda, troca de filtros e verificação de gás e funcionamento. Recomendado a cada 6 meses para melhor desempenho e saúde.",
                        8,
                        "PRESENCIAL",
                        LocalDate.now().plusDays(30),
                        List.of(categoryManutencao),
                        Base64.getEncoder().encodeToString(serviceImageBytes)
                );

                createServiceDirectly(service1, createdUser);
                createServiceDirectly(service2, createdUser);
                createServiceDirectly(service3, createdUser);
                createServiceDirectly(service4, createdUser);
                createServiceDirectly(service5, createdUser);
            } else {
                System.out.println("Banco de dados já possui dados. Inicialização ignorada.");
            }
        };
    }

    private void createServiceDirectly(ServiceDTO serviceDTO, UserEntity userCreator) {
        ServiceEntity service = new ServiceEntity();
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setTimeChronos(serviceDTO.getTimeChronos());
        service.setDeadline(serviceDTO.getDeadline());
        service.setModality(serviceDTO.getModality());
        service.setPostedAt(LocalDateTime.now());
        service.setCategoryEntities(serviceDTO.getCategoryEntities());
        service.setUserCreator(userCreator);

        // Decodifica o Base64
        String[] parts = serviceDTO.getServiceImage().split(",");
        String dataBase64 = (parts.length > 1) ? parts[1] : parts[0];
        service.setServiceImage(Base64.getDecoder().decode(dataBase64));
        serviceRepository.save(service);
    }
}