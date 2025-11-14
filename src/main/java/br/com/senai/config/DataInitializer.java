package br.com.senai.config;

import br.com.senai.model.DTO.DocumentDTO;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.*;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.AuthService;
import br.com.senai.service.SupabaseAuthService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final SupabaseAuthService supabaseAuthService;
    private final AuthService authService;

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    @Transactional
    public CommandLineRunner initializeData() {
        return args -> {
            if (userRepository.count() == 0) {
                System.out.println("🚀 Inicializando dados padrão...");

                try {
                    // SENHA EM TEXTO PURO (igual ao register)
                    String plainPassword = "123123";

                    // Criar UserDTO igual ao register
                    UserDTO defaultUser = new UserDTO();
                    defaultUser.setName("Bertrania Dude");
                    defaultUser.setEmail("email@email.com");
                    defaultUser.setPhoneNumber(5547912345678L);
                    defaultUser.setPassword(plainPassword); // Senha em texto puro

                    // Configurar documento padrão (igual ao register)
                    DocumentDTO documentDTO = new DocumentDTO();
                    documentDTO.setName("documento_padrao.png");
                    documentDTO.setType("image/png");

                    // Carregar imagem padrão
                    byte[] imageBytes = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("images.png")).readAllBytes();
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    documentDTO.setData(base64Image);

                    defaultUser.setDocument(documentDTO);

                    System.out.println("📝 Criando usuário no Supabase...");

                    // Registrar no Supabase (EXATAMENTE como no AuthController)
                    Map<String, Object> userMetadata = new HashMap<>();
                    userMetadata.put("name", defaultUser.getName());
                    userMetadata.put("phone", defaultUser.getPhoneNumber());

                    // Fazer signUp no Supabase (com senha em texto puro)
                    var supabaseUserDTO = supabaseAuthService.signUp(
                            defaultUser.getEmail(),
                            defaultUser.getPassword(), // Senha em texto puro
                            userMetadata
                    );

                    System.out.println("✅ Usuário criado no Supabase: " + supabaseUserDTO.getEmail());

                    // Registrar no banco local (EXATAMENTE como no AuthService.register)
                    UserEntity createdUser = authService.register(defaultUser, supabaseUserDTO.getId());

                    System.out.println("✅ Usuário criado no banco local: " + createdUser.getEmail());

                    // Reanexar o usuário ao contexto de persistência
                    UserEntity managedUser = entityManager.merge(createdUser);
                    entityManager.flush();

                    System.out.println("🔄 Usuário reanexado ao contexto de persistência");

                    // Criar serviços padrão
                    createDefaultServices(managedUser);

                    System.out.println("🎉 Inicialização concluída com sucesso!");

                } catch (Exception e) {
                    System.err.println("❌ Erro durante inicialização: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("📊 Banco de dados já possui dados. Inicialização ignorada.");
            }
        };
    }

    @Transactional
    protected void createDefaultServices(UserEntity userCreator) {
        try {
            System.out.println("🛠️ Criando serviços padrão...");

            // Configurar categorias
            CategoryEntity categoryManutencao = new CategoryEntity();
            categoryManutencao.setName("Manutenção");
            CategoryEntity categoryEncanamento = new CategoryEntity();
            categoryEncanamento.setName("Encanamento");
            CategoryEntity categoryEletrica = new CategoryEntity();
            categoryEletrica.setName("Elétrica");
            CategoryEntity categoryPintura = new CategoryEntity();
            categoryPintura.setName("Pintura");

            // Carregar imagem para serviços
            byte[] serviceImageBytes = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("images.png")).readAllBytes();
            String base64ServiceImage = Base64.getEncoder().encodeToString(serviceImageBytes);

            // Criar serviços
            List<ServiceDTO> services = Arrays.asList(
                    new ServiceDTO(
                            "Manutenção Preventiva de Eletrodomésticos",
                            "Realizamos manutenção preventiva em geladeiras, lavadoras, micro-ondas e outros eletrodomésticos.",
                            6,
                            "PRESENCIAL",
                            LocalDate.now().plusDays(15),
                            List.of(categoryManutencao),
                            base64ServiceImage
                    ),
                    new ServiceDTO(
                            "Desentupimento de Pia e Vaso Sanitário",
                            "Serviço de desentupimento rápido e eficaz para pias, ralos, vasos sanitários e tubulações.",
                            4,
                            "PRESENCIAL",
                            LocalDate.now().plusDays(7),
                            List.of(categoryEncanamento),
                            base64ServiceImage
                    ),
                    new ServiceDTO(
                            "Instalação de Tomadas e Interruptores",
                            "Instalação elétrica residencial e comercial. Garantia de segurança e qualidade.",
                            8,
                            "PRESENCIAL",
                            LocalDate.now().plusDays(10),
                            List.of(categoryEletrica),
                            base64ServiceImage
                    ),
                    new ServiceDTO(
                            "Pintura Interna de Quarto (12m²)",
                            "Pintura completa de quarto com aplicação de massa corrida e duas demãos de tinta acrílica.",
                            16,
                            "PRESENCIAL",
                            LocalDate.now().plusDays(20),
                            List.of(categoryPintura),
                            base64ServiceImage
                    ),
                    new ServiceDTO(
                            "Limpeza e Manutenção de Ar Condicionado Split",
                            "Limpeza profunda, troca de filtros e verificação de gás e funcionamento.",
                            8,
                            "PRESENCIAL",
                            LocalDate.now().plusDays(30),
                            List.of(categoryManutencao),
                            base64ServiceImage
                    )
            );

            for (ServiceDTO serviceDTO : services) {
                createServiceDirectly(serviceDTO, userCreator);
            }

            System.out.println("✅ " + services.size() + " serviços criados com sucesso!");

        } catch (Exception e) {
            System.err.println("❌ Erro ao criar serviços: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    protected void createServiceDirectly(ServiceDTO serviceDTO, UserEntity userCreator) {
        ServiceEntity service = new ServiceEntity();
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setTimeChronos(serviceDTO.getTimeChronos());
        service.setDeadline(serviceDTO.getDeadline());
        service.setModality(serviceDTO.getModality());
        service.setPostedAt(LocalDateTime.now());
        service.setCategoryEntities(serviceDTO.getCategoryEntities());
        service.setUserCreator(userCreator);

        // Processar imagem (igual ao register)
        String base64Data = serviceDTO.getServiceImage().trim();
        if (base64Data.contains(",")) {
            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
        }
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        service.setServiceImage(imageBytes);

        serviceRepository.save(service);
    }
}